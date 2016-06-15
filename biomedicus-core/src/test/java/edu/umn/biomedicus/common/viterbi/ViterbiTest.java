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
import edu.umn.biomedicus.common.grams.Ngram;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.Verifications;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Unit test for {@link Viterbi}.
 */
public class ViterbiTest {
    @Mocked ViterbiProcessorImpl viterbiProcessorImpl;

    @Test
    public void testFirstOrderProcessor(@Mocked EmissionProbabilityModel<String, String> emissionProbabilityModel,
                                        @Mocked TransitionProbabilityModel<String, String> transitionProbabilityModel) throws Exception {
        ViterbiProcessor<String, String> viterbiProcessor = Viterbi.firstOrder(emissionProbabilityModel, transitionProbabilityModel, "state");

        Assert.assertNotNull(viterbiProcessor);

        new Verifications() {{
            Collection<Ancestor<String>> ancestors;
            new ViterbiProcessorImpl<>(emissionProbabilityModel, transitionProbabilityModel,
                    withAny(Ancestor::mostRecent), ancestors = withCapture());
            Assert.assertEquals(ancestors.size(), 1);
            Assert.assertEquals(ancestors.iterator().next().mostRecent(), "state");
        }};
    }

    @Test
    public void testSecondOrderProcessor(@Mocked EmissionProbabilityModel<String, String> emissionProbabilityModel,
                                         @Mocked TransitionProbabilityModel<String, Bigram<String>> transitionProbabilityModel) throws Exception {
        Bigram<String> bigram = Ngram.create("state1", "state2");
        ViterbiProcessor<String, String> viterbiProcessor
                = Viterbi.secondOrder(emissionProbabilityModel, transitionProbabilityModel, bigram, Ngram::create);

        Assert.assertNotNull(viterbiProcessor);

        new Verifications() {{
            Collection<Ancestor<String>> ancestors;
            new ViterbiProcessorImpl<>(emissionProbabilityModel, transitionProbabilityModel, withAny(Ancestor::getBigram),
                    ancestors = withCapture());
            Assert.assertEquals(ancestors.size(), 1);
            Assert.assertEquals(ancestors.iterator().next().getBigram(), bigram);
        }};
    }

    @Test
    public void testCandidateOf() throws Exception {
        CandidateProbability<String> candidateProbability = Viterbi.candidateOf("state", -0.322);

        assertEquals(candidateProbability.getCandidate(), "state");
        assertEquals(candidateProbability.getEmissionLogProbability(), -0.322);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testConstructorThrows() throws Exception {
        Deencapsulation.newInstance(Viterbi.class);

        fail();
    }
}