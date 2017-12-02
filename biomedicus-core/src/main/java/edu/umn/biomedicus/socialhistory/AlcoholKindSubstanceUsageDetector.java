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

import static edu.umn.biomedicus.sections.SubstanceUsageElementType.AMOUNT;
import static edu.umn.biomedicus.sections.SubstanceUsageElementType.FREQUENCY;
import static edu.umn.biomedicus.sections.SubstanceUsageElementType.METHOD;
import static edu.umn.biomedicus.sections.SubstanceUsageElementType.STATUS;
import static edu.umn.biomedicus.sections.SubstanceUsageElementType.TEMPORAL;
import static edu.umn.biomedicus.sections.SubstanceUsageElementType.TYPE;
import static edu.umn.biomedicus.sections.SubstanceUsageKind.ALCOHOL;

import com.google.inject.Singleton;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.parsing.ConstituencyParse;
import edu.umn.biomedicus.parsing.DependencyParse;
import edu.umn.biomedicus.sections.Section;
import edu.umn.biomedicus.sections.SectionContent;
import edu.umn.biomedicus.sections.SectionTitle;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.sh.SocialHistoryCandidate;
import edu.umn.biomedicus.sh.SubstanceUsageElement;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import edu.umn.nlpengine.Span;
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
public class AlcoholKindSubstanceUsageDetector implements KindSubstanceUsageDetector {

  private final Pattern typePattern;
  private final Pattern methodPattern;
  private final Pattern methodHintPattern;
  private final Pattern statusPattern;
  private final Pattern statusPossiblePattern;
  List<Pattern> amountPatternsList;
  List<Pattern> amountPossiblePatternsList;
  List<Pattern> frequencyPatternsList;
  List<Pattern> frequencyPatternsList2;
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


  public AlcoholKindSubstanceUsageDetector() throws BiomedicusException {

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
          .substring(0, strAroundWords.length() - 1);
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
            "edu/umn/biomedicus/config/socialHistory/AlcoholTypePhrases.txt")));
    typePattern = Pattern.compile(
        "(?i)\\b(" + readerType.lines().collect(Collectors.joining("|"))
            + ")\\b", Pattern.MULTILINE);

    // Load amount patterns
    try {

      BufferedReader brUnits = new BufferedReader(new InputStreamReader(
          classLoader.getResourceAsStream(
              "edu/umn/biomedicus/config/socialHistory/AlcoholAmountUnits.txt")));
      String strUnits = "(";
      while ((thisLine = brUnits.readLine()) != null) {
        strUnits = strUnits + thisLine + "|";
      }
      strUnits = strUnits.substring(0, strUnits.length() - 1);
      strUnits += ")";

      amountPatternsList = new ArrayList<>();
      BufferedReader brPatterns = new BufferedReader(
          new InputStreamReader(classLoader.getResourceAsStream(
              "edu/umn/biomedicus/config/socialHistory/AlcoholAmountPatterns.txt")));
      while ((thisLine = brPatterns.readLine()) != null) {

        String str = thisLine
            .replaceAll("\\$number_names", strNumberNames);
        str = str.replaceAll("\\$arround_words", strAroundWords);
        str = str.replaceAll("\\$units", strUnits);
        str = "\\b" + str + "\\b";
        amountPatternsList.add(Pattern.compile(str, Pattern.MULTILINE));
      }
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }

    // Load amount possible patterns
    try {

      BufferedReader brUnits = new BufferedReader(new InputStreamReader(
          classLoader.getResourceAsStream(
              "edu/umn/biomedicus/config/socialHistory/AlcoholAmountUnits.txt")));
      String strUnits = "(";
      while ((thisLine = brUnits.readLine()) != null) {
        strUnits = strUnits + thisLine + "|";
      }
      strUnits = strUnits.substring(0, strUnits.length() - 1);
      strUnits += ")";

      amountPossiblePatternsList = new ArrayList<>();
      BufferedReader brPatterns = new BufferedReader(
          new InputStreamReader(classLoader.getResourceAsStream(
              "edu/umn/biomedicus/config/socialHistory/AlcoholAmountPossiblePatterns.txt")));
      while ((thisLine = brPatterns.readLine()) != null) {

        String str = thisLine
            .replaceAll("\\$number_names", strNumberNames);
        str = str.replaceAll("\\$arround_words", strAroundWords);
        str = str.replaceAll("\\$units", strUnits);
        str = "\\b" + str + "\\b";
        amountPossiblePatternsList
            .add(Pattern.compile(str, Pattern.MULTILINE));
      }
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }

    // Load frequency patterns
    try {

      frequencyPatternsList = new ArrayList<>();
      BufferedReader brPatterns = new BufferedReader(
          new InputStreamReader(classLoader.getResourceAsStream(
              "edu/umn/biomedicus/config/socialHistory/AlcoholFrequencyPatterns.txt")));
      while ((thisLine = brPatterns.readLine()) != null) {

        String str = thisLine
            .replaceAll("\\$number_names", strNumberNames);
        str = str.replaceAll("\\$arround_words", strAroundWords);
        str = str.replaceAll("\\$time_period_names", strTimePeriod);
        frequencyPatternsList
            .add(Pattern.compile(str, Pattern.MULTILINE));
      }
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }
    // Load frequency patterns 2
    try {

      frequencyPatternsList2 = new ArrayList<>();
      BufferedReader brPatterns = new BufferedReader(
          new InputStreamReader(classLoader.getResourceAsStream(
              "edu/umn/biomedicus/config/socialHistory/AlcoholFrequencyPatterns2.txt")));
      while ((thisLine = brPatterns.readLine()) != null) {

        String str = thisLine
            .replaceAll("\\$number_names", strNumberNames);
        str = str.replaceAll("\\$arround_words", strAroundWords);
        str = str.replaceAll("\\$time_period_names", strTimePeriod);
        frequencyPatternsList2
            .add(Pattern.compile(str, Pattern.MULTILINE));
      }
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }

    // Load method patterns and hint patterns
    BufferedReader readerMethod = new BufferedReader(new InputStreamReader(
        classLoader.getResourceAsStream(
            "edu/umn/biomedicus/config/socialHistory/AlcoholMethodWords.txt")));
    methodPattern = Pattern.compile("(?i)\\b(" + readerMethod.lines()
        .collect(Collectors.joining("|")) + ")\\b", Pattern.MULTILINE);

    BufferedReader readerMethodHint = new BufferedReader(
        new InputStreamReader(classLoader.getResourceAsStream(
            "edu/umn/biomedicus/config/socialHistory/AlcoholMethodHintWords.txt")));
    methodHintPattern = Pattern.compile(
        "(?i)\\b(" + readerMethodHint.lines()
            .collect(Collectors.joining("|")) + ")\\b",
        Pattern.MULTILINE);

    // Load status patterns and possible patterns.
    BufferedReader readerStatus = new BufferedReader(new InputStreamReader(
        classLoader.getResourceAsStream(
            "edu/umn/biomedicus/config/socialHistory/AlcoholStatusPatterns.txt")));
    statusPattern = Pattern.compile("(?i)\\b(" + readerStatus.lines()
        .collect(Collectors.joining("|")) + ")\\b", Pattern.MULTILINE);

    BufferedReader readerStatusPossible = new BufferedReader(
        new InputStreamReader(classLoader.getResourceAsStream(
            "edu/umn/biomedicus/config/socialHistory/AlcoholStatusPossiblePatterns.txt")));
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
  public void processCandidate(
      TextView document,
      SocialHistoryCandidate socialHistoryCandidateLabel
  ) throws BiomedicusException {

    sectionLabels = document.getLabelIndex(Section.class);
    sectionTitleLabels = document.getLabelIndex(SectionTitle.class);
    candidateLabeler = document.getLabeler(SocialHistoryCandidate.class);
    sectionContentLabels = document.getLabelIndex(SectionContent.class);
    sentenceLabels = document.getLabelIndex(Sentence.class);
    SubstanceUsageElementLabeler = document
        .getLabeler(SubstanceUsageElement.class);
    parseTokenLabels = document.getLabelIndex(ParseToken.class);

    Sentence sentence = document.getLabelIndex(Sentence.class)
        .firstAtLocation(socialHistoryCandidateLabel);

    List<ParseToken> sentenceTermTokens = parseTokenLabels
        .insideSpan(sentence).asList();
    String strSentence = helper.toTokensString(sentenceTermTokens);

    DependencyParse dependencyParseLabel = document
        .getLabelIndex(DependencyParse.class)
        .firstAtLocation(sentence);

    ConstituencyParse consttParseLabel = document
        .getLabelIndex(ConstituencyParse.class)
        .firstAtLocation(
            sentence);

    String strDepen = dependencyParseLabel.getParseTree();
    String strConstt = consttParseLabel.getParseTree();

    Hashtable<String, Object> googleRelationHash = new Hashtable<>();
    googleRelationHash.put("DEP_STR", strDepen);
    Helpers.getGoogleDepenRelations(strDepen, googleRelationHash);

    allElementsSpanHash = new Hashtable<Integer, Integer>();
    extractType(sentence);
    extractTemporal(sentence, googleRelationHash, strConstt);
    extractAmount(sentence, googleRelationHash);
    extractFrequency(sentence, googleRelationHash);
    extractMethod(sentence, googleRelationHash);
    extractStatus(sentence, googleRelationHash);


  }

  private void extractTemporal(Sentence sentenceLabel,
      Hashtable<String, Object> googleRelationHash,
      String strConstt) throws BiomedicusException {

    List<ParseToken> sentenceTermTokens = parseTokenLabels
        .insideSpan(sentenceLabel).asList();
    String strSentence = helper.toTokensString(sentenceTermTokens);
    // Get all possible longest frequency patterns
    Hashtable<Integer, Integer> spanHash
        = new Hashtable<Integer, Integer>();
    for (Pattern pattern : temporalPatternsList) {

      Matcher matcher = pattern.matcher(strSentence);
      if (matcher.find()) {

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
    // Iterate each matched span, check if all matched text in the span hash are related to alcohol
    // If so create a substance usage label for it
    //
    enumKey = spanHash.keys();
    while (enumKey.hasMoreElements()) {

      Integer start = enumKey.nextElement();
      Integer end = spanHash.get(start);
      Span substanceUsageSpan = new Span(sentenceLabel.getStartIndex() + start,
          sentenceLabel.getStartIndex() + end);
      List<Integer> usageTokens = new ArrayList<>();
      allElementsSpanHash.put(start, end);

      // Get goggle tokens of the span
      Helpers.getUsageTokens(parseTokenLabels, sentenceLabel, start, end,
          usageTokens);

      String strDrugPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strDrugKeywords + ").*";
      String strTobaccoPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strTobaccoKeywords + ").*";

      if (!strSentence.matches(strDrugPattern)
          && !strSentence.matches(strTobaccoPattern)) {
        SubstanceUsageElementLabeler.add(new SubstanceUsageElement(substanceUsageSpan, ALCOHOL,
            TEMPORAL));
      }
      // Check if ancesters of all matched tokens related to alcohol
      else if (Helpers.checkSubstanceDepenRelated(usageTokens,
          KindSubstanceUsageDetector.strAlcoholKeywords,
          googleRelationHash)) {
        SubstanceUsageElementLabeler.add(new SubstanceUsageElement(substanceUsageSpan, ALCOHOL,
            TEMPORAL));
      }
    }
  }

  private void extractStatus(Sentence sentenceLabel,
      Hashtable<String, Object> googleRelationHash)
      throws BiomedicusException {

    List<ParseToken> sentenceTermTokens = parseTokenLabels
        .insideSpan(sentenceLabel).asList();
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
      if (bClear) {
        spanHash.put(start, end);
      }
    }

    //
    // Iterate each matched span, create a substance usage label for it
    //

    Enumeration<Integer> enumKey = spanHash.keys();
    while (enumKey.hasMoreElements()) {

      Integer start = enumKey.nextElement();
      Integer end = spanHash.get(start);
      Span substanceUsageSpan = new Span(sentenceLabel.getStartIndex() + start,
          sentenceLabel.getStartIndex() + end);

      SubstanceUsageElementLabeler.add(
          new SubstanceUsageElement(substanceUsageSpan, ALCOHOL, STATUS)
      );
    }

    // Get all possible longest  patterns
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
      if (bClear) {
        spanHash.put(start, end);
      }
    }

    //
    // Iterate each matched span, check if all matched text in the span hash are related to alcohol
    // If so create a substance usage label for it
    //

    enumKey = spanHash.keys();
    while (enumKey.hasMoreElements()) {

      Integer start = enumKey.nextElement();
      Integer end = spanHash.get(start);
      Span substanceUsageSpan = new Span(sentenceLabel.getStartIndex() + start,
          sentenceLabel.getStartIndex() + end);
      // Get goggle tokens of the span
      List<Integer> usageTokens = new ArrayList<>();
      Helpers.getUsageTokens(parseTokenLabels, sentenceLabel, start, end,
          usageTokens);

      String strDrugPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strDrugKeywords + ").*";
      String strTobaccoPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strTobaccoKeywords + ").*";

      if (!strSentence.matches(strDrugPattern)
          && !strSentence.matches(strTobaccoPattern)) {
        SubstanceUsageElementLabeler.add(
            new SubstanceUsageElement(substanceUsageSpan, ALCOHOL, STATUS)
        );
      }
      // Check if ancesters of all matched tokens related to alcohol
      else {
        if (Helpers.checkSubstanceDepenRelated(usageTokens,
            KindSubstanceUsageDetector.strAlcoholKeywords,
            googleRelationHash)) {
          SubstanceUsageElementLabeler.add(
              new SubstanceUsageElement(substanceUsageSpan, ALCOHOL, STATUS)
          );
        }
      }
    }
  }

  private void extractMethod(
      Sentence sentence,
      Hashtable<String, Object> googleRelationHash
  ) throws BiomedicusException {

    // Match method phrases that explicitly indicate alcohol drink method
    List<ParseToken> sentenceTermTokens = parseTokenLabels
        .insideSpan(sentence).asList();
    String strSentence = helper.toTokensString(sentenceTermTokens);

    Matcher matcher = methodPattern.matcher(strSentence);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();

      Span substanceUsageSpan = new Span(sentence.getStartIndex() + start,
          sentence.getStartIndex() + end);

      // Get goggle tokens of the span
      List<Integer> usageTokens = new ArrayList<>();
      Helpers.getUsageTokens(parseTokenLabels, sentence, start, end,
          usageTokens);

      //Check if the word is noun
//            if (usageTokens.uniqueTerms() == 1 ){
//                Hashtable<Integer, String> posHash = ( Hashtable<Integer, String>) googleRelationHash.get("POS");
//                if (posHash.get(usageTokens.get(0)).toString().matches(".*NN.*")) continue;
//            }

      SubstanceUsageElementLabeler.add(
          new SubstanceUsageElement(substanceUsageSpan, ALCOHOL, STATUS)
      );
    }

    // Match methods hint words that may indicate alcohol drink method, need find if related to alcohol
    matcher = methodHintPattern.matcher(strSentence);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      Span substanceUsageSpan = new Span(sentence.getStartIndex() + start,
          sentence.getStartIndex() + end);

            /*
            * Check if related to alcohol
            */

      // Get goggle tokens of the span
      List<Integer> usageTokens = new ArrayList<>();
      Helpers.getUsageTokens(parseTokenLabels, sentence, start, end, usageTokens);

      //Check if the word is noun
//            if (usageTokens.uniqueTerms() == 1 ){
//                Hashtable<Integer, String> posHash = ( Hashtable<Integer, String>) googleRelationHash.get("POS");
//                if (posHash.get(usageTokens.get(0)).toString().matches(".*NN.*")) continue;
//            }

      String strDrugPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strDrugKeywords + ").*";
      String strTobaccoPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strTobaccoKeywords + ").*";

      if (!strSentence.matches(strDrugPattern) && !strSentence
          .matches(strTobaccoPattern)) {
        SubstanceUsageElementLabeler.add(
            new SubstanceUsageElement(substanceUsageSpan, ALCOHOL, METHOD)
        );
      }
      // Check if ancesters of all matched tokens related to alcohol
      else if (Helpers.checkSubstanceDepenRelated(usageTokens,
          KindSubstanceUsageDetector.strAlcoholKeywords,
          googleRelationHash)) {
        SubstanceUsageElementLabeler.add(
            new SubstanceUsageElement(substanceUsageSpan, ALCOHOL, METHOD)
        );
      }
    }
  }

  private void extractFrequency(Sentence sentence,
      Hashtable<String, Object> googleRelationHash)
      throws BiomedicusException {

    List<ParseToken> sentenceTermTokens = parseTokenLabels.insideSpan(sentence).asList();
    String strSentence = helper.toTokensString(sentenceTermTokens);

    // Get all possible longest frequency patterns like "weekly basis"
    Hashtable<Integer, Integer> spanHash
        = new Hashtable<Integer, Integer>();
    for (Pattern pattern : frequencyPatternsList) {

      Matcher matcher = pattern.matcher(strSentence);
      if (matcher.find()) {

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

    // Iterate second pattern list for possible match like "5 drinks per day"
    for (Pattern pattern : frequencyPatternsList2) {

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

    //
    // Iterate each matched span, check if all matched text in the span hash are related to alcohol
    // If so create a substance usage label for it
    //

    Enumeration<Integer> enumKey = spanHash.keys();
    while (enumKey.hasMoreElements()) {

      Integer start = enumKey.nextElement();
      Integer end = spanHash.get(start);
      Span substanceUsageSpan = new Span(sentence.getStartIndex() + start,
          sentence.getStartIndex() + end);
      List<Integer> usageTokens = new ArrayList<>();

      // See if it's in a larger temporal annotation
      if (checkExistsElementsSpan(start, end)) {
        continue;
      }
      allElementsSpanHash.put(start, end);

      // Get goggle tokens of the span
      Helpers.getUsageTokens(parseTokenLabels, sentence, start, end,
          usageTokens);

      String strDrugPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strDrugKeywords + ").*";
      String strTobaccoPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strTobaccoKeywords + ").*";

      if (!strSentence.matches(strDrugPattern)
          && !strSentence.matches(strTobaccoPattern)) {
        SubstanceUsageElementLabeler.add(
            new SubstanceUsageElement(substanceUsageSpan, ALCOHOL, FREQUENCY)
        );
      }
      // Check if ancesters of all matched tokens related to alcohol
      else if (Helpers.checkSubstanceDepenRelated(usageTokens,
          KindSubstanceUsageDetector.strAlcoholKeywords,
          googleRelationHash)) {
        SubstanceUsageElementLabeler.add(
            new SubstanceUsageElement(substanceUsageSpan, ALCOHOL, FREQUENCY)
        );
      }
    }
  }


  private void extractType(Sentence sentence)
      throws BiomedicusException {

    List<ParseToken> sentenceTermTokens = parseTokenLabels
        .insideSpan(sentence).asList();
    String strSentence = helper.toTokensString(sentenceTermTokens);

    Matcher matcher = typePattern.matcher(strSentence);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      String strUsage = strSentence.substring(start, end);

      Span substanceUsageSpan = new Span(sentence.getStartIndex() + start,
          sentence.getStartIndex() + end);
      SubstanceUsageElementLabeler.add(
          new SubstanceUsageElement(substanceUsageSpan, ALCOHOL, TYPE)
      );
    }
  }

  private void extractAmount(
      Sentence sentence,
      Hashtable<String, Object> googleRelationHash
  ) throws BiomedicusException {

    List<ParseToken> sentenceTermTokens = parseTokenLabels
        .insideSpan(sentence).asList();
    String strSentence = helper.toTokensString(sentenceTermTokens);

    Hashtable<Integer, Integer> spanHash
        = new Hashtable<Integer, Integer>();

    for (Pattern pattern : amountPatternsList) {
      Matcher matcher = pattern.matcher(strSentence);
      while (matcher.find()) {

        int start = matcher.start();
        int end = matcher.end();
        String str = strSentence.substring(start, end);

        // Check if the span already been marked as a temporal element or amount element
        if (checkExistsElementsSpan(start, end)) {
          continue;
        }

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

    // Iterate second pattern list for possible match like "a lot"
    for (Pattern pattern : amountPossiblePatternsList) {

      Matcher matcher = pattern.matcher(strSentence);
      while (matcher.find()) {

        int start = matcher.start();
        int end = matcher.end();
        String str = strSentence.substring(start, end);

        // Check if the span already been marked as a temporal element or amount element
        if (checkExistsElementsSpan(start, end)) {
          continue;
        }

        boolean bClear = false;
        if (spanHash.isEmpty()) {
          // Empty match hash
          bClear = true;
        } else {
          // Non-empty hash, check if the span already included or there is already a longer label
          Enumeration<Integer> enumKeyLocal = spanHash.keys();
          while (enumKeyLocal.hasMoreElements()) {

            int key = enumKeyLocal.nextElement();
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
    Enumeration<Integer> enumKey = spanHash.keys();
    while (enumKey.hasMoreElements()) {

      Integer start = enumKey.nextElement();
      Integer end = spanHash.get(start);
      Span substanceUsageSpan = new Span(sentence.getStartIndex() + start,
          sentence.getStartIndex() + end);

      allElementsSpanHash.put(start, end);
      String strMatch = strSentence.substring(start, end);

      //Check if the word is verb
      List<Integer> usageTokens = new ArrayList<>();
      Helpers.getUsageTokens(parseTokenLabels, sentence, start, end,
          usageTokens);
      if (usageTokens.size() == 1) {
        Hashtable<Integer, String> posHash
            = (Hashtable<Integer, String>) googleRelationHash
            .get("POS");
        if (posHash.get(usageTokens.get(0)).toString()
            .matches(".*VB.*")) {
          continue;
        }
      }

      // Filter out amount pattern "a pack" in "smokes a pack"
      if (strMatch.matches(".*pack.*")) {
        if (strSentence.matches("(?i).*smoke.+pack.*")) {
          continue;
        }
      }

      String strDrugPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strDrugKeywords + ").*";
      String strTobaccoPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strTobaccoKeywords + ").*";
      String strAlcoholPattern = ".*(?i)("
          + KindSubstanceUsageDetector.strAlcoholKeywords + ").*";

      if (!strSentence.matches(strDrugPattern)
          && !strSentence.matches(strTobaccoPattern)) {
        SubstanceUsageElementLabeler.add(
            new SubstanceUsageElement(substanceUsageSpan, ALCOHOL, AMOUNT)
        );
      } else if (strMatch.matches(strAlcoholPattern)) {
        SubstanceUsageElementLabeler.add(
            new SubstanceUsageElement(substanceUsageSpan, ALCOHOL, AMOUNT)
        );
      }
      // Check if ancesters of all matched tokens related to alcohol
      else if (Helpers.checkSubstanceDepenRelated(usageTokens,
          KindSubstanceUsageDetector.strAlcoholKeywords,
          googleRelationHash)) {
        SubstanceUsageElementLabeler.add(
            new SubstanceUsageElement(substanceUsageSpan, ALCOHOL, AMOUNT)
        );
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
