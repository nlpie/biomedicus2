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

package edu.umn.biomedicus.tools.mtsamples;

import edu.umn.biomedicus.type.ClinicalNoteAnnotation;
import edu.umn.biomedicus.uima.Views;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Annotates the category on MTSamples documents using the document id.
 */
public class MtsamplesCategoryAnnotator extends JCasAnnotator_ImplBase {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        LOGGER.debug("Processing a document for MTSamples category");

        JCas systemView;
        try {
            systemView = aJCas.getView(Views.SYSTEM_VIEW);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        AnnotationIndex<Annotation> clinicalNoteIndex = systemView.getAnnotationIndex(ClinicalNoteAnnotation.type);
        for (Annotation annotation : clinicalNoteIndex) {
            @SuppressWarnings("unchecked")
            ClinicalNoteAnnotation clinicalNoteAnnotation = (ClinicalNoteAnnotation) annotation;
            clinicalNoteAnnotation.removeFromIndexes();

            String documentId = clinicalNoteAnnotation.getDocumentId();
            int underscore = documentId.indexOf("_");
            String category = documentId.substring(0, underscore);
            clinicalNoteAnnotation.setCategory(category);
            clinicalNoteAnnotation.addToIndexes();
            LOGGER.debug("Categorizing document {} as {}", documentId, category);
        }

    }
}
