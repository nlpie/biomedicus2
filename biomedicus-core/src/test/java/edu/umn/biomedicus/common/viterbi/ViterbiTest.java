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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.umn.biomedicus.common.grams.Bigram;
import edu.umn.biomedicus.common.grams.Ngram;
import java.util.Collection;
import mockit.Mocked;
import mockit.Verifications;
import mockit.internal.reflection.ConstructorReflection;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link Viterbi}.
 */
class ViterbiTest {

  @Mocked
  ViterbiProcessorImpl viterbiProcessorImpl;

  @Test
  void testFirstOrderProcessor(
      @Mocked EmissionProbabilityModel<String, String> emissionProbabilityModel,
      @Mocked TransitionProbabilityModel<String, String> transitionProbabilityModel) {
    ViterbiProcessor<String, String> viterbiProcessor = Viterbi
        .firstOrder(emissionProbabilityModel, transitionProbabilityModel, "state");

    assertNotNull(viterbiProcessor);

    new Verifications() {{
      Collection<Ancestor<String>> ancestors;
      new ViterbiProcessorImpl<>(emissionProbabilityModel, transitionProbabilityModel,
          withAny(Ancestor::mostRecent), ancestors = withCapture());
      assertEquals(ancestors.size(), 1);
      assertEquals(ancestors.iterator().next().mostRecent(), "state");
    }};
  }

  @Test
  void testSecondOrderProcessor(
      @Mocked EmissionProbabilityModel<String, String> emissionProbabilityModel,
      @Mocked TransitionProbabilityModel<String, Bigram<String>> transitionProbabilityModel) {
    Bigram<String> bigram = Ngram.create("state1", "state2");
    ViterbiProcessor<String, String> viterbiProcessor
        = Viterbi
        .secondOrder(emissionProbabilityModel, transitionProbabilityModel, bigram, Ngram::create);

    assertNotNull(viterbiProcessor);

    new Verifications() {{
      Collection<Ancestor<String>> ancestors;
      new ViterbiProcessorImpl<>(emissionProbabilityModel, transitionProbabilityModel,
          withAny(Ancestor::getBigram),
          ancestors = withCapture());
      assertEquals(ancestors.size(), 1);
      assertEquals(ancestors.iterator().next().getBigram(), bigram);
    }};
  }

  @Test
  void testCandidateOf() {
    CandidateProbability<String> candidateProbability = Viterbi.candidateOf("state", -0.322);

    assertEquals(candidateProbability.getCandidate(), "state");
    assertEquals(candidateProbability.getEmissionLogProbability(), -0.322);
  }

  @Test
  void testConstructorThrows() {
    assertThrows(UnsupportedOperationException.class, () -> ConstructorReflection.newInstance(Viterbi.class));
  }
}
