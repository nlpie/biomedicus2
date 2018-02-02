/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  private int id;

  private WordCapAdapter wordCapAdapter;

  private WordCapFilter filter;

  private transient KnownWordsDataStore knownWordsDataStore;

  public KnownWordProbabilityModel() {
  }

  @Override
  public double logProbabilityOfWord(PartOfSpeech candidate, WordCap wordCap) {
    WordCap adapted = wordCapAdapter.apply(wordCap);
    Double prob = knownWordsDataStore.getProbability(adapted.getWord(), candidate);
    if (prob == null) {
      return Double.NEGATIVE_INFINITY;
    } else {
      return prob;
    }
  }

  @Override
  public Set<PartOfSpeech> getCandidates(WordCap wordCap) {
    WordCap adapted = wordCapAdapter.apply(wordCap);
    List<PartOfSpeech> candidates = knownWordsDataStore.getCandidates(adapted.getWord());
    return new AbstractSet<PartOfSpeech>() {
      @Override
      public Iterator<PartOfSpeech> iterator() {
        return candidates.iterator();
      }

      @Override
      public int size() {
        return candidates.size();
      }
    };
  }

  @Override
  public boolean isKnown(WordCap wordCap) {
    WordCap adapted = wordCapAdapter.apply(wordCap);
    String word = adapted.getWord();
    return knownWordsDataStore.isKnown(word);
  }

  void train(WordPosFrequencies wordPosFrequencies, Set<PartOfSpeech> tagSet) {
    Map<Pair<PartOfSpeech, String>, Double> lexicalProbabilities = new HashMap<>();
    for (String word : wordPosFrequencies.getWords()) {
      for (PartOfSpeech partOfSpeech : tagSet) {
        int wordFreq = wordPosFrequencies.frequencyOfWordAndPartOfSpeech(word, partOfSpeech);
        int posFreq = wordPosFrequencies.frequencyOfPartOfSpeech(partOfSpeech);
        if (posFreq != 0) {
          double probability = Math.log10((double) wordFreq / (double) posFreq);
          if (probability != Double.NEGATIVE_INFINITY) {
            lexicalProbabilities.put(Pair.of(partOfSpeech, word), probability);
          }
        }
      }
    }

    knownWordsDataStore.addAllProbabilities(lexicalProbabilities);
  }

  @Override
  public void createDataStore(DataStoreFactory dataStoreFactory) {
    knownWordsDataStore = dataStoreFactory.createKnownWordsDataStore(id);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void openDataStore(DataStoreFactory dataStoreFactory) {
    knownWordsDataStore = dataStoreFactory.openKnownWordDataStore(id);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void writeData() {
    knownWordsDataStore.write();
  }

  // property getters below are for serialization

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
