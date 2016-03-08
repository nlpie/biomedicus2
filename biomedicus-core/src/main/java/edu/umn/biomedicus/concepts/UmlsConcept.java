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

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
class UmlsConcept implements Concept {
    private final CUI identifier;
    private final List<TUI> types;
    private final double confidence;

    public UmlsConcept(CUI identifier, List<TUI> types, double confidence) {
        this.identifier = identifier;
        this.types = types;
        this.confidence = confidence;
    }

    @Override
    public String getIdentifier() {
        return identifier.getText();
    }

    public String getTypes() {
        return types.stream().map(TUI::getText).collect(Collectors.joining(";"));
    }

    @Override
    public String getSource() {
        return "UMLS";
    }

    @Override
    public double getConfidence() {
        return confidence;
    }
}
