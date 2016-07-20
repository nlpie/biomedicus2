/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.types;

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.semantics.DictionaryConcept;
import edu.umn.biomedicus.common.semantics.DictionaryTerm;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.uima.labels.AbstractLabelAdapter;
import org.apache.uima.cas.*;
import org.apache.uima.cas.text.AnnotationFS;

import java.util.List;

final class DictionaryTermLabelAdapter extends AbstractLabelAdapter<DictionaryTerm> {
    private final DictionaryConceptLabelAdapter dictionaryConceptLabelAdapter;
    private final Feature conceptsFeature;

    private DictionaryTermLabelAdapter(CAS cas,
                                       Type type,
                                       DictionaryConceptLabelAdapter dictionaryConceptLabelAdapter,
                                       Feature conceptsFeature) {
        super(cas, type);
        this.dictionaryConceptLabelAdapter = dictionaryConceptLabelAdapter;
        this.conceptsFeature = conceptsFeature;
    }

    public static DictionaryTermLabelAdapter create(CAS cas) {
        Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.DictionaryTerm");
        DictionaryConceptLabelAdapter dictionaryConceptLabelAdapter = DictionaryConceptLabelAdapter.create(cas);
        Feature conceptsFeature = type.getFeatureByBaseName("concepts");
        return new DictionaryTermLabelAdapter(cas, type, dictionaryConceptLabelAdapter, conceptsFeature);
    }

    @Override
    protected void fillAnnotation(Label<DictionaryTerm> label, AnnotationFS annotationFS) {
        List<DictionaryConcept> concepts = label.value().getConcepts();
        int size = concepts.size();
        ArrayFS arrayFS = cas.createArrayFS(size);
        for (int i = 0; i < size; i++) {
            DictionaryConcept dictionaryConcept = concepts.get(i);
            Label<DictionaryConcept> conceptLabel = new Label<>(new Span(label), dictionaryConcept);
            AnnotationFS conceptAnnotation = dictionaryConceptLabelAdapter.labelToAnnotation(conceptLabel);
            arrayFS.set(i, conceptAnnotation);
        }
        cas.addFsToIndexes(arrayFS);
        annotationFS.setFeatureValue(conceptsFeature, arrayFS);
    }

    @Override
    protected DictionaryTerm createLabelValue(FeatureStructure featureStructure) {
        DictionaryTerm.Builder builder = DictionaryTerm.builder();

        FeatureStructure conceptsFeatureValue = featureStructure.getFeatureValue(conceptsFeature);
        if (!(conceptsFeatureValue instanceof ArrayFS)) {
            throw new IllegalStateException("Concepts feature structure is not array.");
        }

        ArrayFS conceptsArray = (ArrayFS) conceptsFeatureValue;

        int size = conceptsArray.size();

        for (int i = 0; i < size; i++) {
            FeatureStructure conceptFeatureStructure = conceptsArray.get(i);
            DictionaryConcept concept = dictionaryConceptLabelAdapter.createLabelValue(conceptFeatureStructure);
            builder.addConcept(concept);
        }

        return builder.build();
    }
}
