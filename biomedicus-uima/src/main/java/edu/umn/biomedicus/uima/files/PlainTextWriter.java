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

package edu.umn.biomedicus.uima.files;

import edu.umn.biomedicus.uima.common.Views;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A UIMA analysis engine that writes the sofa for the view named {@link Views#SYSTEM_VIEW} to a text file.
 */
public class PlainTextWriter extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlainTextWriter.class);

    private Path outputDir;

    /**
     * Initializes the writer factory
     *
     * @param aContext the uima context, used to get configuration parameters.
     * @throws ResourceInitializationException if our writer factory fails to set the output directory
     */
    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        LOGGER.info("initializing system view writer");

        outputDir = Paths.get((String) aContext.getConfigParameterValue("outputDirectory"));
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    /**
     * Writes the SystemView text to a writer provided by the writer factory.
     *
     * @param jCas the default view
     * @throws AnalysisEngineProcessException if getting the system view or writing fails.
     */
    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        LOGGER.info("writing a system view to text");

        JCas systemView;
        try {
            systemView = jCas.getView(Views.SYSTEM_VIEW);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        String fileName = FileNameProviders.fromCAS(jCas.getCas(), ".txt");
        Path file = outputDir.resolve(fileName);

        try (InputStream sofaDataStream = systemView.getSofaDataStream()) {
            Files.copy(sofaDataStream, file);
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
