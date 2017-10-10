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

package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.common.types.text.Token;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A vector space used to calculate word vectors from context.
 * Keeps a dictionary and frequency statistics.
 *
 * @author Greg Finley
 * @since 1.5.0
 */
public class WordVectorSpace {

  private static final Logger LOGGER = LoggerFactory.getLogger(WordVectorSpace.class);

  /**
   * How quickly the sigmoid falls off. More of an idiosyncratic steepness parameter than a slope
   */
  private static final double SLOPE = 0.3;

  /**
   * Default weighting function is sigmoid that decreases with distance (to 0.5 at maxDist)
   * Need to cast to Serializable to save it
   */
  private static final BiFunction<Integer, Double, Double> DIST_WEIGHT = (BiFunction<Integer, Double, Double> & Serializable) (dist, maxDist) ->
      1.0 / (1.0 + Math.exp(SLOPE * (Math.abs(dist) - maxDist)));

  /**
   * Should the IDF be squared, to effectively apply it to both test and train vectors (or raised to
   * another power)?
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
   * The actual uniqueTerms of the window; past threshWeight, we won't even consider words
   */
  private transient double windowSize;

  private Map<String, Integer> dictionary = new HashMap<>();

  /**
   * A count of how many "documents" (training examples) contain each term
   * Maps words (in their integer form, accessible through dictionary) to counts
   */
  private Map<Integer, Integer> documentsPerTerm = new HashMap<>();

  /**
   * Number of "documents" (contexts) seen in training
   */
  private long totalDocs = 0;

  /**
   * A log-transformed version of documentsPerTerm
   */
  private SparseVector idf;

  /**
   * This will be set to false when calculating the idf, and terms will no longer be added to IDF
   * counts
   */
  private boolean countingDocuments = true;

  private boolean buildingDictionary = true;

  /**
   * Default constructor, uses maximum distance of 9.
   */
  public WordVectorSpace() {
    setMaxDist(9);
  }

  public double getMaxDist() {
    return maxDist;
  }

  public void setMaxDist(double maxDist) {
    this.maxDist = maxDist;
    windowSize = Math.log(1.0 / THRESH_WEIGHT - 1) / SLOPE + maxDist;
  }

  public SparseVector getIdf() {
    return idf;
  }

  public void setIdf(SparseVector idf) {
    this.idf = idf;
  }

  public Map<String, Integer> getDictionary() {
    return dictionary;
  }

  public void setDictionary(Map<String, Integer> dictionary) {
    this.dictionary = dictionary;
    buildingDictionary = false;
  }

  public Map<Integer, Integer> getDocumentsPerTerm() {
    return documentsPerTerm;
  }

  public void setDocumentsPerTerm(Map<Integer, Integer> documentsPerTerm) {
    this.documentsPerTerm = documentsPerTerm;
  }

  public long getTotalDocs() {
    return totalDocs;
  }

  public void setTotalDocs(long totalDocs) {
    this.totalDocs = totalDocs;
  }

  public boolean getBuildingDictionary() {
    return buildingDictionary;
  }

  public void setBuildingDictionary(boolean buildingDictionary) {
    this.buildingDictionary = buildingDictionary;
  }

  public boolean getCountingDocuments() {
    return countingDocuments;
  }

  public void setCountingDocuments(boolean countingDocuments) {
    this.countingDocuments = countingDocuments;
  }

  /**
   * This needs to be called after all training vectors have been passed. It sets up the IDF for
   * each term and will save cycles at test time by stopping counting for the IDF
   */
  public void buildIdf() {
    Map<Integer, Double> idf = new HashMap<>();
    // Add 1 to denominator in case there are zero-counts, and to numerator in case there are 'all'-counts
    for (Map.Entry<Integer, Integer> e : documentsPerTerm.entrySet()) {
      double logged = Math.pow(Math.log((1. + totalDocs) / (e.getValue())), IDF_POWER);
      idf.put(e.getKey(), logged);
    }
    this.idf = new SparseVector(idf);
    countingDocuments = false;
  }

  /**
   * Generate a context vector centered on the token which spans context[startCenterToken:stopCenterToken].
   *
   * @param context a list of tokens which includes the term of interest
   * @param startCenterToken the index of the first token of the term of interest
   * @param stopCenterToken the token index following the term of interest
   */
  SparseVector vectorize(List<Token> context, int startCenterToken, int stopCenterToken) {

    Map<Integer, Double> wordVector = new HashMap<>();

    int startIndex = Math.max(startCenterToken - (int) windowSize, 0);
    int stopIndex = Math.min(stopCenterToken + (int) windowSize, context.size());
    for (int i = startIndex; i < stopIndex; i++) {
      if (i == startCenterToken) {
        if (stopCenterToken >= context.size()) {
          break;
        }
        i = stopCenterToken;
      }
      // Generate a list of words, if deemed acceptable words, whose values in the vector will be updated
      String word = Acronyms.standardContextForm(context.get(i));
      if (ALPHANUMERIC.matcher(word).matches()) {
        int wordInt = dictionary.getOrDefault(word, -1);
        if (buildingDictionary && wordInt == -1) {
          wordInt = dictionary.size();
          dictionary.put(word, wordInt);
        }
        if (countingDocuments) {
          int docPerTerm = documentsPerTerm.getOrDefault(wordInt, 0);
          documentsPerTerm.put(wordInt, docPerTerm + 1);
        }
        if (wordInt != -1) {
          int dist = i < startCenterToken ? startCenterToken - i : i - stopCenterToken;
          double thisIncrement = DIST_WEIGHT.apply(dist, maxDist);
          double oldCount = wordVector.getOrDefault(wordInt, 0.);
          wordVector.put(wordInt, oldCount + thisIncrement);
        }
      }
    }
    if (countingDocuments) {
      totalDocs++;
    }
    return new SparseVector(wordVector);
  }

  public SparseVector vectorize(List<Token> context, int centerToken) {
    return vectorize(context, centerToken, centerToken + 1);
  }


  /**
   * For de-identification purposes: remove a single word from the dictionary
   *
   * @param word a string of the word to be removed
   * @return the integer index of the word removed (null if it was not present)
   */
  @Nullable
  public Integer removeWord(String word) {
    LOGGER.info("removing word {}", word);
    Integer wordInt = dictionary.remove(word);
    if (wordInt != null) {
      idf.set(wordInt, 0);
      documentsPerTerm.remove(wordInt);
    }
    return wordInt;
  }

  /**
   * For de-identification: remove all words except those in a given set
   *
   * @param wordsToKeep the set of words (in String format) to keep
   * @return a Set of integers corresponding to the words removed
   */
  public Set<Integer> removeWordsExcept(Set<String> wordsToKeep) {
    LOGGER.info("dictionary size before de-ID: {}", dictionary.size());
    Set<Integer> indicesRemoved = new HashSet<>();
    Set<String> wordsInDictionary = new HashSet<>(dictionary.keySet());
    for (String word : wordsInDictionary) {
      String standardWord = Acronyms.standardContextForm(word);
      if (!wordsToKeep.contains(standardWord)) {
        indicesRemoved.add(removeWord(word));
      }
    }
    LOGGER.info("{} indices removed", indicesRemoved.size());
    LOGGER.info("dictionary size after de-ID: {}", dictionary.size());
    return indicesRemoved;
  }

}
