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

import javax.annotation.Nullable;
import java.util.*;

/**
 * A bag of terms and their counts.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public final class TermsBag implements Comparable<TermsBag> {
    /**
     * The term identifiers, sorted increasing.
     */
    private final int[] identifiers;

    /**
     * The number of times a term occurs in the bag.
     */
    private final int[] counts;

    private TermsBag(int[] identifiers, int[] counts) {
        this.identifiers = identifiers;
        this.counts = counts;
    }

    List<IndexedTerm> toTerms() {
        List<IndexedTerm> indexedTerms = new ArrayList<>(identifiers.length);
        for (int i = 0; i < identifiers.length; i++) {
            IndexedTerm indexedTerm = new IndexedTerm(identifiers[i]);
            for (int j = 0; j < counts[i]; j++) {
                indexedTerms.add(indexedTerm);
            }
        }
        return indexedTerms;
    }

    private int indexOf(IndexedTerm indexedTerm) {
        return Arrays.binarySearch(identifiers, indexedTerm.indexedTerm());
    }

    public boolean contains(IndexedTerm indexedTerm) {
        return indexOf(indexedTerm) >= 0;
    }

    public int countOf(IndexedTerm indexedTerm) {
        int index = indexOf(indexedTerm);
        if (index < 0) {
            return 0;
        }
        return counts[index];
    }

    public int size() {
        return identifiers.length;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TermsBag that = (TermsBag) o;

        return Arrays.equals(identifiers, that.identifiers) && Arrays.equals(counts, that.counts);
    }

    @Override
    public int hashCode() {
        int value = Arrays.hashCode(identifiers);
        return value * 31 + Arrays.hashCode(counts);
    }

    @Override
    public int compareTo(TermsBag o) {
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

            identifierToCount.compute(indexedTerm.indexedTerm(), (identifier, count) -> {
                if (count == null) {
                    count = 0;
                }
                return count + 1;
            });
            return this;
        }

        public TermsBag build() {
            int size = identifierToCount.size();
            int[] identifiers = new int[size];
            int[] counts = new int[size];
            Iterator<Map.Entry<Integer, Integer>> entryIterator = identifierToCount.entrySet().iterator();
            for (int i = 0; i < size; i++) {
                Map.Entry<Integer, Integer> entry = entryIterator.next();
                identifiers[i] = entry.getKey();
                counts[i] = entry.getValue();
            }

            return new TermsBag(identifiers, counts);
        }
    }
}
