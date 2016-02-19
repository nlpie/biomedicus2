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
import edu.umn.biomedicus.model.tuples.WordCap;
import edu.umn.biomedicus.model.tuples.WordPosCap;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Statistical model used for known words in the TnT tagger. It gives the probability that a word will occur given a
 * part of speech tag and capitalization. It stores these probabilities in a Map from
 * {@link WordPosCap} objects to their double probabilities between 0.0 and 1.0.
 *
 * <p>Stores candidates {@link edu.umn.biomedicus.model.semantics.PartOfSpeech} values for words for speed, even though this
 * data is recoverable from the probabilities map.</p>
 *
 * @since 1.0.0
 * @author Ben Knoll
 */
class KnownWordProbabilityModel implements WordProbabilityModel {
    private Map<String, Map<PartOfSpeech, Double>> lexicalProbabilities;

    /**
     * Default constructor. Takes the lexical probabilities, the probability that a word will be seen conditional on a
     * tag and part of speech. Also takes a candidates list of {@link edu.umn.biomedicus.model.semantics.PartOfSpeech} that have
     * nonzero probabilities for a given word.
     * @param lexicalProbabilities a map from the word and pos-capitalization triplets to their probabilities
     */
    public KnownWordProbabilityModel(Map<String, Map<PartOfSpeech, Double>> lexicalProbabilities) {
        this.lexicalProbabilities = lexicalProbabilities;
    }

    @Override
    public double logProbabilityOfWord(PartOfSpeech candidate, WordCap wordCap) {
        Map<PartOfSpeech, Double> partOfSpeechDoubleMap = lexicalProbabilities.get(wordCap.getWord());
        return partOfSpeechDoubleMap == null ? Double.NEGATIVE_INFINITY : partOfSpeechDoubleMap.get(candidate);
    }

    @Override
    public Set<PartOfSpeech> getCandidates(WordCap wordCap) {
        Map<PartOfSpeech, Double> partOfSpeechProbabilities = lexicalProbabilities.get(wordCap.getWord());
        return partOfSpeechProbabilities.entrySet().stream()
                .filter(e -> e.getValue() != Double.NEGATIVE_INFINITY)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isKnown(WordCap wordCap) {
        return lexicalProbabilities.containsKey(wordCap.getWord());
    }

    public Map<String, Map<PartOfSpeech, Double>> getLexicalProbabilities() {
        return lexicalProbabilities;
    }

    public void setLexicalProbabilities(Map<String, Map<PartOfSpeech, Double>> lexicalProbabilities) {
        this.lexicalProbabilities = lexicalProbabilities;
    }
}
