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

package edu.umn.biomedicus.uima.sentence;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.sentence.SentenceDetector;
import edu.umn.biomedicus.sentence.SentenceDetectorFactory;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Annotates sentences using a {@link edu.umn.biomedicus.sentence.SentenceDetector}.
 *
 * @since 1.1.0
 */
public class SentenceAnnotator extends JCasAnnotator_ImplBase {
    private static final String RESOURCE_SENTENCE_DETECTOR_FACTORY = "sentenceDetector";
    private static final Logger LOGGER = LogManager.getLogger();
    private SentenceDetectorFactory sentenceDetectorFactory;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        SentenceDetectorFactoryResource sentenceDetectorFactoryResource = null;
        try {
            sentenceDetectorFactoryResource = (SentenceDetectorFactoryResource) aContext.getResourceObject(RESOURCE_SENTENCE_DETECTOR_FACTORY);
        } catch (ResourceAccessException e) {
            throw new ResourceInitializationException(e);
        }

        sentenceDetectorFactory = sentenceDetectorFactoryResource.getSentenceDetectorFactory();
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        Document jCasDocument = null;
        try {
            jCasDocument = UimaAdapters.documentFromInitialView(aJCas);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
        LOGGER.info("Detecting sentences in document.");

        SentenceDetector sentenceDetector = sentenceDetectorFactory.create();
        sentenceDetector.processDocument(jCasDocument);
    }
}
