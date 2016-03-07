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

package edu.umn.biomedicus.uima.files;

import edu.umn.biomedicus.type.ClinicalNoteAnnotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Date;

/**
 * A simple collections reader that reads documents from a directory in the filesystem. Uses a documents text to
 * initialize the system view.
 *
 * @see FilesInDirectoryReader
 */
public class PlainTextInputFileAdapter implements InputFileAdapter {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(PlainTextInputFileAdapter.class);

    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     * If not specified, the default system encoding will be used.
     */
    public static final String PARAM_ENCODING = "encoding";

    /**
     * Date formatter for adding date to metadata.
     */
    private final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG);

    /**
     * File encoding
     */
    private String encoding;

    /**
     * Analyzer version.
     */
    private String version;

    /**
     * View to load data into.
     */
    private String viewName;

    @Override
    public void initialize(UimaContext uimaContext, ProcessingResourceMetaData processingResourceMetaData) {
        LOGGER.info("Initializing plain text input file adapter.");
        encoding = (String) uimaContext.getConfigParameterValue(PARAM_ENCODING);

        version = processingResourceMetaData.getVersion();
    }

    @Override
    public void adaptFile(CAS cas, Path path) throws CollectionException, IOException {
        LOGGER.info("Reading text into a CAS view.");
        JCas defaultView;
        try {
            defaultView = cas.getJCas();
        } catch (CASException e) {
            throw new CollectionException(e);
        }

        JCas targetView;
        try {
            targetView = defaultView.createView(viewName);
        } catch (CASException e) {
            throw new CollectionException(e);
        }

        byte[] bytes = Files.readAllBytes(path);
        String documentText = new String(bytes, encoding);
        targetView.setDocumentText(documentText);

        ClinicalNoteAnnotation documentAnnotation = new ClinicalNoteAnnotation(targetView, 0, documentText.length());
        String fileName = path.getFileName().toString();
        int period = fileName.lastIndexOf('.');
        if (period == -1) {
            period = fileName.length();
        }
        documentAnnotation.setDocumentId(fileName.substring(0, period));
        documentAnnotation.setAnalyzerVersion(version);
        documentAnnotation.setRetrievalTime(dateFormatter.format(new Date()));
        documentAnnotation.addToIndexes();
    }

    @Override
    public void setTargetView(String viewName) {
        this.viewName = viewName;
    }
}
