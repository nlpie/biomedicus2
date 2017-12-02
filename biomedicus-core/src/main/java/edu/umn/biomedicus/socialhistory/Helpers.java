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

import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Span;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Helpers {

//    static String toTokensString(List<TermToken> termTokenList) {
//
//        StringBuilder stringBuilder = new StringBuilder();
//        for (TermToken termToken : termTokenList) {
//            stringBuilder.append(termToken.text());
//            if (termToken.hasSpaceAfter()) {
//                stringBuilder.append(" ");
//            }
//        }
//        return stringBuilder.toString();
//    }

  static String toTokensString(List<ParseToken> termTokenList) {

    StringBuilder stringBuilder = new StringBuilder();
    for (ParseToken termToken : termTokenList) {
      stringBuilder.append(termToken.getText());
      if (termToken.getHasSpaceAfter()) {
        stringBuilder.append(" ");
      }
    }
    return stringBuilder.toString();
  }


  static void getGoogleDepenRelations(String strDepen,
      Hashtable<String, Object> googleRelationHash) {

    String[] relations = strDepen.split("\n");

    //Key - token id, Value - token text
    Hashtable<Integer, String> textHash = new Hashtable<>();
    //Key -token id, 	Value - POS tag
    Hashtable<Integer, String> posHash = new Hashtable<>();
    //Key - token id, Value - conll depen tag
    Hashtable<Integer, String> conlTypelHash = new Hashtable<>();
    //Key - token id, Value - conll parents
    Hashtable<Integer, Integer> parentHash = new Hashtable<>();
    //Key - parent token id , Value - another hash of all children
    Hashtable<Integer, Hashtable<Integer, String>> childHash
        = new Hashtable<>();
    // Key - token id, Value - conj of this token
    Hashtable<Integer, Integer> conjHash = new Hashtable<>();
    // Key - token id, Value - array of siblings of this token
    Hashtable<Integer, List<Integer>> siblingHash = new Hashtable<>();

    Pattern pattern = Pattern.compile(
        "(\\d+)\\t(.+?)\\t(.+?)\\t(.+?)\\t(.+?)\\t(.+?)\\t(.+?)\\t(.+?)\\t(.+?)\\t(.+)");
    for (String strRelation : relations) {

      Matcher matcher = pattern.matcher(strRelation);

      while (matcher.find()) {

        conlTypelHash.put(Integer.parseInt(matcher.group(1).toString()),
            matcher.group(8).toString());
        posHash.put(Integer.parseInt(matcher.group(1).toString()),
            matcher.group(5).toString());
        textHash.put(Integer.parseInt(matcher.group(1).toString()),
            matcher.group(2).toString());

        if (matcher.group(8).toString() != "conj") {
          parentHash
              .put(Integer.parseInt(matcher.group(1).toString()),
                  Integer.parseInt(
                      matcher.group(7).toString()));

          // Exists child
          if (childHash.containsKey(
              Integer.parseInt(matcher.group(7).toString()))) {

            Hashtable<Integer, String> hash = childHash.get(Integer
                .parseInt(matcher.group(7).toString()));
            if (!hash.containsKey(Integer.parseInt(
                matcher.group(1).toString()))) {
              hash.put(Integer.parseInt(
                  matcher.group(1).toString()),
                  matcher.group(8).toString());
            }
          } else {
            // New child
            Hashtable<Integer, String> hash
                = new Hashtable<Integer, String>();
            hash.put(Integer.parseInt(matcher.group(1).toString()),
                matcher.group(8).toString());
            childHash.put(Integer
                .parseInt(matcher.group(7).toString()), hash);
          }
        } else {

          conjHash.put(Integer.parseInt(matcher.group(1).toString()),
              Integer.parseInt(matcher.group(7).toString()));
        }
      }
    }

    // Process tokens in conjunction relations to collect more parent and child relations
    // 1. add the parent of each conj as the parent of the token it conjuncted
    // 2. add each conj into the child list of each parent of the token it conjuncted
    // 3. If two token of the conj is not same POS, no processing
    Enumeration<Integer> enumKey = conjHash.keys();
    while (enumKey.hasMoreElements()) {

      Integer token_id = enumKey.nextElement();
      Integer refered_token = conjHash.get(token_id);

      if (!posHash.get(token_id).toString()
          .equals(posHash.get(refered_token).toString())) {
        continue;
      }

      Integer parentID = -1;
      if (parentHash.containsKey(token_id)) {
        parentID = parentHash.get(token_id);
      }

      if (parentID != -1) {

        parentHash.put(token_id, parentID);
        if (childHash.containsKey(parentID)) {

          Hashtable<Integer, String> hash = childHash.get(parentID);
          if (!hash.containsKey(token_id)) {
            hash.put(token_id, conlTypelHash.get(token_id));
          }
        }
      } else {
        Hashtable<Integer, String> hash
            = new Hashtable<Integer, String>();
        hash.put(token_id, conlTypelHash.get(token_id));
        childHash.put(parentID, hash);
      }
    }

    // Get sibling relations
    Enumeration<Integer> enumParent = childHash.keys();
    while (enumParent.hasMoreElements()) {

      Integer parentID = enumParent.nextElement();
      Hashtable<Integer, String> hash = childHash.get(parentID);

      if (hash.keySet().size() < 2) {
        continue;
      }

      // Iterate all children of a parent, for each child, get a hash of all its siblings
      Enumeration<Integer> enumChild = hash.keys();
      while (enumChild.hasMoreElements()) {

        Integer current_token_id = enumChild.nextElement();
        List<Integer> tokens = new ArrayList<>(hash.keySet());

        Iterator<Integer> iter = tokens.iterator();
        while (iter.hasNext()) {
          Integer token_id = iter.next();
          if (token_id == current_token_id) {
            iter.remove();
          }
        }
        siblingHash.put(current_token_id, tokens);
      }
    }

    googleRelationHash.put("PARENT", parentHash);
    googleRelationHash.put("CHILD", childHash);
    googleRelationHash.put("SIBLING", siblingHash);
    googleRelationHash.put("TEXT", textHash);
    googleRelationHash.put("POS", posHash);
    googleRelationHash.put("CONLL", conlTypelHash);

  }

  static void getUsageTokens(
      LabelIndex<ParseToken> parseTokenLabels,
      Sentence sentenceLabel,
      Integer start,
      Integer end,
      List<Integer> usageTokens
  ) {

    // Get the token id in google denpency parse of each token in the span
    Span substanceUsageSpan = new Span(sentenceLabel.getStartIndex() + start, sentenceLabel.getStartIndex() + end);

    Span beforeSpan = new Span(sentenceLabel.getStartIndex(), sentenceLabel.getStartIndex() + start);
    List<ParseToken> usageSpanTokens = parseTokenLabels
        .insideSpan(substanceUsageSpan).asList();
    List<ParseToken> beforeSpanTokens = parseTokenLabels
        .insideSpan(beforeSpan).asList();

    Integer token_id = 1;

    for (ParseToken ignored : beforeSpanTokens) {
      token_id++;
    }
    for (ParseToken ignored : usageSpanTokens) {
      usageTokens.add(token_id);
      token_id++;
    }


  }

  static boolean checkSmokeDrug(List<Integer> usageTokens,
      String strKeywords,
      Hashtable<String, Object> googleRelationHash) {

    Hashtable<Integer, String> textHash
        = (Hashtable<Integer, String>) googleRelationHash.get("TEXT");
    Hashtable<Integer, Hashtable<Integer, String>> childHash
        = (Hashtable<Integer, Hashtable<Integer, String>>) googleRelationHash
        .get("CHILD");
    String strPattern = ".*(?i)\\b(" + strKeywords + ")\\b.*";

    // Get all decendants
    List<Integer> childLst = new ArrayList<>();

    for (Integer usageTokenID : usageTokens) {

      if (childHash.containsKey((usageTokenID))) {
        childLst.addAll(
            getDecendents(usageTokenID, googleRelationHash));
      }
    }
    for (Integer tokenID : childLst) {
      if (textHash.get(tokenID).matches(strPattern)) {
        return true;
      }
    }

    return false;
  }

  static boolean checkSubstanceDepenRelated(List<Integer> usageTokens,
      String strKeywords,
      Hashtable<String, Object> googleRelationHash) {

    Hashtable<Integer, String> textHash
        = (Hashtable<Integer, String>) googleRelationHash.get("TEXT");
    Hashtable<Integer, String> posHash
        = (Hashtable<Integer, String>) googleRelationHash.get("POS");
    Hashtable<Integer, String> conlTypelHash
        = (Hashtable<Integer, String>) googleRelationHash.get("CONLL");
    Hashtable<Integer, Integer> parentlHash
        = (Hashtable<Integer, Integer>) googleRelationHash
        .get("PARENT");
    Hashtable<Integer, Hashtable<Integer, String>> childHash
        = (Hashtable<Integer, Hashtable<Integer, String>>) googleRelationHash
        .get("CHILD");
    Hashtable<Integer, List<Integer>> siblingHash
        = (Hashtable<Integer, List<Integer>>) googleRelationHash
        .get("SIBLING");
    String strPattern = ".*(?i)\\b(" + strKeywords + ")\\b.*";

    //  1. Find if any ancester  has a keyword
    //     1. if an ancester has keyword
    //     2. a ancester is a verb:
    //             1. the dobj of the verb has keyword
    //             2. the verb itself has keyword"

    // Get all ancesters
    Hashtable<Integer, Integer> ancesters = new Hashtable<>();
    Integer order = 1;
    for (Integer usageTokenID : usageTokens) {

      Integer parentID = -1;
      if (parentlHash.containsKey(usageTokenID)) {
        parentID = parentlHash.get(usageTokenID);
      }

      while (parentID != 0 && parentID != -1) {

        if (posHash.get(parentID).toString().matches(".*\\bVB.*")
            && conlTypelHash.get(parentID).toString()
            .matches(".*\\b(aux|cop|auxpass).*")) {
          ancesters.put(parentID, order++);
        } else {
          if (!ancesters.containsKey(parentID)) {
            ancesters.put(parentID, order++);
          }
        }
        if (parentlHash.containsKey(parentID)) {
          parentID = parentlHash.get(parentID);
        }
      }
    }

    // Check if an ancester or dobj of the ancester has keyword
    List<Integer> lst = new ArrayList(ancesters.keySet());

    for (Integer parentID : lst) {

      if (textHash.get(parentID).matches(strPattern)) {
        return true;
      }

      // Check if a dobj related to alcohol
      if (posHash.get(parentID).toString().matches(".*\\bVB.*")
          && !posHash.get(parentID).toString()
          .matches(".*\\bVBG\\b")) {

        if (textHash.get(parentID).toString().matches(strPattern)) {
          return true;
        }

        String strChildText = "";
        if (childHash.containsKey(parentID)) {

          Hashtable<Integer, String> childTypeHash = childHash
              .get(parentID);
          List<Integer> childLst = new ArrayList(
              childTypeHash.keySet());
          for (Integer childId : childLst) {

            if (conlTypelHash.containsKey(childId)) {

              if ((conlTypelHash.get(childId).toString()
                  .equals("dobj") || (conlTypelHash
                  .get(childId).toString()
                  .equals("nsubjpass")))) {
                strChildText = textHash.get(childId);
              }
            }
          }
          if (strChildText.matches(strPattern)) {
            return true;
          }
        }
      }
    }

        /*
        *  2. Find if any desendents  has a keyword
        */

    // Get all decendants
    List<Integer> childLst = new ArrayList<>();

    for (Integer usageTokenID : usageTokens) {

      if (childHash.containsKey((usageTokenID))) {

        if (posHash.get(usageTokenID).toString().matches(".*\\bVB.*")) {
          childLst.addAll(
              getVerbSubject(usageTokenID, googleRelationHash));
        } else {
          childLst.addAll(
              getDecendents(usageTokenID, googleRelationHash));
        }
      }
    }

    for (Integer tokenID : childLst) {
      if (textHash.get(tokenID).matches(strPattern)) {
        return true;
      }
    }

        /*
        *  2. Find if any sibling  has a keyword, and child of sibling
        */

    List<Integer> siblingLst = new ArrayList<>();

    for (Integer usageTokenID : usageTokens) {

      if (siblingHash.containsKey(usageTokenID)) {

        siblingLst = siblingHash.get(usageTokenID);
      }
    }
    for (Integer tokenID : siblingLst) {
      if (textHash.get(tokenID).matches(strPattern)) {
        return true;
      }
    }

    return false;

  }

  static List<Integer> getDecendents(Integer parentID,
      Hashtable<String, Object> googleRelationHash) {

    Hashtable<Integer, Hashtable<Integer, String>> childHash
        = (Hashtable<Integer, Hashtable<Integer, String>>) googleRelationHash
        .get("CHILD");
    List<Integer> lst = null;

    if (childHash.containsKey(parentID)) {

      Hashtable<Integer, String> childTypeHash = childHash.get(parentID);
      lst = new ArrayList<>(childTypeHash.keySet());
    }

    List<Integer> lstRtn = new ArrayList<>();
    while (lst.size() > 0) {

      Integer currentID = lst.get(0);
      lst.remove(0);
      lstRtn.add(currentID);

      if (childHash.containsKey(currentID)) {

        Hashtable<Integer, String> childTypeHash = childHash
            .get(currentID);
        lst.addAll(childTypeHash.keySet());
        for (Integer id : childTypeHash.keySet()) {
        }
      }
    }
    return lstRtn;
  }

  static List<Integer> getVerbSubject(Integer verbID,
      Hashtable<String, Object> googleRelationHash) {

    Hashtable<Integer, Hashtable<Integer, String>> childHash
        = (Hashtable<Integer, Hashtable<Integer, String>>) googleRelationHash
        .get("CHILD");
    Hashtable<Integer, String> posHash
        = (Hashtable<Integer, String>) googleRelationHash.get("POS");
    List<Integer> lst = null;

    if (childHash.containsKey(verbID)) {

      Hashtable<Integer, String> childTypeHash = childHash.get(verbID);
      lst = new ArrayList<>(childTypeHash.keySet());
    }

    List<Integer> lstRtn = new ArrayList<>();
    while (lst.size() > 0) {

      Integer currentID = lst.get(0);
      lst.remove(0);
      lstRtn.add(currentID);

      if (posHash.get(currentID).toString().matches(".*\\bVB.*")) {
        continue;
      }
      if (childHash.containsKey(currentID)) {

        Hashtable<Integer, String> childTypeHash = childHash
            .get(currentID);
        lst.addAll(childTypeHash.keySet());
        for (Integer id : childTypeHash.keySet()) {
        }
      }
    }
    return lstRtn;
  }


  static void getTemporalSyntaxPhrases(String strConstt,
      LabelIndex<ParseToken> parseTokenLabels,
      Hashtable<Integer, Integer> consttPPHash,
      Sentence sentenceLabel) {

    List<String> lstTemporal = new ArrayList<>();

    strConstt = strConstt.replace("\n", " ");
    strConstt = strConstt.replaceAll("\\s+", " ");

    List<ParseToken> sentenceTermTokens = parseTokenLabels.insideSpan(sentenceLabel).asList();
    String strSentence = toTokensString(sentenceTermTokens);
    Hashtable<Integer, Integer> temporalHash
        = new Hashtable<Integer, Integer>();

    String strTemooralKeyword = "(?i).*\\b(life|last|pase)\\b.*";
    String strTimePeriod
        = "(?i).*\\b(year|month|day|week|years|months|days|weeks|hour|hours|minute|minutes|mon|yr|yrs|wk|wks|night|day).*\\b";
    // PP phrases
    // (PP (IN temporal_pp_word)(.*)
    String strTemporalPPWordPattern
        = "(?i)\\(PP \\(IN (in|from|over|around|since|after|on|at|following|before|during|under|for|by|through|until|within|throughout)";
    Pattern pattern = Pattern.compile(strTemporalPPWordPattern);
    Matcher matcher = pattern.matcher((strConstt));
    while (matcher.find()) {
      Integer start = matcher.start();
      Integer end = start;
      Integer count = 0;
      char c;
      while (end < strConstt.length() && end > -1) {

        c = strConstt.charAt(end);
        if (c == '(') {
          count++;
        }
        if (c == ')') {
          count--;
        }
        if (count == 0) {
          break;
        }
        end++;
      }
      String str = strConstt.substring(start, end);
      if ((str.matches(strTemooralKeyword) || str.matches(strTimePeriod))
          && !str.matches(".*(daily|weekly|monthly|yearly).*")) {

        lstTemporal.add(str);
        temporalHash.put(start, end);
      }
    }

    //
    // Other possible phrases
    //

    //PP-TMP (IN As) (PP (IN of) (NP (NNP Sept.) (CD 30)
    String strPattern = "(?i)\\(PP \\(IN as\\) \\(PP \\(IN of\\)";
    pattern = Pattern.compile(strPattern);
    matcher = pattern.matcher((strConstt));
    while (matcher.find()) {

      Integer start = matcher.start();
      Integer end = start;
      Integer count = 0;
      char c;
      while (end < strConstt.length() && end > -1) {

        c = strConstt.charAt(end);
        if (c == '(') {
          count++;
        }
        if (c == ')') {
          count--;
        }
        if (count == 0) {
          break;
        }
        end++;
      }
      if (start > -1) {
        lstTemporal.add(strConstt.substring(start, end));
        temporalHash.put(start, end);
      }
    }

    //(ADVP-TMP (RB late) (PP (IN in)
    strPattern = "(?i)\\(ADVP-TMP \\(RB late\\) \\(PP \\(IN in";
    pattern = Pattern.compile(strPattern);
    matcher = pattern.matcher((strConstt));
    while (matcher.find()) {

      Integer start = matcher.start();
      Integer end = start;
      Integer count = 0;
      char c;
      while (end < strConstt.length() && end > -1) {

        c = strConstt.charAt(end);
        if (c == '(') {
          count++;
        }
        if (c == ')') {
          count--;
        }
        if (count == 0) {
          break;
        }
        end++;
      }
      if (start > -1) {
        lstTemporal.add(strConstt.substring(start, end));
        temporalHash.put(start, end);
      }
    }

    //(ADVP (NP (CD 2) (NNS weeks)) (RB ago)))
    strPattern = "\\(ADVP (?!.*?(?:ADVP)).* ago\\)";
    pattern = Pattern.compile(strPattern);
    matcher = pattern.matcher((strConstt));
    while (matcher.find()) {

      Integer start = matcher.start();
      Integer end = start;
      Integer count = 0;

      char c;
      while (end < strConstt.length() && end > -1) {

        c = strConstt.charAt(end);
        if (c == '(') {
          count++;
        }
        if (c == ')') {
          count--;
        }
        if (count == 0) {
          break;
        }
        end++;
      }
      if (start > -1) {
        lstTemporal.add(strConstt.substring(start, end));
        String str = strConstt.substring(start, end);
        temporalHash.put(start, end);
      }
    }

    // Get location of the phrase
    Enumeration<Integer> enumKey = temporalHash.keys();
    while (enumKey.hasMoreElements()) {

      Integer start = enumKey.nextElement();
      Integer end = temporalHash.get(start);

      // Get the start point in text
      String beforeParse = strConstt.substring(0, start);
      pattern = Pattern.compile(" ([^ )]+)\\)+");
      matcher = pattern.matcher(beforeParse);
      String beforePhrase = "";
      while (matcher.find()) {
        beforePhrase = beforePhrase + matcher.group(1).toString() + " ";
      }
      beforePhrase = beforePhrase.substring(0, beforePhrase.length() - 1);

      // Get end point in text
      String temporalParse = strConstt.substring(start, end);
      matcher = pattern.matcher(temporalParse);
      String temporalPhrase = "";
      while (matcher.find()) {
        temporalPhrase = temporalPhrase + matcher.group(1).toString()
            + " ";
      }
      temporalPhrase = temporalPhrase
          .substring(0, temporalPhrase.length() - 1);

      Integer localStart = strSentence
          .indexOf(temporalPhrase, beforePhrase.length());
      Integer localEnd = localStart + temporalPhrase.length();

      if (localStart > -1) {
        consttPPHash.put(localStart, localEnd);
      }
    }
  }


}
