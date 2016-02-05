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
import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.model.text.Sentence;
import edu.umn.biomedicus.model.tuples.PosCap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * Trains the trigram model for the TnT tagger.
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
class PosCapTrigramModelTrainer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final PosCap BBS_POS_CAP = PosCap.create(PartOfSpeech.BBS, false);
    private static final PosCap BOS_POS_CAP = PosCap.create(PartOfSpeech.BOS, false);
    private static final PosCap EOS_POS_CAP = PosCap.create(PartOfSpeech.EOS, false);

    private final int[] tagFrequencies;
    private final int[][] bigramFrequencies;
    private final int[][][] trigramFrequencies;
    private int taggedTokens;


    /**
     * Default constructor. Initializes variables for counting tokens and trigrams, bigrams, and unigrams of
     * {@link edu.umn.biomedicus.model.tuples.PosCap} objects
     *
     * @param taggedTokens       the total number of tagged tokens seen by the trainer.
     * @param trigramFrequencies a multiset used to count the number of occurrences of PosCap trigrams
     * @param bigramFrequencies  a multiset used to count the number of occurrences of PosCap bigrams
     * @param tagFrequencies     a multiset used to count the number occurrences of PosCap unigrams
     */
    public PosCapTrigramModelTrainer(int taggedTokens,
                                     int[][][] trigramFrequencies,
                                     int[][] bigramFrequencies,
                                     int[] tagFrequencies) {
        this.taggedTokens = taggedTokens;
        this.trigramFrequencies = trigramFrequencies;
        this.bigramFrequencies = bigramFrequencies;
        this.tagFrequencies = tagFrequencies;
    }

    /**
     * Convenience constructor. Initializes the counting multisets to new {@link java.util.HashMap}
     * objects and the number of tagged tokens to 0.
     */
    public PosCapTrigramModelTrainer() {
        this.trigramFrequencies = new int[PosCap.cardinality()][PosCap.cardinality()][PosCap.cardinality()];
        for (int[][] bigramInTrigrams : trigramFrequencies) {
            for (int[] unigramInBigramInTrigrams : bigramInTrigrams) {
                Arrays.fill(unigramInBigramInTrigrams, 0);
            }
        }
        this.bigramFrequencies = new int[PosCap.cardinality()][PosCap.cardinality()];
        for (int[] unigramInBigrams : bigramFrequencies) {
            Arrays.fill(unigramInBigrams, 0);
        }
        this.tagFrequencies = new int[PosCap.cardinality()];
        Arrays.fill(tagFrequencies, 0);
        this.taggedTokens = 0;
    }

    /**
     * Adds the count of part of speech tag and capitalization unigram, bigrams and trigrams in the document to the
     * running counts in the trainer.
     *
     * @param document document that has tokens, sentences, and parts of speech annotated
     */
    public void addDocument(Document document) {
        for (Sentence sentence : document.getSentences()) {
            addSentence(sentence);
        }
    }

    /**
     * Adds the count of part of speech tag and capitalization unigram, bigrams and trigrams in the sentence to the
     * running counts in the trainer.
     *
     * @param sentence document that has tokens, sentences, and parts of speech annotated
     */
    public void addSentence(Sentence sentence) {
        boolean nulls = sentence.tokens()
                .filter(t -> t.getPartOfSpeech() == null)
                .findAny()
                .isPresent();
        if (nulls) {
            return;
        }

        int[] tokenPosCaps = sentence.tokens().map(PosCap::create).mapToInt(PosCap::ordinal).toArray();
        int[] posCaps = new int[tokenPosCaps.length + 3];
        posCaps[0] = BBS_POS_CAP.ordinal();
        posCaps[1] = BOS_POS_CAP.ordinal();
        System.arraycopy(tokenPosCaps, 0, posCaps, 2, tokenPosCaps.length);
        int length = posCaps.length;
        int eos = EOS_POS_CAP.ordinal();
        posCaps[length - 1] = eos;

        tagFrequencies[posCaps[0]] += 1;
        tagFrequencies[posCaps[1]] += 1;
        bigramFrequencies[posCaps[0]][posCaps[1]] += 1;
        taggedTokens += 2;

        for (int i = 0; i < length - 3; i++) {
            int second = posCaps[i + 1];
            int last = posCaps[i + 2];
            trigramFrequencies[posCaps[i]][second][last] += 1;
            bigramFrequencies[second][last] += 1;
            tagFrequencies[last] += 1;
            taggedTokens++;
        }
    }

    /**
     * Builds the unigram, bigram, trigram probabilities and their smoothing coefficients using the frequencies
     * counted by the trainer.
     *
     * @return the PosCapTrigramModel trained by the data collected by this class.
     */
    public PosCapTrigramModel build() {
        LOGGER.info("Building pos cap trigram model");

        LOGGER.debug("Computing unigram probabilities");
        // compute the unigram probabilities
        double[] unigramProbabilities = Arrays.stream(tagFrequencies)
                .mapToDouble(freq -> (double) freq / taggedTokens)
                .toArray();

        LOGGER.debug("Computing bigram probabilities");
        // compute the bigram probabilities
        int cardinality = PosCap.cardinality();
        double[][] bigramProbabilities = new double[cardinality][cardinality];
        for (int i = 0; i < bigramProbabilities.length; i++) {
            for (int j = 0; j < bigramProbabilities[i].length; j++) {
                int bigramFrequency = bigramFrequencies[i][j];
                int unigramFrequency = tagFrequencies[i];
                if (unigramFrequency == 0) {
                    bigramProbabilities[i][j] = 0.0;
                } else {
                    bigramProbabilities[i][j] = (double) bigramFrequency / (double) unigramFrequency;
                }
            }
        }

        int lambda1 = 0;
        int lambda2 = 0;
        int lambda3 = 0;

        LOGGER.debug("Computing trigram probabilities and smoothing coefficients");
        // compute the trigram probabilities and smoothing coefficients
        double[][][] trigramProbabilities = new double[cardinality][cardinality][cardinality];
        for (int i = 0; i < trigramProbabilities.length; i++) {
            for (int j = 0; j < trigramProbabilities[i].length; j++) {
                for (int k = 0; k < trigramProbabilities[i][j].length; k++) {
                    int headBigramFrequency = bigramFrequencies[i][j];
                    int trigramFrequency = trigramFrequencies[i][j][k];
                    double probability;
                    if (headBigramFrequency == 0) {
                        probability = 0.0;
                    } else {
                        probability = (double) trigramFrequency / (double) headBigramFrequency;
                    }
                    trigramProbabilities[i][j][k] = probability;

                    // lambda calculation
                    double case1 = (double) (trigramFrequency - 1) / (double) (headBigramFrequency - 1);

                    double case2 = (double) (bigramFrequencies[j][k] - 1)
                            / (double) (tagFrequencies[j] - 1);

                    double case3 = (double) (tagFrequencies[k] - 1)
                            / (double) (taggedTokens - 1);

                    // select the max of case 1, 2 and 3
                    if (case1 >= case2 && case1 >= case3) {
                        lambda3 += trigramFrequency;
                    } else if (case2 >= case3) {
                        lambda2 += trigramFrequency;
                    } else {
                        lambda1 += trigramFrequency;
                    }
                }
            }
        }

        // normalize lambdas
        double sums = lambda1 + lambda2 + lambda3;
        double unigramLambda = (double) lambda1 / sums;
        double bigramLambda = (double) lambda2 / sums;
        double trigramLambda = (double) lambda3 / sums;

        LOGGER.info("Finished build pos cap trigram model");

        return new PosCapTrigramModel(unigramProbabilities, bigramProbabilities, trigramProbabilities, unigramLambda,
                bigramLambda, trigramLambda);
    }
}
