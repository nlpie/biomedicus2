/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.annotation.Nullable;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple collections reader that reads documents from a directory in the filesystem. Uses a
 * documents text to initialize the system view.
 *
 * @see FilesInDirectoryReader
 */
public class PlainTextInputFileAdapter implements InputFileAdapter {

  /**
   * Name of configuration parameter that contains the character encoding used by the input files.
   * If not specified, the default system encoding will be used.
   */
  public static final String PARAM_ENCODING = "encoding";
  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(PlainTextInputFileAdapter.class);
  /**
   * File encoding
   */
  @Nullable
  private String encoding;

  /**
   * View to load data into.
   */
  @Nullable
  private String viewName;

  @Override
  public void initialize(UimaContext uimaContext,
      ProcessingResourceMetaData processingResourceMetaData) {
    LOGGER.info("Initializing plain text input file adapter.");
    encoding = (String) uimaContext.getConfigParameterValue(PARAM_ENCODING);
  }

  @Override
  public void adaptFile(CAS cas, Path path) throws CollectionException, IOException {
    LOGGER.info("Reading text into a CAS view.");
    CAS targetView = cas.createView(viewName);

    byte[] bytes = Files.readAllBytes(path);
    String documentText = new String(bytes,
        Objects.requireNonNull(encoding,
            "Encoding must not be null"));
    targetView.setDocumentText(documentText);

    String fileName = path.getFileName().toString();
    int period = fileName.lastIndexOf('.');
    if (period == -1) {
      period = fileName.length();
    }
    String documentId = fileName.substring(0, period);
    UimaAdapters.createDocument(cas, null, documentId);
  }

  @Override
  public void setTargetView(String viewName) {
    this.viewName = viewName;
  }
}
