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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An ordered list of term indices.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
public class TermsVector implements Iterable<IndexedTerm> {
    private final int[] identifiers;

    public TermsVector(int[] identifiers) {
        this.identifiers = identifiers;
    }

    public int length() {
        return identifiers.length;
    }

    public IndexedTerm get(int index) {
        return new IndexedTerm(identifiers[index]);
    }

    public TermsBag toBag() {
        TermsBag.Builder builder = TermsBag.builder();
        for (int identifier : identifiers) {
            builder.addIdentifier(identifier);
        }

        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TermsVector that = (TermsVector) o;

        return Arrays.equals(identifiers, that.identifiers);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(identifiers);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Iterator<IndexedTerm> iterator() {
        return new Iterator<IndexedTerm>() {
            private int index = 0;
            @Override
            public boolean hasNext() {
                return index < identifiers.length;
            }

            @Override
            public IndexedTerm next() {
                if (index >= identifiers.length) {
                    throw new NoSuchElementException();
                }
                return get(index++);
            }
        };
    }

    public static class Builder {
        private final ArrayList<Integer> identifiers = new ArrayList<>();

        public void add(IndexedTerm indexedTerm) {
            identifiers.add(indexedTerm.termIdentifier());
        }

        public TermsVector build() {
            return new TermsVector(identifiers.stream().mapToInt(Integer::intValue).toArray());
        }
    }
}
