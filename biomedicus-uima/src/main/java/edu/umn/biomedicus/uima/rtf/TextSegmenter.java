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

import edu.umn.biomedicus.type.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

/**
 * Uses the table and paragraph information to create text segments.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class TextSegmenter extends JCasAnnotator_ImplBase {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        LOGGER.info("Segmenting rtf text.");
        TextSegmentsBuilder textSegmentsBuilder = new TextSegmentsBuilder(jCas);

        textSegmentsBuilder.addAnnotations(ParagraphAnnotation.type);
        textSegmentsBuilder.addAnnotations(RowAnnotation.type);
        textSegmentsBuilder.addAnnotations(CellAnnotation.type);
        textSegmentsBuilder.addAnnotations(NestedRowAnnotation.type);
        textSegmentsBuilder.addAnnotations(NestedCellAnnotation.type);

        textSegmentsBuilder.buildInView();
    }


}
