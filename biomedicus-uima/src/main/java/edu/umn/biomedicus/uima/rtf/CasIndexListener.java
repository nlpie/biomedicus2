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

package edu.umn.biomedicus.uima.rtf;

import edu.umn.biomedicus.common.text.SpanLike;
import edu.umn.biomedicus.rtf.reader.IndexListener;
import edu.umn.biomedicus.rtfuima.type.ViewIndex;
import org.apache.uima.jcas.JCas;

/**
 * Listens to for indices of characters that are written to.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class CasIndexListener implements IndexListener {
    /**
     * The view storing the Rtf document.
     */
    private final JCas originalDocumentView;

    /**
     * Creates an index listener which creates {@link ViewIndex} annotations.
     *
     * @param originalDocumentView the view that the original rtf document is stored in.
     */
    CasIndexListener(JCas originalDocumentView) {
        this.originalDocumentView = originalDocumentView;
    }

    @Override
    public void wroteToDestination(String destinationName, int destinationIndex, SpanLike originalDocumentSpanLike) {
        ViewIndex viewIndex = new ViewIndex(originalDocumentView, originalDocumentSpanLike.getBegin(), originalDocumentSpanLike.getEnd());
        viewIndex.setDestinationIndex(destinationIndex);
        viewIndex.setDestinationName(destinationName);
        viewIndex.addToIndexes();
    }
}
