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

import edu.umn.biomedicus.common.semantics.Concept;

/**
 *
 */
class UmlsConcept implements Concept {
    private final String identifier;
    private final String type;

    public UmlsConcept(String identifier, String type) {
        this.identifier = identifier;
        this.type = type;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getSource() {
        return "UMLS";
    }

    @Override
    public float getConfidence() {
        return 1.0f;
    }
}
