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

package edu.umn.biomedicus.common.texttools;

import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.SpanLike;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Allows for editing a span of text while maintaining the sizing of sub-spans within that phrase.
 * <p>
 * <br/> This class is not thread safe.
 */
public final class PhraseEditor {
    private final StringBuilder phrase;
    private final List<Span> originals;
    private final List<Span> spans;

    /**
     *
     * @param phrase
     * @param spans
     */
    public PhraseEditor(StringBuilder phrase, List<Span> spans) {
        this.phrase = phrase;
        this.originals = spans;
        this.spans = originals.stream().collect(Collectors.toList());
    }

    public static PhraseEditor create(String phrase, List<? extends SpanLike> spans) {
        return new PhraseEditor(new StringBuilder(phrase),
                spans.stream().map(SpanLike::toSpan).collect(Collectors.toList()));
    }

    /**
     *
     * @return
     */
    public String getPhrase() {
        return phrase.toString();
    }

    /**
     *
     * @param span
     * @param replacementText
     */
    public void editSpan(Span span, String replacementText) {
        int start = -1;
        for (int i = 0; i < originals.size(); i++) {
            if (originals.get(i).contains(span)) {
                start = i;
                break;
            }
        }
        if (start == -1) {
            throw new IllegalArgumentException("None of the spans contain the specified span.");
        }
        int originalLength = span.length();
        int replacementLength = replacementText.length();
        int offset = replacementLength - originalLength;

        Span startSpan = spans.get(start);
        Span updated = new Span(startSpan.getBegin(), startSpan.getEnd() + offset);
        spans.set(start, updated);

        for (int i = start + 1; i < spans.size(); i++) {
            spans.set(i, spans.get(i).shift(offset));
        }
        phrase.replace(startSpan.getBegin(), startSpan.getEnd(), replacementText);
    }

    /**
     *
     * @param index
     * @return
     */
    public Span getUpdatedSpan(int index) {
        return spans.get(index);
    }

    public String getUpdatedRange(int startSpan, int endSpan) {
        return phrase.substring(spans.get(startSpan).getEnd(), spans.get(endSpan).getEnd());
    }


}
