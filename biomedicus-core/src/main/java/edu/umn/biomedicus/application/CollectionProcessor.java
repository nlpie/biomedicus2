package edu.umn.biomedicus.application;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;

/**
 *
 */
public interface CollectionProcessor {
    void processDocument(Document document) throws BiomedicusException;

    void allDocumentsProcessed() throws BiomedicusException;
}
