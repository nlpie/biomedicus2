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

package edu.umn.biomedicus.uima.rtf;

import edu.umn.biomedicus.rtfuima.type.NewParagraph;
import edu.umn.biomedicus.type.ParagraphAnnotation;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Annotates paragraphs in rtf text using annotations for the \par keyword.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class ParagraphAnnotator extends JCasAnnotator_ImplBase {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ParagraphAnnotator.class);

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        LOGGER.info("Annotating rtf paragraphs.");
        AnnotationIndex<Annotation> newParagraphIndex = jCas.getAnnotationIndex(NewParagraph.type);
        int start = 0;

        for (Annotation newParagraph : newParagraphIndex) {
            int end = newParagraph.getEnd();
            ParagraphAnnotation paragraphAnnotation = new ParagraphAnnotation(jCas, start, end);
            paragraphAnnotation.addToIndexes();
            start = end;
        }
    }
}
