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

package edu.umn.biomedicus.uima.sentence;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.opennlp.OpenNlpSentenceTrainer;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * UIMA annotator for training OpenNLP
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class OpenNlpSentenceTrainerAnnotator extends JCasAnnotator_ImplBase {
    /**
     * UIMA parameter for the abbreviations file.
     */
    public static final String PARAM_ABBREVS_FILE = "abbrevsFile";

    /**
     * UIMA parameter for where to write the model to.
     */
    public static final String PARAM_OUTPUT_FILE = "outputFile";

    /**
     * UIMA parameter for what characters to use for EOS events.
     */
    public static final String PARAM_EOS_CHARS = "eosChars";

    /**
     * The trainer.
     */
    private OpenNlpSentenceTrainer trainer;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        Path abbrevsFile = Paths.get((String) aContext.getConfigParameterValue(PARAM_ABBREVS_FILE));

        Path outputFile = Paths.get((String) aContext.getConfigParameterValue(PARAM_OUTPUT_FILE));

        String[] eosStrings = (String[]) aContext.getConfigParameterValue(PARAM_EOS_CHARS);
        char[] eosChars = new char[eosStrings.length];
        for (int i = 0; i < eosStrings.length; i++) {
            eosChars[i] = eosStrings[i].charAt(0);
        }

        try {
            trainer = new OpenNlpSentenceTrainer(abbrevsFile, outputFile, eosChars);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        Document document;
        try {
            document = UimaAdapters.documentFromInitialView(aJCas);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        try {
            trainer.addDocument(document);
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        super.collectionProcessComplete();

        try {
            trainer.finish();
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
