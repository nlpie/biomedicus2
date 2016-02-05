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

package edu.umn.biomedicus.tools.newinfo;

import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Writer for the new information detection project.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class NewInformationWriterAE extends JCasAnnotator_ImplBase {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * UIMA configuration parameter name for the base output directory.
     */
    public static final String PARAM_OUTPUT_DIR = "outputDir";

    /**
     * Object responsible for writing out all new information documents.
     */
    private NewInformationWriterFactory newInformationWriterFactory;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        Path outputDir = Paths.get((String) aContext.getConfigParameterValue(PARAM_OUTPUT_DIR));

        try {
            newInformationWriterFactory = NewInformationWriterFactory.createWithOutputDirectory(outputDir);
        } catch (BiomedicusException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        Document document = UimaAdapters.documentFromInitialView(jCas);

        LOGGER.info("Writing tokens, sentences and terms for new information for document.");

        try {
            newInformationWriterFactory.writeForDocument(document);
        } catch (BiomedicusException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
