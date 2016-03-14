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

import edu.umn.biomedicus.common.semantics.Concept;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Term;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.concepts.SemanticTypeNetwork;
import edu.umn.biomedicus.concepts.TUI;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Responsible for writing terms to a file for the new information writer. Tokens in the document should be passed to
 * this class one at a time in the order that they appear in the document.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
final class TermsWriter {
    /**
     * The terms in the document.
     */
    private final List<Term> terms;

    /**
     * The writer to write the terms lines to.
     */
    private final Writer writer;

    private final SemanticTypeNetwork semanticTypeNetwork;

    /**
     * The index of the current first term possibly covered by the token.
     */
    private int firstTermIndex;

    /**
     * Private constructor.
     *  @param terms the list of terms in the document
     * @param writer the writer to write the terms lines to.
     * @param semanticTypeNetwork
     * @param firstTermIndex the index of the current first term possible covered by a token
     */
    private TermsWriter(List<Term> terms, Writer writer, SemanticTypeNetwork semanticTypeNetwork, int firstTermIndex) {
        this.terms = terms;
        this.writer = writer;
        this.semanticTypeNetwork = semanticTypeNetwork;
        this.firstTermIndex = firstTermIndex;
    }

    /**
     * Private constructor. Initializes the terms index to 0.
     *  @param terms the list of the terms in the document.
     * @param writer the writer to write the terms lines to.
     * @param semanticTypeNetwork
     */
    private TermsWriter(List<Term> terms, Writer writer, SemanticTypeNetwork semanticTypeNetwork) {
        this(terms, writer, semanticTypeNetwork, 0);
    }

    /**
     * Static factory method which creates from a {@link Document}.
     *
     * @param document document to pull terms from
     * @param writer the writer to write the terms lines to.
     * @param semanticTypeNetwork
     * @return newly created {@code TermsWriter}.
     */
    static TermsWriter forDocument(Document document, Writer writer, SemanticTypeNetwork semanticTypeNetwork) {
        List<Term> terms = StreamSupport.stream(document.getTerms().spliterator(), false).collect(Collectors.toList());
        return new TermsWriter(terms, writer, semanticTypeNetwork);
    }

    /**
     * Checks to see if the token is covered by a term, outputting a term line if it is, or a plain token line if it
     * isn't.
     *
     * @param token the token to check
     * @param sentenceNumber the current sentence number
     * @param wordNumber the current word number
     * @throws IOException if we fail to write
     */
    void check(Token token, int sentenceNumber, int wordNumber) throws IOException {
        int size = terms.size();
        int tokenBegin = token.getBegin();
        while (firstTermIndex < size && terms.get(firstTermIndex).getEnd() < tokenBegin) {
            firstTermIndex++;
        }

        Term term;
        int index = firstTermIndex;
        int end = token.getEnd();
        while (index < size && (term = terms.get(index)).getBegin() < end) {
            if (term.contains(token)) {
                Concept primaryConcept = term.getPrimaryConcept();
                StringJoiner stringJoiner = new StringJoiner("\t", "", "\n");
                stringJoiner.add(Integer.toString(sentenceNumber));
                stringJoiner.add(Integer.toString(wordNumber));
                stringJoiner.add(Integer.toString(index));
                stringJoiner.add(primaryConcept.getIdentifier());
                stringJoiner.add(primaryConcept.getType());
                stringJoiner.add(semanticTypeNetwork.getSemanticTypeGroup(new TUI(primaryConcept.getType())));
                writer.write(stringJoiner.toString());
            }

            index++;
        }
    }
}
