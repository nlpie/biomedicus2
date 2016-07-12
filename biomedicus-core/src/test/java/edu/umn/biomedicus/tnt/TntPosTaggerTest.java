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

package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.common.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.common.tuples.PosCap;
import edu.umn.biomedicus.common.tuples.WordCap;
import edu.umn.biomedicus.common.viterbi.Viterbi;
import edu.umn.biomedicus.common.viterbi.ViterbiProcessor;
import mockit.*;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.fail;

/**
 * Unit test for {@link TntPosTagger}.
 */
public class TntPosTaggerTest {
    @Tested
    TntPosTagger tntPosTagger;

    @Injectable TntModel tntModel;
    @Injectable(value = "1.0") double beamThreshold;

    @Mocked Viterbi viterbi;
    @Mocked ViterbiProcessor<PosCap, WordCap> viterbiProcessor;
    @Mocked WordCap wordCap;
    @Mocked PosCap posCap;

    @Test
    public void testTagSentence(@Mocked Sentence sentence,
                                @Mocked Token token) throws Exception {
        new Expectations() {{
            sentence.getTokens(); result = Arrays.asList(token, token, token, token, token);
            token.getText(); returns("This", "is", "a", "sentence", ".");
            new WordCap("This", true); result = wordCap;
            new WordCap("is", false); result = wordCap;
            new WordCap("a", false); result = wordCap;
            new WordCap("sentence", false); result = wordCap;
            new WordCap(".", false); result = wordCap;
            viterbiProcessor.advance(wordCap); times = 5;
            viterbiProcessor.beamFilter(1); times = 5;
            viterbiProcessor.end(withAny(posCap), withAny(posCap)); result = Arrays.asList(posCap, posCap, posCap, posCap, posCap, posCap, posCap);
            posCap.getPartOfSpeech(); returns(PartOfSpeech.DT, PartOfSpeech.VBZ, PartOfSpeech.DT, PartOfSpeech.NN, PartOfSpeech.SENTENCE_CLOSER_PUNCTUATION);
        }};

        tntPosTagger.tagSentence(sentence);

        new Verifications() {{
            token.setPennPartOfSpeech(PartOfSpeech.DT);
            token.setPennPartOfSpeech(PartOfSpeech.VBZ);
            token.setPennPartOfSpeech(PartOfSpeech.DT);
            token.setPennPartOfSpeech(PartOfSpeech.NN);
            token.setPennPartOfSpeech(PartOfSpeech.SENTENCE_CLOSER_PUNCTUATION);
        }};
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testTagSentenceException(@Mocked Sentence sentence,
                                @Mocked Token token) throws Exception {
        new Expectations() {{
            sentence.getTokens(); result = Arrays.asList(token, token, token, token, token);
            token.getText(); returns("This", "is", "a", "sentence", ".");
            new WordCap("This", true); result = wordCap;
            new WordCap("is", false); result = wordCap;
            new WordCap("a", false); result = wordCap;
            new WordCap("sentence", false); result = wordCap;
            new WordCap(".", false); result = wordCap;
            viterbiProcessor.advance(wordCap); times = 5;
            viterbiProcessor.beamFilter(1); times = 5;
            viterbiProcessor.end(withAny(posCap), withAny(posCap)); result = Arrays.asList(posCap, posCap, posCap, posCap, posCap, posCap);
        }};

        tntPosTagger.tagSentence(sentence);

        fail();
    }
}