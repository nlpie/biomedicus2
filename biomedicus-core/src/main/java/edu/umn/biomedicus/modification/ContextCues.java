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

package edu.umn.biomedicus.modification;

import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.tokenization.TermToken;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Span;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

class ContextCues {

  private final List<List<String>> leftPhrases;
  private final List<ModificationType> leftTypes;
  private final int maxSizeLeftPhrase;
  private final List<List<String>> rightPhrases;
  private final List<ModificationType> rightTypes;
  private final int maxSizeRightPhrase;
  private final List<PartOfSpeech> scopeDelimitersPos;
  private final List<String> scopeDelimiterWords;

  private ContextCues(
      List<List<String>> leftPhrases,
      List<ModificationType> leftTypes,
      int maxSizeLeftPhrase,
      List<List<String>> rightPhrases,
      List<ModificationType> rightTypes,
      int maxSizeRightPhrase,
      List<PartOfSpeech> scopeDelimitersPos,
      List<String> scopeDelimiterWords
  ) {
    this.leftPhrases = leftPhrases;
    this.leftTypes = leftTypes;
    this.maxSizeLeftPhrase = maxSizeLeftPhrase;
    this.rightPhrases = rightPhrases;
    this.rightTypes = rightTypes;
    this.maxSizeRightPhrase = maxSizeRightPhrase;
    this.scopeDelimitersPos = scopeDelimitersPos;
    this.scopeDelimiterWords = scopeDelimiterWords;
  }

  static Builder builder() {
    return new Builder();
  }

  @Nullable
  private Pair<Integer, List<Span>> search(
      List<TermToken> parseTokenLabels,
      LabelIndex<PosTag> partOfSpeeches,
      List<List<String>> phrases,
      int maxSize
  ) {
    int size = parseTokenLabels.size();
    for (int i = 0; i < size; i++) {
      TermToken firstParseToken = parseTokenLabels.get(i);
      for (PosTag posTag : partOfSpeeches.insideSpan(firstParseToken)) {
        if (scopeDelimitersPos.contains(posTag.getPartOfSpeech())) {
          return null;
        }
      }
      String word = firstParseToken.getText();
      if (scopeDelimiterWords.contains(word)) {
        return null;
      }
      int limit = Math.min(size - i, maxSize);
      for (int j = i + 1; j <= limit; j++) {
        List<TermToken> leftRange = parseTokenLabels.subList(i, i + j);
        List<String> leftSearch = new ArrayList<>(leftRange.size());
        for (TermToken termToken : leftRange) {
          leftSearch.add(termToken.getText());
        }
        int indexOf = phrases.indexOf(leftSearch);
        if (indexOf != -1) {
          ArrayList<Span> result = new ArrayList<>();
          for (TermToken tokenLabel : leftRange) {
            result.add(tokenLabel.toSpan());
          }
          return new Pair<>(indexOf, result);
        }
      }
    }
    return null;
  }

  @Nullable
  Pair<ModificationType, List<Span>> searchLeft(
      List<TermToken> parseTokenLabels,
      LabelIndex<PosTag> partOfSpeeches
  ) {
    Pair<Integer, List<Span>> search = search(parseTokenLabels, partOfSpeeches, leftPhrases,
        maxSizeLeftPhrase);
    if (search != null) {
      Collections.reverse(search.second());
    }
    return search == null ? null : Pair.of(leftTypes.get(search.first()), search.second());
  }

  @Nullable
  Pair<ModificationType, List<Span>> searchRight(
      List<TermToken> parseTokenLabels,
      LabelIndex<PosTag> partOfSpeeches
  ) {
    Pair<Integer, List<Span>> search = search(parseTokenLabels, partOfSpeeches, rightPhrases,
        maxSizeRightPhrase);
    return search == null ? null : Pair.of(rightTypes.get(search.getFirst()), search.getSecond());
  }

  static class Builder {

    private final List<List<String>> leftPhrases = new ArrayList<>();
    private final List<ModificationType> leftTypes = new ArrayList<>();
    private final List<List<String>> rightPhrases = new ArrayList<>();
    private final List<ModificationType> rightTypes = new ArrayList<>();
    private final List<PartOfSpeech> scopeDelimitersPos = new ArrayList<>();
    private final List<String> scopeDelimiterWords = new ArrayList<>();
    private int maxSizeLeftPhrase = 0;
    private int maxSizeRightPhrase = 0;

    Builder addRightPhrase(ModificationType modificationType, String... words) {
      if (words.length > maxSizeRightPhrase) {
        maxSizeRightPhrase = words.length;
      }
      rightPhrases.add(Arrays.asList(words));
      rightTypes.add(modificationType);
      return this;
    }

    Builder addLeftPhrase(ModificationType modificationType, String... words) {
      if (words.length > maxSizeLeftPhrase) {
        maxSizeLeftPhrase = words.length;
      }
      List<String> wordsList = Arrays.asList(words);
      Collections.reverse(wordsList);
      leftPhrases.add(wordsList);
      leftTypes.add(modificationType);
      return this;
    }

    Builder addScopeDelimitingPos(PartOfSpeech partOfSpeech) {
      scopeDelimitersPos.add(partOfSpeech);
      return this;
    }

    Builder addScopeDelimitingWord(String word) {
      scopeDelimiterWords.add(word);
      return this;
    }

    ContextCues build() {
      return new ContextCues(leftPhrases, leftTypes, maxSizeLeftPhrase, rightPhrases, rightTypes,
          maxSizeRightPhrase,
          scopeDelimitersPos, scopeDelimiterWords);
    }
  }
}
