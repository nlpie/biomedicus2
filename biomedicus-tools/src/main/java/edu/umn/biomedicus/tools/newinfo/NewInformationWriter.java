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

package edu.umn.biomedicus.tools.newinfo;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Responsible for writing information about Tokens, Concepts, and Sentences to three separate output files for the
 * new information engine.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
final class NewInformationWriter {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Writer for the tokens.
     */
    private final Writer tokensWriter;

    /**
     * Writer for sentences.
     */
    private final Writer sentencesWriter;

    /**
     * Writer for terms.
     */
    private final TermsWriter termsWriter;

    /**
     * Iterator over all the sentences.
     */
    private final Iterator<Sentence> sentenceIterator;

    /**
     * The current index of the sentence.
     */
    private int sentenceNumber = 0;

    /**
     * The current index of the word.
     */
    private int wordNumber = 0;

    /**
     * Private constructor which initializes the values of the fields.
     *
     * @param tokensWriter     writer for tokens file
     * @param sentencesWriter  writer for sentences file
     * @param termsWriter      object for writing terms to file
     * @param sentenceIterator iterator over sentences.
     */
    private NewInformationWriter(Writer tokensWriter,
                                 Writer sentencesWriter,
                                 TermsWriter termsWriter,
                                 Iterator<Sentence> sentenceIterator) {
        this.tokensWriter = tokensWriter;
        this.sentencesWriter = sentencesWriter;
        this.termsWriter = termsWriter;
        this.sentenceIterator = sentenceIterator;
    }

    /**
     * Returns true if there is another sentence to process.
     *
     * @return true if there is another sentences to process, false otherwise
     */
    boolean hasNextSentence() {
        return sentenceIterator.hasNext();
    }

    /**
     * Writes the tokens, terms, and sentences for the current sentence.
     *
     * @throws IOException if one of the writers fails to write.
     */
    void writeNextSentence() throws IOException {
        Sentence sentence = sentenceIterator.next();
        LOGGER.debug("Writing sentence. begin: {}, end: {}", sentence.getBegin(), sentence.getEnd());

        for (Token token : sentence.getTokens()) {
            StringJoiner tokenLine = new StringJoiner("\t", "", "\n");
            tokenLine.add(Integer.toString(sentenceNumber));
            tokenLine.add(Integer.toString(wordNumber));
            tokenLine.add(token.getText().replace("\\", "\\\\").replace("\t", "\\t"));
            tokensWriter.write(tokenLine.toString());

            termsWriter.check(token, sentenceNumber, wordNumber);

            wordNumber++;
        }

        sentencesWriter.write(sentenceNumber + "\t" + sentence.getText().replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n") + "\n");

        sentenceNumber++;
    }

    /**
     * Creates a new builder for new information writers.
     *
     * @return new builder for {@code NewInformationWriter}.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Class for creating {@link NewInformationWriter} instances.
     */
    static class Builder {
        /**
         * Iterator over sentences.
         */
        @Nullable
        private Iterator<Sentence> sentenceIterator;

        /**
         * Writer for tokens.
         */
        @Nullable
        private Writer tokensWriter;

        /**
         * Writer for terms.
         */
        @Nullable
        private TermsWriter termsWriter;

        /**
         * Writer for sentences.
         */
        @Nullable
        private Writer sentencesWriter;

        /**
         * Private constructor for builders.
         */
        private Builder() {
        }

        /**
         * Sets the document.
         *
         * @param document document to take tokens, concepts, and sentences from
         * @return this builder
         */
        Builder withDocument(Document document) {
            sentenceIterator = NewInformationSentenceIterator.create(document);
            return this;
        }

        /**
         * Sets the writer for the tokens file.
         *
         * @param tokensWriter writer for the tokens file
         * @return this builder
         */
        Builder withTokensWriter(Writer tokensWriter) {
            this.tokensWriter = tokensWriter;
            return this;
        }

        /**
         * Sets the object responsible for writing terms.
         *
         * @param termsWriter terms writer
         * @return this builder
         */
        Builder withTermsWriter(TermsWriter termsWriter) {
            this.termsWriter = termsWriter;
            return this;
        }

        /**
         * Sets the object responsible for writing sentences.
         *
         * @param sentencesWriter sentences writer
         * @return this builder
         */
        Builder withSentencesWriter(Writer sentencesWriter) {
            this.sentencesWriter = sentencesWriter;
            return this;
        }

        /**
         * Builds using the set fields.
         *
         * @return new NewInformationWriter
         */
        NewInformationWriter build() {
            return new NewInformationWriter(Objects.requireNonNull(tokensWriter),
                    Objects.requireNonNull(sentencesWriter), Objects.requireNonNull(termsWriter),
                    Objects.requireNonNull(sentenceIterator));
        }
    }
}
