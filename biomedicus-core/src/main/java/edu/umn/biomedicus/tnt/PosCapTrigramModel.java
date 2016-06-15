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

import edu.umn.biomedicus.common.tuples.PosCap;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Statistical model determining the probability of a trigram of part of speech tags. It is a maximum likelihood
 * probability based on the relative frequencies of unigrams, bigrams, and trigrams of words (specifically their
 * part of speech tag and their capitalization).
 * It stores maps of
 * <ul>
 * <li>Unigrams: the probability that a word will have a POS tag and capitalization.</li>
 * <li>Bigrams: the probability that a word will have a POS tag and capitalization, conditional on the previous POS
 * tag and capitalization.</li>
 * <li>Trigrams: the probability that a word will have a POS tag and capitalization, conditional on the two
 * previous POS tags and capitalizations</li>
 * </ul>
 * <p/>
 * <p>Unigrams, bigrams, and trigrams are smoothed in order to prevent the case of 0 probability when a specific
 * trigram has not been encountered in the training corpus.</p>
 * <p>
 * <p>This data is then factored into viterbi markov model calculations.
 * <p>
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
class PosCapTrigramModel {

    /**
     * Probability that a pos cap ordinal will occur.
     */
    private final double[] unigramProbabilities;

    /**
     * Probability that a pos cap bigram will occur.
     */
    private final double[][] bigramProbabilities;

    /**
     * Probability that a pos cap trigram will occur.
     */
    private final double[][][] trigramProbabilities;

    /**
     * Lambda smoothing coefficient for unigrams.
     */
    private final double unigramLambda;

    /**
     * Lambda smoothing coefficient for bigrams.
     */
    private final double bigramLambda;

    /**
     * Lambda smoothing coefficient for trigrams.
     */
    private final double trigramLambda;

    /**
     * Default constructor. Creates the model with the given probability maps.
     *
     * @param unigramProbabilities probabilities that a pos-capitalization ordinal will occur
     * @param bigramProbabilities  probabilities that a pos-capitalization twin will occur conditional on a previous
     *                             pos-capitalization twin
     * @param trigramProbabilities probabilities that a pos-capitalization twin will occur conditional on a previous
     *                             sequence of two pos-capitalization twins
     * @param unigramLambda        smoothing factor for the unigram probabilities
     * @param bigramLambda         smoothing factor for the bigram probabilities
     * @param trigramLambda        smoothing factor for the trigram probabilities
     */
    public PosCapTrigramModel(double[] unigramProbabilities,
                              double[][] bigramProbabilities,
                              double[][][] trigramProbabilities,
                              double unigramLambda,
                              double bigramLambda,
                              double trigramLambda) {
        this.unigramProbabilities = unigramProbabilities;
        this.bigramProbabilities = bigramProbabilities;
        this.trigramProbabilities = trigramProbabilities;
        this.unigramLambda = unigramLambda;
        this.bigramLambda = bigramLambda;
        this.trigramLambda = trigramLambda;
    }

    /**
     * Returns the probability that a pos-capitalization (the third object in the trigram) will occur given the sequence
     * of two previous pos-capitalizations (the first and second {@link PosCap} objects.
     *
     * @return a double precision probability between 0.0 and 1.0
     */
    public double getTrigramProbability(PosCap first, PosCap second, PosCap third) {
        double unigramProbability = unigramProbabilities[third.ordinal()];
        double bigramProbability = bigramProbabilities[second.ordinal()][third.ordinal()];
        double trigramProbability = trigramProbabilities[first.ordinal()][second.ordinal()][third.ordinal()];

        return unigramLambda * unigramProbability + bigramLambda * bigramProbability + trigramLambda * trigramProbability;
    }

    public Map<String, Object> createStore() {
        Map<String, Object> store = new TreeMap<>();
        store.put("unigramLambda", unigramLambda);
        store.put("bigramLambda", bigramLambda);
        store.put("trigramLambda", trigramLambda);

        Map<PosCap, Double> unigrams = new TreeMap<>();
        for (int i = 0; i < unigramProbabilities.length; i++) {
            if (unigramProbabilities[i] > 0) {
                unigrams.put(PosCap.createFromOrdinal(i), unigramProbabilities[i]);
            }
        }
        store.put("unigram", unigrams);

        Map<PosCap, Map<PosCap, Double>> bigrams = new TreeMap<>();
        for (int i = 0; i < bigramProbabilities.length; i++) {
            for (int j = 0; j < bigramProbabilities[i].length; j++) {
                int second = j;
                if (bigramProbabilities[i][j] > 0) {
                    bigrams.compute(PosCap.createFromOrdinal(i), (key, value) -> {
                        if (value == null) {
                            value = new TreeMap<>();
                        }
                        value.put(PosCap.createFromOrdinal(second), bigramProbabilities[key.ordinal()][second]);
                        return value;
                    });
                }
            }
        }
        store.put("bigram", bigrams);

        Map<PosCap, Map<PosCap, Map<PosCap, Double>>> trigrams = new TreeMap<>();
        for (int i = 0; i < trigramProbabilities.length; i++) {
            for (int j = 0; j < trigramProbabilities[i].length; j++) {
                for (int k = 0; k < trigramProbabilities[i][j].length; k++) {
                    if (trigramProbabilities[i][j][k] > 0) {
                        int second = j;
                        int third = k;
                        trigrams.compute(PosCap.createFromOrdinal(i), (key, value) -> {
                            if (value == null) {
                                value = new TreeMap<>();
                            }
                            value.compute(PosCap.createFromOrdinal(second), (key2, value2) -> {
                                if (value2 == null) {
                                    value2 = new TreeMap<>();
                                }
                                value2.put(PosCap.createFromOrdinal(third), trigramProbabilities[key.ordinal()][key2.ordinal()][third]);
                                return value2;
                            });
                            return value;
                        });
                    }
                }
            }
        }
        store.put("trigram", trigrams);

        return store;
    }

    public static PosCapTrigramModel createFromStore(Map<String, Object> store) {
        @SuppressWarnings("unchecked")
        Map<PosCap, Double> unigram = (Map<PosCap, Double>) store.get("unigram");
        double[] unigrams = new double[PosCap.cardinality()];
        Arrays.fill(unigrams, 0.0);
        unigram.entrySet().forEach(e -> unigrams[e.getKey().ordinal()] = e.getValue());

        @SuppressWarnings("unchecked")
        Map<PosCap, Map<PosCap, Double>> bigram = (Map<PosCap, Map<PosCap, Double>>) store.get("bigram");
        double[][] bigrams = new double[PosCap.cardinality()][PosCap.cardinality()];
        for (double[] doubles : bigrams) {
            Arrays.fill(doubles, 0.0);
        }
        bigram.entrySet().forEach(e1 -> e1.getValue().entrySet()
                .forEach(e2 -> bigrams[e1.getKey().ordinal()][e2.getKey().ordinal()] = e2.getValue()));

        @SuppressWarnings("unchecked")
        Map<PosCap, Map<PosCap, Map<PosCap, Double>>> trigram = (Map<PosCap, Map<PosCap, Map<PosCap, Double>>>) store.get("trigram");
        double[][][] trigrams = new double[PosCap.cardinality()][PosCap.cardinality()][PosCap.cardinality()];
        for (double[][] bigramsInTrigram : trigrams) {
            for (double[] unigramsInTrigram : bigramsInTrigram) {
                Arrays.fill(unigramsInTrigram, 0.0);
            }
        }
        trigram.entrySet().forEach(e1 -> e1.getValue().entrySet().forEach(e2 -> e2.getValue().entrySet()
                .forEach(e3 -> trigrams[e1.getKey().ordinal()][e2.getKey().ordinal()][e3.getKey().ordinal()] = e3.getValue())));

        return new PosCapTrigramModel(unigrams, bigrams, trigrams, (double) store.get("unigramLambda"),
                (double) store.get("bigramLambda"), (double) store.get("trigramLambda"));
    }
}
