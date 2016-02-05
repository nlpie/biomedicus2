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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An internal class implementing a stateful Viterbi processor. Responsible for performing Viterbi decoding on arbitrary
 * order HMMs.
 * <p>This class is not thread safe or reusable. It is designed to be used for a single process of viterbi decoding.
 * </p>
 *
 * @param <S> The hidden state type.
 * @param <Y> The output/emitted-value type
 * @param <R> A function which returns the optimal subproblem key, given an ancestor.
 * @author Ben Knoll
 * @since 1.2.0
 */
class ViterbiProcessorImpl<S, Y, R> implements ViterbiProcessor<S, Y> {
    /**
     * The probability model to use for emission, what state is emitted from a particular hidden state.
     */
    private final EmissionProbabilityModel<S, Y> emissionProbabilityModel;

    /**
     * The probability model to use for transition from a reduction of past hidden states to a candidate hidden state.
     */
    private final TransitionProbabilityModel<S, R> transitionProbabilityModel;

    /**
     * The function which reduces an ancestor chain in order to find the transition probability.
     */
    private final Function<Ancestor<S>, R> reducer;

    /**
     * The current collection of ancestors.
     */
    private Collection<Ancestor<S>> ancestors;

    /**
     * Constructor which initializes the class's fields.
     *
     * @param emissionProbabilityModel   The probability model to use for emission, what state is emitted from a
     *                                   particular hidden state.
     * @param transitionProbabilityModel The probability model to use for transition from a reduction of past hidden
     *                                   states to a candidate hidden state.
     * @param reducer                    The function which reduces an ancestor chain in order to find the transition
     *                                   probability.
     * @param ancestors                  The current collection of ancestors.
     */
    ViterbiProcessorImpl(EmissionProbabilityModel<S, Y> emissionProbabilityModel,
                         TransitionProbabilityModel<S, R> transitionProbabilityModel,
                         Function<Ancestor<S>, R> reducer,
                         Collection<Ancestor<S>> ancestors) {
        this.emissionProbabilityModel = emissionProbabilityModel;
        this.transitionProbabilityModel = transitionProbabilityModel;
        this.reducer = reducer;
        this.ancestors = ancestors;
    }

    @Override
    public void advance(Y emittedValue) {
        Stream<CandidateProbability<S>> candidateProbabilityStream = emissionProbabilityModel
                .getCandidates(emittedValue)
                .stream();
        Stream<Function<Ancestor<S>, Ancestor<S>>> transitionFunctionStream = candidateProbabilityStream
                .map(candidate -> (Function<Ancestor<S>, Ancestor<S>>) ancestor -> {
                    S candidateState = candidate.getCandidate();
                    double transitionLogProbability = transitionProbabilityModel
                            .getTransitionLogProbability(reducer.apply(ancestor), candidateState);

                    double logProbability = transitionLogProbability + candidate.getEmissionLogProbability()
                            + ancestor.getLogProbability();
                    return ancestor.createDescendant(logProbability, candidateState);
                });
        Stream<Ancestor<S>> potentialAncestorStream = transitionFunctionStream
                .flatMap(f -> ancestors.stream().map(f::apply));
        Stream<Ancestor<S>> filteredAncestorStream = potentialAncestorStream
                .filter(s -> s.getLogProbability() > Double.NEGATIVE_INFINITY);
        Map<R, Ancestor<S>> ancestorByStateMap = filteredAncestorStream
                .collect(Collectors.toMap(reducer, ancestor -> ancestor, Ancestor::moreProbable));
        Collection<Ancestor<S>> candidates = ancestorByStateMap.values();

        if (candidates.size() > 0) {
            ancestors = candidates;
        } else {
            ancestors = ancestors.stream()
                    .map(Ancestor::skip)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void beamFilter(double beamThreshold) {
        if (ancestors.size() < 10) {
            return;
        }

        double logGreatest = ancestors.stream()
                .mapToDouble(Ancestor::getLogProbability)
                .max()
                .getAsDouble();

        double logBoundary = logGreatest - beamThreshold;

        ancestors = ancestors.stream()
                .filter(s -> s.getLogProbability() >= logBoundary)
                .collect(Collectors.toList());
        if (ancestors.size() == 0) {
            throw new AssertionError("Number of ancestors should never drop to zero");
        }
    }

    @Override
    public List<S> end(S skipValue, S terminalValue) {
        double maxLogProb = Double.NEGATIVE_INFINITY;
        Ancestor<S> maxProbState = null;
        for (Ancestor<S> ancestor : ancestors) {
            double trigramProbability = transitionProbabilityModel.getTransitionLogProbability(reducer.apply(ancestor),
                    terminalValue);
            double logProbability = ancestor.getLogProbability() + Math.log10(trigramProbability);
            if (logProbability > maxLogProb) {
                maxLogProb = logProbability;
                maxProbState = ancestor;
            }
        }

        if (maxProbState == null) {
            for (Ancestor<S> state : ancestors) {
                double logProbability = state.getLogProbability();
                if (logProbability > maxLogProb) {
                    maxLogProb = logProbability;
                    maxProbState = state;
                }
            }
            if (maxProbState == null) {
                throw new AssertionError("0-probability result");
            }
        }
        return maxProbState.getHistory(skipValue);
    }

}
