package edu.umn.biomedicus.common.collect;

import edu.umn.biomedicus.model.text.Span;

import java.util.Arrays;

/**
 * Maps a subset of integers expressed as ranges to a continuous set of integers starting at 0.
 *
 * @author Ben Knoll
 */
public class RangeMap {
    /**
     * The source indexes of the ranges.
     */
    private final int[] indexes;

    /**
     * The length of the ranges.
     */
    private final int[] lengths;

    /**
     * The destination index of the ranges.
     */
    private final int[] mapping;

    RangeMap(int[] indexes, int[] lengths, int[] mapping) {
        this.indexes = indexes;
        this.lengths = lengths;
        this.mapping = mapping;
    }

    public int map(int index) {
        int i = Arrays.binarySearch(indexes, index);

        if (i >= 0) {
            return mapping[i];
        }

        int prev = -1 * (i + 1);

        int offset = index - indexes[prev];
        if (offset < lengths[prev]) {
            return mapping[prev] + offset;
        } else {
            return -1;
        }
    }

    public int size() {
        int size = indexes.length - 1;
        return mapping[size] + lengths[size];
    }

    public static class Builder {
        private int size = 0;

        private int[] indexes = new int[15];

        private int[] lengths = new int[15];

        public void add(Span span) {
            addRange(span.getBegin(), span.length());
        }

        public void addSpan(int begin, int end) {
            assert begin >= 0;
            assert end >= begin;
            addRange(begin, end - begin);
        }

        public void addRange(int begin, int length) {
            assert begin >= 0;
            assert length >= 0;

            int search = Arrays.binarySearch(indexes, begin);
            if (search < 0) {
                int insertion = search * -1 - 1;

            } else {

            }
        }

        public RangeMap build() {
            return null;
        }
    }
}
