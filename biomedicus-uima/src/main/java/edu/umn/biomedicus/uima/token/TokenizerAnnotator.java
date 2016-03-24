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

package edu.umn.biomedicus.uima.token;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.processing.Tokenizer;
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
 * UIMA {@link org.apache.uima.analysis_component.AnalysisComponent} which uses a
 * {@link edu.umn.biomedicus.processing.Tokenizer} provider by a
 * {@link TokenizerResource} to tokenize cas documents.
 */
public class TokenizerAnnotator extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String RESOURCE_TOKENIZER = "tokenizerResource";
    private Tokenizer tokenizer;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        TokenizerResource tokenizerResource;
        try {
            tokenizerResource = (TokenizerResource) aContext.getResourceObject(RESOURCE_TOKENIZER);
        } catch (ResourceAccessException e) {
            throw new ResourceInitializationException(e);
        }


        tokenizer = tokenizerResource.getTokenizer();
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        Document document = null;
        try {
            document = UimaAdapters.documentFromInitialView(aJCas);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
        LOGGER.info("Tokenizing document.");

        tokenizer.tokenize(document);
    }
}
