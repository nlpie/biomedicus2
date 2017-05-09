/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.socialhistory;

import com.google.inject.Singleton;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.common.types.semantics.ImmutableSubstanceUsageElement;
import edu.umn.biomedicus.common.types.semantics.SocialHistoryCandidate;
import edu.umn.biomedicus.common.types.semantics.SubstanceUsageElement;
import edu.umn.biomedicus.common.types.text.*;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static edu.umn.biomedicus.common.types.semantics.SubstanceUsageElementType.*;
import static edu.umn.biomedicus.common.types.semantics.SubstanceUsageKind.NICOTINE;

@Singleton
public class TobaccoKindSubstanceUsageDetector implements KindSubstanceUsageDetector {

    private final Pattern typePattern;
    private final Pattern methodPattern;
    private final Pattern methodHintPattern;
    private final Pattern statusPattern;
    private final Pattern statusPossiblePattern;
    List<Pattern> amountPatternsList;
    List<Pattern> frequencyPatternsList;
    List<Pattern> temporalPatternsList;
    Hashtable<Integer, Integer> allElementsSpanHash;
    private LabelIndex<SectionTitle> sectionTitleLabels;
    private LabelIndex<ParseToken> parseTokenLabels;
    private LabelIndex<SectionContent> sectionContentLabels;
    private LabelIndex<Sentence> sentenceLabels;
    private LabelIndex<Section> sectionLabels;
    private Labeler<SocialHistoryCandidate> candidateLabeler;
    private Labeler<SubstanceUsageElement> SubstanceUsageElementLabeler;
    private Helpers helper = new Helpers();


    public TobaccoKindSubstanceUsageDetector() throws BiomedicusException {

        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();

        // Load number names, arround names;
        String strNumberNames = "(";
        String strAroundWords = "(";
        String strTimePeriod = "(";
        String thisLine = null;
        try {

            BufferedReader brNumberNames = new BufferedReader(
                    new InputStreamReader(classLoader.getResourceAsStream(
                            "edu/umn/biomedicus/config/socialHistory/NumberNames.txt")));
            while ((thisLine = brNumberNames.readLine()) != null) {
                strNumberNames = strNumberNames + thisLine + "|";
            }
            strNumberNames = strNumberNames
                    .substring(0, strNumberNames.length() - 1);
            strNumberNames += ")";

            // Load arround words
            BufferedReader brAroundWords = new BufferedReader(
                    new InputStreamReader(classLoader.getResourceAsStream(
                            "edu/umn/biomedicus/config/socialHistory/AroundWords.txt")));

            while ((thisLine = brAroundWords.readLine()) != null) {
                strAroundWords = strAroundWords + thisLine + " |";
            }
            strAroundWords = strAroundWords
                    .substring(0, strAroundWords.length() - 2);
            strAroundWords += ")";

            BufferedReader brTimePeriod = new BufferedReader(
                    new InputStreamReader(classLoader.getResourceAsStream(
                            "edu/umn/biomedicus/config/socialHistory/TimePeriodNames.txt")));
            while ((thisLine = brTimePeriod.readLine()) != null) {
                strTimePeriod = strTimePeriod + thisLine + "|";
            }
            strTimePeriod = strTimePeriod
                    .substring(0, strTimePeriod.length() - 1);
            strTimePeriod += ")";

        } catch (IOException e) {
            throw new BiomedicusException(e);
        }


        // Load type patterns
        BufferedReader readerType = new BufferedReader(new InputStreamReader(
                classLoader.getResourceAsStream(
                        "edu/umn/biomedicus/config/socialHistory/NicotineTypePhrases.txt")));
        typePattern = Pattern.compile(
                "(?i)\\b(" + readerType.lines().collect(Collectors.joining("|"))
                        + ")\\b", Pattern.MULTILINE);

        // Load amount patterns
        try {

            BufferedReader brUnits = new BufferedReader(new InputStreamReader(
                    classLoader.getResourceAsStream(
                            "edu/umn/biomedicus/config/socialHistory/NicotineAmountUnits.txt")));
            String strUnits = "(";
            while ((thisLine = brUnits.readLine()) != null) {
                strUnits = strUnits + thisLine + "|";
            }
            strUnits = strUnits.substring(0, strUnits.length() - 1);
            strUnits += ")";

            amountPatternsList = new ArrayList<>();
            BufferedReader brPatterns = new BufferedReader(
                    new InputStreamReader(classLoader.getResourceAsStream(
                            "edu/umn/biomedicus/config/socialHistory/NicotineAmountPatterns.txt")));
            while ((thisLine = brPatterns.readLine()) != null) {

                String str = thisLine.replaceAll("\\$nicotine_unit", strUnits);
                str = "\\b" + str + "\\b";
                amountPatternsList.add(Pattern.compile(str, Pattern.MULTILINE));
            }
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }


        // Load frequency patterns
        try {

            frequencyPatternsList = new ArrayList<>();
            BufferedReader brPatterns = new BufferedReader(
                    new InputStreamReader(classLoader.getResourceAsStream(
                            "edu/umn/biomedicus/config/socialHistory/NicotineFrequencyPhrases.txt")));
            while ((thisLine = brPatterns.readLine()) != null) {

                String str = thisLine
                        .replaceAll("\\$number_names", strNumberNames);
                str = str.replaceAll("\\$time_period_names", strTimePeriod);
                frequencyPatternsList
                        .add(Pattern.compile(str, Pattern.MULTILINE));
            }
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }

        // Load method patterns and hint patterns
        BufferedReader readerMethod = new BufferedReader(new InputStreamReader(
                classLoader.getResourceAsStream(
                        "edu/umn/biomedicus/config/socialHistory/NicotineMethodWords.txt")));
        methodPattern = Pattern.compile("(?i)\\b(" + readerMethod.lines()
                .collect(Collectors.joining("|")) + ")\\b", Pattern.MULTILINE);

        BufferedReader readerMethodHint = new BufferedReader(
                new InputStreamReader(classLoader.getResourceAsStream(
                        "edu/umn/biomedicus/config/socialHistory/NicotineMethodHintWords.txt")));
        methodHintPattern = Pattern.compile(
                "(?i)\\b(" + readerMethodHint.lines()
                        .collect(Collectors.joining("|")) + ")\\b",
                Pattern.MULTILINE);

        // Load status patterns and possible patterns.
        BufferedReader readerStatus = new BufferedReader(new InputStreamReader(
                classLoader.getResourceAsStream(
                        "edu/umn/biomedicus/config/socialHistory/NicotineStatusPhrases.txt")));
        statusPattern = Pattern.compile("(?i)\\b(" + readerStatus.lines()
                .collect(Collectors.joining("|")) + ")\\b", Pattern.MULTILINE);

        BufferedReader readerStatusPossible = new BufferedReader(
                new InputStreamReader(classLoader.getResourceAsStream(
                        "edu/umn/biomedicus/config/socialHistory/NicotineStatusPossiblePhrases.txt")));
        statusPossiblePattern = Pattern.compile(
                "(?i)\\b(" + readerStatusPossible.lines()
                        .collect(Collectors.joining("|")) + ")\\b",
                Pattern.MULTILINE);

        // Load temporal patterns
        try {

            temporalPatternsList = new ArrayList<>();
            BufferedReader brPatterns = new BufferedReader(
                    new InputStreamReader(classLoader.getResourceAsStream(
                            "edu/umn/biomedicus/config/socialHistory/TemporalPatterns.txt")));
            while ((thisLine = brPatterns.readLine()) != null) {

                String str = thisLine
                        .replaceAll("\\$number_names", strNumberNames);
                str = str.replaceAll("\\$arround_words", strAroundWords);
                str = str.replaceAll("\\$number_names", strNumberNames);
                str = str.replaceAll("\\$time_period_names", strTimePeriod);
                str = str.replaceAll("\\$month_names", "(" + strMonNames + ")");
                str = str.replaceAll("\\$day_names", "(" + strDayNames + ")");
                str = "(?i)\\b" + str + "\\b";
                temporalPatternsList
                        .add(Pattern.compile(str, Pattern.MULTILINE));
            }
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }

    }

    static ImmutableSubstanceUsageElement.Builder newBuilder() {
        return ImmutableSubstanceUsageElement.builder()
                .kind(NICOTINE);
    }

    @Override
    public void processCandidate(TextView document,
                                 Label<SocialHistoryCandidate> socialHistoryCandidateLabel)
            throws BiomedicusException {

        sectionLabels = document.getLabelIndex(Section.class);
        sectionTitleLabels = document.getLabelIndex(SectionTitle.class);
        parseTokenLabels = document.getLabelIndex(ParseToken.class);
        candidateLabeler = document.getLabeler(SocialHistoryCandidate.class);
        sectionContentLabels = document.getLabelIndex(SectionContent.class);
        sentenceLabels = document.getLabelIndex(Sentence.class);
        SubstanceUsageElementLabeler = document
                .getLabeler(SubstanceUsageElement.class);
        parseTokenLabels = document.getLabelIndex(ParseToken.class);

        Label<Sentence> sentenceLabel = document.getLabelIndex(Sentence.class)
                .withTextLocation(socialHistoryCandidateLabel)
                .orElseThrow(() -> new BiomedicusException(
                        "SocialHistory Candidate does not have sentence"));


        Label<DependencyParse> dependencyParseLabel = document
                .getLabelIndex(DependencyParse.class)
                .withTextLocation(sentenceLabel)
                .orElseThrow(() -> new BiomedicusException(
                        "No parse for sentence."));


        List<ParseToken> sentenceTermTokens = parseTokenLabels
                .insideSpan(sentenceLabel).values();
        String strSentence = helper.toTokensString(sentenceTermTokens);

        Label<ConstituencyParse> consttParseLabel = document
                .getLabelIndex(ConstituencyParse.class)
                .withTextLocation(sentenceLabel)
                .orElseThrow(() -> new BiomedicusException(
                        "No constituency parse for sentence."));
        String strConstt = consttParseLabel.value().parse();


        String strDepen = dependencyParseLabel.value().parseTree();
        Hashtable<String, Object> googleRelationHash = new Hashtable<>();
        googleRelationHash.put("DEP_STR", strDepen);
        Helpers.getGoogleDepenRelations(strDepen, googleRelationHash);


        allElementsSpanHash = new Hashtable<>();
        extractTemporal(sentenceLabel, googleRelationHash, strConstt);
        extractType(sentenceLabel, document);
        extractAmount(sentenceLabel, googleRelationHash);
        extractFrequency(sentenceLabel, googleRelationHash);
        extractMethod(sentenceLabel, googleRelationHash);
        extractStatus(sentenceLabel, googleRelationHash);

    }

    private void extractTemporal(Label<Sentence> sentenceLabel,
                                 Hashtable<String, Object> googleRelationHash,
                                 String strConstt) throws BiomedicusException {

        List<ParseToken> sentenceTermTokens = parseTokenLabels
                .insideSpan(sentenceLabel).values();
        String strSentence = helper.toTokensString(sentenceTermTokens);
        // Get all possible longest frequency patterns

        Hashtable<Integer, Integer> spanHash
                = new Hashtable<Integer, Integer>();
        for (Pattern pattern : temporalPatternsList) {

            Matcher matcher = pattern.matcher(strSentence);

            while (matcher.find()) {

                int start = matcher.start(1);
                int end = matcher.end(1);

                boolean bClear = false;
                if (spanHash.isEmpty()) {
                    // Empty match hash
                    bClear = true;
                } else {
                    // Non-empty hash, check if the span already included or there is already a longer label
                    Enumeration<Integer> enumKey = spanHash.keys();
                    while (enumKey.hasMoreElements()) {

                        int key = enumKey.nextElement();
                        int value = spanHash.get(key);

                        if (key > end || value < start) {
                            // No overlap
                            bClear = true;
                        } else if (start == key && end == value) {
                            bClear = false;
                            break;
                        } else {
                            if (start <= key && end >= value) {
                                // compass an existing large match, then remove the old match
                                spanHash.remove(key);
                                bClear = true;
                                break;

                            }
                            if (start >= key && end <= value) {
                                // within an existing large match,
                                bClear = false;
                                break;
                            }
                        }
                    }
                }
                if (bClear) spanHash.put(start, end);
            }
        }

        // Get constituent phrases from constituency parse
        Hashtable<Integer, Integer> consttPPHash = new Hashtable<>();
        helper.getTemporalSyntaxPhrases(strConstt, parseTokenLabels,
                consttPPHash, sentenceLabel);

        // Check if spans from constituent phrases exists in current span list
        Enumeration<Integer> enumKey = consttPPHash.keys();
        while (enumKey.hasMoreElements()) {

            Integer start = enumKey.nextElement();
            Integer end = consttPPHash.get(start);
            boolean bClear = false;
            if (spanHash.isEmpty()) {
                // Empty match hash
                bClear = true;
            } else {
                // Non-empty hash, check if the span already included or there is already a longer label
                Enumeration<Integer> enumSpanKey = spanHash.keys();
                while (enumSpanKey.hasMoreElements()) {

                    int key = enumSpanKey.nextElement();
                    int value = spanHash.get(key);

                    if (key > end || value < start) {
                        // No overlap
                        bClear = true;
                    } else if (start == key && end == value) {
                        bClear = false;
                        break;
                    } else {
                        if (start <= key && end >= value) {
                            // compass an existing large match, then remove the old match
                            spanHash.remove(key);
                            bClear = true;
                            break;

                        }
                        if (start >= key && end <= value) {
                            // within an existing large match,
                            bClear = false;
                            break;
                        }
                    }
                }
            }
            if (bClear) spanHash.put(start, end);
        }

        //
        // Iterate each matched span, check if all matched text in the span hash are related to tobacco
        // If so create a substance usage label for it
        //
        enumKey = spanHash.keys();
        while (enumKey.hasMoreElements()) {

            Integer start = enumKey.nextElement();
            Integer end = spanHash.get(start);
            String strMatch = strSentence.substring(start, end);
            allElementsSpanHash.put(start, end);

            Span substanceUsageSpan = sentenceLabel
                    .derelativize(new Span(start, end));
            List<Integer> usageTokens = new ArrayList<>();

            // Get goggle tokens of the span
            Helpers.getUsageTokens(parseTokenLabels, sentenceLabel, start, end,
                    usageTokens);

            String strAlcoholPattern = ".*(?i)("
                    + KindSubstanceUsageDetector.strAlcoholKeywords + ").*";
            String strDrugPattern = ".*(?i)("
                    + KindSubstanceUsageDetector.strDrugKeywords + ").*";

            if (!strSentence.toString().matches(strDrugPattern) && !strSentence
                    .toString().matches(strAlcoholPattern)) {
                SubstanceUsageElementLabeler
                        .value(newBuilder()
                                .type(TEMPORAL)
                                .build())
                        .label(substanceUsageSpan);
            }
            // Check if relatives of all matched tokens related to tobacco
            else {
                if (Helpers.checkSubstanceDepenRelated(usageTokens,
                        KindSubstanceUsageDetector.strTobaccoKeywords,
                        googleRelationHash)) {
                    SubstanceUsageElementLabeler
                            .value(newBuilder()
                                    .type(METHOD)
                                    .build())
                            .label(substanceUsageSpan);
                }
            }
        }
    }

    private void extractStatus(Label<Sentence> sentenceLabel,
                               Hashtable<String, Object> googleRelationHash)
            throws BiomedicusException {

        List<ParseToken> sentenceTermTokens = parseTokenLabels
                .insideSpan(sentenceLabel).values();
        String strSentence = helper.toTokensString(sentenceTermTokens);

        // Get all exact longest frequency patterns
        Hashtable<Integer, Integer> spanHash
                = new Hashtable<Integer, Integer>();
        Matcher matcher = statusPattern.matcher(strSentence);

        while (matcher.find()) {

            int start = matcher.start();
            int end = matcher.end();

            boolean bClear = false;
            if (spanHash.isEmpty()) {
                // Empty match hash
                bClear = true;
            } else {
                // Non-empty hash, check if the span already included or there is already a longer label
                Enumeration<Integer> enumKey = spanHash.keys();
                while (enumKey.hasMoreElements()) {

                    int key = enumKey.nextElement();
                    int value = spanHash.get(key);

                    if (key > end || value < start) {
                        // No overlap
                        bClear = true;
                    } else if (start == key && end == value) {
                        bClear = false;
                        break;
                    } else {
                        if (start <= key && end >= value) {
                            // compass an existing large match, then remove the old match
                            spanHash.remove(key);
                            bClear = true;
                            break;
                        }
                        if (start >= key && end <= value) {
                            // within an existing large match,
                            bClear = false;
                            break;
                        }
                    }
                }
            }
            if (bClear) spanHash.put(start, end);
        }

        //
        // Iterate each matched span, create a substance usage label for it
        //
        Enumeration<Integer> enumKey = spanHash.keys();
        while (enumKey.hasMoreElements()) {
            Integer start = enumKey.nextElement();
            Integer end = spanHash.get(start);
            Span substanceUsageSpan = sentenceLabel
                    .derelativize(new Span(start, end));
            SubstanceUsageElementLabeler
                    .value(newBuilder()
                            .type(METHOD)
                            .build())
                    .label(substanceUsageSpan);
        }


        // Get all possible longest frequency patterns
        spanHash.clear();
        matcher = statusPossiblePattern.matcher(strSentence);
        while (matcher.find()) {

            int start = matcher.start();
            int end = matcher.end();

            boolean bClear = false;
            if (spanHash.isEmpty()) {
                // Empty match hash
                bClear = true;
            } else {
                // Non-empty hash, check if the span already included or there is already a longer label
                enumKey = spanHash.keys();
                while (enumKey.hasMoreElements()) {

                    int key = enumKey.nextElement();
                    int value = spanHash.get(key);

                    if (key > end || value < start) {
                        // No overlap
                        bClear = true;
                    } else if (start == key && end == value) {
                        bClear = false;
                        break;
                    } else {
                        if (start <= key && end >= value) {
                            // compass an existing large match, then remove the old match
                            spanHash.remove(key);
                            bClear = true;
                            break;

                        }
                        if (start >= key && end <= value) {
                            // within an existing large match,
                            bClear = false;
                            break;
                        }
                    }
                }
            }
            if (bClear) spanHash.put(start, end);
        }

        //
        // Iterate each matched span, check if all matched text in the span hash are related to tobacco
        // If so create a substance usage label for it
        //
        enumKey = spanHash.keys();
        while (enumKey.hasMoreElements()) {

            Integer start = enumKey.nextElement();
            Integer end = spanHash.get(start);
            Span substanceUsageSpan = sentenceLabel
                    .derelativize(new Span(start, end));

            // Get goggle tokens of the span
            List<Integer> usageTokens = new ArrayList<>();
            Helpers.getUsageTokens(parseTokenLabels, sentenceLabel, start, end,
                    usageTokens);

            String strAlcoholPattern = ".*(?i)("
                    + KindSubstanceUsageDetector.strAlcoholKeywords + ").*";
            String strDrugPattern = ".*(?i)("
                    + KindSubstanceUsageDetector.strDrugKeywords + ").*";

            if (!strSentence.toString().matches(strDrugPattern) && !strSentence
                    .toString().matches(strAlcoholPattern)) {
                SubstanceUsageElementLabeler
                        .value(newBuilder().type(METHOD).build())
                        .label(substanceUsageSpan);
            }
            // Check if ancesters of all matched tokens related to alcohol
            else {
                if (Helpers.checkSubstanceDepenRelated(usageTokens,
                        KindSubstanceUsageDetector.strTobaccoKeywords,
                        googleRelationHash)) {
                    SubstanceUsageElementLabeler
                            .value(newBuilder().type(METHOD).build())
                            .label(substanceUsageSpan);
                }
            }
        }
    }

    private void extractMethod(Label<Sentence> sentenceLabel,
                               Hashtable<String, Object> googleRelationHash)
            throws BiomedicusException {

        // Match method phrases that explicitly indicate alcohol drink method
        List<ParseToken> sentenceTermTokens = parseTokenLabels
                .insideSpan(sentenceLabel).values();
        String strSentence = helper.toTokensString(sentenceTermTokens);

        Matcher matcher = methodPattern.matcher(strSentence);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            Span substanceUsageSpan = sentenceLabel
                    .derelativize(new Span(start, end));
            SubstanceUsageElementLabeler
                    .value(newBuilder().type(METHOD).build())
                    .label(substanceUsageSpan);
        }

        // Match methods hint words that may indicate tobacco drink method, need find if related to tobacco
        matcher = methodHintPattern.matcher(strSentence);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            Span substanceUsageSpan = sentenceLabel
                    .derelativize(new Span(start, end));
            String strMatch = strSentence.substring(start, end);

            // Get goggle tokens of the span
            List<Integer> usageTokens = new ArrayList<>();
            Helpers.getUsageTokens(parseTokenLabels, sentenceLabel, start, end,
                    usageTokens);

            // use in "used to" is not a method
            if ((strSentence.substring(start, end).matches(".*(?i)use.*")
                    && strSentence.substring(start, end)
                    .matches(".*(?i)to.*"))) {
                continue;
            }

            String strAlcoholPattern = ".*(?i)("
                    + KindSubstanceUsageDetector.strAlcoholKeywords + ").*";
            String strDrugPattern = ".*(?i)("
                    + KindSubstanceUsageDetector.strDrugKeywords + ").*";
            String strTobaccoPattern = ".*(?i)("
                    + KindSubstanceUsageDetector.strTobaccoKeywords + ").*";

            if (!strSentence.toString().matches(strAlcoholPattern)
                    && !strSentence.toString().matches(strDrugPattern)) {
                SubstanceUsageElementLabeler
                        .value(newBuilder().type(METHOD).build())
                        .label(substanceUsageSpan);
            }
            // Check if ancesters of all matched tokens related to alcohol
            else {
                if (Helpers.checkSubstanceDepenRelated(usageTokens,
                        KindSubstanceUsageDetector.strTobaccoKeywords,
                        googleRelationHash)
                        || strMatch.matches(strTobaccoPattern)
                        ) {
                    SubstanceUsageElementLabeler
                            .value(newBuilder().type(METHOD).build())
                            .label(substanceUsageSpan);
                } else if (!Helpers.checkSmokeDrug(usageTokens,
                        KindSubstanceUsageDetector.strDrugKeywords,
                        googleRelationHash)) {
                    SubstanceUsageElementLabeler
                            .value(newBuilder().type(METHOD).build())
                            .label(substanceUsageSpan);

                }
            }
        }
    }

    private void extractFrequency(Label<Sentence> sentenceLabel,
                                  Hashtable<String, Object> googleRelationHash)
            throws BiomedicusException {

        List<ParseToken> sentenceTermTokens = parseTokenLabels
                .insideSpan(sentenceLabel).values();
        String strSentence = helper.toTokensString(sentenceTermTokens);

        // Get all possible longest frequency patterns
        Hashtable<Integer, Integer> spanHash
                = new Hashtable<Integer, Integer>();
        for (Pattern pattern : frequencyPatternsList) {

            Matcher matcher = pattern.matcher(strSentence);
            while (matcher.find()) {

                int start = matcher.start();
                int end = matcher.end();

                boolean bClear = false;
                if (spanHash.isEmpty()) {
                    // Empty match hash
                    bClear = true;
                } else {
                    // Non-empty hash, check if the span already included or there is already a longer label
                    Enumeration<Integer> enumKey = spanHash.keys();
                    while (enumKey.hasMoreElements()) {

                        int key = enumKey.nextElement();
                        int value = spanHash.get(key);

                        if (key > end || value < start) {
                            // No overlap
                            bClear = true;
                        } else if (start == key && end == value) {
                            bClear = false;
                            break;
                        } else {
                            if (start <= key && end >= value) {
                                // compass an existing large match, then remove the old match
                                spanHash.remove(key);
                                bClear = true;
                                break;

                            }
                            if (start >= key && end <= value) {
                                // within an existing large match,
                                bClear = false;
                                break;
                            }
                        }
                    }
                }
                if (bClear) spanHash.put(start, end);
            }
        }

        //
        // Iterate each matched span, check if all matched text in the span hash are related to alcohol
        // If so create a substance usage label for it
        //
        Enumeration<Integer> enumKey = spanHash.keys();
        while (enumKey.hasMoreElements()) {

            Integer start = enumKey.nextElement();
            Integer end = spanHash.get(start);
            List<Integer> usageTokens = new ArrayList<>();
            Span substanceUsageSpan = sentenceLabel
                    .derelativize(new Span(start, end));

            // See if it's in a larger temporal annotation
            if (checkExistsElementsSpan(start, end)) { continue; }
            allElementsSpanHash.put(start, end);

            // Get goggle tokens of the span
            Helpers.getUsageTokens(parseTokenLabels, sentenceLabel, start, end,
                    usageTokens);
            String strAlcoholPattern = ".*(?i)("
                    + KindSubstanceUsageDetector.strAlcoholKeywords + ").*";
            String strDrugPattern = ".*(?i)("
                    + KindSubstanceUsageDetector.strDrugKeywords + ").*";

            if (!strSentence.toString().matches(strAlcoholPattern)
                    && !strSentence.toString().matches(strDrugPattern)) {
                SubstanceUsageElementLabeler
                        .value(newBuilder()
                                .type(FREQUENCY)
                                .build())
                        .label(substanceUsageSpan);
            }
            // Check if ancesters of all matched tokens related to alcohol
            else {
                if (Helpers.checkSubstanceDepenRelated(usageTokens,
                        KindSubstanceUsageDetector.strTobaccoKeywords,
                        googleRelationHash)) {
                    SubstanceUsageElementLabeler
                            .value(newBuilder()
                                    .type(FREQUENCY)
                                    .build())
                            .label(substanceUsageSpan);
                }
            }

        }
    }

    private void extractType(Label<Sentence> sentenceLabel, TextView document)
            throws BiomedicusException {

        List<ParseToken> sentenceTermTokens = parseTokenLabels
                .insideSpan(sentenceLabel).values();
        String strSentence = helper.toTokensString(sentenceTermTokens);

        Matcher matcher = typePattern.matcher(strSentence);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            Span substanceUsageSpan = sentenceLabel
                    .derelativize(new Span(start, end));
            SubstanceUsageElementLabeler
                    .value(newBuilder()
                            .type(TYPE)
                            .build()).label(substanceUsageSpan);
        }
    }

    private void extractAmount(Label<Sentence> sentenceLabel,
                               Hashtable<String, Object> googleRelationHash)
            throws BiomedicusException {

        List<ParseToken> sentenceTermTokens = parseTokenLabels
                .insideSpan(sentenceLabel).values();
        String strSentence = helper.toTokensString(sentenceTermTokens);

        Hashtable<Integer, Integer> spanHash
                = new Hashtable<Integer, Integer>();
        for (Pattern pattern : amountPatternsList) {

            Matcher matcher = pattern.matcher(strSentence);
            while (matcher.find()) {

                int start = matcher.start();
                int end = matcher.end();

                // Check if the span already been marked as a temporal element or amount element
                if (checkExistsElementsSpan(start, end)) { continue; }

                boolean bClear = false;
                if (spanHash.isEmpty()) {
                    // Empty match hash
                    bClear = true;
                } else {
                    // Non-empty hash, check if the span already included or there is already a longer label
                    Enumeration<Integer> enumKey = spanHash.keys();
                    while (enumKey.hasMoreElements()) {

                        int key = enumKey.nextElement();
                        int value = spanHash.get(key);

                        if (key > end || value < start) {
                            // No overlap
                            bClear = true;
                        } else if (start == key && end == value) {
                            bClear = false;
                            break;
                        } else {
                            if (start <= key && end >= value) {
                                // compass an existing large match, then remove the old match
                                spanHash.remove(key);
                                bClear = true;
                                break;
                            }
                            if (start >= key && end <= value) {
                                // within an existing large match,
                                bClear = false;
                                break;
                            }
                        }
                    }
                }
                if (bClear) spanHash.put(start, end);
            }
        }
        Enumeration<Integer> enumKey = spanHash.keys();
        while (enumKey.hasMoreElements()) {
            Integer start = enumKey.nextElement();
            Integer end = spanHash.get(start);
            allElementsSpanHash.put(start, end);
            String strMatch = strSentence.substring(start, end);

            List<Integer> usageTokens = new ArrayList<>();
            Span substanceUsageSpan = sentenceLabel
                    .derelativize(new Span(start, end));

            // Get goggle tokens of the span
            Helpers.getUsageTokens(parseTokenLabels, sentenceLabel, start, end,
                    usageTokens);
            String strAlcoholPattern = ".*(?i)("
                    + KindSubstanceUsageDetector.strAlcoholKeywords + ").*";
            String strDrugPattern = ".*(?i)("
                    + KindSubstanceUsageDetector.strDrugKeywords + ").*";

            if (!strSentence.matches(strAlcoholPattern)
                    && !strSentence.matches(strDrugPattern)) {
                SubstanceUsageElementLabeler
                        .value(newBuilder().type(AMOUNT).build())
                        .label(substanceUsageSpan);
            }
            // Check if ancesters of all matched tokens related to alcohol
            else {
                if (Helpers.checkSubstanceDepenRelated(usageTokens,
                        KindSubstanceUsageDetector.strTobaccoKeywords,
                        googleRelationHash)) {
                    SubstanceUsageElementLabeler
                            .value(newBuilder().type(AMOUNT).build())
                            .label(substanceUsageSpan);
                }
            }

        }
    }

    private boolean checkExistsElementsSpan(Integer start, Integer end) {

        // Empty global match hash
        if (allElementsSpanHash.isEmpty()) { return false; }

        Enumeration<Integer> enumKey = allElementsSpanHash.keys();
        boolean bExists = true;
        while (enumKey.hasMoreElements()) {

            int key = enumKey.nextElement();
            int value = allElementsSpanHash.get(key);

            if (key > end || value < start) {
                // No overlap
                bExists = false;
            } else if (start == key && end == value) {
                bExists = true;
                break;
            } else {
                if (start <= key && end >= value) {
                    // compass an existing large match,
                    bExists = false;
                    break;
                }
                if (start >= key && end <= value) {
                    // within an existing large match,
                    bExists = true;
                    break;
                }
            }
        }
        return bExists;
    }
}
