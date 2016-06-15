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
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;
import java.util.function.Function;

class LabelAdapter<T, U extends Annotation> {
    private final Class<U> jCasClass;
    private final BiConsumer<T, U> forwardAdapter;
    private final Function<U, T> reverseAdapter;
    private final Constructor<U> constructor;

    LabelAdapter(Class<U> jCasClass,
                 BiConsumer<T, U> forwardAdapter,
                 Function<U, T> reverseAdapter) {
        this.jCasClass = jCasClass;
        this.forwardAdapter = forwardAdapter;
        this.reverseAdapter = reverseAdapter;
        try {
            constructor = jCasClass.getConstructor(JCas.class, Integer.TYPE, Integer.TYPE);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("This method should always be there for a JCas cover class");
        }
    }

    Class<? extends Annotation> getjCasClass() {
        return jCasClass;
    }

    void createLabel(JCas jCas, Label<T> label) throws BiomedicusException {
        try {
            U annotation = constructor.newInstance(jCas, label.toSpan().getBegin(), label.toSpan().getEnd());
            forwardAdapter.accept(label.value(), annotation);
            annotation.addToIndexes();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new BiomedicusException("Could not create annotation for label", e);
        }
    }

    Label<T> adaptAnnotation(Annotation annotation) {
        if (!jCasClass.isInstance(annotation)) {
            throw new IllegalArgumentException("Annotation is invalid type");
        }
        T label = reverseAdapter.apply(jCasClass.cast(annotation));
        return new Label<>(new Span(annotation.getBegin(), annotation.getEnd()), label);
    }

    static <T> BuilderNeedsAnnotationClass<T> builder() {
        return new BuilderNeedsAnnotationClass<T>() {
            @Override
            public <U extends Annotation> BuilderNeedsLabelableAdapter<T, U> withAnnotationClass(Class<U> annotationClass) {
                return labelableAdapter -> annotationAdapter -> new LabelAdapter<>(annotationClass, labelableAdapter, annotationAdapter);
            }
        };
    }

    static <T> BuilderNeedsAnnotationClass<T> builder(Class<T> forClass) {
        return builder();
    }

    interface BuilderNeedsAnnotationClass<T> {
        <U extends Annotation> BuilderNeedsLabelableAdapter<T, U> withAnnotationClass(Class<U> annotationClass);
    }

    interface BuilderNeedsLabelableAdapter<T, U extends Annotation> {
        BuilderNeedsAnnotationAdapter<T, U> withLabelableAdapter(BiConsumer<T, U> labelableAdapter);

    }

    interface BuilderNeedsAnnotationAdapter<T, U extends Annotation> {
        LabelAdapter<T, U> withAnnotationAdapter(Function<U, T> annotationAdapter);
    }
}
