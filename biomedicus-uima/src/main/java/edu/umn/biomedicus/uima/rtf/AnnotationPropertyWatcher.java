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

package edu.umn.biomedicus.uima.rtf;

import edu.umn.biomedicus.rtf.reader.State;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;

import javax.annotation.Nullable;

/**
 * Responsible for monitoring changes to properties and then creating annotations if properties match conditions in
 * the {@link PropertyCasMapping}.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class AnnotationPropertyWatcher {
    /**
     * The property cas mapping object which determines which property is monitored and the condition for creating an
     * annotation.
     */
    private final PropertyCasMapping propertyCasMapping;

    /**
     * The begin of the currently active annotation.
     */
    @Nullable
    private Integer begin = null;

    /**
     * The value of the currently active annotation.
     */
    @Nullable
    private Integer value = null;

    /**
     * Creates an annotation property watcher from the cas mapping.
     *
     * @param propertyCasMapping property cas mapping.
     */
    AnnotationPropertyWatcher(PropertyCasMapping propertyCasMapping) {
        this.propertyCasMapping = propertyCasMapping;
    }

    /**
     * Handles changes to the properties in the state.
     *
     * @param index the current index of the character output in the destination view.
     * @param state the current state of the rtf document.
     * @param jCas the destination view.
     */
    @Nullable
    AnnotationFS handleChanges(int index, State state, JCas jCas) {
        if (index < 0) {
            throw new IllegalArgumentException("Index less than 0");
        }

        if (begin != null && index < begin) {
            throw new IllegalStateException("Index before the beginning of the currently tracked annotation.");
        }

        int propertyValue = propertyCasMapping.getPropertyValue(state);

        AnnotationFS finished = null;
        if (begin != null) {
            if (value != null && value != propertyValue) {
                finished = propertyCasMapping.getAnnotation(jCas, begin, index, value);
                begin = null;
                value = null;
            }
        }
        if (begin == null && propertyCasMapping.test(propertyValue)) {
            begin = index;
            value = propertyValue;
        }
        return finished;
    }
}
