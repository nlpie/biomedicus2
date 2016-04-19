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

package edu.umn.biomedicus.uima.stopword;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.stopwords.Stopwords;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * UIMA annotator component which uses biomedicus's Stopwords class to determine whether or not Tokens are Stopwords.
 *
 * @see edu.umn.biomedicus.stopwords.Stopwords
 */
public class TokenStopwordsAnnotator extends JCasAnnotator_ImplBase {

    private static final Logger LOGGER = LogManager.getLogger(TokenStopwordsAnnotator.class);

    public static final String RESOURCE_STOPWORDS = "stopwordsResource";
    private Stopwords stopwords;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        LOGGER.info("Initializing stopwords annotator");

        StopwordsResource stopwordsResource;
        try {
            stopwordsResource = (StopwordsResource) aContext.getResourceObject(RESOURCE_STOPWORDS);
        } catch (ResourceAccessException e) {
            throw new ResourceInitializationException(e);
        }
        stopwords = stopwordsResource.getStopwords();
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        LOGGER.info("Processing a document for stopwords");

        Document document;
        try {
            document = UimaAdapters.documentFromInitialView(jCas);
        } catch (BiomedicusException e) {
            throw new AnalysisEngineProcessException(e);
        }

        stopwords.annotateStopwords(document);
    }
}
