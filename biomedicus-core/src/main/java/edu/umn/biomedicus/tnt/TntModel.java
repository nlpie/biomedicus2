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

import edu.umn.biomedicus.common.grams.Bigram;
import edu.umn.biomedicus.model.tuples.PosCap;
import edu.umn.biomedicus.model.tuples.WordCap;
import edu.umn.biomedicus.common.viterbi.CandidateProbability;
import edu.umn.biomedicus.common.viterbi.EmissionProbabilityModel;
import edu.umn.biomedicus.common.viterbi.TransitionProbabilityModel;
import edu.umn.biomedicus.common.viterbi.Viterbi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class TntModel implements Serializable, EmissionProbabilityModel<PosCap, WordCap>,
        TransitionProbabilityModel<PosCap, Bigram<PosCap>> {
    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 2892378393931260044L;

    /**
     * Trigram model used for transition probability.
     */
    private final PosCapTrigramModel posCapTrigramModel;

    /**
     * Word probability models used for emission probability.
     */
    private final List<WordProbabilityModel> wordProbabilityModels;

    public TntModel(PosCapTrigramModel posCapTrigramModel, List<WordProbabilityModel> wordProbabilityModels) {
        this.posCapTrigramModel = posCapTrigramModel;
        this.wordProbabilityModels = wordProbabilityModels;
    }

    private WordProbabilityModel getWordProbabilityModel(WordCap emittedValue) {
        WordProbabilityModel wordProbabilityModel = null;
        for (WordProbabilityModel probabilityModel : wordProbabilityModels) {
            if (probabilityModel.isKnown(emittedValue)) {
                wordProbabilityModel = probabilityModel;
                break;
            }
        }

        if (wordProbabilityModel == null) {
            throw new AssertionError("could not find any word probability model");
        }
        return wordProbabilityModel;
    }

    @Override
    public Collection<CandidateProbability<PosCap>> getCandidates(WordCap emittedValue) {
        WordProbabilityModel wordProbabilityModel = getWordProbabilityModel(emittedValue);

        return wordProbabilityModel.getCandidates(emittedValue)
                .stream()
                .map(candidate -> {
                    double emissionLogProbability = wordProbabilityModel.logProbabilityOfWord(candidate, emittedValue);
                    PosCap candidatePosCap = PosCap.create(candidate, emittedValue.isCapitalized());
                    return Viterbi.candidateOf(candidatePosCap, emissionLogProbability);
                })
                .collect(Collectors.toList());
    }

    @Override
    public double getTransitionLogProbability(Bigram<PosCap> statesReduction, PosCap candidate) {
        return Math.log10(posCapTrigramModel.getTrigramProbability(statesReduction.getFirst(),
                statesReduction.getSecond(), candidate));
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }
}
