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

package edu.umn.biomedicus.common.viterbi;

import edu.umn.biomedicus.common.grams.Bigram;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Constructor class for viterbi processors.
 *
 * @author Ben Knoll
 * @since 1.2.0
 */
public final class Viterbi {
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private Viterbi() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new first-order {@link ViterbiProcessor}. A first order viterbi processor only relies on the most
     * recent state to compute transition probabilities, and only stores the most probable sequence for each possible
     * most recent state for each step of computation.
     *
     * @param emissionProbabilityModel a {@link EmissionProbabilityModel} to use appropriate types.
     * @param initialState             a unigram of the hidden state type.
     * @param <S>                      the HMM hidden state type
     * @param <Y>                      the HMM emitted value type
     * @return ViterbiProcessor which consumes emitted values and computes the most probable hidden state sequence.
     */
    public static <S, Y> ViterbiProcessor<S, Y> firstOrder(EmissionProbabilityModel<S, Y> emissionProbabilityModel,
                                                           TransitionProbabilityModel<S, S> transitionProbabilityModel,
                                                           S initialState) {
        Set<Ancestor<S>> ancestors = Collections.singleton(Ancestor.createInitial(null, initialState));
        return new ViterbiProcessorImpl<>(emissionProbabilityModel, transitionProbabilityModel, Ancestor::mostRecent,
                ancestors);
    }

    /**
     * Creates a new second-order {@link ViterbiProcessor}. A second order viterbi processor relies on the two most
     * recent hidden states, and stores the most probable sequence for each possible permutation of the two most
     * recent states at each step of computation.
     *
     * @param emissionModel   a {@link EmissionProbabilityModel} to use with the appropriate types.
     * @param transitionModel the probability model to use for transition from one state to another.
     * @param initialStates              a bigram of the hidden state type.
     * @param bigramProvider             a provider for new bigram objects.
     * @param <S>                        the HMM hidden state type
     * @param <Y>                        the HMM emitted value type
     * @return ViterbiProcessor which consumes emitted values and computes most probable hidden state sequence.
     */
    public static <S, Y> ViterbiProcessor<S, Y> secondOrder(EmissionProbabilityModel<S, Y> emissionModel,
                                                            TransitionProbabilityModel<S, Bigram<S>> transitionModel,
                                                            Bigram<S> initialStates,
                                                            BiFunction<S, S, Bigram<S>> bigramProvider) {
        Set<Ancestor<S>> ancestors = Collections.singleton(Ancestor.createInitial(initialStates, bigramProvider));
        return new ViterbiProcessorImpl<>(emissionModel, transitionModel, Ancestor::getBigram,
                ancestors);
    }

    /**
     * Creates a new {@link CandidateProbability} of a candidate-state and an emission probable for that candidate
     * state. This method would be used when a {@link EmissionProbabilityModel} needs to a provide a
     * {@link CandidateProbability} to the viterbi processor.
     *
     * @param candidate              the hidden state candidate
     * @param emissionLogProbability the base-10 log probability that the candidate state will emit an output value
     * @param <S>                    the HMM hidden state type
     * @return newly created Candidate object that stores the parameters.
     */
    public static <S> CandidateProbability<S> candidateOf(S candidate, double emissionLogProbability) {
        return new SimpleCandidateProbability<>(candidate, emissionLogProbability);
    }
}
