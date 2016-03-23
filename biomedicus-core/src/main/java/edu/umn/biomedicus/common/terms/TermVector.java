package edu.umn.biomedicus.common.terms;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class TermVector implements Comparable<TermVector> {
    private final int[] identifiers;

    private final int[] counts;

    private TermVector(int[] identifiers, int[] counts) {
        this.identifiers = identifiers;
        this.counts = counts;
    }

    public List<IndexedTerm> toTerms() {
        List<IndexedTerm> indexedTerms = new ArrayList<>(identifiers.length);
        for (int i = 0; i < identifiers.length; i++) {
            IndexedTerm indexedTerm = new IndexedTerm(identifiers[i]);
            for (int j = 0; j < counts[i]; j++) {
                indexedTerms.add(indexedTerm);
            }
        }
        return indexedTerms;
    }

    public boolean contains(IndexedTerm indexedTerm) {
        return Arrays.binarySearch(identifiers, indexedTerm.termIdentifier()) >= 0;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TermVector that = (TermVector) o;

        return Arrays.equals(identifiers, that.identifiers) && Arrays.equals(counts, that.counts);
    }

    @Override
    public int hashCode() {
        int value = Arrays.hashCode(identifiers);
        return value * 31 + Arrays.hashCode(counts);
    }

    @Override
    public int compareTo(TermVector o) {
        int compare = Integer.compare(identifiers.length, o.identifiers.length);
        if (compare != 0) {
            return compare;
        }
        for (int i = 0; i < identifiers.length; i++) {
            compare = Integer.compare(identifiers[i], o.identifiers[i]);
            if (compare != 0) {
                return compare;
            }
            compare = Integer.compare(counts[i], o.counts[i]);
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final TreeMap<Integer, Integer> identifierToCount = new TreeMap<>();

        public Builder addTerm(IndexedTerm indexedTerm) {
            if (indexedTerm.isUnknown()) {
                return this;
            }

            identifierToCount.compute(indexedTerm.termIdentifier(), (identifier, count) -> {
                if (count == null) {
                    count = 0;
                }
                return count + 1;
            });
            return this;
        }

        public TermVector build() {
            int size = identifierToCount.size();
            int[] identifiers = new int[size];
            int[] counts = new int[size];
            Iterator<Map.Entry<Integer, Integer>> entryIterator = identifierToCount.entrySet().iterator();
            for (int i = 0; i < size; i++) {
                Map.Entry<Integer, Integer> entry = entryIterator.next();
                identifiers[i] = entry.getKey();
                counts[i] = entry.getValue();
            }

            return new TermVector(identifiers, counts);
        }
    }
}
