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

package edu.umn.biomedicus.common.tokensets;

import edu.umn.biomedicus.common.simple.Spans;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.terms.TermVector;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.Token;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents an ordered set of tokens within a document. They are not necessarily continuous or even in the same order
 * as the original document.
 */
public interface OrderedTokenSet {
    /**
     * Returns an in-order list of {@link Token} objects in this sentence.
     *
     * @return list of tokens ordered by position in sentence
     */
    List<Token> getTokens();

    /**
     * Gets a {@link java.util.stream.Stream} of the {@link Token} in this ordered set
     *
     * @return stream of tokens ordered by position in sentence
     */
    Stream<Token> getTokensStream();

    /**
     * Gets the span covered by this ordered token set, since the tokens may not be in the same order as in text, this
     * method searches for the earliest begin of a token and the last end of a token.
     *
     * @return span covering all tokens in this ordered token set
     */
    default Span getSpan() {
        List<Token> tokens = getTokens();
        int leastBegin = tokens.get(0).getBegin();
        int greatestEnd = tokens.get(0).getEnd();
        for (int i = 1; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            int begin = token.getBegin();
            if (leastBegin > begin) {
                leastBegin = begin;
            }
            int end = token.getEnd();
            if (greatestEnd < end) {
                greatestEnd = end;
            }
        }
        return Spans.spanning(leastBegin, greatestEnd);
    }

    /**
     * Returns the text given by concatenating all the tokens in this span with spaces in-between.
     *
     * @return string of concatenated token text
     */
    default String getTokensText() {
        return getTokensStream().map(Token::getText).collect(Collectors.joining(" "));
    }

    /**
     * Returns the text given by concatenating all the normal forms of the tokens in this span with spaces in-between.
     *
     * @return string of concatenated token normal forms
     */
    default String getNormalizedTokensText() {
        return getTokensStream().map(Token::getNormalForm).collect(Collectors.joining(" "));
    }

    default TermVector getWordVector() {
        return getTermVector(Token::getWordTerm);
    }

    default TermVector getNormVector() {
        return getTermVector(Token::getNormTerm);
    }

    default TermVector getTermVector(Function<Token, IndexedTerm> accessor) {
        TermVector.Builder builder = TermVector.builder();
        getTokensStream().map(accessor).forEach(builder::addTerm);
        return builder.build();
    }
}
