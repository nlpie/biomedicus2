/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.common.vectorspace;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A sparse vector of terms.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class TermVector {
    /**
     * The indices of the terms
     */
    private final TermVectorSpace termVectorSpace;

    /**
     * The counts of the terms stored as a map from index to count.
     */
    private final Map<Integer, Integer> termCounts;

    /**
     * Creates a new term vector with the given model and backing map of counts.
     *
     * @param termVectorSpace the term vector model which provides the indices of terms
     * @param termCounts      the count of each term
     */
    public TermVector(TermVectorSpace termVectorSpace, Map<Integer, Integer> termCounts) {
        this.termVectorSpace = termVectorSpace;
        this.termCounts = termCounts;
    }

    /**
     * Creates a new term vector with the specified model and an empty {@link HashMap} to count the terms.
     *
     * @param termVectorSpace the indices of the terms
     */
    public TermVector(TermVectorSpace termVectorSpace) {
        this(termVectorSpace, new HashMap<>());
    }

    /**
     * Increments the count of the term.
     *
     * @param term term encountered
     * @throws java.util.NoSuchElementException if the term is not part of the term vector model.
     */
    public void incrementTerm(String term) {
        int index = termVectorSpace.getIndex(term);
        termCounts.compute(index, (Integer key, @Nullable Integer value) -> {
            if (value == null) {
                value = 0;
            }
            value = value + 1;
            return value;
        });
    }

    /**
     * Retrieves the count of the term with the index.
     *
     * @param index the index of the term.
     * @return the number of instances of that term.
     */
    public int countOfIndex(int index) {
        return termCounts.get(index);
    }

    /**
     * Retrieves the count of the term.
     *
     * @param term the term.
     * @return the count of the term.
     */
    public int countOfTerm(String term) {
        int index = termVectorSpace.getIndex(term);
        return countOfIndex(index);
    }

    /**
     * Returns the number of non-zero terms in the vector.
     *
     * @return number of terms.
     */
    public int numberOfTerms() {
        return termCounts.size();
    }

    /**
     * Returns the non-zero indices.
     *
     * @return non-zero indices.
     */
    public Set<Map.Entry<Integer, Integer>> entries() {
        return termCounts.entrySet();
    }
}
