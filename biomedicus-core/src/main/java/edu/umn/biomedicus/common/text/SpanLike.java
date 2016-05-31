package edu.umn.biomedicus.common.text;

import java.util.Arrays;
import java.util.stream.IntStream;

import static java.lang.Math.abs;

/**
 * A span within text, with indexes of characters in standard java form.
 *
 * @since 1.0.0
 */
public interface SpanLike {
    /**
     * The begin offset of the text.
     *
     * @return the index of the begin character
     */
    int getBegin();

    /**
     * The end offset of the text
     *
     * @return the index after the last character
     */
    int getEnd();

    default boolean spanEquals(SpanLike other) {
        return getBegin() == other.getBegin() && getEnd() == other.getEnd();
    }

    default Span span() {
        return new Span(getBegin(), getEnd());
    }

    /**
     * Returns true if this span contains the specified span, i.e. that the other span's begin is after or the same as
     * this span's begin, and the other span's end is before or the same as this span's end.
     *
     * @param other span to test if
     * @return true if this span contains the other, false otherwise.
     */
    default boolean contains(SpanLike other) {
        return getBegin() <= other.getBegin() && getEnd() >= other.getEnd();
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
     * Takes the document text and returns the substring covered by this span
     *
     * @param documentText document text
     * @return the text that this span covers of the document text
     */
    default CharSequence getCovered(CharSequence documentText) {
        return documentText.subSequence(getBegin(), getEnd());
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
        return firstInsert >= 0 && abs(lastInsert) - abs(firstInsert) == length() - 1;
    }

    default int compare(SpanLike o) {
        int value = Integer.compare(getBegin(), o.getBegin());
        if (value != 0) {
            return value;
        }
        return Integer.compare(getEnd(), o.getEnd());
    }
}
