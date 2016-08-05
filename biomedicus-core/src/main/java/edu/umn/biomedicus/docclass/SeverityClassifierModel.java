package edu.umn.biomedicus.docclass;

import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.filters.Filter;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Classify documents based on symptom severity
 * Uses a Weka classifier with attribute selection
 *
 * Created by gpfinley on 7/28/16.
 */
@ProvidedBy(SeverityClassifierModel.Loader.class)
public class SeverityClassifierModel implements DocumentClassifierModel, Serializable {

    // For unknown classes (test data or poorly formatted training data)
    protected static final String UNK = "unknown";

    private final Classifier classifier;
    private final Filter attSel;
    private final TextWekaProcessor severityWekaProcessor;
    private final String keyName = "Severity";

    private final Map<Double, String> severityMap;

    /**
     * Initialize this model
     * All training happens in the trainer; just store what we need to keep for classification
     * @param classifier a Weka Classifier object
     * @param attSel an attribute selection object
     * @param severityWekaProcessor a processor to convert Document objects into Weka Instance objects
     * @throws BiomedicusException
     */
    public SeverityClassifierModel(Classifier classifier, Filter attSel, TextWekaProcessor severityWekaProcessor) throws BiomedicusException {
        severityMap = new HashMap<>();
        severityMap.put(0., "ABSENT");
        severityMap.put(1., "MILD");
        severityMap.put(2., "MODERATE");
        severityMap.put(3., "SEVERE");
        severityMap.put(4., UNK);
        this.classifier = classifier;
        this.attSel = attSel;
        this.severityWekaProcessor = severityWekaProcessor;
    }

    /**
     * Perform attribute selection and then classification using the stored Weka objects
     * @param document the document
     * @return a string (from the predefined classes) representing this document's symptom severity
     * @throws BiomedicusException
     */
    @Override
    public String predict(Document document) throws BiomedicusException {
        Instance inst = severityWekaProcessor.getTestData(document);
        double result;
        try {
            if(attSel.input(inst)) {
                inst = attSel.output();
                result = classifier.classifyInstance(inst);
            } else {
                throw new Exception();
            }
        } catch(Exception e) {
            throw new BiomedicusException();
        }
        return severityMap.get(result);
    }

    @Override
    public String getMetadataKey() {
        return keyName;
    }

    /**
     * Load a serialized model
     */
    @Singleton
    static class Loader extends DataLoader<SeverityClassifierModel> {

        private final Path modelPath;

        @Inject
        public Loader(@Setting("docclass.severity.model.path") Path modelPath) {
            this.modelPath = modelPath;
        }

        @Override
        protected SeverityClassifierModel loadModel() throws BiomedicusException {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath.toFile()));
                return (SeverityClassifierModel) ois.readObject();
            } catch(Exception e) {
                throw new BiomedicusException();
            }
        }
    }

}
