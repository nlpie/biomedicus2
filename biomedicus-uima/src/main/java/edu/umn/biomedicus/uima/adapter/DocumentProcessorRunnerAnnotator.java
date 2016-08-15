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

package edu.umn.biomedicus.uima.adapter;

import com.google.inject.Injector;
import com.google.inject.Key;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.application.DocumentProcessorRunner;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

/**
 *
 */
public final class DocumentProcessorRunnerAnnotator extends CasAnnotator_ImplBase {
    private static final List<String> KNOWN_PARAMETERS = Arrays.asList("viewName", "eagerLoad", "postProcessors");
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentProcessorRunnerAnnotator.class);
    @Nullable private String viewName;
    @Nullable private DocumentProcessorRunner documentProcessorRunner;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        try {
            documentProcessorRunner = ((GuiceInjector) aContext.getResourceObject("guiceInjector"))
                    .createDocumentProcessorRunner();
        } catch (ResourceAccessException e) {
            throw new ResourceInitializationException(e);
        }

        try {
            String documentProcessorClassName = (String) aContext.getConfigParameterValue("documentProcessor");
            documentProcessorRunner.setDocumentProcessorClassName(documentProcessorClassName);
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        String[] postProcessors = (String[]) aContext.getConfigParameterValue("postProcessors");
        if (postProcessors != null) {
            for (String postProcessor : postProcessors) {
                try {
                    documentProcessorRunner.addPostProcessorClassName(postProcessor);
                } catch (ClassNotFoundException e) {
                    throw new ResourceInitializationException(e);
                }
            }
        }

        Map<String, Object> settingsMap = new HashMap<>();
        for (String parameterName : aContext.getConfigParameterNames()) {
            if (parameterName != null && !KNOWN_PARAMETERS.contains(parameterName)) {
                settingsMap.put(parameterName, aContext.getConfigParameterValue(parameterName));
            }
        }

        try {
            documentProcessorRunner.initialize(settingsMap, Collections.emptyMap());
        } catch (BiomedicusException e) {
            throw new ResourceInitializationException(e);
        }

        String[] eagerLoad = (String[]) aContext.getConfigParameterValue("eagerLoad");
        if (eagerLoad != null) {
            for (String className : eagerLoad) {
                try {
                    documentProcessorRunner.eagerLoadClassName(className);
                } catch (BiomedicusException e) {
                    throw new ResourceInitializationException(e);
                }
            }
        }

        viewName = (String) aContext.getConfigParameterValue("viewName");
    }

    @Override
    public void process(CAS cas) throws AnalysisEngineProcessException {
        if (documentProcessorRunner == null) {
            throw new IllegalStateException("Document processor runner is null.");
        }

        if (viewName == null) {
            throw new IllegalStateException("view name is null");
        }

        try {
            LOGGER.debug("Processing document from view: {}", viewName);
            CAS view = cas.getView(viewName);
            if (view == null) {
                LOGGER.error("Trying to process null view");
                throw new BiomedicusException("View was null");
            }

            HashMap<Key<?>, Object> additionalSeeded = new HashMap<>();
            additionalSeeded.put(Key.get(CAS.class), view);

            CASDocument casDocument = new CASDocument(view);
            documentProcessorRunner.processDocument(casDocument, additionalSeeded);
        } catch (BiomedicusException e) {
            LOGGER.error("error while processing document");
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        if (documentProcessorRunner == null) {
            throw new IllegalStateException("Document processor runner is null.");
        }

        try {
            documentProcessorRunner.processingFinished();
        } catch (BiomedicusException e) {
            LOGGER.error("Error during collection processing complete", e);
            throw new AnalysisEngineProcessException(e);
        }
    }
}
