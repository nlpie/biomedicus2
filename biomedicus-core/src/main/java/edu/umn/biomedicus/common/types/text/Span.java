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

package edu.umn.biomedicus.common.types.text;

import javax.annotation.Nullable;

/**
 * A simple, immutable implementation of the {@link TextLocation} interface.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public final class Span implements TextLocation, Comparable<Span> {
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

    public Span(TextLocation textLocation) {
        this.begin = textLocation.getBegin();
        this.end = textLocation.getEnd();
    }

    @Override
    public int getBegin() {
        return begin;
    }

    @Override
    public int getEnd() {
        return end;
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
        int compare = Integer.compare(begin, o.begin);
        if (compare != 0) return compare;
        return Integer.compare(o.end, end);
    }

    @Override
    public String toString() {
        return "Span(" + begin + ", " + end + ")";
    }

    /**
     * Creates a new span between the begin and end.
     *
     * @param begin the begin of the span.
     * @param end   the end of the span.
     * @return newly initialized span.
     */
    public static Span create(int begin, int end) {
        return new Span(begin, end);
    }
}
