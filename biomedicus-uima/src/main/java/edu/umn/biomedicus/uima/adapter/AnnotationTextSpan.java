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

import edu.umn.biomedicus.common.text.TextSpan;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * An adapter for text span from UIMA annotations.
 *
 * @author Ben Knoll
 * @since 1.4
 */
class AnnotationTextSpan<T extends Annotation> implements TextSpan {
    /**
     * The annotation itself.
     */
    protected final T annotation;

    AnnotationTextSpan(T annotation) {
        this.annotation = annotation;
    }

    /**
     * Returns the annotation for use by subclasses.
     *
     * @return Annotation object itself.
     */
    protected T getAnnotation() {
        return annotation;
    }

    @Override
    public String getText() {
        return annotation.getCoveredText();
    }

    @Override
    public int getBegin() {
        return annotation.getBegin();
    }

    @Override
    public int getEnd() {
        return annotation.getEnd();
    }
}
