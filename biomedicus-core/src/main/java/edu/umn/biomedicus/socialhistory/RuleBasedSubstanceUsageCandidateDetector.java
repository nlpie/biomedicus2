package edu.umn.biomedicus.socialhistory;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.application.DocumentProcessor;

/**
 *
 */
@DocumentScoped
public class RuleBasedSubstanceUsageCandidateDetector implements DocumentProcessor {

    private final Document document;

    @Inject
    public RuleBasedSubstanceUsageCandidateDetector(Document document) {
        this.document = document;
    }


    @Override
    public void process() throws BiomedicusException {

    }
}
