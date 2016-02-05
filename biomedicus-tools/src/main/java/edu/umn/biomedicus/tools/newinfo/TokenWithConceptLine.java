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

import edu.umn.biomedicus.concepts.UmlsSemanticType;

/**
 * Formats a token and concept for output to the concepts document for the new information writer.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class TokenWithConceptLine {
    /**
     * The number of the sentence relative to the document.
     */
    private final int sentenceNumber;

    /**
     * The number of the word relative to the document.
     */
    private final int wordNumber;

    /**
     * If the word is part of a term, this is the term's number relative to the document.
     */
    private final Integer conceptNumber;

    /**
     * If the word is part of a term, this is the term's primary concept cui.
     */
    private final String cui;

    /**
     * If the word is part of a term, this is the term's primary concept's tui.
     */
    private final String tui;

    /**
     * If the word is part of a term, this is the term's primary concept semantic type group.
     */
    private final String semanticType;

    /**
     * Constructs a concept with a token.
     *
     * @param sentenceNumber The number of the sentence relative to the document.
     * @param wordNumber     The number of the word relative to the document.
     * @param conceptNumber  the term's number relative to the document.
     * @param cui            The term's primary concept cui.
     * @param tui            The term's primary concept's tui.
     */
    TokenWithConceptLine(int sentenceNumber,
                         int wordNumber,
                         int conceptNumber,
                         String cui,
                         String tui) {
        this.sentenceNumber = sentenceNumber;
        this.wordNumber = wordNumber;
        this.conceptNumber = conceptNumber;
        this.cui = cui;
        this.tui = tui;
        this.semanticType = UmlsSemanticType.forTui(tui).getGroup().getIdentifier();
    }

    /**
     * Creates the line for output to the concepts file.
     *
     * @return line to be output to concept file.
     */
    String createLine() {
        return String.join("\t", Integer.toString(sentenceNumber), Integer.toString(wordNumber),
                Integer.toString(conceptNumber), cui, tui, semanticType);
    }
}
