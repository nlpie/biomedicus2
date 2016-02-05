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

import java.io.Serializable;
import java.util.Set;

/**
 * Probability model for words used in calculating probabilities in the TnT tagger's viterbi algorithm. This will be
 * used along with trigram probability to calculate the emission probability of a word given the part of speech tag
 * history.
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
abstract class WordProbabilityModel implements Serializable {
    private static final long serialVersionUID = -2830247795588758520L;
    private final WordCapFilter filter;
    private final WordCapAdapter wordCapAdapter;

    protected WordProbabilityModel(WordCapFilter filter, WordCapAdapter wordCapAdapter) {
        this.filter = filter;
        this.wordCapAdapter = wordCapAdapter;
    }

    /**
     * Convenience method for #logProbabilityOfWord(edu.umn.biomedicus.syntax.tnt.models.WordPosCap). Constructs a new
     * {@link edu.umn.biomedicus.model.tuples.WordPosCap} from the arguments.
     *
     * @param candidate     the conditional PartOfSpeech
     * @return a negative double representing the log10 probability of the word
     */
    abstract double logProbabilityOfWord(PartOfSpeech candidate, WordCap wordCap);

    /**
     * Given a word, returns if this model can account for its probability.
     *
     * @return true if this model can provide a probability for the word, false otherwise
     */
    boolean isKnown(WordCap wordCap) {
        WordCap adapted = wordCapAdapter.apply(wordCap);
        return filter.test(adapted);
    }

    /**
     * Returns the potential part of speech candidates for a given word
     *
     * @return a set of {@link edu.umn.biomedicus.model.semantics.PartOfSpeech} enum values
     */
    abstract Set<PartOfSpeech> getCandidates(WordCap wordCap);

    protected WordCap adaptWordCap(WordCap wordCap) {
        return wordCapAdapter.apply(wordCap);
    }
}
