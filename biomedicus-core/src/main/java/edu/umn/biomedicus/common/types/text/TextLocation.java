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

import java.util.Arrays;
import java.util.stream.IntStream;

import static java.lang.Math.*;

/**
 * A template for a subsection of text, or a span. Indexes are in standard java string format, end exclusive, i.e. the
 * substring represented by this TextLocation will be the same as calling {@link String#substring(int, int)} with the
 * values of {@link #getBegin()} and {@link #getEnd()}.
 *
 * @since 1.5.0
 */
public interface TextLocation {
    /**
     * The begin offset of the text.
     *
     * @return the index of the begin character
     */
    int getBegin();

    /**
     * Alias for getBegin();
     *
     * @return the index of the first character
     */
    default int begin() {
        return getBegin();
    }

    /**
     * The end offset of the text
     *
     * @return the index after the last character
     */
    int getEnd();

    /**
     * The end offset of the text
     *
     * @return the index after the last character in the text.
     */
    default int end() {
        return getEnd();
    }

    /**
     * Converts this text location
     *
     * @return
     */
    default Span toSpan() {
        return new Span(getBegin(), getEnd());
    }

    /**
     * Converts an index with the same basis (0-index) as this span to use this span's begin as a basis.
     *
     * @param index the index to convert
     * @return the relativized index
     */
    default int relativize(int index) {
        return index - getBegin();
    }

    /**
     * Converts a span with the same basis (0-index) as this span to use this span's begin as a basis.
     *
     * @param other the index to convert
     * @return the relativized span
     */
    default Span relativize(TextLocation other) {
        return new Span(relativize(other.getBegin()),
                relativize(other.getEnd()));
    }

    /**
     * Converts an index with this span's begin as a basis to an index with the same basis (0-index) as this span.
     *
     * @param index the index to convert
     * @return the derelativized index
     */
    default int derelativize(int index) {
        return getBegin() + index;
    }

    /**
     * Converts a span with this span's begin as a basis (0-index) to have the same basis as this span.
     *
     * @param other the span to convert
     * @return the derelativized span
     */
    default Span derelativize(TextLocation other) {
        return new Span(derelativize(other.getBegin()),
                derelativize(other.getEnd()));
    }

    /**
     * Moves the span by the specified number of characters.
     *
     * @param characters the number of characters to shift.
     * @return new span.
     */
    default Span shift(int characters) {
        return new Span(getBegin() + characters, getEnd() + characters);
    }

    default boolean spanEquals(TextLocation other) {
        return getBegin() == other.getBegin() && getEnd() == other.getEnd();
    }

    /**
     * Returns true if this span contains the specified span, i.e. that the other span's begin is after or the same as
     * this span's begin, and the other span's end is before or the same as this span's end.
     *
     * @param other span to test if
     * @return true if this span contains the other, false otherwise.
     */
    default boolean contains(TextLocation other) {
        return getBegin() <= other.getBegin() && getEnd() >= other.getEnd();
    }

    default boolean isContainedBy(TextLocation other) {
        return other.contains(this);
    }

    /**
     * The length of the text covered by this span.
     *
     * @return the length covered by this span
     */
    default int length() {
        return getEnd() - getBegin();
    }

    /**
     * Takes a character sequence and returns the sub sequence that this text covers.
     *
     * @param charSequence the overall sequence
     * @return the subsequence that this span covers.
     */
    default CharSequence getCovered(CharSequence charSequence) {
        return charSequence.subSequence(getBegin(), getEnd());
    }

    /**
     * Returns a stream of all indices in the span.
     *
     * @return stream containing all indices in the span.
     */
    default IntStream indices() {
        return IntStream.range(getBegin(), getEnd());
    }

    /**
     * Determines if one text location overlaps another.
     *
     * @param other
     * @return
     */
    default boolean overlaps(TextLocation other) {
        return other.getBegin() < getEnd() || other.getEnd() > getBegin();
    }

    default Span mergeOverlapping(TextLocation other) {
        if (!overlaps(other)) {
            throw new IllegalArgumentException("Text spans do not overlap");
        }
        return new Span(min(getBegin(), other.getBegin()),
                max(getEnd(), other.getEnd()));
    }

    /**
     * Determines whether all of the indexes in this span are in the specified sorted array of indexes. Used for
     * exclusion / ignore lists.
     *
     * @param sortedIndexes a list of sorted indexes.
     * @return true if all of the indexes in this span are in the specified array.
     */
    default boolean allIndicesAreIn(int[] sortedIndexes) {
        int firstInsert = Arrays.binarySearch(sortedIndexes, getBegin());
        int lastInsert = Arrays.binarySearch(sortedIndexes, getEnd() - 1);
        // firstInsert will be negative if the first character is not in the array.
        // if every character is in the array than the distance between firstInsert and lastInsert will be equal to the
        // annotation length - 1, otherwise less.
        return firstInsert >= 0
                && abs(lastInsert) - abs(firstInsert) == length() - 1;
    }

    /**
     * Returns the span that is everything after this span up to the end of the
     * provided span.
     *
     * @param other the span to include up to the end
     * @return new span from the end of this span up to the end of the other
     * span
     */
    default Span upToInclusive(Span other) {
        return new Span(getEnd(), other.getEnd());
    }
}
