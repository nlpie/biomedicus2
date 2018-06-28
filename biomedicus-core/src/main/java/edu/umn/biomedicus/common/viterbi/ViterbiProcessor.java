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

import java.util.List;

/**
 * A Viterbi processor uses the viterbi algorithm for processing hidden Markov models to compute the
 * hidden states for a sequence of emission/output values.
 *
 * @param <S> the hidden state type
 * @param <Y> the emitted value type.
 * @since 1.2.0
 */
public interface ViterbiProcessor<S, Y> {

  /**
   * Advances the processor by one emitted value.
   *
   * @param emittedValue emitted value
   */
  void advance(Y emittedValue);

  /**
   * Filters the states by the beam threshold. The beam threshold is a log 10 probability value,
   * values that are more than the beam threshold less probable than the most probable state are
   * removed from computation.
   *
   * @param beamThreshold a log base 10 probability
   */
  void beamFilter(double beamThreshold);

  /**
   * Ends the computation returning the most probable sequence of hidden states for the emitted
   * values that were passed into the viterbi processor.
   *
   * @param skipValue the value to substitute for states that were skipped because they only had
   * 0-probability answers.
   * @param terminalValue the terminal value / end indicator for the sequence.
   * @return a list of the sequence of hidden states that is most probable
   */
  List<S> end(S skipValue, S terminalValue);
}
