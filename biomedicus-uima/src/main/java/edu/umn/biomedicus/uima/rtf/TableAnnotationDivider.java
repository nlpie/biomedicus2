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

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Responsible for dividing annotations using 0-length marker annotations within those annotations.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class TableAnnotationDivider {
    /**
     * View to work in.
     */
    private final JCas jCas;

    /**
     * CAS version of view.
     */
    private final CAS cas;

    /**
     * Annotations to divide.
     */
    @Nullable
    private AnnotationIndex<Annotation> annotations;

    /**
     * Annotations to use as divisions.
     */
    @Nullable
    private AnnotationIndex<Annotation> dividers;

    /**
     * Divided annotations type to create.
     */
    @Nullable
    private Type typeToCreate;

    /**
     * Initializes using a view.
     *
     * @param jCas the view.
     */
    private TableAnnotationDivider(JCas jCas) {
        this.jCas = jCas;
        cas = jCas.getCas();
    }

    static TableAnnotationDivider in(JCas jCas) {
        return new TableAnnotationDivider(jCas);
    }

    /**
     * Sets the type to divide.
     *
     * @param annotationType type to divide.
     * @return this object
     */
    TableAnnotationDivider divide(int annotationType) {
        annotations = jCas.getAnnotationIndex(annotationType);
        return this;
    }

    /**
     * Sets the type to use for division.
     *
     * @param dividerType type code for the dividing annotation type.
     * @return this object
     */
    TableAnnotationDivider using(int dividerType) {
        dividers = jCas.getAnnotationIndex(dividerType);
        return this;
    }

    /**
     * Sets the type to create.
     *
     * @param typeCode type code for the type to create;
     * @return this object
     */
    TableAnnotationDivider into(int typeCode) {
        typeToCreate = jCas.getCasType(typeCode);
        return this;
    }

    /**
     * Runs the divider.
     */
    void execute() {
        Objects.requireNonNull(annotations);

        annotations.forEach(this::divideAnnotation);

        annotations = null;
        dividers = null;
    }

    private void divideAnnotation(Annotation annotation) {
        Objects.requireNonNull(typeToCreate);
        Objects.requireNonNull(dividers);

        FSIterator<Annotation> subiterator = dividers.subiterator(annotation);
        int begin = annotation.getBegin();
        while (subiterator.hasNext()) {
            int end = subiterator.next().getBegin();
            cas.addFsToIndexes(cas.createAnnotation(typeToCreate, begin, end));
            begin = end;
        }
    }


}
