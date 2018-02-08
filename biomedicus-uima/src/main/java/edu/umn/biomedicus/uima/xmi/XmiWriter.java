/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.xmi;

import edu.umn.biomedicus.exc.BiomedicusException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nullable;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * A UIMA analysis engine that writes the contents of CASes to a files in a folder.
 */
public class XmiWriter extends CasAnnotator_ImplBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(XmiWriter.class);

  @Nullable
  private TypeSystemWriter typeSystemWriter;

  @Nullable
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
      typeSystemWriter = (TypeSystemWriter) context.getResourceObject("typeSystemWriter");
    } catch (ResourceAccessException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(CAS cas) throws AnalysisEngineProcessException {
    assert typeSystemWriter != null;
    assert outputDir != null;
    try {
      typeSystemWriter.writeToPath(outputDir.resolve("TypeSystem.xml"));
    } catch (BiomedicusException e) {
      throw new AnalysisEngineProcessException(e);
    }

    Type type = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_5.DocumentId");
    Feature documentId = type.getFeatureByBaseName("documentId");
    String fileName = cas.getView("metadata")
        .getIndexRepository()
        .getAllIndexedFS(type)
        .next()
        .getStringValue(documentId) + ".xmi";
    Path path = outputDir.resolve(fileName);
    LOGGER.debug("Writing XMI CAS to location: {}", path.toString());

    try (OutputStream out = new FileOutputStream(path.toFile())) {
      XmiCasSerializer.serialize(cas, out);
    } catch (IOException | SAXException e) {
      LOGGER.error("Failed on document: {}");
      throw new AnalysisEngineProcessException(e);
    }
  }
}
