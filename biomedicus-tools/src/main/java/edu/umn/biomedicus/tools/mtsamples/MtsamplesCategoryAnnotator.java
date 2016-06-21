/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.tools.mtsamples;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import edu.umn.biomedicus.uima.common.Views;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Annotates the category on MTSamples documents using the document id.
 */
public class MtsamplesCategoryAnnotator extends JCasAnnotator_ImplBase {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MtsamplesCategoryAnnotator.class);

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        LOGGER.debug("Processing a document for MTSamples category");

        JCas systemView;
        try {
            systemView = aJCas.getView(Views.SYSTEM_VIEW);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        try {
            Document document = UimaAdapters.documentFromView(systemView);
            String documentId = document.getDocumentId();
            int underscore = documentId.indexOf("_");
            String category = documentId.substring(0, underscore);
            document.setMetadata("category", category);
        } catch (BiomedicusException e) {
            throw new AnalysisEngineProcessException(e);
        }

    }
}
