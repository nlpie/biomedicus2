/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.common;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;

/**
 * A list of UIMA CAS views.
 */
public final class Views {
    private Views() { }

    /**
     * The system view is the primary view used for document text and nlp annotations.
     */
    public static final String SYSTEM_VIEW = "SystemView";

    /**
     * The gold view is used for the gold annotations for evaluation
     */
    public static final String GOLD_VIEW = "GoldView";

    /**
     * The Augmented document view is used for documents modified during processing
     */
    public static final String AUGMENTED_DOCUMENT_VIEW = "AugmentedDocumentView";

    /**
     * The original document view is used for the original document.
     */
    public static final String ORIGINAL_DOCUMENT_VIEW = "OriginalDocumentView";

    public static JCas getSystemView(JCas jCas) throws AnalysisEngineProcessException {
        try {
            return jCas.getView(Views.SYSTEM_VIEW);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
