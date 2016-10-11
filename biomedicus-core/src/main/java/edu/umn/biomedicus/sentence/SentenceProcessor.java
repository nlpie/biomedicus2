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
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.types.text.TextSegment;
import edu.umn.biomedicus.exc.BiomedicusException;

public final class SentenceProcessor implements DocumentProcessor {
    private final SentenceDetectorFactory sentenceDetectorFactory;
    private final Document document;
    private final LabelIndex<TextSegment> textSegmentLabelIndex;
    private final Labeler<Sentence> sentenceLabeler;

    @Inject
    public SentenceProcessor(@Setting("sentenceDetectorFactory.implementation") SentenceDetectorFactory sentenceDetectorFactory,
                             Document document) {
        this.sentenceDetectorFactory = sentenceDetectorFactory;
        this.document = document;
        textSegmentLabelIndex = document.getLabelIndex(TextSegment.class);
        sentenceLabeler = document.getLabeler(Sentence.class);
    }

    @Override
    public void process() throws BiomedicusException {
        SentenceDetector sentenceDetector = sentenceDetectorFactory.create();
        sentenceDetector.processDocument(document, textSegmentLabelIndex, sentenceLabeler);
    }
}
