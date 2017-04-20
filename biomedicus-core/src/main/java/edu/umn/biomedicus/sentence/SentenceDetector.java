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
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.labels.ValueLabeler;
import edu.umn.biomedicus.application.TextView;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TextSegment;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.processing.Preprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Matcher;

import static edu.umn.biomedicus.application.Biomedicus.Patterns.NEWLINE;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(SentenceDetector.class);

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
     * Adds all sentences in the {@link TextView}.
     *
     * @param document          the document to process
     * @param sentenceLabeler  labeler to use
     * @param textSegmentLabelIndex the labels for the text segments
     */
    public void processDocument(TextView document,
                                LabelIndex<TextSegment> textSegmentLabelIndex,
                                Labeler<Sentence> sentenceLabeler) throws BiomedicusException {
        String documentText = document.getText();

        StringReader stringReader = new StringReader(documentText);
        BufferedReader bufferedReader = new BufferedReader(stringReader);
        OptionalInt max = bufferedReader.lines().mapToInt(String::length).max();
        if (!max.isPresent()) {
            LOGGER.warn("Document has no lines: {}", document.getDocumentId());
            return;
        }

        int maxLength = max.getAsInt();

        List<Label<TextSegment>> textSegments = textSegmentLabelIndex.all();
        if (textSegments.isEmpty()) {
            textSegments = new ArrayList<>();
            textSegments.add(new Label<>(new Span(0, documentText.length()), new TextSegment()));
        }

        ValueLabeler valueLabeler = sentenceLabeler.value(new Sentence());

        int splitLinesShorterThan = maxLength - 12;

        List<Span> sentencePreCandidates = new ArrayList<>();

        for (Label<TextSegment> textSegment : textSegments) {
            CharSequence textSegmentText = textSegment.getCovered(documentText);
            int runningBegin = textSegment.getBegin();
            int lastLineBegin = textSegment.getBegin();
            Matcher matcher = NEWLINE.matcher(textSegmentText);
            while (matcher.find()) {
                int newLine = textSegment.derelativize(matcher.start());
                if (newLine > lastLineBegin && newLine - lastLineBegin < splitLinesShorterThan) {
                    int lastLineEnd = lastLineBegin - 1;
                    if (runningBegin < lastLineEnd) {
                        sentencePreCandidates.add(new Span(runningBegin, lastLineEnd));
                    }
                    sentencePreCandidates.add(new Span(lastLineBegin, newLine));
                    runningBegin = newLine + 1;
                }
                lastLineBegin = newLine + 1;
            }
            sentencePreCandidates.add(new Span(runningBegin, textSegment.getEnd()));
        }


        for (Span sentencePreCandidate : sentencePreCandidates) {
            String textSegmentText = sentencePreprocessor.processText(sentencePreCandidate.getCovered(documentText));

            sentenceSplitter.setDocumentText(textSegmentText);

            Iterator<Span> iterator = sentenceCandidateGenerator.generateSentenceSpans(textSegmentText)
                    .stream()
                    .flatMap(sentenceSplitter::splitCandidate)
                    .map(sentencePreCandidate::derelativize)
                    .iterator();

            while (iterator.hasNext()) {
                Span next = iterator.next();
                valueLabeler.label(next);
            }
        }
    }
}
