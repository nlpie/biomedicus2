package edu.umn.biomedicus.common.terms;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
abstract class AbstractTermIndex implements TermIndex {
    abstract String getTerm(int termIdentifier);

    abstract int internalizeInt(CharSequence term);

    @Override
    public List<String> getStrings(TermVector termVector) {
        return termVector.toTerms().stream().map(this::getString).collect(Collectors.toList());
    }

    @Override
    public TermVector lookup(List<? extends CharSequence> terms) {
        return new TermVector(terms.stream().mapToInt(this::internalizeInt).sorted().toArray());
    }

    @Override
    public <T extends CharSequence> TermVector lookup(T[] terms) {
        return new TermVector(Arrays.stream(terms).mapToInt(this::internalizeInt).sorted().toArray());
    }
}
