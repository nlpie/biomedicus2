/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.sentence;

import edu.umn.biomedicus.application.Biomedicus;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TextSegment;
import edu.umn.biomedicus.exc.BiomedicusException;
import opennlp.tools.sentdetect.SentenceDetectorME;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ONLPSentenceDetector implements DocumentProcessor {
    private final Labeler<Sentence> sentenceLabeler;
    private final SentenceDetectorME sentenceDetector;
    private final LabelIndex<TextSegment> textSegmentLabelIndex;
    private final String text;

    @Inject
    ONLPSentenceDetector(Document document,
                         ONLPSentenceModel ONLPSentenceModel) {
        text = document.getText();
        sentenceLabeler = document.getLabeler(Sentence.class);
        sentenceDetector = ONLPSentenceModel.createSentenceDetector();
        textSegmentLabelIndex = document.getLabelIndex(TextSegment.class);
    }

    @Override
    public void process() throws BiomedicusException {
        List<Span> segments = new ArrayList<>();
        for (Label<TextSegment> textSegmentLabel : textSegmentLabelIndex) {
            segments.add(textSegmentLabel.toSpan());
        }

        if (segments.size() == 0) {
            segments.add(new Span(0, text.length()));
        }

        for (Span segment : segments) {
            String segmentText = segment.getCovered(text).toString();
            for (opennlp.tools.util.Span onlpSpan : sentenceDetector
                    .sentPosDetect(segmentText)) {
                Span spanInSegment = new Span(onlpSpan.getStart(),
                        onlpSpan.getEnd());
                Span sentenceSpan = segment.derelativize(spanInSegment);
                if (Biomedicus.Patterns.NON_WHITESPACE
                        .matcher(sentenceSpan.getCovered(text)).find()) {
                    sentenceLabeler.value(new Sentence()).label(sentenceSpan);
                }
            }
        }
    }
}
