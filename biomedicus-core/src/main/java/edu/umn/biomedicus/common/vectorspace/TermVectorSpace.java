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

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A term space, provides indices for terms.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class TermVectorSpace {
    /**
     * The backing map from term to its index.
     */
    private final Map<String, Integer> termIndices;

    /**
     * A list of the terms.
     */
    private final List<String> terms;

    /**
     * Provides a lock that prevents writing the same term twice.
     */
    private final transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * Initializes a term vector model with the given indices for the terms.
     *
     * @param termIndices backing map from terms to their index.
     * @param terms       the terms themselves.
     */
    public TermVectorSpace(Map<String, Integer> termIndices, List<String> terms) {
        this.termIndices = Objects.requireNonNull(termIndices);
        this.terms = Objects.requireNonNull(terms);
    }

    /**
     * No-arg constructor. Initializes term vector space with empty terms.
     */
    public TermVectorSpace() {
        this(new HashMap<>(), new ArrayList<>());
    }

    /**
     * Adds the term to the term vector space if it hasn't already been added.
     *
     * @param term term to add.
     */
    public void addTerm(String term) {
        if (termIndices.containsKey(term)) {
            return;
        }

        final Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            if (termIndices.containsKey(term)) {
                return;
            }

            int index = terms.size();
            terms.add(term);
            termIndices.put(term, index);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Returns the index of a term.
     *
     * @param term the term itself.
     * @return the index of the term.
     * @throws NoSuchElementException if the term isn't part of the model.
     */
    public int getIndex(String term) {
        final Lock read = readWriteLock.readLock();
        read.lock();
        try {
            Integer index = termIndices.get(term);
            if (index == null) {
                throw new NoSuchElementException();
            }
            return index;
        } finally {
            read.unlock();
        }
    }

    /**
     * Returns a term given its index.
     *
     * @param index the index of the term.
     * @return the term.
     * @throws IndexOutOfBoundsException if the index is greater than the number of terms.
     */
    public String getTerm(int index) {
        final Lock read = readWriteLock.readLock();
        read.lock();
        try {
            return terms.get(index);
        } finally {
            read.unlock();
        }
    }

    /**
     * Returns a copy of the list of all terms.
     *
     * @return copy of the list of terms in this {@code TermVectorSpace}.
     */
    public List<String> getTerms() {
        return new ArrayList<>(terms);
    }
}
