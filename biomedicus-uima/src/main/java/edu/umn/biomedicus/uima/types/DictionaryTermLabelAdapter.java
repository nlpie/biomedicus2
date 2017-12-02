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
import edu.umn.biomedicus.concepts.DictionaryTerm;
import edu.umn.biomedicus.uima.labels.AbstractLabelAdapter;
import java.util.ArrayList;
import java.util.List;
import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.text.AnnotationFS;

final class DictionaryTermLabelAdapter extends AbstractLabelAdapter<DictionaryTerm> {

  private final DictionaryConceptLabelAdapter dictionaryConceptLabelAdapter;
  private final Feature conceptsFeature;

  @Inject
  DictionaryTermLabelAdapter(CAS cas) {
    super(cas, cas.getTypeSystem()
        .getType("edu.umn.biomedicus.uima.type1_6.DictionaryTerm"));
    dictionaryConceptLabelAdapter = new DictionaryConceptLabelAdapter(cas);
    conceptsFeature = type.getFeatureByBaseName("concepts");
  }

  @Override
  protected void fillAnnotation(DictionaryTerm label, AnnotationFS annotationFS) {
    List<DictionaryConcept> concepts = label.getConcepts();
    int size = concepts.size();
    ArrayFS arrayFS = cas.createArrayFS(size);
    for (int i = 0; i < size; i++) {
      DictionaryConcept dictionaryConcept = concepts.get(i);
      AnnotationFS conceptAnnotation = dictionaryConceptLabelAdapter.labelToAnnotation(dictionaryConcept);
      arrayFS.set(i, conceptAnnotation);
    }
    cas.addFsToIndexes(arrayFS);
    annotationFS.setFeatureValue(conceptsFeature, arrayFS);
  }

  @Override
  public DictionaryTerm annotationToLabel(AnnotationFS annotationFS) {
    FeatureStructure conceptsFeatureValue = annotationFS.getFeatureValue(conceptsFeature);
    if (!(conceptsFeatureValue instanceof ArrayFS)) {
      throw new IllegalStateException("Concepts feature structure is not array.");
    }

    ArrayFS conceptsArray = (ArrayFS) conceptsFeatureValue;

    int size = conceptsArray.size();

    List<DictionaryConcept> concepts = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      AnnotationFS conceptFeatureStructure = (AnnotationFS) conceptsArray.get(i);
      concepts.add(dictionaryConceptLabelAdapter.annotationToLabel(conceptFeatureStructure));
    }
    return new DictionaryTerm(annotationFS.getBegin(), annotationFS.getEnd(), concepts);
  }
}
