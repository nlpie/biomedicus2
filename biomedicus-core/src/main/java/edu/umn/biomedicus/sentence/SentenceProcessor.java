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

package edu.umn.biomedicus.sentence;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.TextSegment;
import edu.umn.biomedicus.exc.BiomedicusException;

public final class SentenceProcessor implements DocumentProcessor {
    private final SentenceDetectorFactory sentenceDetectorFactory;
    private final Document document;
    private final Labels<TextSegment> textSegmentLabels;
    private final Labeler<Sentence> sentence2Labeler;

    @Inject
    public SentenceProcessor(@Setting("sentenceDetectorFactory.implementation") SentenceDetectorFactory sentenceDetectorFactory,
                             Document document,
                             Labels<TextSegment> textSegmentLabels,
                             Labeler<Sentence> sentence2Labeler) {
        this.sentenceDetectorFactory = sentenceDetectorFactory;
        this.document = document;
        this.textSegmentLabels = textSegmentLabels;
        this.sentence2Labeler = sentence2Labeler;
    }

    @Override
    public void process() throws BiomedicusException {
        SentenceDetector sentenceDetector = sentenceDetectorFactory.create();
        sentenceDetector.processDocument(document.getText(), textSegmentLabels, sentence2Labeler);
    }
}
