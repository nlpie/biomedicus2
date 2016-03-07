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

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.type.ClinicalNoteAnnotation;
import edu.umn.biomedicus.uima.Views;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.cas.TOP;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Utility class for file name providers.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public final class FileNameProviders {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Private constructor to prevent instantiation.
     */
    private FileNameProviders() {
        throw new UnsupportedOperationException();
    }

    /**
     * Constructor which takes a potentially null identifier and forms a document id, using a UUID if the identifier is
     * null.
     *
     * @param identifier document identifier
     * @param extension  extension to add, should include the separator e.g. ".".
     * @return return new DocumentIdFileNameProvider with the created file name.
     */
    private static FileNameProvider withPotentiallyNullIdentifier(@Nullable String identifier, String extension) {
        String documentId = identifier == null ? "unidentified-" + UUID.randomUUID().toString() : identifier;
        String fileName = documentId + extension;
        return new BaseFileNameProvider(fileName);
    }

    /**
     * Provides a file name provider from the system view of a JCas document.
     *
     * @param systemView the systemView JCas document
     * @param extension  the extension to name the file, should include the separator e.g. ".".
     * @return new file name provider
     */
    public static FileNameProvider fromSystemView(JCas systemView, String extension) {
        String documentIdentifier = null;
        JFSIndexRepository jfsIndexRepository = systemView.getJFSIndexRepository();
        if (jfsIndexRepository != null) {
            FSIterator<TOP> clinicalNotes = jfsIndexRepository.getAllIndexedFS(ClinicalNoteAnnotation.type);
            if (clinicalNotes.hasNext()) {
                @SuppressWarnings("unchecked")
                ClinicalNoteAnnotation clinicalNoteAnnotation = (ClinicalNoteAnnotation) clinicalNotes.next();
                if (clinicalNoteAnnotation != null) {
                    documentIdentifier = clinicalNoteAnnotation.getDocumentId();
                } else {
                    LOGGER.warn("Clinical note annotation was null.");
                }
            }
        }
        return FileNameProviders.withPotentiallyNullIdentifier(documentIdentifier, extension);
    }

    /**
     * Creates a file name provider given the initial view of a jCas passed to a ae_writer
     *
     * @param jCas      jCas initial view
     * @param extension extension to use, should include the separator e.g. ".".
     * @return a newly initialized file name provider.
     * @throws BiomedicusException if we fail to get the system view
     */
    public static FileNameProvider fromInitialView(JCas jCas, String extension) throws BiomedicusException {
        JCas systemView;
        try {
            systemView = jCas.getView(Views.SYSTEM_VIEW);
        } catch (CASException e) {
            throw new BiomedicusException(e);
        }
        return FileNameProviders.fromSystemView(systemView, extension);
    }

    /**
     * Creates a file name provider from a biomedicus {@link Document} type.
     *
     * @param document  the document to pull the document identifier from.
     * @param extension the extension to give the file name
     * @return the file name provider which gives the file name.
     */
    public static FileNameProvider fromDocument(Document document, String extension) {
        String documentIdentifier = document.getIdentifier();
        return FileNameProviders.withPotentiallyNullIdentifier(documentIdentifier, extension);
    }
}
