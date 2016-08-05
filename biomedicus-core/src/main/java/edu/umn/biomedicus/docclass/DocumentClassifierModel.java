package edu.umn.biomedicus.docclass;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;

/**
 * Generic interface for document classification
 *
 * Created by gpfinley on 8/4/16.
 */
public interface DocumentClassifierModel {

    /**
     * Return the hypothesized class for this document
     * @param document the document
     * @return a String identifying the class
     * @throws BiomedicusException
     */
    String predict(Document document) throws BiomedicusException;

    /**
     * Return the key for metadata (how document classes are stored)
     * @return the name of this classification type
     */
    String getMetadataKey();

}