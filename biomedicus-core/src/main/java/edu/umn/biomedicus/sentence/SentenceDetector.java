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

package edu.umn.biomedicus.sentence;

import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.TextSpan;
import edu.umn.biomedicus.processing.Preprocessor;

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
public class SentenceDetector {
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
     * Adds all sentences in the {@link Document} to the document using
     * {@link Document#createSentence}.
     *
     * @param document document
     */
    public void processDocument(Document document) {
        document.textSegments().forEach(textSpan -> processTextSpan(document, textSpan));
    }

    /**
     * Processes a text span in a document, tagging all of the sentences in that span of text.
     *
     * @param document document
     * @param textSpan the text span to tag sentences in.
     */
    public void processTextSpan(Document document, TextSpan textSpan) {
        String text = sentencePreprocessor.processText(textSpan.getText());

        sentenceSplitter.setDocumentText(text);

        sentenceCandidateGenerator.generateSentenceSpans(text)
                .stream()
                .flatMap(sentenceSplitter::splitCandidate)
                .map(candidate -> Span.normalizeChild(textSpan, candidate))
                .forEach(document::createSentence);
    }
}
