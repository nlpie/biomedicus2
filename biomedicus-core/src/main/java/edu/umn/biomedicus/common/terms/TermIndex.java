package edu.umn.biomedicus.common.terms;

import edu.umn.biomedicus.common.collect.HashIndexMap;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    public boolean contains(String string) {
        return hashIndexMap.contains(string);
    }

    public void addTerm(CharSequence term) {
        String string = term.toString();
        hashIndexMap.addItem(string);
    }

    String getTerm(int termIdentifier) {
        return hashIndexMap.forIndex(termIdentifier);
    }

    int getIdentifier(CharSequence term) {
        String item = term.toString();
        Integer index = hashIndexMap.indexOf(item);
        return index == null ? -1 : index;
    }

    @Nullable
    public String getTerm(IndexedTerm indexedTerm) {
        if (indexedTerm.isUnknown()) {
            return null;
        }
        return getTerm(indexedTerm.termIdentifier());
    }

    public IndexedTerm getIndexedTerm(CharSequence term) {
        return new IndexedTerm(getIdentifier(term));
    }

    public TermVector getTermVector(Iterable<? extends CharSequence> terms) {
        TermVector.Builder builder = TermVector.builder();
        for (CharSequence term : terms) {
            IndexedTerm indexedTerm = getIndexedTerm(term);
            builder.addTerm(indexedTerm);
        }
        return builder.build();
    }

    public List<String> getTerms(TermVector termVector) {
        return termVector.toTerms().stream().map(this::getTerm).collect(Collectors.toList());
    }

    public Iterator<IndexedTerm> iterator() {
        return stream().iterator();
    }

    public Stream<IndexedTerm> stream() {
        return IntStream.range(0, hashIndexMap.size()).mapToObj(IndexedTerm::new);
    }

    public int size() {
        return hashIndexMap.size();
    }
}
