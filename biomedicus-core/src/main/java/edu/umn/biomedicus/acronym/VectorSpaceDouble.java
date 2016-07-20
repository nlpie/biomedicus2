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

package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.common.text.Token;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * A vector space used to calculate word vectors from context
 * Used by the AcronymExpander
 *
 * @author Greg Finley
 * @since 1.5.0
 */
public class VectorSpaceDouble {
    /**
     * How quickly the sigmoid falls off. More of an idiosyncratic steepness parameter than a slope
     */
    private static final double SLOPE = 0.3;

    /**
     * Default weighting function is sigmoid that decreases with distance (to 0.5 at maxDist)
     * Need to cast to Serializable to save it
     */
    private static final BiFunction<Integer, Double, Double> DIST_WEIGHT = (BiFunction<Integer, Double, Double> & Serializable) (dist, maxDist) -> 1.0 / (1.0 + Math.exp(SLOPE * (Math.abs(dist) - maxDist)));

    /**
     * Should the IDF be squared, to effectively apply it to both test and train vectors (or raised to another power)?
     */
    private static final double IDF_POWER = 1;

    /**
     *
     */
    private static final double THRESH_WEIGHT = 0.25;

    private static final Pattern ALPHANUMERIC = Pattern.compile("[a-zA-Z0-9.&_]*");

    /**
     * Distance to use for weighting function
     */
    private transient double maxDist;

    /**
     * The actual size of the window; past threshWeight, we won't even consider words
     */
    private transient double windowSize;

    private Map<String, Integer> dictionary = new HashMap<>();

    /**
     * A count of how many "documents" (training examples) contain each term
     * Maps words (in their integer form, accessible through dictionary) to counts
     */
    private Map<Integer, Integer> documentsPerTerm = new HashMap<>();

    /**
     *  Number of "documents" (contexts) seen in training
     */
    private int totalDocs = 0;

    /**
     *  A log-transformed version of documentsPerTerm
     */
    private DoubleVector idf;

    /**
     * This will be set to false when calculating the idf, and terms will no longer be added to IDF counts
     */
    private boolean training = true;

    /**
     * Default constructor, uses maximum distance of 9.
     */
    public VectorSpaceDouble() {
        setMaxDist(9);
    }

    public double getMaxDist() {
        return maxDist;
    }

    public void setMaxDist(double maxDist) {
        this.maxDist = maxDist;
        windowSize = Math.log(1.0 / THRESH_WEIGHT - 1) / SLOPE + maxDist;
    }

    public DoubleVector getIdf() {
        return idf;
    }

    public Map<String, Integer> getDictionary() {
        return dictionary;
    }

    public void setDictionary(Map<String, Integer> dictionary) {
        this.dictionary = dictionary;
    }

    public Map<Integer, Integer> getDocumentsPerTerm() {
        return documentsPerTerm;
    }

    public void setDocumentsPerTerm(Map<Integer, Integer> documentsPerTerm) {
        this.documentsPerTerm = documentsPerTerm;
    }

    public double getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(double windowSize) {
        this.windowSize = windowSize;
    }

    public int getTotalDocs() {
        return totalDocs;
    }

    public void setTotalDocs(int totalDocs) {
        this.totalDocs = totalDocs;
    }

    public void setIdf(DoubleVector idf) {
        this.idf = idf;
    }

    public boolean isTraining() {
        return training;
    }

    public void setTraining(boolean training) {
        this.training = training;
    }

    /**
     * This needs to be called after all training vectors have been passed.
     * It sets up the IDF for each term and will save cycles at test time by stopping counting for the IDF
     */
    public void finishTraining() {
        Map<Integer, Double> idf = new HashMap<>();
        // Add 1 to denominator in case there are zero-counts, and to numerator in case there are 'all'-counts
        for (Map.Entry<Integer, Integer> e : documentsPerTerm.entrySet()) {
            double logged = Math.pow(Math.log((1 + (double) totalDocs) / (e.getValue())), IDF_POWER);
            idf.put(e.getKey(), logged);
        }
        this.idf = new WordVectorDouble();
        this.idf.setVector(idf);
        training = false;
    }

    /**
     * Generate a WordVectorDouble from a list of Tokens
     * The Token of interest should also be passed so we know positions for weighting
     *
     * @param context         A list of Tokens taken from the Document that the word of interest appears in
     * @param tokenOfInterest The token that we want to calculate a vector for
     * @return The calculated vector
     */
    public WordVectorDouble vectorize(List<Token> context, Token tokenOfInterest) {

        assert context.contains(tokenOfInterest);

        Map<Integer, Double> wordVector = new HashMap<>();

        // Contains a list of words in the given tokens (standard forms, and filtering out non-alphanumeric tokens)
        List<Integer> wordIntList = new ArrayList<>();

        // If we're still determining IDF of tokens, we'll use this Set at the end to update those counts
        Set<Integer> wordIntSetForIdf = new HashSet<>();

        // Index of the center token in our list of words
        int centerWord = 0;
        // To determine our position in the word list. Useful when calculating distance from center
        int i = 0;

        for (Token token : context) {

            // Determine if we've hit the token of interest yet
            if (centerWord == 0 && token == tokenOfInterest) {
                centerWord = i;
            }

            // Generate a list of words, if deemed acceptable words, whose values in the vector will be updated
            String word = standardForm(token);
            if (ALPHANUMERIC.matcher(word).matches() || token == tokenOfInterest) {
                int wordInt = dictionary.getOrDefault(word, -1);
                if (training) {
                    dictionary.putIfAbsent(word, dictionary.size());
                    wordInt = dictionary.get(word);
                }
                wordIntList.add(wordInt);
                i++;
                if (training) {
                    wordIntSetForIdf.add(wordInt);
                }
            }
        }
        // Array of integers that correspond to the position relative to tokenOfInterest of each word in the wordList
        int[] position = IntStream.range(-centerWord, wordIntList.size() - centerWord).toArray();
        i = 0;
        for (int wordInt : wordIntList) {
            if (Math.abs(position[i]) <= windowSize && position[i] != 0) {
                double thisCount = DIST_WEIGHT.apply(position[i], maxDist);
                double oldWordScore = 0;
                // Don't add the center token (the one at position 0); that's the term of interest
                if (position[i] != 0) {
                    if (wordVector.containsKey(wordInt)) {
                        oldWordScore = wordVector.get(wordInt);
                    }
                    wordVector.put(wordInt, oldWordScore + thisCount);
                }
            }
            i++;
        }

        // Update the counts needed for calculating an IDF if we're still in the training phase
        if (training) {
            for (int wordInt : wordIntSetForIdf) {
                documentsPerTerm.putIfAbsent(wordInt, 0);
                documentsPerTerm.put(wordInt, documentsPerTerm.get(wordInt) + 1);
            }
            totalDocs++;
        }
        WordVectorDouble wordVectorDouble = new WordVectorDouble();
        wordVectorDouble.setVector(wordVector);
        return wordVectorDouble;
    }

    /**
     * Return a stemmed, case-insensitive, and de-numeralized version of the string
     *
     * @param t a token
     * @return its flattened form
     */
    private String standardForm(Token t) {
        String form = t.text();
        return Acronyms.standardFormString(form).toLowerCase();
    }

    /**
     * For de-identification purposes: remove a single word from the dictionary
     *
     * @param word a string of the word to be removed
     * @return the integer index of the word removed
     */
    public int removeWord(String word) {
        System.out.println(word);
        return dictionary.remove(Acronyms.standardFormString(word).toLowerCase());
    }

    /**
     * For de-identification: remove all words except those in a given set
     *
     * @param wordsToKeep the set of words (in String format) to keep
     * @return a Set of integers corresponding to the words removed
     */
    public Set<Integer> removeWordsExcept(Set<String> wordsToKeep) {
        System.out.println(dictionary.size());
        Set<Integer> indicesRemoved = new HashSet<>();
        Set<String> wordsInDictionary = new HashSet<>(dictionary.keySet());
        for (String word : wordsInDictionary) {
            word = Acronyms.standardFormString(word).toLowerCase();
            if (!wordsToKeep.contains(word)) {
                indicesRemoved.add(removeWord(word));
            }
        }
        System.out.println(indicesRemoved.size());
        System.out.println(dictionary.size());
        return indicesRemoved;
    }

}
