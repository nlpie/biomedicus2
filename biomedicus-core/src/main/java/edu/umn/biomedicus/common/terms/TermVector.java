package edu.umn.biomedicus.common.terms;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class TermVector implements Comparable<TermVector> {
    private final int[] termIdentifiers;

    TermVector(int[] termIdentifiers) {
        this.termIdentifiers = termIdentifiers;
    }

    List<IndexedTerm> toTerms() {
        return Arrays.stream(termIdentifiers)
                .mapToObj(IndexedTerm::new)
                .collect(Collectors.toList());
    }

    public boolean contains(IndexedTerm indexedTerm) {
        return Arrays.binarySearch(termIdentifiers, indexedTerm.termIdentifier()) >= 0;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TermVector that = (TermVector) o;

        return Arrays.equals(termIdentifiers, that.termIdentifiers);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(termIdentifiers);
    }

    @Override
    public int compareTo(TermVector o) {
        int compare = Integer.compare(termIdentifiers.length, o.termIdentifiers.length);
        if (compare != 0) {
            return compare;
        }
        for (int i = 0; i < termIdentifiers.length; i++) {
            compare = Integer.compare(termIdentifiers[i], o.termIdentifiers[i]);
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }
}
