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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages the different term vector spaces. Term vector spaces are identified
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class TermVectorSpaceManager {
    /**
     * Singleton instance.
     */
    private static final TermVectorSpaceManager INSTANCE = new TermVectorSpaceManager();

    /**
     * The stored term vector spaces.
     */
    private final ConcurrentMap<String, TermVectorSpace> termVectorSpaces;

    /**
     * Constructor for term vector space manager.
     *
     * @param termVectorSpaces the map of term vector spaces.
     */
    private TermVectorSpaceManager(ConcurrentMap<String, TermVectorSpace> termVectorSpaces) {
        this.termVectorSpaces = termVectorSpaces;
    }

    /**
     * No-Arg constructor. Initializes the backing map to an empty concurrent map.
     */
    private TermVectorSpaceManager() {
        this(new ConcurrentHashMap<>());
    }

    /**
     * Gets or creates the term vector space specified by the identifier.
     *
     * @param identifier identifier
     * @return term vector space
     */
    TermVectorSpace get(String identifier) {
        TermVectorSpace termVectorSpace = termVectorSpaces.get(identifier);
        if (termVectorSpace != null) {
            return termVectorSpace;
        }

        termVectorSpace = new TermVectorSpace();
        final TermVectorSpace prev = termVectorSpaces.putIfAbsent(identifier, termVectorSpace);
        return prev == null ? termVectorSpace : prev;
    }

    /**
     * Gets or creates the term vector space specified by the identifier.
     *
     * @param identifier identifier of the term vector space.
     * @return the existing or newly created TermVectorSpace.
     */
    public static TermVectorSpace getTermVectorSpace(String identifier) {
        return INSTANCE.get(identifier);
    }
}
