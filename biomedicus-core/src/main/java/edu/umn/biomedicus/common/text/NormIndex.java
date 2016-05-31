package edu.umn.biomedicus.common.text;

import edu.umn.biomedicus.common.terms.IndexedTerm;

import java.util.Objects;

public final class NormIndex {
    private final IndexedTerm term;

    public NormIndex(IndexedTerm term) {
        this.term = Objects.requireNonNull(term);
    }

    public IndexedTerm term() {
        return term;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NormIndex normIndex = (NormIndex) o;

        return term.equals(normIndex.term);

    }

    @Override
    public int hashCode() {
        return term.hashCode();
    }

    @Override
    public String toString() {
        return "NormIndex(" + term + ")";
    }
}
