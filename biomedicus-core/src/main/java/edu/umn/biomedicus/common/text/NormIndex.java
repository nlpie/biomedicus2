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
