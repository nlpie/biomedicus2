package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.application.PostProcessor;
import edu.umn.biomedicus.model.text.Document;

/**
 * Interface for a trainer of an acronym model
 *
 * Only needs to be able to add in new documents and return a model (probably for serialization)
 * Some model types do a lot of the heavy lifting during the getModel() phase, after all docs have been added
 *
 * Created by gpfinley on 10/30/15.
 */
public interface AcronymModelTrainer extends PostProcessor {

    AcronymModel getModel();

    void addDocumentToModel(Document document);

}
