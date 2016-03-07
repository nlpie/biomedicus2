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

import edu.umn.biomedicus.common.semantics.Concept;
import edu.umn.biomedicus.common.text.Term;
import edu.umn.biomedicus.type.ConceptAnnotation;
import edu.umn.biomedicus.type.TermAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapts the {@link Term} interface for UIMA using the backing annotation
 * {@link edu.umn.biomedicus.type.TermAnnotation}.
 */
class TermAdapter implements Term {
    private final TermAnnotation termAnnotation;

    TermAdapter(TermAnnotation termAnnotation) {
        this.termAnnotation = termAnnotation;
    }

    @Override
    public Concept getPrimaryConcept() {
        return new ConceptAdapter(termAnnotation.getPrimaryConcept());
    }

    @Override
    public List<Concept> getAlternativeConcepts() {
        return Arrays.stream(termAnnotation.getAlternativeConcepts().toArray())
                .map(c -> (ConceptAnnotation) c)
                .map(ConceptAdapter::new)
                .collect(Collectors.toList());
    }

    @Override
    public int getBegin() {
        return termAnnotation.getBegin();
    }

    @Override
    public int getEnd() {
        return termAnnotation.getEnd();
    }

    /**
     * Creates a new {@link TermAdapter} by copying the {@link Term}
     * object into the destination cas. Creates a new {@link edu.umn.biomedicus.type.TermAnnotation} and new
     * {@link edu.umn.biomedicus.type.ConceptAnnotation} for the term and each of the concepts in the term. The original
     * term is completely unchanged and the new term is completely separate from the existing one.
     *
     * @param term term to be copied
     * @param destinationCas destination cas
     */
    public static TermAdapter copyOf(Term term, JCas destinationCas) {
        TermAnnotation copyTermAnnotation = new TermAnnotation(destinationCas, term.getBegin(), term.getEnd());

        ConceptAdapter primaryConcept = ConceptAdapter.copyOf(term.getPrimaryConcept(), term, destinationCas);
        copyTermAnnotation.setPrimaryConcept(primaryConcept.getConceptAnnotation());

        List<Concept> alternativeConcepts = term.getAlternativeConcepts();
        int size = alternativeConcepts.size();
        FSArray fsArray = new FSArray(destinationCas, size);
        for (int i = 0; i < size; i++) {
            fsArray.set(i, ConceptAdapter.copyOf(alternativeConcepts.get(i), term, destinationCas).getConceptAnnotation());
        }
        copyTermAnnotation.setAlternativeConcepts(fsArray);

        copyTermAnnotation.addToIndexes();
        return new TermAdapter(copyTermAnnotation);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TermAdapter that = (TermAdapter) o;

        return termAnnotation.equals(that.termAnnotation);

    }

    @Override
    public int hashCode() {
        return termAnnotation.hashCode();
    }
}
