package edu.umn.biomedicus.common.terms;

import edu.umn.biomedicus.common.collect.HashIndexMap;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class TermIndex {
    private final HashIndexMap<String> hashIndexMap;

    public TermIndex() {
        hashIndexMap = new HashIndexMap<>();
    }

    public TermIndex(Collection<? extends CharSequence> collection) {
        hashIndexMap = new HashIndexMap<>();
        for (CharSequence charSequence : collection) {
            addTerm(charSequence);
        }
    }

    public void addTerm(CharSequence term) {
        String string = term.toString();
        hashIndexMap.addItem(string);
    }

    String getTerm(int termIdentifier) {
        return hashIndexMap.forIndex(termIdentifier);
    }

    int lookupIdentifier(CharSequence term) {
        String item = term.toString();
        Integer index = hashIndexMap.indexOf(item);
        return index == null ? -1 : index;
    }

    public List<String> getStrings(TermVector termVector) {
        return termVector.toTerms().stream().map(this::getString).collect(Collectors.toList());
    }

    public Optional<TermVector> lookup(List<? extends CharSequence> terms) {
        return getTermVector(terms.stream());
    }

    private Optional<TermVector> getTermVector(Stream<? extends CharSequence> strim) {
        int[] termIdentifiers = strim.mapToInt(this::lookupIdentifier).sorted().distinct().toArray();
        if (Arrays.stream(termIdentifiers).anyMatch(i -> i == -1)) {
            return Optional.empty();
        }
        return Optional.of(new TermVector(termIdentifiers));
    }

    public <T extends CharSequence> Optional<TermVector> lookup(T[] terms) {
        return getTermVector(Arrays.stream(terms));
    }

    @Nullable
    public String getString(IndexedTerm indexedTerm) {
        int index = indexedTerm.termIdentifier();
        return index == -1 ? null : getTerm(index);
    }

    public Optional<IndexedTerm> lookup(CharSequence term) {
        int termIdentifier = lookupIdentifier(term);
        return termIdentifier == -1 ? Optional.empty() : Optional.of(new IndexedTerm(termIdentifier));
    }
}
