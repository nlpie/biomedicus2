/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.pos;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Evaluates for accuracy of part of speech tagging.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class PartOfSpeechEvaluator extends JCasAnnotator_ImplBase {
    /**
     * Uima parameter for view to use as gold view.
     */
    public static final String PARAM_GOLD_VIEW = "goldView";

    /**
     * Uima parameter for the view to write test data to.
     */
    public static final String PARAM_TEST_VIEW = "testView";

    /**
     * File to write the results to.
     */
    public static final String PARAM_OUTPUT_FILE = "outputFile";

    /**
     * File to write misses to.
     */
    public static final String PARAM_MISSES_FILE = "missesFile";

    /**
     * The writer for files.
     */
    private FileWriter fileWriter;

    /**
     * Evaluator.
     */
    @Nullable
    private DocumentPartOfSpeechEvaluator totals;

    /**
     * The path for the misses file.
     */
    @Nullable
    private String missesFile;

    /**
     * The name of the gold view.
     */
    @Nullable
    private String goldView;

    /**
     * The name of the test view.
     */
    @Nullable
    private String testView;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        String filePath = (String) aContext.getConfigParameterValue(PARAM_OUTPUT_FILE);
        File outputFile = Paths.get(filePath).toFile();
        try {
            fileWriter = new FileWriter(outputFile);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        missesFile = (String) aContext.getConfigParameterValue(PARAM_MISSES_FILE);

        try {
            DocumentPartOfSpeechEvaluator.writeHeader(fileWriter);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        totals = new DocumentPartOfSpeechEvaluator("Totals");

        goldView = (String) aContext.getConfigParameterValue(PARAM_GOLD_VIEW);

        testView = (String) aContext.getConfigParameterValue(PARAM_TEST_VIEW);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        assert testView != null;
        assert goldView != null;
        assert totals != null;

        Document systemDocument = UimaAdapters.documentFromView(aJCas, testView);
        Document goldDocument = UimaAdapters.documentFromView(aJCas, goldView);

        DocumentPartOfSpeechEvaluator documentPartOfSpeechEvaluator = DocumentPartOfSpeechEvaluator.create(systemDocument, goldDocument);
        documentPartOfSpeechEvaluator.evaluate();
        try {
            documentPartOfSpeechEvaluator.write(fileWriter);
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }

        totals.add(documentPartOfSpeechEvaluator);
    }

    @Override
    public void batchProcessComplete() throws AnalysisEngineProcessException {
        super.batchProcessComplete();


    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        assert totals != null;

        super.collectionProcessComplete();

        try {
            totals.write(fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }

        try (FileWriter missesWriter = new FileWriter(missesFile)) {
            totals.writeMisses(missesWriter);
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }

    }
}
