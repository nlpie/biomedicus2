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
import edu.umn.biomedicus.common.tuples.WordPosCap;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Statistical model used for known words in the TnT tagger. It gives the probability that a word
 * will occur given a part of speech tag and capitalization. It stores these probabilities in a Map
 * from {@link WordPosCap} objects to their double probabilities between 0.0 and 1.0.
 *
 * <p>Stores candidates {@link PartOfSpeech} values for words for speed, even though this data is
 * recoverable from the probabilities map.</p>
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
public class KnownWordProbabilityModel implements WordProbabilityModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(KnownWordProbabilityModel.class);

  private transient Map<String, Integer> words;

  private transient Map<Pair<Integer, PartOfSpeech>, Double> lexicalProbabilities;

  private transient Map<Integer, PartOfSpeech[]> candidates;

  private int id;

  private WordCapAdapter wordCapAdapter;

  private WordCapFilter filter;

  public KnownWordProbabilityModel() {
  }

  @Override
  public double logProbabilityOfWord(PartOfSpeech candidate, WordCap wordCap) {
    WordCap adapted = wordCapAdapter.apply(wordCap);
    Integer index = words.get(adapted.getWord());
    Double prob = lexicalProbabilities.get(new Pair<>(index, candidate));
    if (prob == null) {
      return Double.NEGATIVE_INFINITY;
    } else {
      return prob;
    }
  }

  @Override
  public Set<PartOfSpeech> getCandidates(WordCap wordCap) {
    WordCap adapted = wordCapAdapter.apply(wordCap);
    Integer index = words.get(adapted.getWord());
    PartOfSpeech[] arr = candidates.get(index);
    return new HashSet<>(Arrays.asList(arr != null ? arr : new PartOfSpeech[0]));
  }

  @Override
  public boolean isKnown(WordCap wordCap) {
    WordCap adapted = wordCapAdapter.apply(wordCap);
    Integer integer = words.get(adapted.getWord());
    return integer != null && candidates.containsKey(integer);
  }

  void train(WordPosFrequencies wordPosFrequencies, Set<PartOfSpeech> tagSet) {
    int count = 0;
    lexicalProbabilities = new HashMap<>();
    candidates = new HashMap<>();
    words = new HashMap<>();
    for (String word : wordPosFrequencies.getWords()) {
      List<PartOfSpeech> wordCandidates = new ArrayList<>();
      Integer index = null;
      for (PartOfSpeech partOfSpeech : tagSet) {
        int wordFreq = wordPosFrequencies.frequencyOfWordAndPartOfSpeech(word, partOfSpeech);
        int posFreq = wordPosFrequencies.frequencyOfPartOfSpeech(partOfSpeech);
        if (posFreq != 0) {
          double probability = Math.log10((double) wordFreq / (double) posFreq);
          if (probability != Double.NEGATIVE_INFINITY) {
            if (index == null) {
              index = count++;
              words.put(word, index);
            }

            lexicalProbabilities.put(Pair.of(index, partOfSpeech), probability);
            wordCandidates.add(partOfSpeech);
          }
        }
      }
      if (index != null) {
        candidates.put(index, wordCandidates.toArray(new PartOfSpeech[wordCandidates.size()]));
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void loadData(DB db, boolean inMemory) {
    LOGGER.debug("Reading data for TnT word model id: {}", id);
    words = db.hashMap("words" + id + "strings", Serializer.STRING_DELTA, Serializer.INTEGER)
        .open();

    lexicalProbabilities = (Map<Pair<Integer, PartOfSpeech>, Double>) db
        .hashMap("words" + id + "lex", Serializer.JAVA, Serializer.DOUBLE)
        .open();

    candidates = (Map<Integer, PartOfSpeech[]>) db
        .hashMap("words" + id + "cand", Serializer.STRING, Serializer.JAVA).open();

    if (inMemory) {
      LOGGER.debug("Reading data to memory for TnT word model id: {}", id);
      words = new HashMap<>(words);
      lexicalProbabilities = new HashMap<>(lexicalProbabilities);
      candidates = new HashMap<>(candidates);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void writeData(DB db) {
    LOGGER.info("Writing data for word model id: {}", id);
    HTreeMap<String, Integer> dbStrings = db
        .hashMap("words" + id + "strings", Serializer.STRING_DELTA, Serializer.INTEGER)
        .create();
    dbStrings.putAll(words);

    Map<Pair<Integer, PartOfSpeech>, Double> dbLex = (Map<Pair<Integer, PartOfSpeech>, Double>) db
        .hashMap("words" + id + "lex", Serializer.JAVA, Serializer.DOUBLE)
        .create();
    dbLex.putAll(lexicalProbabilities);

    Map<Integer, PartOfSpeech[]> dbCandidates = (Map<Integer, PartOfSpeech[]>) db
        .hashMap("words" + id + "cand", Serializer.INTEGER, Serializer.JAVA).create();
    dbCandidates.putAll(candidates);
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public void setId(int id) {
    this.id = id;
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
