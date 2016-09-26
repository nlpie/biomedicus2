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

/**
 *
 */
public final class IndexedTerm implements Comparable<IndexedTerm> {
    private final int termIdentifier;

    public IndexedTerm(int termIdentifier) {
        this.termIdentifier = termIdentifier;
    }

    public int termIdentifier() {
        return termIdentifier;
    }

    public boolean isUnknown() {
        return termIdentifier == -1;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexedTerm that = (IndexedTerm) o;

        return termIdentifier == that.termIdentifier;
    }

    @Override
    public int hashCode() {
        return termIdentifier;
    }

    @Override
    public int compareTo(IndexedTerm o) {
        return Integer.compare(termIdentifier, o.termIdentifier);
    }
}
