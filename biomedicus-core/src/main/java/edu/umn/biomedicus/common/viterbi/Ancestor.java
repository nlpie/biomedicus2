/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.common.viterbi;

import edu.umn.biomedicus.common.grams.Bigram;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;


/**
 * An internal class representing an ancestor or path of states that have led to the current state in the viterbi
 * algorithm.
 *
 * @param <S> the hidden state type
 * @author Ben Knoll
 * @since 1.2.0
 */
class Ancestor<S> {
    /**
     * The log base 10 probability that this ancestor state will occur.
     */
    private final double logProbability;

    /**
     * The hidden states that occur previous to this one stored in a history chain.
     */
    private final HistoryChain<S> historyChain;

    /**
     * A provider for bigram objects.
     */
    @Nullable
    private BiFunction<S, S, Bigram<S>> bigramProvider;

    /**
     * Private constructor used by static factory method.
     *
     * @param logProbability value to set to the log probability field.
     * @param historyChain   value to set to the history chain field.
     * @param bigramProvider a provider for new bigram objects
     */
    private Ancestor(double logProbability,
                     HistoryChain<S> historyChain,
                     @Nullable BiFunction<S, S, Bigram<S>> bigramProvider) {
        this.logProbability = logProbability;
        this.historyChain = historyChain;
        this.bigramProvider = bigramProvider;
    }

    /**
     * Static factory method which takes an iterable of hidden states.
     *
     * @param states the initial states.
     * @param <S>    the hidden state type
     * @return new ancestor initialized from the hidden states.
     */
    static <S> Ancestor<S> createInitial(Iterable<S> states, @Nullable BiFunction<S, S, Bigram<S>> bigramProvider) {
        HistoryChain<S> history = null;
        boolean atLeastOne = false;
        for (S state : states) {
            atLeastOne = true;
            history = new HistoryChain<>(history, state);
        }
        if (!atLeastOne) {
            throw new IllegalArgumentException("there should be at least one initial state");
        }
        return new Ancestor<>(0.0, history, bigramProvider);
    }

    /**
     * Static factory method which takes varargs of the initial hidden states.
     *
     * @param states the initial states
     * @param <S>    the hidden state type
     * @return new initialized from the hidden states.
     */
    @SafeVarargs
    static <S> Ancestor<S> createInitial(@Nullable BiFunction<S, S, Bigram<S>> bigramProvider, S... states) {
        return createInitial(() -> Arrays.stream(states).iterator(), bigramProvider);
    }

    /**
     * Gets the log base 10 probability that this ancestor set of hidden states occurred.
     *
     * @return double floating point log base 10 probability
     */
    double getLogProbability() {
        return logProbability;
    }

    /**
     * Returns the history stored by this ancestor.
     *
     * @param skipValue value to replace null values with.
     * @return a list of the hidden states leading to this ancestor.
     */
    List<S> getHistory(S skipValue) {
        LinkedList<S> history = new LinkedList<>();
        HistoryChain<S> pointer = this.historyChain;
        while (pointer != null) {
            S payload = pointer.getState();
            if (payload == null) {
                payload = skipValue;
            }
            history.addFirst(payload);
            pointer = pointer.getPrevious();
        }
        return history;
    }

    /**
     * Creates a descendant ancestor with the specified state and log probability.
     *
     * @param logProbability log base 10 probability of the descendant state.
     * @param state          the state of the descendant
     * @return ancestor.
     */
    Ancestor<S> createDescendant(double logProbability, S state) {
        return new Ancestor<>(logProbability, historyChain.append(state), bigramProvider);
    }

    /**
     * Creates a descendant ancestor by skipping a state that has 0 log probability at all states.
     *
     * @return new descendant which skips over a state.
     */
    Ancestor<S> skip() {
        return new Ancestor<>(logProbability, historyChain.skip(), bigramProvider);
    }

    /**
     * Returns the bigram of the two most recent nonnull states. Used for second order HMMs.
     *
     * @return bigram of two most recent states.
     */
    Bigram<S> getBigram() {
        Objects.requireNonNull(bigramProvider);
        return bigramProvider.apply(historyChain.getNonnullPayload(1), historyChain.getNonnullPayload(0));
    }

    /**
     * Returns the most recent nonnull state.
     *
     * @return most recent nonnull state.
     */
    S mostRecent() {
        return historyChain.getNonnullPayload(0);
    }

    /**
     * Compares two ancestors and returns the more probable of the two.
     *
     * @param first  first ancestor to compare.
     * @param second the second ancestor to compare
     * @param <S>    type of state.
     * @return the more probable of the two ancestors.
     */
    static <S> Ancestor<S> moreProbable(Ancestor<S> first, Ancestor<S> second) {
        return first.logProbability > second.logProbability ? first : second;
    }
}
