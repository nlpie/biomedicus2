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

package edu.umn.biomedicus.concepts;

/**
 * Represents the semantic type groups in the UMLS library.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public enum UmlsSemanticTypeGroup {
    ACTIVITIES_AND_BEHAVIORS("ACTI", "Activities & Behaviors"),
    ANATOMY("ANAT", "Anatomy"),
    CHEMICALS_AND_DRUGS("CHEM", "Chemicals & Drugs"),
    CONCEPTS_AND_IDEAS("CONC", "Concepts & Ideas"),
    DEVICES("DEVI", "Devices"),
    DISORDERS("DISO", "Disorders"),
    GENES_AND_MOLECULAR_SEQUENCES("GENE", "Genes & Molecular Sequences"),
    GEOGRAPHIC_AREAS("GEOG", "Geographic Areas"),
    LIVING_BEINGS("LIVB", "Living Beings"),
    OBJECTS("OBJC", "Objects"),
    OCCUPATIONS("OCCU", "Occupations"),
    ORGANIZATIONS("ORGA", "Organizations"),
    PHENOMENA("PHEN", "Phenomena"),
    PHYSIOLOGY("PHYS", "Physiology"),
    PROCEDURES("PROC", "Procedures");


    private final String identifier;
    private final String value;

    UmlsSemanticTypeGroup(String identifier, String value) {
        this.identifier = identifier;
        this.value = value;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return value;
    }
}
