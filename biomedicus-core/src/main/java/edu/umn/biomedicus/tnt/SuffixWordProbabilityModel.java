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
import edu.umn.biomedicus.common.utilities.Strings;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * Statistical model used for determining the probability of words given the part of speech and capitalization. Is based
 * off a suffix method described in <a href="http://www.coli.uni-saarland.de/~thorsten/publications/Brants-ANLP00.pdf>
 * TnT -- A Statistical Part-of-Speech Tagger</a> by Thorsten Brants.
 * </p>
 * <p>
 * The original idea comes from a paper: "Morphological tagging based entirely on Bayesian inference"
 * by Christer Samuelsson 1993
 * </p>
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
class SuffixWordProbabilityModel implements WordProbabilityModel {
    private static final Set<PartOfSpeech> PARTS_OF_SPEECH = PartOfSpeech.REAL_TAGS;

    private Map<String, Map<PartOfSpeech, Double>> probabilities;
    private int maxSuffixLength;

    /**
     * Default SuffixWordProbabilityModel constructor. Takes a Map of {@link edu.umn.biomedicus.model.tuples.WordPosCap} to
     * {@link java.lang.Double} probabilities between NEGATIVE_INFINITY and 0.0, and a max suffix length
     *
     * @param probabilities
     * @param maxSuffixLength
     */
    SuffixWordProbabilityModel(Map<String, Map<PartOfSpeech, Double>> probabilities, int maxSuffixLength) {
        this.probabilities = probabilities;
        this.maxSuffixLength = maxSuffixLength;
    }

    @Override
    public double logProbabilityOfWord(PartOfSpeech candidate, WordCap wordCap) {
        Double probability = Strings.generateSuffixes(wordCap.getWord(), maxSuffixLength)
                .map(probabilities::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new AssertionError("at least 0-length suffix should return a map"))
                .getOrDefault(candidate, Double.NEGATIVE_INFINITY);
        return probability == null ? Double.NEGATIVE_INFINITY : probability;
    }

    @Override
    public Set<PartOfSpeech> getCandidates(WordCap wordCap) {
        return PARTS_OF_SPEECH;
    }

    @Override
    public boolean isKnown(WordCap wordCap) {
        return true;
    }

    public Map<String, Map<PartOfSpeech, Double>> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(Map<String, Map<PartOfSpeech, Double>> probabilities) {
        this.probabilities = probabilities;
    }

    public int getMaxSuffixLength() {
        return maxSuffixLength;
    }

    public void setMaxSuffixLength(int maxSuffixLength) {
        this.maxSuffixLength = maxSuffixLength;
    }
}
