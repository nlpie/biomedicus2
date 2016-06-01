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

package edu.umn.biomedicus.common.text;

import edu.umn.biomedicus.common.simple.SimpleTextSpan;

import javax.annotation.Nullable;

import static java.lang.Math.abs;

/**
 * A simple, immutable implementation of the {@link SpanLike} interface.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public final class Span implements SpanLike, Comparable<Span> {
    /**
     * The begin of the span, inclusive.
     */
    private final int begin;

    /**
     * The end index of the span, exclusive.
     */
    private final int end;

    /**
     * Creates a new span from the begin index to the end index (exclusive).
     *
     * @param begin begin of the span, inclusive.
     * @param end end of the span, exclusive.
     */
    public Span(int begin, int end) {
        if (begin > end || begin < 0) {
            throw new IllegalArgumentException("begin can't be greater than end or less than 0.");
        }
        this.begin = begin;
        this.end = end;
    }

    @Override
    public int getBegin() {
        return begin;
    }

    @Override
    public int getEnd() {
        return end;
    }

    public Span relativize(Span child) {
        return new Span(begin + child.begin, begin + child.end);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Span that = (Span) o;
        return begin == that.begin && end == that.end;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(begin) * 31 + Integer.hashCode(end);
    }

    @Override
    public int compareTo(Span o) {
        return compare(this, o);
    }
    @Override
    public String toString() {
        return "Span(" + begin + ", " + end + ")";
    }

    /**
     * Takes a child span that is relative to the parent span and puts it in the same coordinate space as the parent
     * span.
     *
     * @param parent parent span containing the child
     * @param child  child span whose begin and end indexes are relative to the parents.
     * @return the child span in the same coordinate space as the parent.
     */
    public static Span normalizeChild(SpanLike parent, SpanLike child) {
        return new Span(parent.getBegin() + child.getBegin(), parent.getBegin() + child.getEnd());
    }

    /**
     * Creates a new span between the begin and end.
     *
     * @param begin the begin of the span.
     * @param end   the end of the span.
     * @return newly initialized span.
     */
    public static Span spanning(int begin, int end) {
        return new Span(begin, end);
    }

    /**
     * Creates a text span given the document text and a region in the document.
     *
     * @param documentText the document text.
     * @param begin the begin of the region.
     * @param end the end of a region.
     * @return newly created TextSpan object.
     */
    public static TextSpan textSpan(String documentText, int begin, int end) {
        return new SimpleTextSpan(spanning(begin, end), documentText);
    }

    /**
     * Creates a text span covering the entire contents of a document.
     *
     * @param documentText the document text.
     * @return a text span containing the entire document.
     */
    public static TextSpan textSpan(String documentText) {
        return textSpan(documentText, 0, documentText.length());
    }

    public static int compare(SpanLike first, SpanLike second) {
        int compare = Integer.compare(first.getBegin(), second.getBegin());
        if (compare != 0) return compare;
        return Integer.compare(first.getEnd(), second.getEnd());
    }
}
