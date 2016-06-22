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

package edu.umn.biomedicus.uima.labels;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.uima.adapter.JCasDocument;
import org.apache.uima.jcas.JCas;

import java.util.Map;

@Singleton
public class UimaLabelsFactory {
    private final Map<Class, LabelAdapter> labelAdapterMap;

    @Inject
    public UimaLabelsFactory(Map<Class, LabelAdapter> labelAdapterMap) {
        this.labelAdapterMap = labelAdapterMap;
    }

    public <T> Labels<T> labelsForDocument(Class<T> labelClass, Document document) {
        if (!(document instanceof JCasDocument)) {
            throw new IllegalArgumentException("Document is not jCas document.");
        }
        JCasDocument jCasDocument = (JCasDocument) document;
        JCas view = jCasDocument.getView();
        @SuppressWarnings("unchecked")
        LabelAdapter<T, ?> labelAdapter = (LabelAdapter<T, ?>) labelAdapterMap.get(labelClass);
        return new UimaLabels<>(view, labelAdapter);
    }
}
