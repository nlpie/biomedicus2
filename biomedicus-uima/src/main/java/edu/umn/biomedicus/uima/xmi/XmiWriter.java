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

package edu.umn.biomedicus.uima.xmi;

import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.uima.files.FileNameProviders;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A UIMA analysis engine that writes the contents of CASes to a files in a folder.
 */
public class XmiWriter extends JCasAnnotator_ImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmiWriter.class);

    private TypeSystemWriterResource typeSystemWriter;

    private Path outputDir;

    /**
     * Initializes the outputDirectory.
     *
     * @param context the uima context
     * @throws ResourceInitializationException if we fail to initialize DocumentIdOutputStreamFactory
     */
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        LOGGER.info("Initializing XMI writer AE");

        outputDir = Paths.get((String) context.getConfigParameterValue("outputDirectory"));
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        try {
            typeSystemWriter = (TypeSystemWriterResource) context.getResourceObject("typeSystemWriterResource");
        } catch (ResourceAccessException e) {
            throw new ResourceInitializationException(e);
        }
    }

    /**
     * Uses documentIdOutputStreamFactory to write a cas to disk.
     *
     * @param aJCas the cas to write
     * @throws AnalysisEngineProcessException if the output fails
     */
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        try {
            typeSystemWriter.writeToPath(outputDir.resolve("TypeSystem.xml"), aJCas.getTypeSystem());
        } catch (IOException | SAXException e) {
            throw new AnalysisEngineProcessException(e);
        }
        String fileName;
        try {
            fileName = FileNameProviders.fromInitialView(aJCas, ".xmi");
        } catch (BiomedicusException e) {
            throw new AnalysisEngineProcessException(e);
        }
        Path path = outputDir.resolve(fileName);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Writing XMI CAS to location: {}", path.toString());
        }

        try (OutputStream out = new FileOutputStream(path.toFile())) {
            XmiCasSerializer.serialize(aJCas.getCas(), out);
        } catch (IOException | SAXException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
