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

import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.types.text.TextSegment;
import edu.umn.biomedicus.common.utilities.Patterns;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextLocation;
import edu.umn.biomedicus.framework.store.TextView;
import java.util.Collections;
import java.util.regex.Matcher;
import javax.inject.Inject;
import opennlp.tools.sentdetect.SentenceDetectorME;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ONLPSentenceDetector implements DocumentProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(ONLPSentenceDetector.class);

  private final SentenceDetectorME sentenceDetector;

  @Inject
  ONLPSentenceDetector(ONLPSentenceModel ONLPSentenceModel) {
    sentenceDetector = ONLPSentenceModel.createSentenceDetector();
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);
    String text = systemView.getText();
    Labeler<Sentence> sentenceLabeler = systemView.getLabeler(Sentence.class);
    LabelIndex<TextSegment> textSegmentLabelIndex = systemView.getLabelIndex(TextSegment.class);

    Iterable<Label<TextSegment>> segments;
    if (textSegmentLabelIndex.isEmpty()) {
      segments = Collections.singleton(new Label<>(new Span(0, text.length()), new TextSegment()));
    } else {
      segments = textSegmentLabelIndex;
    }

    for (TextLocation segment : segments) {
      if (segment.length() == 0) {
        continue;
      }
      String segmentText = segment.getCovered(text).toString();
      if (!Patterns.NON_WHITESPACE.matcher(segmentText).find()) {
        continue;
      }

      Matcher initialWhitespace = Patterns.INITIAL_WHITESPACE.matcher(segmentText);
      if (initialWhitespace.find()) {
        segmentText = segmentText.substring(initialWhitespace.end());
        segment = new Span(segment.getBegin() + initialWhitespace.end(), segment.getEnd());
      }

      Matcher finalWhitespace = Patterns.FINAL_WHITESPACE.matcher(segmentText);
      if (finalWhitespace.find()) {
        segmentText = segmentText.substring(0, finalWhitespace.start());
        segment = new Span(segment.getBegin(), segment.getBegin() + finalWhitespace.start());
      }

      if (segment.length() == 0) {
        continue;
      }

      LOGGER.trace("Detecting sentences: {}", segmentText);
      for (opennlp.tools.util.Span onlpSpan : sentenceDetector.sentPosDetect(segmentText)) {
        Span spanInSegment = new Span(onlpSpan.getStart(), onlpSpan.getEnd());
        Span sentenceSpan = segment.derelativize(spanInSegment);
        if (Patterns.NON_WHITESPACE.matcher(sentenceSpan.getCovered(text)).find()) {
          sentenceLabeler.value(new Sentence()).label(sentenceSpan);
        }
      }
    }
  }
}
