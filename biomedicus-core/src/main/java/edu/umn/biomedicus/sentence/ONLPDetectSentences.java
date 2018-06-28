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

package edu.umn.biomedicus.sentence;

import edu.umn.biomedicus.common.utilities.Patterns;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.sentences.TextSegment;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.DocumentTask;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import edu.umn.nlpengine.Span;
import edu.umn.nlpengine.TextRange;
import java.util.Collections;
import java.util.regex.Matcher;
import javax.inject.Inject;
import opennlp.tools.sentdetect.SentenceDetectorME;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ONLPDetectSentences implements DocumentTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(ONLPDetectSentences.class);

  private final SentenceDetectorME sentenceDetector;

  @Inject
  ONLPDetectSentences(ONLPSentenceModel ONLPSentenceModel) {
    sentenceDetector = ONLPSentenceModel.createSentenceDetector();
  }

  @Override
  public void run(@Nonnull Document document) {
    String text = document.getText();
    Labeler<Sentence> sentenceLabeler = document.labeler(Sentence.class);
    LabelIndex<TextSegment> textSegmentLabelIndex = document.labelIndex(TextSegment.class);

    Iterable<TextSegment> segments;
    if (textSegmentLabelIndex.isEmpty()) {
      segments = Collections.singleton(new TextSegment(0, text.length()));
    } else {
      segments = textSegmentLabelIndex;
    }

    for (TextRange segment : segments) {
      if (segment.length() == 0) {
        continue;
      }
      String segmentText = segment.coveredString(text);
      if (!Patterns.NON_WHITESPACE.matcher(segmentText).find()) {
        continue;
      }

      Matcher initialWhitespace = Patterns.INITIAL_WHITESPACE.matcher(segmentText);
      if (initialWhitespace.find()) {
        segmentText = segmentText.substring(initialWhitespace.end());
        segment = new Span(segment.getStartIndex() + initialWhitespace.end(), segment.getEndIndex());
      }

      Matcher finalWhitespace = Patterns.FINAL_WHITESPACE.matcher(segmentText);
      if (finalWhitespace.find()) {
        segmentText = segmentText.substring(0, finalWhitespace.start());
        segment = new Span(segment.getStartIndex(), segment.getStartIndex() + finalWhitespace.start());
      }

      if (segment.length() == 0) {
        continue;
      }

      LOGGER.trace("Detecting sentences: {}", segmentText);
      for (opennlp.tools.util.Span onlpSpan : sentenceDetector.sentPosDetect(segmentText)) {
        Span span = new Span(onlpSpan.getStart(), onlpSpan.getEnd()).offsetRightByStartIndex(segment);
        if (Patterns.NON_WHITESPACE.matcher(span.coveredString(text)).find()) {
          sentenceLabeler.add(new Sentence(span));
        }
      }
    }
  }
}
