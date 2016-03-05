package edu.umn.biomedicus.application;

import edu.umn.biomedicus.exc.BiomedicusException;

/**
 *
 */
public interface PostProcessor {
   void afterProcessing() throws BiomedicusException;
}
