package edu.umn.biomedicus.application;

import edu.umn.biomedicus.exc.BiomedicusException;

/**
 * A base processor of a document.
 *
 * Subclasses of this are designed to be injected using Guice.
 *
 * @since 1.4.0
 */
public interface DocumentProcessor {
    /**
     * Processes the document.
     */
    void process() throws BiomedicusException;
}
