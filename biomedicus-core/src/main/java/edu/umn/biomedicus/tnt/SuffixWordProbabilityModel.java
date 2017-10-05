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

package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.common.tuples.WordCap;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.syntax.PartsOfSpeech;
import edu.umn.biomedicus.common.utilities.Strings;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * <p> Statistical model used for determining the probability of words given the part of speech and
 * capitalization. Is based off a suffix method described in
 * <a href="http://www.coli.uni-saarland.de/~thorsten/publications/Brants-ANLP00.pdf>
 * TnT -- A Statistical Part-of-Speech Tagger</a> by Thorsten Brants. </p> <p> The original idea
 * comes from a paper: "Morphological tagging based entirely on Bayesian inference" by Christer
 * Samuelsson 1993 </p>
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
public class SuffixWordProbabilityModel implements WordProbabilityModel {

  private static final Set<PartOfSpeech> PARTS_OF_SPEECH = PartsOfSpeech.getRealTags();

  private transient SuffixDataStore suffixDataStore;

  private int maxSuffixLength;

  private WordCapAdapter wordCapAdapter;

  private WordCapFilter filter;

  private int id;

  @Override
  public double logProbabilityOfWord(PartOfSpeech candidate, WordCap wordCap) {
    WordCap adapted = wordCapAdapter.apply(wordCap);
    return Strings.generateSuffixes(adapted.getWord(), maxSuffixLength)
        .map((String suffix) -> suffixDataStore.getProbability(suffix, candidate))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(Double.NEGATIVE_INFINITY);
  }

  @Override
  public Set<PartOfSpeech> getCandidates(WordCap wordCap) {
    return PARTS_OF_SPEECH;
  }

  @Override
  public boolean isKnown(WordCap wordCap) {
    return filter.test(wordCap);
  }

  @Override
  public void createDataStore(DataStoreFactory dataStoreFactory) {
    suffixDataStore = dataStoreFactory.createSuffixDataStore(id);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void openDataStore(DataStoreFactory dataStoreFactory) {
    suffixDataStore = dataStoreFactory.openSuffixDataStore(id);
  }

  @Override
  public void writeData() {
    suffixDataStore.write();
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public void setId(int id) {
    this.id = id;
  }

  @Deprecated
  void trainMsl(WordPosFrequencies wordPosFrequencies, Set<PartOfSpeech> tagSet) {
    throw new UnsupportedOperationException("MSL model is unsupported.");
  }

  void trainPI(WordPosFrequencies wordPosFrequencies, Set<PartOfSpeech> tagSet) {
    Map<Integer, WordPosFrequencies> byWordLength = wordPosFrequencies.byWordLength();

    // computes weights by using unbiased sample variance for the suffix length samples.
    double[] weights = byWordLength.keySet()
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

    TreeMap<Pair<PartOfSpeech, String>, Double> probabilities = new TreeMap<>(
        (o1, o2) -> {
          int compare = o1.getFirst().compareTo(o2.getFirst());
          if (compare != 0) {
            return compare;
          }
          return o1.getSecond().compareTo(o2.getSecond());
        });


    for (String word : wordPosFrequencies.getWords()) {
      for (PartOfSpeech partOfSpeech : tagSet) {
        List<String> collect = Strings.generateSuffixes(word, maxSuffixLength)
            .collect(Collectors.toList());

        double probability = wordPosFrequencies.probabilityOfPartOfSpeech(partOfSpeech);
        for (int i = collect.size() - 2; i >= 0; i--) {
          String suffix = collect.get(i);
          double maxLikelihood = wordPosFrequencies
              .probabilityOfPartOfSpeechConditionalOnWord(word, partOfSpeech);

          double weight = weights[suffix.length() - 1];
          probability = (maxLikelihood + weight * probability) / (1.0 + weight);

          if (probability != 0) {
            probabilities.put(Pair.of(partOfSpeech, suffix), probability);
          }
        }
      }
    }

    probabilities.replaceAll((key, probability) -> {
      String word = key.second();
      PartOfSpeech partOfSpeech = key.getFirst();
      double suffixProbability = wordPosFrequencies.probabilityOfWord(word);
      double posProbability = wordPosFrequencies.probabilityOfPartOfSpeech(partOfSpeech);
      if (posProbability != 0 && probability != null) {
        probability = Math.log10(probability * suffixProbability / posProbability);
      } else {
        probability = null;
      }
      return probability;
    });

    suffixDataStore.addAllProbabilities(probabilities);
  }

  // The following property getter + setters are for serialization

  public int getMaxSuffixLength() {
    return maxSuffixLength;
  }

  public void setMaxSuffixLength(int maxSuffixLength) {
    this.maxSuffixLength = maxSuffixLength;
  }

  public WordCapAdapter getWordCapAdapter() {
    return wordCapAdapter;
  }

  public void setWordCapAdapter(WordCapAdapter wordCapAdapter) {
    this.wordCapAdapter = wordCapAdapter;
  }

  public WordCapFilter getFilter() {
    return filter;
  }

  public void setFilter(WordCapFilter filter) {
    this.filter = filter;
  }
}
