package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.model.text.Document;

/**
 *
 */
public class AcronymProcessor implements DocumentProcessor {
    private final Document document;

    @Inject
    public AcronymProcessor(Document document) {
        this.document = document;
    }

    @Override
    public void process() throws BiomedicusException {

    }
}
