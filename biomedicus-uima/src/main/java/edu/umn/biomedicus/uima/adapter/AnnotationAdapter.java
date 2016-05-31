/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.uima.labels.FSIteratorAdapter;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Base class for adapting an UIMA annotation to the Biomedicus type system.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class AnnotationAdapter<T extends Annotation> extends AnnotationTextSpan<T> {
    /**
     * The {@code JCas} in which the annotation is stored.
     */
    private final JCas jCas;

    /**
     * Protected constructor for AnnotationAdapter. Initializes the two fields, {@code jCas} and {@code annotation}.
     *
     * @param jCas       the {@link JCas} document the annotation is stored in.
     * @param annotation the {@link Annotation} itself.
     */
    AnnotationAdapter(JCas jCas, T annotation) {
        super(annotation);
        this.jCas = jCas;
    }

    /**
     * Returns the jCas for use by subclasses.
     *
     * @return JCas object the annotation is stored in.
     */
    protected JCas getJCas() {
        return jCas;
    }

    /**
     * Gets a stream of annotations within this annotation using the JCas {@link AnnotationIndex#subiterator(AnnotationFS)}
     * functionality.
     *
     * @param type    the UIMA type of annotations to return, the static {@code .type} variable on any generated JCas class.
     * @param adapter the adapter to wrap the Annotation in.
     * @param <U>     the biomedicus type that the annotations
     * @return stream of annotations that are within the bounds of this annotation
     */
    <U> Stream<U> getCoveredStream(int type, Function<Annotation, U> adapter) {
        AnnotationIndex<Annotation> index = jCas.getAnnotationIndex(type);
        Iterable<U> iterable = () -> FSIteratorAdapter.coveredIteratorAdapter(index, annotation, adapter);
        return StreamSupport.stream(iterable.spliterator(), false);
    }

}
