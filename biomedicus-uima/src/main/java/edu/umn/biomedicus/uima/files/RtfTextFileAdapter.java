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

import edu.umn.biomedicus.type.ClinicalNoteAnnotation;
import edu.umn.biomedicus.type.IllegalXmlCharacter;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Date;

/**
 *
 */
public class RtfTextFileAdapter implements InputFileAdapter {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RtfTextFileAdapter.class);

    /**
     * Date formatter for adding date to metadata.
     */
    private final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG);

    /**
     * View to load data into.
     */
    @Nullable
    private String viewName;

    @Nullable
    private String version;

    @Override
    public void initialize(UimaContext uimaContext, ProcessingResourceMetaData processingResourceMetaData) {
        LOGGER.info("Initializing xml validating file adapter.");
        version = processingResourceMetaData.getVersion();
    }

    @Override
    public void adaptFile(CAS cas, Path path) throws CollectionException, IOException {
        if (cas == null) {
            LOGGER.error("Null CAS");
            throw new IllegalArgumentException("CAS was null");
        }

        LOGGER.info("Reading text from: {} into a CAS view: {}", path, viewName);
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

        StringBuilder stringBuilder = new StringBuilder();
        try (Reader stringReader = Files.newBufferedReader(path, StandardCharsets.US_ASCII)) {
            int ch;
            while ((ch = stringReader.read()) != -1) {
                if (isValid(ch)) {
                    stringBuilder.append((char) ch);
                } else {
                    int len = stringBuilder.length();
                    LOGGER.warn("Illegal rtf character with code point: {} at {} in {}", ch, len, path.toString());
                    IllegalXmlCharacter illegalXmlCharacter = new IllegalXmlCharacter(targetView, len, len);
                    illegalXmlCharacter.setValue(ch);
                    illegalXmlCharacter.addToIndexes();
                }
            }
        }

        String documentText = stringBuilder.toString();
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

    private static boolean isValid(int ch) {
        return (ch >= 0x20 && ch <= 0x7F) || ch == 0x09 || ch == 0x0A || ch == 0x0D;
    }
}
