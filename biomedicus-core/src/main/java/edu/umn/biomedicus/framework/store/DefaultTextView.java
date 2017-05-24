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

package edu.umn.biomedicus.framework.store;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class DefaultTextView implements TextView {
    private final String text;
    private final Map<Class<?>, SpansMap<?>> spanMaps
            = new HashMap<>();

    public DefaultTextView(String text) {
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> LabelIndex<T> getLabelIndex(Class<T> labelClass) {
        SpansMap<T> spansMap = (SpansMap<T>) spanMaps.get(labelClass);
        return new DefaultLabelIndex<>(spansMap);
    }

    @Override
    public <T> Labeler<T> getLabeler(Class<T> labelClass) {
        return new Labeler<T>() {
            SpansMap<T> backingMap = new OrderedSpanMap<>();

            @Override
            public ValueLabeler value(T value) {
                return null;
            }

            @Override
            public void label(Label<T> label) {

            }

            @Override
            public void finish() {

            }
        };
    }

    @Override
    public Span getDocumentSpan() {
        return new Span(0, text.length());
    }
}
