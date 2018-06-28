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

import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SimpleCandidateProbability}.
 */
class SimpleCandidateProbabilityTest {

  @Tested
  private
  SimpleCandidateProbability<String> simpleCandidateProbability;

  @Injectable(value = "state")
  String candidate;

  @Injectable(value = "-1.0")
  double emissionLogProbability;

  @Test
  void testGetCandidate() {
    String candidate = simpleCandidateProbability.getCandidate();

    new Verifications() {{
      assertEquals(candidate, "state");
    }};
  }

  @Test
  void testGetEmissionLogProbability() {
    double emissionLogProbability = simpleCandidateProbability.getEmissionLogProbability();

    new Verifications() {{
      assertEquals(emissionLogProbability, -1.0d);
    }};
  }
}