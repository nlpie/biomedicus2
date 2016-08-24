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

import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.uima.labels.LabelAdapter;
import edu.umn.biomedicus.uima.labels.LabelAdapters;
import edu.umn.biomedicus.uima.labels.UimaLabeler;
import edu.umn.biomedicus.uima.labels.UimaLabels;
import org.apache.uima.cas.*;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

/**
 * UIMA implementation of the {@link Document} model in the BioMedICUS type system. Uses a
 * combination of {@link CAS} and the document id feature structure.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
final class CASDocument implements Document {
    private final CAS view;
    private final FeatureStructure documentIdFS;
    private final Feature documentIdFeature;
    private final Type documentMetadataType;
    private final Feature keyFeature;
    private final Feature valueFeature;
    private final LabelAdapters labelAdapters;

    /**
     * Default constructor. Instantiates a Document class backed up by a system view {@link CAS}, and the
     * DocumentId within that system view.
     *
     * @param view the {@link CAS} system view
     */
    CASDocument(CAS view, LabelAdapters labelAdapters) throws BiomedicusException {
        if (labelAdapters == null) {
            throw new IllegalArgumentException("labelAdapters was null.");
        }
        this.view = view;
        TypeSystem typeSystem = view.getTypeSystem();
        Type docIdType = typeSystem.getType("edu.umn.biomedicus.uima.type1_5.DocumentId");
        documentIdFeature = docIdType.getFeatureByBaseName("documentId");
        FSIterator<FeatureStructure> it = view.getIndexRepository().getAllIndexedFS(docIdType);
        if (it.hasNext()) {
            @SuppressWarnings("unchecked")
            FeatureStructure annotation = it.next();
            documentIdFS = annotation;
        } else {
            documentIdFS = view.createFS(docIdType);
            view.addFsToIndexes(documentIdFS);
        }
        documentMetadataType = typeSystem.getType("edu.umn.biomedicus.uima.type1_5.DocumentMetadata");
        keyFeature = documentMetadataType.getFeatureByBaseName("key");
        valueFeature = documentMetadataType.getFeatureByBaseName("value");
        this.labelAdapters = labelAdapters;
    }

    @Override
    public Reader getReader() {
        InputStream sofaDataStream = view.getSofaDataStream();
        return new BufferedReader(new InputStreamReader(sofaDataStream));
    }

    @Override
    public String getText() {
        return view.getDocumentText();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CASDocument that = (CASDocument) o;

        return view.equals(that.view);
    }

    @Override
    public int hashCode() {
        return view.hashCode();
    }


    @Nullable
    @Override
    public String getDocumentId() {
        return documentIdFS.getStringValue(documentIdFeature);
    }

    @Override
    public void setDocumentId(String documentId) {
        view.removeFsFromIndexes(documentIdFS);
        documentIdFS.setStringValue(documentIdFeature, documentId);
        view.addFsToIndexes(documentIdFS);
    }

    private FeatureStructure getMapEntry(String key) {
        FSIterator<FeatureStructure> fsIterator = view.getIndexRepository().getAllIndexedFS(documentMetadataType);
        while (fsIterator.hasNext()) {
            FeatureStructure featureStructure = fsIterator.next();
            if (Objects.equals(featureStructure.getStringValue(keyFeature), key)) {
                return featureStructure;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getMetadata(String key) throws BiomedicusException {
        FeatureStructure mapEntry = getMapEntry(key);
        if (mapEntry == null) {
            throw new BiomedicusException("Entry for key not found: " + key);
        }
        return mapEntry.getStringValue(valueFeature);
    }

    @Override
    public void setMetadata(String key, String value) throws BiomedicusException {
        FeatureStructure mapEntry = getMapEntry(key);
        if (mapEntry != null) {
            view.removeFsFromIndexes(mapEntry);
        } else {
            mapEntry = view.createFS(documentMetadataType);
            mapEntry.setStringValue(keyFeature, key);
        }
        mapEntry.setStringValue(valueFeature, value);
        view.addFsToIndexes(mapEntry);
    }

    @Override
    public Document getSiblingDocument(String identifier) throws BiomedicusException {
        CAS targetView = this.view.getView(identifier);
        return new CASDocument(targetView, labelAdapters);
    }

    @Override
    public <T> Labels<T> labels(Class<T> labelClass) {
        LabelAdapter<T> labelAdapter = labelAdapters.getLabelAdapterFactory(labelClass).create(view);
        return new UimaLabels<>(view, labelAdapter);
    }

    @Override
    public <T> Labeler<T> labeler(Class<T> labelClass) {
        LabelAdapter<T> labelAdapter = labelAdapters.getLabelAdapterFactory(labelClass).create(view);
        return new UimaLabeler<>(labelAdapter);
    }
}
