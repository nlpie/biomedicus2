/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

import static edu.umn.biomedicus.common.types.semantics.SubstanceUsageElementType.AMOUNT;
import static edu.umn.biomedicus.common.types.semantics.SubstanceUsageElementType.FREQUENCY;
import static edu.umn.biomedicus.common.types.semantics.SubstanceUsageElementType.METHOD;
import static edu.umn.biomedicus.common.types.semantics.SubstanceUsageElementType.STATUS;
import static edu.umn.biomedicus.common.types.semantics.SubstanceUsageElementType.TEMPORAL;
import static edu.umn.biomedicus.common.types.semantics.SubstanceUsageElementType.TYPE;
import static edu.umn.biomedicus.common.types.semantics.SubstanceUsageKind.DRUG;

import com.google.inject.Singleton;
import edu.umn.biomedicus.common.types.semantics.ImmutableSubstanceUsageElement;
import edu.umn.biomedicus.common.types.semantics.SocialHistoryCandidate;
import edu.umn.biomedicus.common.types.semantics.SubstanceUsageElement;
import edu.umn.biomedicus.common.types.text.ConstituencyParse;
import edu.umn.biomedicus.common.types.text.DependencyParse;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Section;
import edu.umn.biomedicus.common.types.text.SectionContent;
import edu.umn.biomedicus.common.types.text.SectionTitle;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextView;
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

@Singleton
public class DrugKindSubstanceUsageDetector implements KindSubstanceUsageDetector {

  private final Pattern typePattern;
  private final Pattern amountPattern;
  private final Pattern methodPattern;
  private final Pattern methodHintPattern;
  private final Pattern statusPossiblePattern;
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
  private String strAlcoholPattern;


  public DrugKindSubstanceUsageDetector() throws BiomedicusException {

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
            "edu/umn/biomedicus/config/socialHistory/DrugTypePhrases.txt")));
    typePattern = Pattern.compile(
        "(?i)\\b(" + readerType.lines().collect(Collectors.joining("|"))
            + ")\\b", Pattern.MULTILINE);

    // Load amount patterns
    try {

      BufferedReader brPatterns = new BufferedReader(
          new InputStreamReader(classLoader.getResourceAsStream(
              "edu/umn/biomedicus/config/socialHistory/DrugAmountPatterns.txt")));
      String strPattern = "\\b(";
      while ((thisLine = brPatterns.readLine()) != null) {
        strPattern = strPattern + thisLine
            .replaceAll("\\$number_names", strNumberNames) + "|";
      }
      strPattern = strPattern.substring(0, strPattern.length() - 1);
      strPattern += ")\\b";

      amountPattern = Pattern.compile(strPattern, Pattern.MULTILINE);
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }

    // Load frequency patterns
    try {

      frequencyPatternsList = new ArrayList<>();
      BufferedReader brPatterns = new BufferedReader(
          new InputStreamReader(classLoader.getResourceAsStream(
              "edu/umn/biomedicus/config/socialHistory/DrugFrequencyPhrases.txt")));
      while ((thisLine = brPatterns.readLine()) != null) {

        frequencyPatternsList
            .add(Pattern.compile(thisLine, Pattern.MULTILINE));
      }
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }

    // Load method patterns and hint patterns
    BufferedReader readerMethod = new BufferedReader(new InputStreamReader(
        classLoader.getResourceAsStream(
            "edu/umn/biomedicus/config/socialHistory/DrugMethodWords.txt")));
    methodPattern = Pattern.compile("(?i)\\b(" + readerMethod.lines()
        .collect(Collectors.joining("|")) + ")\\b", Pattern.MULTILINE);

    BufferedReader readerMethodHint = new BufferedReader(
        new InputStreamReader(classLoader.getResourceAsStream(
            "edu/umn/biomedicus/config/socialHistory/DrugMethodHintWords.txt")));
    methodHintPattern = Pattern.compile(
        "(?i)\\b(" + readerMethodHint.lines()
            .collect(Collectors.joining("|")) + ")\\b",
        Pattern.MULTILINE);

    // Load possible status patterns
    BufferedReader readerStatusPossible = new BufferedReader(
        new InputStreamReader(classLoader.getResourceAsStream(
            "edu/umn/biomedicus/config/socialHistory/DrugStatusPossiblePhrases.txt")));
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


  @Override
  public void processCandidate(TextView document,
      Label<SocialHistoryCandidate> socialHistoryCandidateLabel)
      throws BiomedicusException {

    sectionLabels = document.getLabelIndex(Section.class);
    sectionTitleLabels = document.getLabelIndex(SectionTitle.class);
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
        .insideSpan(sentenceLabel).valuesAsList();
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
    extractType(sentenceLabel);
    extractAmount(sentenceLabel, googleRelationHash);
    extractFrequency(sentenceLabel, googleRelationHash);
    extractMethod(sentenceLabel, googleRelationHash);
    extractStatus(sentenceLabel, googleRelationHash);


  }

  private void extractTemporal(Label<Sentence> sentenceLabel,
      Hashtable<String, Object> googleRelationHash,
      String strConstt) throws BiomedicusException {

    List<ParseToken> sentenceTermTokens = parseTokenLabels
        .insideSpan(sentenceLabel).valuesAsList();
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
        if (bClear) {
          spanHash.put(start, end);
        }
      }
    }

    // Get constituent phrases from constituency parse
    Hashtable<Integer, Integer> consttPPHash = new Hashtable<>();
    helper.getTemporalSyntaxPhrases(strConstt, parseTokenLabels,
        consttPPHash, sentenceLabel);

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
      if (bClear) {
        spanHash.put(start, end);
      }
    }

    //
    // Iterate each matched span, check if all matched text in the span hash are related to drug
    // If so create a substance usage label for it
    //
    enumKey = spanHash.keys();
    while (enumKey.hasMoreElements()) {

      Integer start = enumKey.nextElement();
      Integer end = spanHash.get(start);
      allElementsSpanHash.put(start, end);
      Span substanceUsageSpan = sentenceLabel
          .derelativize(new Span(start, end));
      List<Integer> usageTokens = new ArrayList<>();

      // Get goggle tokens of the span
      Helpers.getUsageTokens(parseTokenLabels, sentenceLabel, start, end,
          usageTokens);

      String strAlcoholPattern = ".*(?i)\\b("
          + KindSubstanceUsageDetector.strAlcoholKeywords + ")\\b.*";
      String strTobaccoPattern = ".*(?i)\\b("
          + KindSubstanceUsageDetector.strTobaccoKeywords + ")\\b.*";

      if (!strSentence.matches(strAlcoholPattern) && !strSentence
          .matches(strTobaccoPattern)) {
        SubstanceUsageElementLabeler
            .value(ImmutableSubstanceUsageElement.builder()
                .type(TEMPORAL)
                .kind(DRUG)
                .build())
            .label(substanceUsageSpan);
      }
      // Check if ancesters of all matched tokens related to alcohol
      else {
        if (Helpers.checkSubstanceDepenRelated(usageTokens,
            KindSubstanceUsageDetector.strDrugKeywords,
            googleRelationHash)) {
          SubstanceUsageElementLabeler
              .value(ImmutableSubstanceUsageElement.builder()
                  .type(TEMPORAL)
                  .kind(DRUG)
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
        .insideSpan(sentenceLabel).valuesAsList();
    String strSentence = helper.toTokensString(sentenceTermTokens);

    // Get all possible longest frequency patterns
    Hashtable<Integer, Integer> spanHash
        = new Hashtable<Integer, Integer>();
    Matcher matcher = statusPossiblePattern.matcher(strSentence);
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
      if (bClear) {
        spanHash.put(start, end);
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
      Span substanceUsageSpan = sentenceLabel
          .derelativize(new Span(start, end));

      // "no" in "no history" is not a status
      if ((strSentence.substring(start, end).matches("(?i)no")
          && strSentence.matches(".*(?i)no history.*"))) {
        continue;
      }

      // "no" in "no use" is not a status
      if ((strSentence.substring(start, end).matches("(?i)no")
          && strSentence.matches(".*(?i)no use.*"))) {
        continue;
      }

      // Get goggle tokens of the span
      List<Integer> usageTokens = new ArrayList<>();
      Helpers.getUsageTokens(parseTokenLabels, sentenceLabel, start, end,
          usageTokens);

      String strAlcoholPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strAlcoholKeywords + ").*";
      String strTobaccoPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strTobaccoKeywords + ").*";

      if (!strSentence.matches(strAlcoholPattern)
          && !strSentence.matches(strTobaccoPattern)) {
        SubstanceUsageElementLabeler
            .value(ImmutableSubstanceUsageElement.builder()
                .type(STATUS)
                .kind(DRUG)
                .build())
            .label(substanceUsageSpan);
      }
      // Check if ancesters of all matched tokens related to alcohol
      else {
        if (Helpers.checkSubstanceDepenRelated(usageTokens,
            KindSubstanceUsageDetector.strDrugKeywords,
            googleRelationHash)) {
          SubstanceUsageElementLabeler
              .value(ImmutableSubstanceUsageElement.builder()
                  .type(STATUS)
                  .kind(DRUG)
                  .build())
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
        .insideSpan(sentenceLabel).valuesAsList();
    String strSentence = helper.toTokensString(sentenceTermTokens);

    Matcher matcher = methodPattern.matcher(strSentence);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      Span substanceUsageSpan = sentenceLabel
          .derelativize(new Span(start, end));
      SubstanceUsageElementLabeler
          .value(ImmutableSubstanceUsageElement.builder()
              .type(METHOD)
              .kind(DRUG)
              .build())
          .label(substanceUsageSpan);
    }

    // Match methods hint words that may indicate alcohol drink method, need find if related to alcohol
    matcher = methodHintPattern.matcher(strSentence);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      Span substanceUsageSpan = sentenceLabel
          .derelativize(new Span(start, end));
      String strMatch = strSentence.substring(start, end);

            /*
            * Check if related to drug
            */

      // Get goggle tokens of the span
      List<Integer> usageTokens = new ArrayList<>();
      Helpers.getUsageTokens(parseTokenLabels, sentenceLabel, start, end,
          usageTokens);

      //Check if the word is noun
      if (usageTokens.size() == 1) {
        Hashtable<Integer, String> posHash
            = (Hashtable<Integer, String>) googleRelationHash
            .get("POS");
        //if (posHash.get(usageTokens.get(0)).toString().matches(".*NN.*")) continue;
      }

      // use in "used to" is not a method
      if ((strSentence.substring(start, end).matches(".*(?i)use.*")
          && strSentence.substring(start, end)
          .matches(".*(?i)to.*"))) {
        continue;
      }

      // "smoke" in "smoke cigatte" is not method
      if (strMatch.matches("(?i)smoke.*") && strSentence
          .matches(".*(?i)smoke(s|d)* +cig.*")) {
        continue;
      }

      String strAlcoholPattern = ".*(?i)\\b("
          + KindSubstanceUsageDetector.strAlcoholKeywords + ")\\b.*";
      String strTobaccoPattern = ".*(?i)\\b("
          + KindSubstanceUsageDetector.strTobaccoKeywords + ")\\b.*";

      if (!strSentence.matches(strAlcoholPattern)
          && !strSentence.matches(strTobaccoPattern)) {
        SubstanceUsageElementLabeler
            .value(ImmutableSubstanceUsageElement.builder()
                .type(METHOD)
                .kind(DRUG)
                .build())
            .label(substanceUsageSpan);
      }
      // Check if relatives of all matched tokens related to alcohol
      else {
        if (Helpers.checkSubstanceDepenRelated(usageTokens,
            KindSubstanceUsageDetector.strDrugKeywords,
            googleRelationHash)) {
          SubstanceUsageElementLabeler
              .value(ImmutableSubstanceUsageElement.builder()
                  .type(METHOD)
                  .kind(DRUG)
                  .build())
              .label(substanceUsageSpan);
        }
      }
    }
  }

  private void extractFrequency(Label<Sentence> sentenceLabel,
      Hashtable<String, Object> googleRelationHash)
      throws BiomedicusException {

    List<ParseToken> sentenceTermTokens = parseTokenLabels
        .insideSpan(sentenceLabel).valuesAsList();
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
        if (bClear) {
          spanHash.put(start, end);
        }
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
      String strMatch = strSentence.substring(start, end);

      // See if it's in a larger temporal annotation
      if (checkExistsElementsSpan(start, end)) {
        continue;
      }
      allElementsSpanHash.put(start, end);

      // Get goggle tokens of the span
      Helpers.getUsageTokens(parseTokenLabels, sentenceLabel, start, end,
          usageTokens);

      String strAlcoholPattern = ".*(?i)\\b("
          + KindSubstanceUsageDetector.strAlcoholKeywords + ")\\b.*";
      String strTobaccoPattern = ".*(?i)\\b("
          + KindSubstanceUsageDetector.strTobaccoKeywords + ")\\b.*";

      if (!strSentence.matches(strAlcoholPattern)
          && !strSentence.matches(strTobaccoPattern)) {

        Span substanceUsageSpan = sentenceLabel
            .derelativize(new Span(start, end));
        SubstanceUsageElementLabeler
            .value(ImmutableSubstanceUsageElement.builder()
                .type(FREQUENCY)
                .kind(DRUG)
                .build())
            .label(substanceUsageSpan);
      }
      // Check if ancesters of all matched tokens related to alcohol
      else {
        if (Helpers.checkSubstanceDepenRelated(usageTokens,
            KindSubstanceUsageDetector.strDrugKeywords,
            googleRelationHash)) {
          Span substanceUsageSpan = sentenceLabel
              .derelativize(new Span(start, end));
          SubstanceUsageElementLabeler
              .value(ImmutableSubstanceUsageElement.builder()
                  .type(FREQUENCY)
                  .kind(DRUG)
                  .build())
              .label(substanceUsageSpan);
        }
      }
    }
  }


  private void extractType(Label<Sentence> sentenceLabel)
      throws BiomedicusException {

    List<ParseToken> sentenceTermTokens = parseTokenLabels
        .insideSpan(sentenceLabel).valuesAsList();
    String strSentence = helper.toTokensString(sentenceTermTokens);

    Matcher matcher = typePattern.matcher(strSentence);

    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      Span substanceUsageSpan = sentenceLabel
          .derelativize(new Span(start, end));
      SubstanceUsageElementLabeler
          .value(ImmutableSubstanceUsageElement.builder()
              .type(TYPE)
              .kind(DRUG)
              .build())
          .label(substanceUsageSpan);
    }
  }


  private void extractAmount(Label<Sentence> sentenceLabel,
      Hashtable<String, Object> googleRelationHash)
      throws BiomedicusException {

    List<ParseToken> sentenceTermTokens = parseTokenLabels
        .insideSpan(sentenceLabel).valuesAsList();
    String strSentence = helper.toTokensString(sentenceTermTokens);
    Hashtable<Integer, Integer> spanHash
        = new Hashtable<Integer, Integer>();

    Matcher matcher = amountPattern.matcher(strSentence);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      String strMatch = strSentence.substring(start, end);

      if ((strMatch.matches("(?i)any")) && strSentence
          .matches("(?i).*any +(history|kind).*")) {
        continue;
      }
      // Check if the span already been marked as a temporal element or amount element
      if (checkExistsElementsSpan(start, end)) {
        continue;
      }
      allElementsSpanHash.put(start, end);

      List<Integer> usageTokens = new ArrayList<>();

      // Get goggle tokens of the span
      Helpers.getUsageTokens(parseTokenLabels, sentenceLabel, start, end,
          usageTokens);

      String strAlcoholPattern = ".*(?i)\\b("
          + KindSubstanceUsageDetector.strAlcoholKeywords + ")\\b.*";
      String strTobaccoPattern = ".*(?i)\\b("
          + KindSubstanceUsageDetector.strTobaccoKeywords + ")\\b.*";

      if (!strSentence.toString().matches(strAlcoholPattern)
          && !strSentence.toString().matches(strTobaccoPattern)) {

        Span substanceUsageSpan = sentenceLabel
            .derelativize(new Span(start, end));
        SubstanceUsageElementLabeler
            .value(ImmutableSubstanceUsageElement.builder()
                .type(AMOUNT)
                .kind(DRUG)
                .build())
            .label(substanceUsageSpan);
      }
      // Check if ancesters of all matched tokens related to alcohol
      else {
        if (Helpers.checkSubstanceDepenRelated(usageTokens,
            KindSubstanceUsageDetector.strDrugKeywords,
            googleRelationHash)) {
          Span substanceUsageSpan = sentenceLabel
              .derelativize(new Span(start, end));
          SubstanceUsageElementLabeler
              .value(ImmutableSubstanceUsageElement.builder()
                  .type(AMOUNT)
                  .kind(DRUG)
                  .build())
              .label(substanceUsageSpan);
        }
      }
    }
  }

  private boolean checkExistsElementsSpan(Integer start, Integer end) {

    // Empty global match hash
    if (allElementsSpanHash.isEmpty()) {
      return false;
    }

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

