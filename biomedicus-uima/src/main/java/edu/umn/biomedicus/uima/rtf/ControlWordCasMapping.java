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

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * XML bean object for a mapping from a RTF control word to a UIMA annotation. When the control word is encountered,
 * the annotation is emitted in the output destination view.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
@XmlRootElement
@XmlType
public class ControlWordCasMapping {
    /**
     * The fully qualified UIMA annotation class name.
     */
    @Nullable
    private String annotationName;

    /**
     * The rtf control word.
     */
    @Nullable
    private String controlWord;

    /**
     * Getter for the fully qualified UIMA annotation class name.
     *
     * @return fully qualified UIMA annotation class name string.
     */
    @XmlElement(required = true)
    @Nullable
    public String getAnnotationName() {
        return annotationName;
    }

    /**
     * Setter for the fully qualified UIMA annotation class name.
     *
     * @param annotationName the UIMA annotation class name.
     */
    public void setAnnotationName(@Nullable String annotationName) {
        this.annotationName = annotationName;
    }

    /**
     * Getter for the Rtf control word.
     *
     * @return string of the rtf control word
     */
    @XmlElement(required = true)
    @Nullable
    public String getControlWord() {
        return controlWord;
    }

    /**
     * Setter for the rtf control word.
     *
     * @param controlWord rtf control word.
     */
    public void setControlWord(@Nullable String controlWord) {
        this.controlWord = controlWord;
    }
}
