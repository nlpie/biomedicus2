/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.model.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.utilities.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
class PiSuffixModelTrainer extends WordModelTrainer {
    private static final Logger LOGGER = LogManager.getLogger();
    private double[] weights;

    PiSuffixModelTrainer(Set<PartOfSpeech> tagSet) {
        super(tagSet);
    }

    @Override
    public Map<String, Map<PartOfSpeech, Double>> apply(WordPosFrequencies wordPosFrequencies) {
        LOGGER.debug("Training probability interpolation suffix model");
        Map<Integer, WordPosFrequencies> byWordLength = wordPosFrequencies.byWordLength();

        // computes weights by using unbiased sample variance for the suffix length samples.
        weights = byWordLength.keySet()
                .stream()
                .sorted()
                .mapToDouble(suffixLength -> {
                    WordPosFrequencies ofLength = byWordLength.get(suffixLength);

                    Set<PartOfSpeech> partsOfSpeech = ofLength.partsOfSpeech();

                    double sumOfProbabilities = partsOfSpeech.stream()
                            .mapToDouble(ofLength::probabilityOfPartOfSpeech)
                            .sum();
                    double expected = sumOfProbabilities / (double) partsOfSpeech.size();

                    double sampleVariable = partsOfSpeech.stream()
                            .mapToDouble(ofLength::probabilityOfPartOfSpeech)
                            .map(prob -> Math.pow(prob - expected, 2))
                            .sum();
                    return Math.sqrt(sampleVariable / (double) (partsOfSpeech.size() - 1));
                })
                .toArray();
        LOGGER.debug("Computed weights for probability interpolation suffix model");

        Map<String, Map<PartOfSpeech, Double>> posProbabilitiesForWords = super.apply(wordPosFrequencies);
        LOGGER.debug("Computed probabilities for probability interpolation suffix model");

        posProbabilitiesForWords.replaceAll((word, posProbabilities) -> {
            posProbabilities.replaceAll((PartOfSpeech posKey, @Nullable Double probability) -> {
                double suffixProbability = wordPosFrequencies.probabilityOfWord(word);
                double posProbability = wordPosFrequencies.probabilityOfPartOfSpeech(posKey);
                if (posProbability != 0 && probability != null) {
                    probability = Math.log10(probability * suffixProbability / posProbability);
                } else {
                    probability = null;
                }
                return probability;
            });
            return posProbabilities;
        });

        LOGGER.debug("Finished training probability interpolation suffix model");

        return posProbabilitiesForWords;
    }

    @Override
    protected double getProbability(WordPosFrequencies wordPosFrequencies, String word, PartOfSpeech partOfSpeech) {
        List<String> collect = Strings.generateSuffixes(word).collect(Collectors.toList());

        double probability = wordPosFrequencies.probabilityOfPartOfSpeech(partOfSpeech);
        for (int i = collect.size() - 2; i >= 0; i--) {
            String suffix = collect.get(i);
            double maxLikelihood = wordPosFrequencies.probabilityOfPartOfSpeechConditionalOnWord(word, partOfSpeech);

            double weight = weights[suffix.length() - 1];
            probability = (maxLikelihood + weight * probability) / (1.0 + weight);
        }

        return probability;
    }

    static PiSuffixModelTrainer get(Set<PartOfSpeech> tagSet) {
        return new PiSuffixModelTrainer(tagSet);
    }
}
