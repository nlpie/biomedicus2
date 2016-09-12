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

package edu.umn.biomedicus.common.standard;

import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.exc.BiomedicusException;

import javax.annotation.Nullable;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class StandardDocument implements Document {
    private final Map<String, String> metadata = new HashMap<>();
    private final String text;
    private String documentId;

    public StandardDocument(String text) {
        this.text = text;
    }

    @Override
    public Reader getReader() {
        return new StringReader(text);
    }

    @Override
    public String getText() {
        return text;
    }

    @Nullable
    @Override
    public String getDocumentId() {
        return documentId;
    }

    @Override
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @Nullable
    @Override
    public String getMetadata(String key) throws BiomedicusException {
        return metadata.get(key);
    }

    @Override
    public void setMetadata(String key, String value) throws BiomedicusException {
        metadata.put(key, value);
    }

    @Override
    public Document getSiblingDocument(String identifier) throws BiomedicusException {
        return null;
    }

    @Override
    public <T> Labels<T> labels(Class<T> labelClass) {
        return null;
    }

    @Override
    public <T> Labeler<T> labeler(Class<T> labelClass) {
        return null;
    }
}
