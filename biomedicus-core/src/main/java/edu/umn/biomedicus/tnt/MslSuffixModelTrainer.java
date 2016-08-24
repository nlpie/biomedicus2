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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
class MslSuffixModelTrainer extends WordModelTrainer {
    private MslSuffixModelTrainer(Set<PartOfSpeech> tagSet) {
        super(tagSet);
    }

    public static MslSuffixModelTrainer get(Set<PartOfSpeech> tagSet) {
        return new MslSuffixModelTrainer(tagSet);
    }

    @Override
    protected double getProbability(WordPosFrequencies wordPosFrequencies, String word, PartOfSpeech partOfSpeech) {
        List<String> suffixes = Strings.generateSuffixes(word, word.length()).collect(Collectors.toList());
        suffixes.remove("");

        int prev = 0;
        int max = 0;
        for (String suffix : suffixes) {
            int freq = wordPosFrequencies.frequencyOfWordAndPartOfSpeech(suffix, partOfSpeech);
            int disjointFreq = freq - prev;
            if (disjointFreq > max) {
                max = disjointFreq;
            }
            prev = freq;
        }

        int posFreq = wordPosFrequencies.frequencyOfPartOfSpeech(partOfSpeech);
        return posFreq == 0 ? Double.NEGATIVE_INFINITY : Math.log10((double) max / (double) posFreq);
    }
}
