package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.model.text.Document;

/**
 *
 */
@DocumentScoped
public class AcronymVectorModelTrainerProcessor implements DocumentProcessor {

    private final Document document;

    private final AcronymVectorModelTrainer acronymVectorModelTrainer;

    @Inject
    public AcronymVectorModelTrainerProcessor(Document document, AcronymVectorModelTrainer acronymVectorModelTrainer) {
        this.document = document;
        this.acronymVectorModelTrainer = acronymVectorModelTrainer;
    }

    @Override
    public void process() throws BiomedicusException {
        acronymVectorModelTrainer.addDocumentToModel(document);
    }
}
