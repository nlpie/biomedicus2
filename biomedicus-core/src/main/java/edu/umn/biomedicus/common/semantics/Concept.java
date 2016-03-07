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

package edu.umn.biomedicus.common.semantics;

/**
 * Biomedicus basic unit for concepts. Represents an idea in some kind of ontology, for example UMLS concepts with CUIs.
 */
public interface Concept {
    /**
     * The identifier for the concept within the ontology, an example would be CUIs in UMLS.
     * @return string identifier
     */
    String getIdentifier();

    /**
     * Sets the source of the Concept, which ontology that it comes from. For example: "UMLS"
     * @return string identifier for the source of concept
     */
    String getSource();

    /**
     * Sets the semantic type or grouping of the concept, an example would be TUIs in UMLS
     * @return string identifier for the type of the concept
     */
    String getType();

    /**
     * Returns the confidence that this is a correct concept
     * @return the float confidence between 0.0 and 1.0
     */
    float getConfidence();
}
