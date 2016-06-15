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

package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.common.text.ParseToken;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.type.SentenceAnnotation;
import edu.umn.biomedicus.type.TermAnnotation;
import edu.umn.biomedicus.uima.common.Views;
import edu.umn.biomedicus.uima.type1_5.*;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Utility class for adapting the UIMA backend to the Biomedicus type system.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class UimaAdapters {

    private UimaAdapters() {
    }

    public static Sentence sentenceAdapter(Annotation sentence) {
        if (sentence instanceof SentenceAnnotation) {
            try {
                return new SentenceAdapter(sentence.getCAS().getJCas(), (SentenceAnnotation) sentence);
            } catch (CASException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            throw new IllegalArgumentException("Annotation is not of type Sentence");
        }
    }

    public static Token tokenAdapter(Annotation token) {
        if (token instanceof edu.umn.biomedicus.uima.type1_5.ParseToken) {
            try {
                return new TokenAdapter(token.getCAS().getJCas(), ((edu.umn.biomedicus.uima.type1_5.ParseToken) token));
            } catch (CASException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            throw new IllegalArgumentException("Annotation is not of type Token");
        }
    }

    public static Term termAdapter(Annotation term) {
        if (term instanceof TermAnnotation) {
            return new TermAdapter((TermAnnotation) term);
        } else {
            throw new IllegalArgumentException("Annotation is not of type Term");
        }
    }

    /**
     * Creates a Biomedicus {@link Document} implementation using the data stored in the
     * UIMA "SystemView" view {@link Views#SYSTEM_VIEW}.
     *
     * @param initialView the _initialView, i.e. the JCas first passed to an annotator
     * @return newly instantiated {@code Document} object from the data stored in the SystemView.
     * @throws CASException
     */
    public static Document documentFromInitialView(JCas initialView) throws BiomedicusException {
        return documentFromView(initialView, Views.SYSTEM_VIEW);
    }

    /**
     * Creates a Biomedicus {@link Document} implementation using the data stored in the
     * UIMA "GoldView" view {@link Views#GOLD_VIEW}.
     *
     * @param initialView the _initialView, i.e. the JCas first passed to an annotator
     * @return newly instantiated {@code Document} object from the data stored in the GoldView.
     * @throws CASException
     */
    public static Document goldDocumentFromInitialView(JCas initialView) throws BiomedicusException {
        return documentFromView(initialView, Views.SYSTEM_VIEW);
    }

    /**
     * Creates a Biomedicus {@link Document} implementation using the data stored in the
     * an arbitrary UIMA view.
     *
     * @param initialView the _initialView, i.e. the JCas first passed to an annotator
     * @param viewName    the view to create a document from
     * @return newly instantiated {@code Document} object from the data stored in the specified view.
     * @throws BiomedicusException
     */
    public static Document documentFromView(JCas initialView, String viewName) throws BiomedicusException {
        if (CAS.NAME_DEFAULT_SOFA.equals(viewName)) {
            throw new IllegalArgumentException("Cannot create document from _initialView");
        }
        JCas view;
        try {
            view = initialView.getView(viewName);
        } catch (CASException e) {
            throw new BiomedicusException(e);
        }
        return new JCasDocument(view);
    }

    /**
     * Returns a biomedicus {@link TextSpan} by wrapping the UIMA {@link Annotation}.
     *
     * @param annotation UIMA annotation.
     * @return biomedicus textspan.
     */
    public static TextSpan textSpanFromAnnotation(Annotation annotation) {
        return new AnnotationTextSpan<>(annotation);
    }
}
