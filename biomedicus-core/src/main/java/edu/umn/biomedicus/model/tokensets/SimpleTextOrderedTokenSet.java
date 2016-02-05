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

package edu.umn.biomedicus.model.tokensets;

import edu.umn.biomedicus.model.text.Token;

import java.util.List;
import java.util.stream.Stream;

/**
 * A simple, immutable {@link TextOrderedTokenSet}
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class SimpleTextOrderedTokenSet extends TextOrderedTokenSet {
    private final List<Token> tokens;

    /**
     * Default constructor, initializes with a {@link java.util.List} of tokens. The tokens should be sorted in
     * the same order that they occur in text and distinct (non-overlapping).
     *
     * @param tokens the tokens
     */
    public SimpleTextOrderedTokenSet(List<Token> tokens) {
        this.tokens = tokens;
    }

    @Override
    public List<Token> getTokens() {
        return tokens;
    }

    @Override
    public Stream<Token> getTokensStream() {
        return tokens.stream();
    }
}
