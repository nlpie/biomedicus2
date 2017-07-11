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

import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
abstract class WordModelTrainer implements
    Function<WordPosFrequencies, Map<String, Map<PartOfSpeech, Double>>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WordModelTrainer.class);

  protected final Set<PartOfSpeech> tagSet;

  WordModelTrainer(Set<PartOfSpeech> tagSet) {
    this.tagSet = tagSet;
  }

  @Override
  public Map<String, Map<PartOfSpeech, Double>> apply(WordPosFrequencies wordPosFrequencies) {
    LOGGER.debug("Training a word model");
    Map<String, Map<PartOfSpeech, Double>> probabilities = new HashMap<>();
    for (String word : wordPosFrequencies.getWords()) {
      Map<PartOfSpeech, Double> probabilitiesForWord = new EnumMap<>(PartOfSpeech.class);
      for (PartOfSpeech partOfSpeech : tagSet) {
        double probability = getProbability(wordPosFrequencies, word, partOfSpeech);
        probabilitiesForWord.put(partOfSpeech, probability);
      }
      probabilities.put(word, probabilitiesForWord);
    }

    LOGGER.debug("Finished training a word model");
    return probabilities;
  }

  protected abstract double getProbability(WordPosFrequencies wordPosFrequencies,
      String word,
      PartOfSpeech partOfSpeech);
}
