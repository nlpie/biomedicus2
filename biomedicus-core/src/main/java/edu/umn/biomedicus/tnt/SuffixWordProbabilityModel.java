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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p> Statistical model used for determining the probability of words given the part of speech and
 * capitalization. Is based off a suffix method described in <a href="http://www.coli.uni-saarland.de/~thorsten/publications/Brants-ANLP00.pdf>
 * TnT -- A Statistical Part-of-Speech Tagger</a> by Thorsten Brants. </p> <p> The original idea
 * comes from a paper: "Morphological tagging based entirely on Bayesian inference" by Christer
 * Samuelsson 1993 </p>
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
public class SuffixWordProbabilityModel implements WordProbabilityModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(SuffixWordProbabilityModel.class);

  private static final Set<PartOfSpeech> PARTS_OF_SPEECH = PartsOfSpeech.getRealTags();

  private transient Map<String, Integer> suffixes;

  private transient Map<Pair<Integer, PartOfSpeech>, Double> probabilities;

  private int maxSuffixLength;

  private SuffixWordProbabilityModel wordProbabilityModel;

  private WordCapAdapter wordCapAdapter;

  private WordCapFilter filter;

  private int id;

  @Override
  public double logProbabilityOfWord(PartOfSpeech candidate, WordCap wordCap) {
    WordCap adapted = wordCapAdapter.apply(wordCap);
    Iterator<String> suffixesIt = Strings.generateSuffixes(adapted.getWord(), maxSuffixLength)
        .iterator();
    while (suffixesIt.hasNext()) {
      String suffix = suffixesIt.next();
      Integer suffixInt = suffixes.get(suffix);
      if (suffixInt == null) {
        continue;
      }
      Double prob = probabilities.get(Pair.of(suffixInt, candidate));
      if (prob != null) {
        return prob;
      }
    }
    return Double.NEGATIVE_INFINITY;
  }

  @Override
  public Set<PartOfSpeech> getCandidates(WordCap wordCap) {
    return PARTS_OF_SPEECH;
  }

  @Override
  public boolean isKnown(WordCap wordCap) {
    return filter.test(wordCap);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void loadData(DB db, boolean inMemory) {
    suffixes = db.hashMap("suffix" + id + "map", Serializer.STRING_DELTA, Serializer.INTEGER)
        .open();
    probabilities = (Map<Pair<Integer, PartOfSpeech>, Double>)
        db.hashMap("suffix" + id + "probs", Serializer.JAVA, Serializer.DOUBLE)
            .open();
    if (inMemory) {
      suffixes = new HashMap<>(suffixes);
      probabilities = new HashMap<>(probabilities);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void writeData(DB db) {
    LOGGER.info("Writing suffix model: {}", id);
    HTreeMap<String, Integer> dbMaps = db
        .hashMap("suffix" + id + "map", Serializer.STRING_DELTA, Serializer.INTEGER)
        .create();
    dbMaps.putAll(suffixes);

    Map<Pair<Integer, PartOfSpeech>, Double> map = db
        .hashMap("suffix" + id + "probs", Serializer.JAVA, Serializer.DOUBLE).create();
    map.putAll(probabilities);
    LOGGER.info("Finished writing suffix model.");
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public void setId(int id) {
    this.id = id;
  }

  void trainMsl(WordPosFrequencies wordPosFrequencies, Set<PartOfSpeech> tagSet) {
    int count = 0;
    probabilities = new HashMap<>();
    for (String word : wordPosFrequencies.getWords()) {
      for (PartOfSpeech partOfSpeech : tagSet) {
        List<String> suffixes = Strings.generateSuffixes(word, word.length())
            .collect(Collectors.toList());
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
        if (posFreq != 0) {
          int index = count++;
          this.suffixes.put(word, index);
          double freq = Math.log10((double) max / (double) posFreq);
          probabilities.put(new Pair<>(index, partOfSpeech), freq);
        }
      }
    }
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

    probabilities = new HashMap<>();
    suffixes = new HashMap<>();
    Map<Integer, String> wordForIndex = new HashMap<>();
    int count = 0;
    for (String word : wordPosFrequencies.getWords()) {
      Integer index = null;
      for (PartOfSpeech partOfSpeech : tagSet) {
        List<String> collect = Strings.generateSuffixes(word).collect(Collectors.toList());

        double probability = wordPosFrequencies.probabilityOfPartOfSpeech(partOfSpeech);
        for (int i = collect.size() - 2; i >= 0; i--) {
          String suffix = collect.get(i);
          double maxLikelihood = wordPosFrequencies
              .probabilityOfPartOfSpeechConditionalOnWord(word, partOfSpeech);

          double weight = weights[suffix.length() - 1];
          probability = (maxLikelihood + weight * probability) / (1.0 + weight);
        }

        if (probability != 0) {
          if (index == null) {
            index = count++;
            suffixes.put(word, index);
            wordForIndex.put(index, word);
          }

          probabilities.put(Pair.of(index, partOfSpeech), probability);
        }
      }
    }

    probabilities.replaceAll((key, probability) -> {
      Integer index = key.getFirst();

      PartOfSpeech partOfSpeech = key.getSecond();
      double suffixProbability = wordPosFrequencies.probabilityOfWord(wordForIndex.get(index));
      double posProbability = wordPosFrequencies.probabilityOfPartOfSpeech(partOfSpeech);
      if (posProbability != 0 && probability != null) {
        probability = Math.log10(probability * suffixProbability / posProbability);
      } else {
        probability = null;
      }
      return probability;
    });
  }

  public int getMaxSuffixLength() {
    return maxSuffixLength;
  }

  public void setMaxSuffixLength(int maxSuffixLength) {
    this.maxSuffixLength = maxSuffixLength;
  }

  public SuffixWordProbabilityModel getWordProbabilityModel() {
    return wordProbabilityModel;
  }

  public void setWordProbabilityModel(SuffixWordProbabilityModel wordProbabilityModel) {
    this.wordProbabilityModel = wordProbabilityModel;
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
