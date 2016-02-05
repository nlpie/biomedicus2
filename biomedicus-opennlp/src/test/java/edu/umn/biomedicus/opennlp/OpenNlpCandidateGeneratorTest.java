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

package edu.umn.biomedicus.opennlp;

import edu.umn.biomedicus.model.simple.Spans;
import edu.umn.biomedicus.model.text.TextSpan;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.util.Span;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class OpenNlpCandidateGeneratorTest {
    @Tested
    OpenNlpCandidateGenerator openNlpCandidateGenerator;
    @Injectable SentenceDetectorME sentenceDetectorME;
    @Injectable TextSpan textSpan;

    @Test
    public void testGenerateSentenceSpans() throws Exception {
        final String text = "lets say that this is a test sentence.  ok";
        new NonStrictExpectations() {{
            sentenceDetectorME.sentPosDetect(text); result = new Span[]{new Span(0, 38), new Span(38, 40), new Span(40, 42)};
            textSpan.containsNonWhitespace(); result = true;
            textSpan.containsNonWhitespace(); result = true;
            textSpan.containsNonWhitespace(); result = false;
        }};
        List<edu.umn.biomedicus.model.text.Span> spans = openNlpCandidateGenerator.generateSentenceSpans(text);
        Assert.assertEquals(2, spans.size());
        Assert.assertEquals(spans.get(0), Spans.spanning(0, 38));
        Assert.assertEquals(spans.get(1), Spans.spanning(40, 42));
    }
}