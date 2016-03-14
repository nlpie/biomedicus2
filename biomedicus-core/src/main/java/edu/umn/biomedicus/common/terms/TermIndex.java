package edu.umn.biomedicus.common.terms;

import java.util.List;

/**
 *
 */
public interface TermIndex {
    String getString(IndexedTerm indexedTerm);

    IndexedTerm lookup(CharSequence term);

    List<String> getStrings(TermVector termVector);

    TermVector lookup(List<? extends CharSequence> terms);

    <T extends CharSequence> TermVector lookup(T[] terms);
}
