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

import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.model.text.Sentence;
import edu.umn.biomedicus.model.text.Token;
import edu.umn.biomedicus.tnt.TntModel;
import edu.umn.biomedicus.tnt.TntModelTrainer;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import javax.annotation.Nullable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

/**
 * Trainer for the TnT part of speech tagger.
 *
 * @author Ben Knoll
 * @see edu.umn.biomedicus.tnt.TntModelTrainer
 * @since 1.3.0
 */
public class TntPosTrainerAE extends JCasAnnotator_ImplBase {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * UIMA parameter for the maximum suffix length.
     */
    private static final String PARAM_MAX_SUFFIX_LENGTH = "maxSuffixLength";

    /**
     * UIMA parameter for the maximum word frequency.
     */
    private static final String PARAM_SUFFIX_MAX_WORD_FREQUENCY = "maxWordFrequency";

    /**
     * UIMA parameter for whether to use capitalization.
     */
    private static final String PARAM_USE_CAPITALIZATION = "useCapitalization";

    /**
     * UIMA parameter for the view name to use for training.
     */
    private static final String PARAM_VIEW_NAME = "viewName";

    /**
     * UIMA parameter determining if we should restrict to the open class.
     */
    private static final String PARAM_RESTRICT_TO_OPEN_CLASS = "restrictToOpenClass";

    /**
     * UIMA parameter for if we should use the MSL suffix model
     */
    private static final String PARAM_USE_MSL_SUFFIX_MODEL = "useMslSuffixModel";

    /**
     * UIMA parameter for the output file.
     */
    private static final String PARAM_OUTPUT_FILE = "outputFile";

    /**
     * The view name to use for training.
     */
    @Nullable
    private String viewName;

    /**
     * The output file.
     */
    @Nullable
    private String outputFile;

    /**
     * The trainer.
     */
    @Nullable
    private TntModelTrainer tntTrainer;

    /**
     * The number of documents removed because they contained unrecognizable parts of speech.
     */
    private int tossed = 0;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        tossed = 0;

        viewName = (String) aContext.getConfigParameterValue(PARAM_VIEW_NAME);
        int maxSuffixLength = (int) aContext.getConfigParameterValue(PARAM_MAX_SUFFIX_LENGTH);
        int maxWordFrequency = (int) aContext.getConfigParameterValue(PARAM_SUFFIX_MAX_WORD_FREQUENCY);
        outputFile = (String) aContext.getConfigParameterValue(PARAM_OUTPUT_FILE);
        boolean useCapitalization = (boolean) aContext.getConfigParameterValue(PARAM_USE_CAPITALIZATION);
        boolean useMslSuffixModel = (boolean) aContext.getConfigParameterValue(PARAM_USE_MSL_SUFFIX_MODEL);
        boolean restrictToOpenClass = (boolean) aContext.getConfigParameterValue(PARAM_RESTRICT_TO_OPEN_CLASS);

        tntTrainer = TntModelTrainer.builder()
                .maxSuffixLength(maxSuffixLength)
                .maxWordFrequency(maxWordFrequency)
                .useCapitalization(useCapitalization)
                .useMslSuffixModel(useMslSuffixModel)
                .restrictToOpenClass(restrictToOpenClass)
                .build();
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        assert viewName != null;
        assert tntTrainer != null;

        Document jCasDocument = UimaAdapters.documentFromView(aJCas, viewName);

        for (Sentence sentence : jCasDocument.getSentences()) {
            tntTrainer.addSentence(sentence);
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        assert tntTrainer != null;
        assert outputFile != null;
        LOGGER.info("Finished processing documents for tnt trainer. We threw away {} documents because of unrecognized parts of speech.", tossed);

        TntModel tntModel = tntTrainer.createModel();

        try {
            tntModel.write(Paths.get(outputFile));
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }

        super.collectionProcessComplete();
    }
}
