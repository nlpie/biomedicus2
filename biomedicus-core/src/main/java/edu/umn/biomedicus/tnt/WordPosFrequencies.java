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

package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.utilities.Strings;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ben Knoll
 * @since 1.1.0
 */
class WordPosFrequencies implements Serializable {
    private static final long serialVersionUID = 5039280481312327401L;
    private final Map<String, Map<PartOfSpeech, Integer>> posFrequenciesForWord;
    private final Map<PartOfSpeech, Integer> overallPosFrequencies;
    private int totalWords;

    WordPosFrequencies(Map<String, Map<PartOfSpeech, Integer>> posFrequenciesForWord,
                              Map<PartOfSpeech, Integer> overallPosFrequencies,
                              int totalWords) {
        this.posFrequenciesForWord = posFrequenciesForWord;
        this.overallPosFrequencies = overallPosFrequencies;
        this.totalWords = totalWords;
    }

    WordPosFrequencies() {
        this(new HashMap<>(), new EnumMap<>(PartOfSpeech.class), 0);
    }

    int getTotalWords() {
        return totalWords;
    }

    static WordPosFrequencies bySumming(Map<String, Map<PartOfSpeech, Integer>> posFrequenciesForWord) {
        Map<PartOfSpeech, Integer> overallPosFrequencies = posFrequenciesForWord.values()
                .stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));
        int totalWords = overallPosFrequencies.values().stream().mapToInt(v -> v).sum();
        return new WordPosFrequencies(posFrequenciesForWord, overallPosFrequencies, totalWords);
    }

    int frequencyOfWordAndPartOfSpeech(String word, PartOfSpeech partOfSpeech) {
        Map<PartOfSpeech, Integer> wordPosFrequencies = posFrequenciesForWord.get(word);
        return wordPosFrequencies == null ? 0 : wordPosFrequencies.getOrDefault(partOfSpeech, 0);
    }

    int frequencyOfWord(String word) {
        return posFrequenciesForWord.get(word).values().stream().mapToInt(i -> i).sum();
    }

    double probabilityOfWord(String word) {
        return (double) frequencyOfWord(word) / (double) totalWords;
    }

    double probabilityOfPartOfSpeech(PartOfSpeech partOfSpeech) {
        return (double) frequencyOfPartOfSpeech(partOfSpeech) / (double) totalWords;
    }

    int frequencyOfPartOfSpeech(PartOfSpeech tag) {
        return overallPosFrequencies.getOrDefault(tag, 0);
    }

    Set<String> getWords() {
        return posFrequenciesForWord.keySet();
    }

    Set<PartOfSpeech> partsOfSpeech() {
        return overallPosFrequencies.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(PartOfSpeech.class)));
    }

    WordPosFrequencies onlyWordsOccurringUpTo(int times) {
        Map<String, Map<PartOfSpeech, Integer>> filtered = posFrequenciesForWord.entrySet().stream()
                .filter(e -> e.getValue().values().stream().mapToInt(v -> v).sum() <= times)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return bySumming(filtered);
    }

    WordPosFrequencies expandSuffixes(int length) {
        WordPosFrequencies wordPosFrequencies = new WordPosFrequencies();

        for (Map.Entry<String, Map<PartOfSpeech, Integer>> entry : posFrequenciesForWord.entrySet()) {
            List<String> suffixes = Strings.generateSuffixes(entry.getKey(), length).collect(Collectors.toList());
            Map<PartOfSpeech, Integer> counts = entry.getValue();
            for (String suffix : suffixes) {
                wordPosFrequencies.addCounts(suffix, counts);
            }
        }

        return wordPosFrequencies;
    }

    Map<Integer, WordPosFrequencies> byWordLength() {
        Map<Integer, Map<String, Map<PartOfSpeech, Integer>>> builder = new HashMap<>();
        for (String s : getWords()) {
            int length = s.length();
            builder.compute(length, (Integer k, @Nullable Map<String, Map<PartOfSpeech, Integer>> v) -> {
                if (v == null) {
                    v = new HashMap<>();
                }
                v.put(s, posFrequenciesForWord.get(s));
                return v;
            });
        }
        return builder.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> bySumming(e.getValue())));
    }

    void addCounts(String word, Map<PartOfSpeech, Integer> m) {
        for (Map.Entry<PartOfSpeech, Integer> entry : m.entrySet()) {
            addCount(word, entry.getKey(), entry.getValue());
        }
    }

    void addCount(String word, PartOfSpeech partOfSpeech, int count) {
        posFrequenciesForWord.compute(word, (String key, @Nullable Map<PartOfSpeech, Integer> value) -> {
            Map<PartOfSpeech, Integer> partOfSpeechFrequencies = value;
            if (partOfSpeechFrequencies == null) {
                partOfSpeechFrequencies = new EnumMap<>(PartOfSpeech.class);
            }
            partOfSpeechFrequencies.merge(partOfSpeech, count, Integer::sum);
            return partOfSpeechFrequencies;
        });

        overallPosFrequencies.merge(partOfSpeech, count, Integer::sum);
        totalWords += count;
    }

    double probabilityOfPartOfSpeechConditionalOnWord(String word, PartOfSpeech partOfSpeech) {
        return (double) frequencyOfWordAndPartOfSpeech(word, partOfSpeech) / frequencyOfWord(word);
    }
}
