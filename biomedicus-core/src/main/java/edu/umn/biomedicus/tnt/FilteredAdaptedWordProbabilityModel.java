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

import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.tuples.WordCap;

import java.util.Set;

/**
 * Probability model for words used in calculating probabilities in the TnT tagger's viterbi algorithm. This will be
 * used along with trigram probability to calculate the emission probability of a word given the part of speech tag
 * history.
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
public class FilteredAdaptedWordProbabilityModel implements WordProbabilityModel {
    private int priority;
    private WordProbabilityModel wordProbabilityModel;
    private WordCapAdapter wordCapAdapter;
    private WordCapFilter filter;

    public FilteredAdaptedWordProbabilityModel() {
    }

    @Override
    public double logProbabilityOfWord(PartOfSpeech candidate, WordCap wordCap) {
        WordCap adapted = wordCapAdapter.apply(wordCap);
        return wordProbabilityModel.logProbabilityOfWord(candidate, adapted);
    }

    @Override
    public Set<PartOfSpeech> getCandidates(WordCap wordCap) {
        WordCap adapted = wordCapAdapter.apply(wordCap);
        return wordProbabilityModel.getCandidates(adapted);
    }

    @Override
    public boolean isKnown(WordCap wordCap) {
        WordCap adapted = wordCapAdapter.apply(wordCap);
        return filter.test(adapted) && wordProbabilityModel.isKnown(adapted);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public WordCapFilter getFilter() {
        return filter;
    }

    public void setFilter(WordCapFilter filter) {
        this.filter = filter;
    }

    public WordCapAdapter getWordCapAdapter() {
        return wordCapAdapter;
    }

    public void setWordCapAdapter(WordCapAdapter wordCapAdapter) {
        this.wordCapAdapter = wordCapAdapter;
    }

    public WordProbabilityModel getWordProbabilityModel() {
        return wordProbabilityModel;
    }

    public void setWordProbabilityModel(WordProbabilityModel wordProbabilityModel) {
        this.wordProbabilityModel = wordProbabilityModel;
    }

    @Override
    public void reduce() {
        wordProbabilityModel.reduce();
    }
}
