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
import edu.umn.biomedicus.uima.labels.AbstractLabelAdapter;
import org.apache.uima.cas.*;
import org.apache.uima.cas.text.AnnotationFS;

final class DictionaryConceptLabelAdapter extends AbstractLabelAdapter<DictionaryConcept> {
    private final Feature identifierFeature;
    private final Feature sourceFeature;
    private final Feature confidenceFeature;
    private final Feature semanticTypeFeature;

    private DictionaryConceptLabelAdapter(CAS cas,
                                            Type type,
                                            Feature identifierFeature,
                                            Feature sourceFeature,
                                            Feature confidenceFeature,
                                            Feature semanticTypeFeature) {
        super(cas, type);
        this.identifierFeature = identifierFeature;
        this.sourceFeature = sourceFeature;
        this.confidenceFeature = confidenceFeature;
        this.semanticTypeFeature = semanticTypeFeature;
    }

    public static DictionaryConceptLabelAdapter create(CAS cas) {
        TypeSystem typeSystem = cas.getTypeSystem();
        Type conceptsType = typeSystem.getType("edu.umn.biomedicus.uima.type1_6.DictionaryConcept");
        Feature identifierFeature = conceptsType.getFeatureByBaseName("identifier");
        Feature sourceFeature = conceptsType.getFeatureByBaseName("source");
        Feature confidenceFeature = conceptsType.getFeatureByBaseName("confidence");
        Feature semanticTypeFeature = conceptsType.getFeatureByBaseName("semanticType");
        return new DictionaryConceptLabelAdapter(cas, conceptsType, identifierFeature, sourceFeature, confidenceFeature,
                semanticTypeFeature);

    }

    @Override
    protected void fillAnnotation(Label<DictionaryConcept> label, AnnotationFS annotationFS) {
        DictionaryConcept dictionaryConcept = label.value();
        annotationFS.setStringValue(identifierFeature, dictionaryConcept.getIdentifier());
        annotationFS.setStringValue(sourceFeature, dictionaryConcept.getSource());
        annotationFS.setDoubleValue(confidenceFeature, dictionaryConcept.getConfidence());
        annotationFS.setStringValue(semanticTypeFeature, dictionaryConcept.getType());
    }

    @Override
    protected DictionaryConcept createLabelValue(FeatureStructure featureStructure) {
        return DictionaryConcept.builder()
                .withType(featureStructure.getStringValue(semanticTypeFeature))
                .withSource(featureStructure.getStringValue(sourceFeature))
                .withIdentifier(featureStructure.getStringValue(identifierFeature))
                .withConfidence(featureStructure.getDoubleValue(confidenceFeature))
                .build();
    }
}
