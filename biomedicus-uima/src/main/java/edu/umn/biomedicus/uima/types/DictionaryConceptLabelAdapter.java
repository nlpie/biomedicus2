/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.types;

import com.google.inject.Inject;
import edu.umn.biomedicus.concepts.DictionaryConcept;
import edu.umn.biomedicus.uima.labels.AbstractLabelAdapter;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.text.AnnotationFS;

final class DictionaryConceptLabelAdapter extends AbstractLabelAdapter<DictionaryConcept> {

  private final Feature identifierFeature;
  private final Feature sourceFeature;
  private final Feature confidenceFeature;
  private final Feature semanticTypeFeature;

  @Inject
  DictionaryConceptLabelAdapter(CAS cas) {
    super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.DictionaryConcept"));
    identifierFeature = getType().getFeatureByBaseName("identifier");
    sourceFeature = getType().getFeatureByBaseName("source");
    confidenceFeature = getType().getFeatureByBaseName("confidence");
    semanticTypeFeature = getType().getFeatureByBaseName("semanticType");
  }

  @Override
  public DictionaryConcept annotationToLabel(AnnotationFS annotationFS) {
    return new DictionaryConcept(
        annotationFS.getBegin(),
        annotationFS.getEnd(),
        annotationFS.getStringValue(identifierFeature),
        annotationFS.getStringValue(sourceFeature),
        annotationFS.getStringValue(semanticTypeFeature),
        annotationFS.getDoubleValue(confidenceFeature)
    );
  }

  @Override
  protected void fillAnnotation(DictionaryConcept label, AnnotationFS annotationFS) {
    annotationFS.setStringValue(identifierFeature, label.getIdentifier());
    annotationFS.setStringValue(sourceFeature, label.getSource());
    annotationFS.setDoubleValue(confidenceFeature, label.getConfidence());
    annotationFS.setStringValue(semanticTypeFeature, label.getType());
  }
}
