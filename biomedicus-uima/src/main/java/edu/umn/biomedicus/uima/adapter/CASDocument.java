/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.uima.labels.LabelAdapters;
import org.apache.uima.cas.*;

import javax.annotation.Nullable;
import java.util.*;

/**
 *
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
final class CASDocument implements Document {
    @Nullable private final LabelAdapters labelAdapters;

    private final CAS view;
    private final CAS metadata;

    private final Type metadataType;
    private final Feature keyFeature;
    private final Feature valueFeature;

    private final String documentId;

    CASDocument(@Nullable LabelAdapters labelAdapters, CAS view) {
        this.labelAdapters = labelAdapters;
        this.view = view;

        TypeSystem typeSystem = view.getTypeSystem();
        metadataType = typeSystem
                .getType("edu.umn.biomedicus.uima.type1_5.DocumentMetadata");
        keyFeature = metadataType.getFeatureByBaseName("key");
        valueFeature = metadataType.getFeatureByBaseName("value");

        metadata = view.getView("metadata");
        Type idType = typeSystem
                .getType("edu.umn.biomedicus.uima.type1_5.DocumentId");
        Feature idFeat = idType.getFeatureByBaseName("documentId");
        documentId = metadata.getIndexRepository()
                .getAllIndexedFS(idType)
                .get()
                .getStringValue(idFeat);
    }

    CASDocument(@Nullable LabelAdapters labelAdapters,
                CAS view,
                String documentId) {
        this.labelAdapters = labelAdapters;
        this.view = view;

        TypeSystem typeSystem = view.getTypeSystem();
        metadataType = typeSystem
                .getType("edu.umn.biomedicus.uima.type1_5.DocumentMetadata");
        keyFeature = metadataType.getFeatureByBaseName("key");
        valueFeature = metadataType.getFeatureByBaseName("value");

        metadata = view.createView("metadata");
        Type idType = typeSystem
                .getType("edu.umn.biomedicus.uima.type1_5.DocumentId");
        Feature idFeat = idType.getFeatureByBaseName("documentId");
        this.documentId = documentId;
        FeatureStructure documentIdFs = metadata.createFS(idType);
        documentIdFs.setStringValue(idFeat, documentId);
        metadata.addFsToIndexes(documentIdFs);
    }

    @Override
    public String getDocumentId() {
        return documentId;
    }

    @Nullable
    private FeatureStructure getMapEntry(String key) {
        FSIterator<FeatureStructure> fsIterator = view.getIndexRepository()
                .getAllIndexedFS(metadataType);
        while (fsIterator.hasNext()) {
            FeatureStructure featureStructure = fsIterator.next();
            String curKey = featureStructure.getStringValue(keyFeature);
            if (Objects.equals(curKey, key)) {
                return featureStructure;
            }
        }
        return null;
    }

    @Override
    public Optional<String> getMetadata(String key) {
        FeatureStructure mapEntry = getMapEntry(key);
        if (mapEntry == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapEntry.getStringValue(valueFeature));
    }

    @Override
    public Map<String, String> getAllMetadata() {
        Map<String, String> returnVal = new HashMap<>();
        FSIterator<FeatureStructure> fsIterator = view.getIndexRepository()
                .getAllIndexedFS(metadataType);
        while (fsIterator.hasNext()) {
            FeatureStructure featureStructure = fsIterator.next();
            String key = featureStructure.getStringValue(keyFeature);
            String val = featureStructure.getStringValue(valueFeature);
            returnVal.put(key, val);
        }

        return returnVal;
    }

    @Override
    public void putMetadata(String key, String value) {
        FeatureStructure mapEntry = getMapEntry(key);
        if (mapEntry != null) {
            view.removeFsFromIndexes(mapEntry);
        } else {
            mapEntry = view.createFS(metadataType);
            mapEntry.setStringValue(keyFeature, key);
        }
        mapEntry.setStringValue(valueFeature, value);
        view.addFsToIndexes(mapEntry);
    }

    @Override
    public void putAllMetadata(Map<String, String> metadata) {
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            putMetadata(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public TextView createTextView(String name, String text) {
        CAS textView = view.createView(name);
        textView.setDocumentText(text);
        return new CASTextView(textView, labelAdapters);
    }

    @Override
    public Optional<TextView> getTextView(String name) {
        Iterator<CAS> it = view.getViewIterator();
        while (it.hasNext()) {
            CAS view = it.next();
            if (view.getViewName().equals(name)) {
                return Optional.of(new CASTextView(view, labelAdapters));
            }
        }
        return Optional.empty();
    }
}
