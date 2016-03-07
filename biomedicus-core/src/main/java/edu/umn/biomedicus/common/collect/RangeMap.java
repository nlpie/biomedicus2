package edu.umn.biomedicus.common.collect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * Maps a subset of integers expressed as ranges to a continuous set of integers starting at 0.
 *
 * @author Ben Knoll
 */
public class RangeMap {
    /**
     * The source indexes of the ranges.
     */
    private final List<Integer> indexes;

    /**
     * The length of the ranges.
     */
    private final List<Integer> lengths;

    /**
     * The destination index of the ranges.
     */
    private final List<Integer> mappings;

    public RangeMap(List<Integer> indexes, List<Integer> lengths, List<Integer> mappings) {
        this.indexes = indexes;
        this.lengths = lengths;
        this.mappings = mappings;
    }

    public int map(int index) {
        return genericMap(index, indexes, mappings);
    }

    private int genericMap(int index, List<Integer> from, List<Integer> to) {
        int i = Collections.binarySearch(from, index);

        if (i >= 0) {
            return to.get(i);
        }

        if (i == -1) {
            return -1;
        }

        int prev = -1 * (i + 1) - 1;

        int offset = index - from.get(prev);
        if (offset < lengths.get(prev)) {
            return to.get(prev) + offset;
        } else {
            return -1;
        }
    }

    public int reverseMap(int index) {
        return genericMap(index, mappings, indexes);
    }

    public int size() {
        int last = indexes.size() - 1;
        return mappings.get(last) + lengths.get(last);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int size = 0;

        private TreeSet<Integer> values = new TreeSet<>();

        private Builder() {

        }

        public Builder add(int value) {
            values.add(value);
            return this;
        }

        public RangeMap build() {
            List<Integer> indexes = new ArrayList<>();
            List<Integer> lengths = new ArrayList<>();
            List<Integer> mappings = new ArrayList<>();

            int filled = 0;

            Integer begin = values.pollFirst();
            Integer prev = begin;
            while (begin != null) {
                Integer next;
                while ((next = values.pollFirst()) != null && next - prev <= 1) {
                    prev = next;
                }
                int length = prev - begin + 1;
                indexes.add(begin);
                lengths.add(length);
                mappings.add(filled);
                filled += length;
                begin = next;
                prev = begin;
            }
            return new RangeMap(indexes, lengths, mappings);
        }
    }
}
