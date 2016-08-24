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

package edu.umn.biomedicus.opennlp;

import edu.umn.biomedicus.Biomedicus;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.TextLocation;
import edu.umn.biomedicus.sentence.SentenceCandidateGenerator;
import opennlp.tools.sentdetect.SentenceDetectorME;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses an open {@link opennlp.tools.sentdetect.SentenceDetectorME} to detect the sentence candidates in a single
 * document.
 *
 * @author Ben Knoll
 * @see opennlp.tools.sentdetect.SentenceDetectorME
 * @since 1.1.0
 */
public class OpenNlpCandidateGenerator implements SentenceCandidateGenerator {
    /**
     * The OpenNLP MaxEnt sentence detector.
     */
    private final SentenceDetectorME sentenceDetectorME;

    /**
     * Default constructor. Initializes with the OpenNLP MaxEnt sentence detector.
     *
     * @param sentenceDetectorME the OpenNLP MaxEnt sentence detector to use to find sentence span candidates.
     */
    public OpenNlpCandidateGenerator(SentenceDetectorME sentenceDetectorME) {
        this.sentenceDetectorME = sentenceDetectorME;
    }

    @Override
    public List<TextLocation> generateSentenceSpans(String text) {
        opennlp.tools.util.Span[] spans = sentenceDetectorME.sentPosDetect(text);
        List<TextLocation> sentenceSpans = new ArrayList<>(spans.length);
        for (opennlp.tools.util.Span span : spans) {
            Span sentenceSpan = Span.create(span.getStart(), span.getEnd());
            if (Biomedicus.Patterns.NON_WHITESPACE.matcher(sentenceSpan.getCovered(text)).find()) {
                sentenceSpans.add(sentenceSpan);
            }
        }
        return sentenceSpans;
    }
}
