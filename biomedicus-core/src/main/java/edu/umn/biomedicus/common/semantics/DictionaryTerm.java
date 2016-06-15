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

package edu.umn.biomedicus.common.semantics;

import java.util.ArrayList;
import java.util.List;

public final class DictionaryTerm {
    private final List<DictionaryConcept> concepts;

    private DictionaryTerm(List<DictionaryConcept> concepts) {
        this.concepts = concepts;
    }

    public List<DictionaryConcept> getConcepts() {
        return concepts;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<DictionaryConcept> concepts = new ArrayList<>();

        public Builder addConcept(DictionaryConcept dictionaryConcept) {
            concepts.add(dictionaryConcept);
            return this;
        }

        public DictionaryTerm build() {
            return new DictionaryTerm(concepts);
        }
    }
}
