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

package edu.umn.biomedicus.modification;

import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.TermToken;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class ContextCues {
    private final List<List<String>> leftPhrases;
    private final int maxSizeLeftPhrase;
    private final List<List<String>> rightPhrases;
    private final int maxSizeRightPhrase;
    private final List<PartOfSpeech> scopeDelimitersPos;
    private final List<String> scopeDelimiterWords;

    private ContextCues(List<List<String>> leftPhrases,
                        int maxSizeLeftPhrase,
                        List<List<String>> rightPhrases,
                        int maxSizeRightPhrase,
                        List<PartOfSpeech> scopeDelimitersPos,
                        List<String> scopeDelimiterWords) {
        this.leftPhrases = leftPhrases;
        this.maxSizeLeftPhrase = maxSizeLeftPhrase;
        this.rightPhrases = rightPhrases;
        this.maxSizeRightPhrase = maxSizeRightPhrase;
        this.scopeDelimitersPos = scopeDelimitersPos;
        this.scopeDelimiterWords = scopeDelimiterWords;
    }

    @Nullable
    List<Label<TermToken>> searchLeft(List<Label<TermToken>> parseTokenLabels,
                                      LabelIndex<PartOfSpeech> partOfSpeeches) {
        List<Label<TermToken>> search = search(parseTokenLabels, partOfSpeeches, leftPhrases, maxSizeLeftPhrase);
        if (search != null) {
            Collections.reverse(search);
        }
        return search;
    }

    @Nullable
    List<Label<TermToken>> searchRight(List<Label<TermToken>> parseTokenLabels,
                                       LabelIndex<PartOfSpeech> partOfSpeeches) {
        return search(parseTokenLabels, partOfSpeeches, rightPhrases, maxSizeRightPhrase);
    }

    @Nullable
    private List<Label<TermToken>> search(List<Label<TermToken>> parseTokenLabels,
                                          LabelIndex<PartOfSpeech> partOfSpeeches,
                                          List<List<String>> phrases,
                                          int maxSize) {
        int size = parseTokenLabels.size();
        for (int i = 0; i < size; i++) {
            Label<TermToken> firstParseToken = parseTokenLabels.get(i);
            if (partOfSpeeches.insideSpan(firstParseToken).stream().anyMatch(scopeDelimitersPos::contains)) {
                return null;
            }
            String word = firstParseToken.value().text();
            if (scopeDelimiterWords.contains(word)) {
                return null;
            }
            int limit = Math.min(size - i, maxSize);
            for (int j = 1; j <= limit; j++) {
                List<Label<TermToken>> leftRange = parseTokenLabels.subList(i, i + j);
                List<String> leftSearch = new ArrayList<>(leftRange.size());
                for (Label<TermToken> parseTokenLabel : leftRange) {
                    leftSearch.add(parseTokenLabel.value().text());
                }
                if (phrases.contains(leftSearch)) {
                    return new ArrayList<>(leftRange);
                }
            }
        }
        return null;
    }

    static Builder builder() {
        return new Builder();
    }

    static class Builder {
        private final List<List<String>> leftPhrases = new ArrayList<>();
        private int maxSizeLeftPhrase = 0;

        private final List<List<String>> rightPhrases = new ArrayList<>();
        private int maxSizeRightPhrase = 0;

        private final List<PartOfSpeech> scopeDelimitersPos = new ArrayList<>();

        private final List<String> scopeDelimiterWords = new ArrayList<>();

        Builder addRightPhrase(String... words) {
            if (words.length > maxSizeRightPhrase) {
                maxSizeRightPhrase = words.length;
            }
            rightPhrases.add(Arrays.asList(words));
            return this;
        }

        Builder addLeftPhrase(String... words) {
            if (words.length > maxSizeLeftPhrase) {
                maxSizeLeftPhrase = words.length;
            }
            List<String> wordsList = Arrays.asList(words);
            Collections.reverse(wordsList);
            leftPhrases.add(wordsList);
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
            return new ContextCues(leftPhrases, maxSizeLeftPhrase, rightPhrases, maxSizeRightPhrase,
                    scopeDelimitersPos, scopeDelimiterWords);
        }
    }
}
