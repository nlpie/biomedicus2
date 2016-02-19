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

import edu.umn.biomedicus.model.tuples.PosCap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
 * <p/>
 * <p>This data is then factored into viterbi markov model calculations.
 * <p/>
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
class PosCapTrigramModel {

    /**
     * Probability that a pos cap ordinal will occur.
     */
    private double[] unigramProbabilities;

    /**
     * Probability that a pos cap bigram will occur.
     */
    private double[][] bigramProbabilities;

    /**
     * Probability that a pos cap trigram will occur.
     */
    private double[][][] trigramProbabilities;

    /**
     * Lambda smoothing coefficient for unigrams.
     */
    private double unigramLambda;

    /**
     * Lambda smoothing coefficient for bigrams.
     */
    private double bigramLambda;

    /**
     * Lambda smoothing coefficient for trigrams.
     */
    private double trigramLambda;

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

    public double[] getUnigramProbabilities() {
        return unigramProbabilities;
    }

    public void setUnigramProbabilities(double[] unigramProbabilities) {
        this.unigramProbabilities = unigramProbabilities;
    }

    public double[][] getBigramProbabilities() {
        return bigramProbabilities;
    }

    public void setBigramProbabilities(double[][] bigramProbabilities) {
        this.bigramProbabilities = bigramProbabilities;
    }

    public double[][][] getTrigramProbabilities() {
        return trigramProbabilities;
    }

    public void setTrigramProbabilities(double[][][] trigramProbabilities) {
        this.trigramProbabilities = trigramProbabilities;
    }

    public double getUnigramLambda() {
        return unigramLambda;
    }

    public void setUnigramLambda(double unigramLambda) {
        this.unigramLambda = unigramLambda;
    }

    public double getBigramLambda() {
        return bigramLambda;
    }

    public void setBigramLambda(double bigramLambda) {
        this.bigramLambda = bigramLambda;
    }

    public double getTrigramLambda() {
        return trigramLambda;
    }

    public void setTrigramLambda(double trigramLambda) {
        this.trigramLambda = trigramLambda;
    }
}
