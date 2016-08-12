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

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.text.Span;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;

public abstract class AbstractLabelAdapter<T> implements LabelAdapter<T> {
    protected final CAS cas;
    protected final Type type;

    protected AbstractLabelAdapter(CAS cas, Type type) {
        this.cas = cas;
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public AnnotationFS labelToAnnotation(Label<T> label) {
        AnnotationFS annotation = cas.createAnnotation(type, label.getBegin(),
                label.getEnd());
        fillAnnotation(label, annotation);
        cas.addFsToIndexes(annotation);
        return annotation;
    }

    protected void fillAnnotation(Label<T> label, AnnotationFS annotationFS) {

    }

    @Override
    public Label<T> annotationToLabel(AnnotationFS annotationFS) {
        T labelValue = createLabelValue(annotationFS);
        return new Label<>(new Span(annotationFS.getBegin(), annotationFS.getEnd()), labelValue);
    }

    protected abstract T createLabelValue(FeatureStructure featureStructure);
}
