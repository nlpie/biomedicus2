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

package edu.umn.biomedicus.uima.util;

import edu.umn.biomedicus.uima.common.Views;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Removes everything from the conceptId field except for the CUIs.
 *
 * @author Ben Knoll
 * @since 1.4
 */
public class ConceptCleanerAnnotator extends CasAnnotator_ImplBase {
    @Override
    public void process(CAS aCAS) throws AnalysisEngineProcessException {
        CAS systemViewCas = aCAS.getView(Views.SYSTEM_VIEW);

        Type termType = systemViewCas.getTypeSystem().getType("edu.umn.biomedicus.mtsamples.types.Term");
        Feature conceptFeature = termType.getFeatureByBaseName("termConcept");
        Type conceptType = systemViewCas.getTypeSystem().getType("edu.umn.biomedicus.mtsamples.types.Concept");
        Feature conceptIdFeature = conceptType.getFeatureByBaseName("conceptId");

        AnnotationIndex<AnnotationFS> termsIndex = systemViewCas.getAnnotationIndex(termType);

        for (AnnotationFS termAnnotation : termsIndex) {
            FeatureStructure concept = termAnnotation.getFeatureValue(conceptFeature);

            if (concept == null) {
                continue;
            }

            String conceptIdsString = concept.getStringValue(conceptIdFeature);
            if (conceptIdsString == null) {
                continue;
            }

            List<String> conceptIds = new ArrayList<>();
            Matcher matcher = Pattern.compile("C[0-9]{7}+").matcher(conceptIdsString);
            while (matcher.find()) {
                conceptIds.add(matcher.group());
            }

            concept.setStringValue(conceptIdFeature, conceptIds.stream().collect(Collectors.joining(" ")));
        }
    }
}
