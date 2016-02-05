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

import edu.umn.biomedicus.type.NewLineAnnotation;
import edu.umn.biomedicus.uima.Views;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds NewLine characters and annotates them.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class NewLineAnnotator extends JCasAnnotator_ImplBase {
    /**
     * Pattern that matches new lines.
     */
    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\n");

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        JCas systemView;
        try {
            systemView = jCas.getView(Views.SYSTEM_VIEW);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        String documentText = systemView.getDocumentText();

        Matcher newLineMatcher = NEW_LINE_PATTERN.matcher(documentText);

        while (newLineMatcher.find()) {
            new NewLineAnnotation(systemView, newLineMatcher.start(), newLineMatcher.end()).addToIndexes();
        }
    }
}
