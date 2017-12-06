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
 * A simple immutable implementation of {@link CandidateProbability}.
 *
 * @param <S> type of the state.
 * @author Ben Knoll
 * @since 1.2.0
 */
class SimpleCandidateProbability<S> implements CandidateProbability<S> {

  /**
   * The candidate state.
   */
  private final S candidate;

  /**
   * The probability that it will be emitted
   */
  private final double emissionLogProbability;

  /**
   * Creates a new candidate probability with the given candidate and probability of emission.
   *
   * @param candidate candidate state.
   * @param emissionLogProbability log base 10 probability of emission.
   */
  SimpleCandidateProbability(S candidate, double emissionLogProbability) {
    this.candidate = candidate;
    this.emissionLogProbability = emissionLogProbability;
  }

  @Override
  public S getCandidate() {
    return candidate;
  }

  @Override
  public double getEmissionLogProbability() {
    return emissionLogProbability;
  }
}
