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

import edu.umn.biomedicus.application.TextView;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.uima.common.Views;
import edu.umn.biomedicus.uima.labels.LabelAdapters;
import org.apache.uima.cas.CAS;

/**
 * Utility class for adapting the UIMA backend to the Biomedicus type system.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public final class UimaAdapters {

    private UimaAdapters() {
        throw new UnsupportedOperationException("Instantiation of utility class");
    }

    /**
     * Creates a Biomedicus {@link TextView} implementation using the data stored in the
     * UIMA "SystemView" view {@link Views#SYSTEM_VIEW}.
     *
     * @param initialView the _initialView, i.e. the JCas first passed to an annotator
     * @return newly instantiated {@code Document} object from the data stored in the SystemView.
     * @throws BiomedicusException
     */
    public static TextView documentFromInitialView(CAS initialView, LabelAdapters labelAdapters) throws BiomedicusException {
        return documentFromView(initialView, Views.SYSTEM_VIEW, labelAdapters);
    }

    /**
     * Creates a Biomedicus {@link TextView} implementation using the data stored in the
     * UIMA "GoldView" view {@link Views#GOLD_VIEW}.
     *
     * @param initialView the _initialView, i.e. the JCas first passed to an annotator
     * @return newly instantiated {@code Document} object from the data stored in the GoldView.
     * @throws BiomedicusException
     */
    public static TextView goldDocumentFromInitialView(CAS initialView, LabelAdapters labelAdapters) throws BiomedicusException {
        return documentFromView(initialView, Views.SYSTEM_VIEW, labelAdapters);
    }

    /**
     * Creates a Biomedicus {@link TextView} implementation using the data stored in the
     * an arbitrary UIMA view.
     *
     * @param initialView the _initialView, i.e. the JCas first passed to an annotator
     * @param viewName    the view to create a document from
     * @return newly instantiated {@code Document} object from the data stored in the specified view.
     * @throws BiomedicusException
     */
    public static TextView documentFromView(CAS initialView, String viewName, LabelAdapters labelAdapters) throws BiomedicusException {
        if (CAS.NAME_DEFAULT_SOFA.equals(viewName)) {
            throw new IllegalArgumentException("Cannot create document from _initialView");
        }
        CAS view = initialView.getView(viewName);
        return new CASTextView(view, labelAdapters);
    }

    /**
     * Creates a Biomedicus {@link TextView} implementation using the data stored in the
     * an arbitrary UIMA view.
     *
     * @param view the view to create a document from
     * @return newly instantiated {@code Document} object from the data stored in the specified view.
     * @throws BiomedicusException
     */
    public static TextView documentFromView(CAS view, LabelAdapters labelAdapters) throws BiomedicusException {
        return new CASTextView(view, labelAdapters);
    }
}
