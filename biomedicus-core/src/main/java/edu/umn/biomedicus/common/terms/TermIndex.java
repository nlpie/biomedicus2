/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    private String getTerm(int termIdentifier) {
        return hashIndexMap.forIndex(termIdentifier);
    }

    private int getIdentifier(@Nullable CharSequence term) {
        if (term == null) {
            return -1;
        }
        String item = term.toString();
        Integer index = hashIndexMap.indexOf(item);
        return index == null ? -1 : index;
    }

    @Nullable
    public String getTerm(IndexedTerm indexedTerm) {
        if (indexedTerm.isUnknown()) {
            return null;
        }
        return getTerm(indexedTerm.indexedTerm());
    }

    public IndexedTerm getIndexedTerm(@Nullable CharSequence term) {
        return new IndexedTerm(getIdentifier(term));
    }

    public TermsBag getTermVector(Iterable<? extends CharSequence> terms) {
        TermsBag.Builder builder = TermsBag.builder();
        for (CharSequence term : terms) {
            IndexedTerm indexedTerm = getIndexedTerm(term);
            builder.addTerm(indexedTerm);
        }
        return builder.build();
    }

    public List<String> getTerms(TermsBag termsBag) {
        return termsBag.toTerms().stream().map(this::getTerm).collect(Collectors.toList());
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
