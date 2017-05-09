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

import edu.umn.biomedicus.framework.store.TextLocation;
import edu.umn.biomedicus.rtf.reader.IndexListener;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;

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
    private final CAS originalDocumentView;
    private final Type viewIndexType;
    private final Feature destinationNameFeature;
    private final Feature destinationIndexFeature;

    /**
     * Creates an index listener which creates ViewIndex annotations.
     *
     * @param originalDocumentView the view that the original rtf document is stored in.
     */
    CasIndexListener(CAS originalDocumentView) {
        this.originalDocumentView = originalDocumentView;
        viewIndexType = originalDocumentView.getTypeSystem()
                .getType("edu.umn.biomedicus.rtfuima.type.ViewIndex");
        destinationIndexFeature = viewIndexType
                .getFeatureByBaseName("destinationIndex");
        destinationNameFeature = viewIndexType
                .getFeatureByBaseName("destinationName");
    }

    @Override
    public void wroteToDestination(String destinationName,
                                   int destinationIndex,
                                   TextLocation originalDocumentTextLocation) {
        AnnotationFS viewIndex = originalDocumentView
                .createAnnotation(viewIndexType,
                        originalDocumentTextLocation.getBegin(),
                        originalDocumentTextLocation.getEnd());
        viewIndex.setStringValue(destinationNameFeature, destinationName);
        viewIndex.setIntValue(destinationIndexFeature, destinationIndex);
        originalDocumentView.addFsToIndexes(viewIndex);
    }
}
