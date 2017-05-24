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

import edu.umn.biomedicus.common.utilities.Patterns;
import edu.umn.biomedicus.framework.*;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.types.text.TextSegment;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.store.*;
import opennlp.tools.sentdetect.SentenceDetectorME;

import javax.inject.Inject;
import java.util.Collections;

public class ONLPSentenceDetector implements DocumentProcessor {
    private final Labeler<Sentence> sentenceLabeler;
    private final SentenceDetectorME sentenceDetector;
    private final LabelIndex<TextSegment> textSegmentLabelIndex;
    private final String text;

    @Inject
    ONLPSentenceDetector(TextView textView,
                         ONLPSentenceModel ONLPSentenceModel) {
        text = textView.getText();
        sentenceLabeler = textView.getLabeler(Sentence.class);
        sentenceDetector = ONLPSentenceModel.createSentenceDetector();
        textSegmentLabelIndex = textView.getLabelIndex(TextSegment.class);
    }

    @Override
    public void process() throws BiomedicusException {
        Iterable<Label<TextSegment>> segments;
        if (textSegmentLabelIndex.isEmpty()) {
            segments = Collections.singleton(new Label<>(
                    new Span(0, text.length()), new TextSegment()));
        } else {
            segments = textSegmentLabelIndex;
        }

        for (TextLocation segment : segments) {
            String segmentText = segment.getCovered(text).toString();
            for (opennlp.tools.util.Span onlpSpan : sentenceDetector
                    .sentPosDetect(segmentText)) {
                Span spanInSegment = new Span(onlpSpan.getStart(),
                        onlpSpan.getEnd());
                Span sentenceSpan = segment.derelativize(spanInSegment);
                if (Patterns.NON_WHITESPACE
                        .matcher(sentenceSpan.getCovered(text)).find()) {
                    sentenceLabeler.value(new Sentence()).label(sentenceSpan);
                }
            }
        }
    }
}
