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

package edu.umn.biomedicus.uima.xmi;

import edu.umn.biomedicus.application.Biomedicus;
import edu.umn.biomedicus.uima.files.InputFileAdapter;
import edu.umn.biomedicus.uima.type1_5.DocumentId;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.*;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A simple {@link InputFileAdapter} that reads CASes in XMI format from a directory in the filesystem and initializes
 * the {@link edu.umn.biomedicus.uima.type1_5.DocumentId} object on the document.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class XmiInputFileAdapter implements InputFileAdapter {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(XmiInputFileAdapter.class);

    /**
     * Name of the configuration parameter that must be set to indicate if the execution fails if an encountered type is
     * unknown.
     */
    public static final String PARAM_FAIL_UNKNOWN = "failOnUnknownType";

    /**
     * Whether the process should fail if an unknown type is encountered when deserializing cas.
     */
    private Boolean failOnUnknownType;

    /**
     * Name of the configuration parameter which sets whether the document id from the file name is included in the CAS.
     */
    public static final String PARAM_ADD_DOCUMENT_ID = "addDocumentId";

    /**
     * Whether to add the document id from the name of the file.
     */
    private Boolean addDocumentId;

    @Override
    public void initialize(UimaContext uimaContext, ProcessingResourceMetaData processingResourceMetaData) {
        LOGGER.info("Initializing the xmi reader parameters");
        failOnUnknownType = (Boolean) uimaContext.getConfigParameterValue(PARAM_FAIL_UNKNOWN);
        addDocumentId = (Boolean) uimaContext.getConfigParameterValue(PARAM_ADD_DOCUMENT_ID);
    }

    @Override
    public void adaptFile(CAS cas, Path path) throws CollectionException {
        LOGGER.info("Deserializing an input stream into a cas");
        try (InputStream inputStream = Files.newInputStream(path)) {
            XmiCasDeserializer.deserialize(inputStream, cas, !(failOnUnknownType == null || failOnUnknownType));
        } catch (SAXException | IOException e) {
            LOGGER.error("Failed on document: {}", path);
            throw new CollectionException(e);
        }

        if (addDocumentId != null && addDocumentId) {
            JCas defaultView;
            try {
                defaultView = cas.getJCas().getView(Biomedicus.ViewIdentifiers.SYSTEM);
            } catch (CASException e) {
                throw new CollectionException(e);
            }
            DocumentId documentId = new DocumentId(defaultView);
            String docIdString = path.getFileName().toString();
            docIdString = docIdString.substring(0, docIdString.lastIndexOf('.'));
            documentId.setDocumentId(docIdString);
            documentId.addToIndexes();
        }
    }

    @Override
    public void setTargetView(String viewName) {
        // target view has no effect on XMI deserializer since all views serialized / deserialized together.
        throw new UnsupportedOperationException();
    }
}
