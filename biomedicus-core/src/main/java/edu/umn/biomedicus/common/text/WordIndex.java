package edu.umn.biomedicus.common.text;

import edu.umn.biomedicus.common.terms.IndexedTerm;

import java.util.Objects;

public final class WordIndex {
    private final IndexedTerm term;

    public WordIndex(IndexedTerm term) {
        this.term = term;
    }

    public IndexedTerm term() {
        return term;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WordIndex wordIndex = (WordIndex) o;

        return Objects.equals(term, wordIndex.term);
    }

    @Override
    public int hashCode() {
        return term.hashCode();
    }

    @Override
    public String toString() {
        return "WordIndex(" + term + ")";
    }
}
