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

package edu.umn.biomedicus.tokenization;

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.common.types.text.Token;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Iterator over a collection of merged tokens. Tokens that are connected by - / \ ' or _ without spaces are merged.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
public final class TermTokenMerger implements Iterator<Label<TermToken>> {
    private static final Set<Character> MERGE = new HashSet<>(Arrays.asList('-', '/', '\\', '\'', '_'));
    private final List<Label<Token>> running = new ArrayList<>();
    private final Iterator<Label<Token>> iterator;
    @Nullable private Label<TermToken> next;

    public TermTokenMerger(Iterator<Label<Token>> iterator) {
        this.iterator = iterator;
        findNext();
    }

    public TermTokenMerger(Iterable<Label<Token>> iterable) {
        this(iterable.iterator());
    }

    private void findNext() {
        next = null;
        while (next == null && iterator.hasNext()) {
            Label<Token> tokenLabel = iterator.next();
            if (running.size() == 0) {
                running.add(tokenLabel);
                continue;
            }

            Label<Token> lastLabel = running.get(running.size() - 1);
            Token lastToken = lastLabel.value();
            if (lastToken.hasSpaceAfter()) {
                makeTermToken();
            }
            running.add(tokenLabel);
        }

        if (next == null && !running.isEmpty()) {
            makeTermToken();
        }
    }

    private void makeTermToken() {
        if (running.size() == 0) {
            return;
        }
        StringBuilder tokenText = new StringBuilder();
        for (Label<? extends Token> label : running) {
            Token token = label.value();
            tokenText.append(token.text());
        }
        Label<? extends Token> lastTokenLabel = running.get(running.size() - 1);
        boolean hasSpaceAfter = lastTokenLabel.value().hasSpaceAfter();

        TermToken termToken = new TermToken(tokenText.toString(), hasSpaceAfter);
        next = new Label<>(new Span(running.get(0).getBegin(), lastTokenLabel.getEnd()), termToken);
        running.clear();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Label<TermToken> next() {
        if (next == null) {
            throw new NoSuchElementException();
        }
        Label<TermToken> copy = next;
        findNext();
        return copy;
    }
}
