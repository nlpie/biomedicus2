package edu.umn.biomedicus.docclass;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.ProcessorScoped;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.CollectionProcessor;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Train a Weka model to classify documents according to symptom severity
 * Created for the 2016 i2b2 NLP Shared Task
 *
 * Created by gpfinley on 7/28/16.
 */
@ProcessorScoped
public class SeverityClassifierTrainer implements CollectionProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeverityClassifierTrainer.class);

    private final Path outPath;
    private final TextWekaProcessor wekaProcessor;
    private final int ATTRIBUTES_TO_KEEP = 1000;
    private final int MIN_WORD_COUNT = 2;

    /**
     * Initialize this trainer. If the stopwords file is not present or can't be read from, trainer will still work
     * @param outPath the path to write the model to
     * @param stopWordsPath path to a stopwords file
     * @throws BiomedicusException
     */
    @Inject
    public SeverityClassifierTrainer(@Setting("docclass.severity.output.path") Path outPath, @Setting("docclass.stopwords.path") @Nullable Path stopWordsPath) throws BiomedicusException {
        Set<String> stopWords = null;
        if(stopWordsPath != null) {
            try {
                List<String> stopWordsList = Files.readAllLines(stopWordsPath);
                stopWords = new HashSet<>(stopWordsList);
            } catch (IOException e) {
                LOGGER.warn("Could not load stopwords file; will not exclude stopwords");
            }
        }
        this.outPath = outPath;
        wekaProcessor = new SeverityWekaProcessor(stopWords, MIN_WORD_COUNT, true);
    }

    /**
     * Add the document to the collection, which will be trained all at once at the end
     * @param document a document
     */
    @Override
    public void processDocument(Document document) {
        wekaProcessor.addTrainingDocument(document);
    }

    /**
     * Do training after all documents have been passed
     * Uses Weka's attribute selection and classifier libraries
     */
    @Override
    public void allDocumentsProcessed() throws BiomedicusException {

        Instances trainSet = wekaProcessor.getTrainingData();
        Classifier classifier = new NaiveBayesMultinomial();
        AttributeSelection sel = new AttributeSelection();
        ASEvaluation infogain = new InfoGainAttributeEval();
        Ranker ranker = new Ranker();
        Remove remove = new Remove();

        ranker.setNumToSelect(ATTRIBUTES_TO_KEEP);
        sel.setEvaluator(infogain);
        sel.setSearch(ranker);

        try {
            sel.SelectAttributes(trainSet);
            int[] selected = sel.selectedAttributes();
            remove.setInvertSelection(true);
            remove.setAttributeIndicesArray(selected);
            remove.setInputFormat(trainSet);
            trainSet = Filter.useFilter(trainSet, remove);
            classifier.buildClassifier(trainSet);
        } catch (Exception e) {
            throw new BiomedicusException();
        }

        DocumentClassifierModel model = new SeverityClassifierModel(classifier, remove, wekaProcessor);

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outPath.toFile()));
            oos.writeObject(model);
            oos.close();
        } catch(IOException e) {
            throw new BiomedicusException();
        }
    }

}
