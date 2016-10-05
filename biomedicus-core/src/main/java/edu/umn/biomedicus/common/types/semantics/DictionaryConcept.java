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

package edu.umn.biomedicus.common.types.semantics;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Biomedicus basic unit for concepts. Represents an idea in some kind of ontology, for example UMLS concepts with CUIs.
 *
 * @since 1.5.0
 */
public final class DictionaryConcept implements Concept {
    private final String identifier;

    private final String source;

    private final String type;

    private final double confidence;

    private DictionaryConcept(String identifier, String source, String type, double confidence) {
        this.identifier = Objects.requireNonNull(identifier, "identifier must not be null");
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.confidence = Objects.requireNonNull(confidence, "confidence must not null");
    }

    public static DictionaryConcept copyOf(Concept concept) {
        return new DictionaryConcept(concept.getIdentifier(), concept.getSource(), concept.getType(),
                concept.getConfidence());
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public double getConfidence() {
        return confidence;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private String identifier;
        @Nullable
        private String source;
        @Nullable
        private String type;
        @Nullable
        private Double confidence;

        public Builder withIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder withSource(String source) {
            this.source = source;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withConfidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public DictionaryConcept build() {
            if (identifier == null) {
                throw new IllegalArgumentException("Identifier is null");
            }
            if (source == null) {
                throw new IllegalArgumentException("source is null");
            }
            if (type == null) {
                throw new IllegalArgumentException("type is null");
            }
            if (confidence == null) {
                throw new IllegalArgumentException("Confidence is null");
            }
            return new DictionaryConcept(identifier, source, type, confidence);
        }
    }
}
