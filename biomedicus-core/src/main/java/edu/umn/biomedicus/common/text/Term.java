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

import edu.umn.biomedicus.common.semantics.Concept;

import java.util.List;

/**
 * Biomedicus base class for terms within text. A term is an identified concept.
 */
public interface Term extends SpanLike {
    /**
     * Get the primary concept of this term.
     * @return Get a secondary concept for the term
     */
    Concept getPrimaryConcept();

    /**
     * The alternative concepts of this term
     * @return an Iterable of the alternative concepts of this term
     */
    List<Concept> getAlternativeConcepts();
}
