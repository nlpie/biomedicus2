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

package edu.umn.biomedicus.framework;

import edu.umn.biomedicus.framework.store.DefaultTextView;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
public class DefaultDocument implements Document {
    private final Map<String, String> metadata;
    private final Map<String, DefaultTextView> views;

    private final String documentId;

    public DefaultDocument(Map<String, String> metadata,
                           Map<String, DefaultTextView> views,
                           String documentId) {
        this.metadata = metadata;
        this.views = views;
        this.documentId = documentId;
    }

    public DefaultDocument(String documentId) {
        this.documentId = documentId;
        metadata = new HashMap<>();
        views = new HashMap<>();
    }

    @Override
    public String getDocumentId() {
        return documentId;
    }

    @Override
    public Optional<String> getMetadata(String key) {
        return Optional.ofNullable(metadata.get(key));
    }

    @Override
    public Map<String, String> getAllMetadata() {
        return new HashMap<>(metadata);
    }

    @Override
    public void putMetadata(String key, String value) {
        metadata.put(key, value);
    }

    @Override
    public void putAllMetadata(Map<String, String> metadata) {
        this.metadata.putAll(metadata);
    }

    @Override
    public TextView createTextView(String name, String text) {
        DefaultTextView textView = new DefaultTextView(text);
        views.put(name, textView);
        return textView;
    }

    @Override
    public Optional<TextView> getTextView(String name) {
        return Optional.ofNullable(views.get(name));
    }
}
