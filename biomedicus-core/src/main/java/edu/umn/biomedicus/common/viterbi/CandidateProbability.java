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
 * A pair of a hidden state candidate and the log10 probability that this candidate will emit a
 * specific output.
 *
 * @param <S> the type for the hidden state
 * @since 1.2.0
 */
public interface CandidateProbability<S> {

  /**
   * A candidate hidden state.
   *
   * @return the candidate hidden state
   */
  S getCandidate();

  /**
   * The log base 10 probability that the candidate will emit a value or output.
   *
   * @return log base 10 probability double-precision floating point number
   */
  double getEmissionLogProbability();
}
