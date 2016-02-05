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

package edu.umn.biomedicus.tools.newinfo;

import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.model.text.Token;
import edu.umn.biomedicus.type.NewInformationAnnotation;
import edu.umn.biomedicus.uima.Views;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * Adds new information annotations to the UIMA CAS documents.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class NewInformationAnnotator extends JCasAnnotator_ImplBase {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * UIMA parameter for the directory of new info files.
     */
    private static final String PARAM_NEW_INFO_DIRECTORY = "newInfoDirectory";

    /**
     * The directory of new information files.
     */
    @Nullable
    private Path newInfoDirectory;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        LOGGER.info("Initializing new information annotator.");

        newInfoDirectory = Paths.get((String) aContext.getConfigParameterValue(PARAM_NEW_INFO_DIRECTORY));
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        assert newInfoDirectory != null;
        LOGGER.info("Processing new information output into annotations.");

        JCas systemView = Views.getSystemView(jCas);

        Document document = UimaAdapters.documentFromInitialView(jCas);

        Iterator<Token> tokenIterator = document.getTokens().iterator();
        int currToken = -1;
        Token token = null;

        Path newInformationFile = newInfoDirectory.resolve(document.getIdentifier() + ".txt");

        if (Files.notExists(newInformationFile)) {
            LOGGER.warn("File does not exist: {}", newInformationFile.toString());
            return;
        }

        try (BufferedReader bufferedReader = Files.newBufferedReader(newInformationFile)) {
            Iterator<String[]> newInfoIterator = bufferedReader.lines()
                    .map(line -> line.split("\\t"))
                    .iterator();
            while (newInfoIterator.hasNext()) {
                String[] newInfo = newInfoIterator.next();
                int tokenIndex = Integer.parseInt(newInfo[1]);

                while (currToken != tokenIndex) {
                    token = tokenIterator.next();
                    currToken++;
                }

                NewInformationAnnotation newInformationAnnotation = new NewInformationAnnotation(systemView,
                        token.getBegin(), token.getEnd());
                if (newInfo.length == 5) {
                    newInformationAnnotation.setKind(newInfo[4]);
                }
                newInformationAnnotation.addToIndexes();
            }
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
