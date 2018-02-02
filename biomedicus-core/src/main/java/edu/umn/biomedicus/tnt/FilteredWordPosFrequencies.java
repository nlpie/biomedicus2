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

import edu.umn.biomedicus.common.tuples.WordCap;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;

/**
 * A filter that goes on top of the word-part-of-speech frequencies used
 * to train the TnT model.
 *
 * @since 1.1.0
 */
class FilteredWordPosFrequencies {

  private final WordPosFrequencies wordPosFrequencies;

  private final WordCapFilter filter;

  private final WordCapAdapter wordCapAdapter;

  FilteredWordPosFrequencies(WordPosFrequencies wordPosFrequencies, WordCapFilter filter,
      WordCapAdapter wordCapAdapter) {
    this.wordPosFrequencies = wordPosFrequencies;
    this.filter = filter;
    this.wordCapAdapter = wordCapAdapter;
  }

  FilteredWordPosFrequencies(WordCapFilter filter, WordCapAdapter wordCapAdapter) {
    this(new WordPosFrequencies(), filter, wordCapAdapter);
  }

  void addWord(WordCap wordCap, PartOfSpeech partOfSpeech) {
    WordCap adapted = wordCapAdapter.apply(wordCap);
    if (filter.test(adapted)) {
      wordPosFrequencies.addCount(adapted.getWord(), partOfSpeech, 1);
    }
  }

  WordCapFilter getFilter() {
    return filter;
  }

  WordCapAdapter getWordCapAdapter() {
    return wordCapAdapter;
  }

  WordPosFrequencies getWordPosFrequencies() {
    return wordPosFrequencies;
  }
}
