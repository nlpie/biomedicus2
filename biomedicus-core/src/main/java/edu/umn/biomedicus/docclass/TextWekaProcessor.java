package edu.umn.biomedicus.docclass;

import edu.umn.biomedicus.common.text.Document;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Interface for converting documents to Weka Instance objects
 *
 * Created by gpfinley on 8/4/16.
 */
public interface TextWekaProcessor {

    /**
     * Pass in a document for training
     * @param document a single document
     */
    void addTrainingDocument(Document document);

    /**
     * Call this after adding all documents(to build a text model, etc.)
     * @return an Instances object containing all training data
     */
    Instances getTrainingData();

    /**
     * Probably used at test time
     * @param document a single document to classify
     * @return an Instance to pass to a classifier
     */
    Instance getTestData(Document document);
}
