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

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.labels.ValueLabeler;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.TextSegment;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.processing.Preprocessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Detects the sentences in a document.
 * <p/>
 * <p>First will preprocess the document text using a {@link edu.umn.biomedicus.processing.Preprocessor}.
 * Second, use a {@link SentenceCandidateGenerator} to generate the candidate spans.
 * Finally, use a {@link SentenceSplitter} to split the candidate spans into the
 * final sentence spans.
 * </p>
 *
 * @author Ben Knoll
 * @since 1.1.0
 */
public final class SentenceDetector {
    /**
     * Transforms the text, replacing problematic stings or characters, must not change the indexes of characters.
     */
    private final Preprocessor sentencePreprocessor;

    /**
     * Generates sentence candidates.
     */
    private final SentenceCandidateGenerator sentenceCandidateGenerator;

    /**
     * Splits sentence candidates based on rules.
     */
    private final SentenceSplitter sentenceSplitter;

    /**
     * Initializes a sentence detector with a preprocessor, to process the entire document.
     *
     * @param sentencePreprocessor       preprocessor to use. Replaces problematic strings or characters with
     *                                   equal-length strings.
     * @param sentenceCandidateGenerator candidate generator to use.
     * @param sentenceSplitter           splitter to use.
     */
    public SentenceDetector(Preprocessor sentencePreprocessor,
                            SentenceCandidateGenerator sentenceCandidateGenerator,
                            SentenceSplitter sentenceSplitter) {
        this.sentencePreprocessor = sentencePreprocessor;
        this.sentenceCandidateGenerator = sentenceCandidateGenerator;
        this.sentenceSplitter = sentenceSplitter;
    }

    /**
     * Adds all sentences in the {@link Document}.
     *
     * @param documentText      the entire text of the document
     * @param sentence2Labeler  labeler to use
     * @param textSegmentLabels the labels for the text segments
     */
    public void processDocument(String documentText,
                                Labels<TextSegment> textSegmentLabels,
                                Labeler<Sentence> sentence2Labeler) throws BiomedicusException {
        List<Label<TextSegment>> textSegments = textSegmentLabels.all();
        if (textSegments.isEmpty()) {
            textSegments = new ArrayList<>();
            textSegments.add(new Label<>(new Span(0, documentText.length()), new TextSegment()));
        }

        ValueLabeler valueLabeler = sentence2Labeler.value(new Sentence());

        for (Label<TextSegment> textSegment : textSegments) {

            String textSegmentText = sentencePreprocessor.processText(textSegment.getCovered(documentText));

            sentenceSplitter.setDocumentText(textSegmentText);

            Iterator<Span> iterator = sentenceCandidateGenerator.generateSentenceSpans(textSegmentText)
                    .stream()
                    .flatMap(sentenceSplitter::splitCandidate)
                    .map(textSegment::derelativize)
                    .iterator();

            while (iterator.hasNext()) {
                Span next = iterator.next();
                valueLabeler.label(next);
            }
        }
    }
}
