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

package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.model.semantics.Concept;
import edu.umn.biomedicus.model.text.Span;
import edu.umn.biomedicus.model.text.TextConcept;
import edu.umn.biomedicus.type.ConceptAnnotation;
import org.apache.uima.jcas.JCas;

import javax.annotation.Nullable;

/**
 * Adapts the UIMA type {@link ConceptAnnotation} to the Biomedicus type interface {@link Concept}.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class ConceptAdapter implements TextConcept {
    /**
     * The concept annotation.
     */
    private final ConceptAnnotation conceptAnnotation;

    /**
     * Constructor which initializes the class's fields.
     *
     * @param conceptAnnotation the concept annotation.
     */
    ConceptAdapter(ConceptAnnotation conceptAnnotation) {
        this.conceptAnnotation = conceptAnnotation;
    }

    /**
     * Creates a concept as a copy of an existing concept.
     *
     * @param concept        the concept
     * @param span           the span that the concept covers.
     * @param destinationCas the cas to save the new annotation in
     * @return the concept
     */
    static ConceptAdapter copyOf(Concept concept, Span span, JCas destinationCas) {
        ConceptAnnotation copyConceptAnnotation = new ConceptAnnotation(destinationCas, span.getBegin(), span.getEnd());
        copyConceptAnnotation.setSemanticType(concept.getType());
        copyConceptAnnotation.setConfidence(concept.getConfidence());
        copyConceptAnnotation.setSource(concept.getSource());
        copyConceptAnnotation.setIdentifier(concept.getIdentifier());
        copyConceptAnnotation.addToIndexes();
        return new ConceptAdapter(copyConceptAnnotation);
    }

    /**
     * Returns the concept annotation stored by this adapter.
     *
     * @return concept annotation
     */
    ConceptAnnotation getConceptAnnotation() {
        return conceptAnnotation;
    }

    @Override
    public String getIdentifier() {
        return conceptAnnotation.getIdentifier();
    }

    @Override
    public String getSource() {
        return conceptAnnotation.getSource();
    }

    @Override
    public String getType() {
        return conceptAnnotation.getSemanticType();
    }

    @Override
    public float getConfidence() {
        return conceptAnnotation.getConfidence();
    }

    @Override
    public int getBegin() {
        return conceptAnnotation.getBegin();
    }

    @Override
    public int getEnd() {
        return conceptAnnotation.getEnd();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConceptAdapter that = (ConceptAdapter) o;

        if (!conceptAnnotation.equals(that.conceptAnnotation)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return conceptAnnotation.hashCode();
    }

    @Override
    public void beginEditing() {
        conceptAnnotation.removeFromIndexes();
    }

    @Override
    public void endEditing() {
        conceptAnnotation.addToIndexes();
    }
}
