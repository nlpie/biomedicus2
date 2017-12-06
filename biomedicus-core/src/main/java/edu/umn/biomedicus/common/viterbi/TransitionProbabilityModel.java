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

package edu.umn.biomedicus.common.viterbi;

/**
 * Returns the probability that some reduction of previous hidden states will transition to a given
 * hidden state.
 *
 * @param <S> The type of the hidden states.
 * @param <R> A reduction type, some number of previous states reduced to a single object. Examples
 * would be the single state type for first order models and a {@link
 * edu.umn.biomedicus.common.grams.Bigram} of the State types for second order models.
 * @since 1.2.0
 */
@FunctionalInterface
public interface TransitionProbabilityModel<S, R> {

  /**
   * Gets the log base 10 probability that some reduction of the history of ancestor hidden states
   * leads to a candidate next state.
   *
   * @param statesReduction the ancestor states reduced to a single object.
   * @param candidate the candidate state.
   * @return the log base 10 double precision floating point number probability
   */
  double getTransitionLogProbability(R statesReduction, S candidate);
}
