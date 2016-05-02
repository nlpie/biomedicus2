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

package edu.umn.biomedicus.tools;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Logs any illegal annotations that will cause {@link AnnotationFS#getCoveredText()} to fail.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class AnnotationVerifier extends JCasAnnotator_ImplBase {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationVerifier.class);

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        try {
            Iterator<JCas> viewIterator = aJCas.getViewIterator();

            while (viewIterator.hasNext()) {
                JCas view = viewIterator.next();

                String sofaDataString = view.getSofaDataString();
                int length = sofaDataString != null ? sofaDataString.length() : -1;

                AnnotationIndex<Annotation> annotationIndex = view.getAnnotationIndex();

                for (Annotation annotation : annotationIndex) {
                    int begin = annotation.getBegin();
                    int end = annotation.getEnd();

                    if (begin > end) {
                        LOGGER.error("Annotation {} begin {} after end {}", annotation.getType().getName(), begin, end);
                    }

                    if (begin < 0) {
                        LOGGER.error("Annotation {} begin {} before 0", annotation.getType().getName(), begin);
                    }

                    if (end > length) {
                        LOGGER.error("Annotation {} end {} after length of sofa {}", annotation.getType().getName(),
                                end, length);
                    }

                }
            }

        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
