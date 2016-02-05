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
 * A mapping from an rtf destination name to an UIMA view name. Controls what rtf destinations output to uima views.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
@XmlRootElement
@XmlType
public class DestinationCasMapping {
    /**
     * The rtf destination name.
     */
    @Nullable
    private String destinationName;

    /**
     * The UIMA view name.
     */
    @Nullable
    private String viewName;

    /**
     * Getter for the rtf destination name.
     *
     * @return string rtf destination.
     */
    @XmlElement(required = true)
    @Nullable
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * Setter for the rtf destination name.
     *
     * @param destinationName string destination name.
     */
    public void setDestinationName(@Nullable String destinationName) {
        this.destinationName = destinationName;
    }

    /**
     * Getter for the UIMA view name.
     *
     * @return string uima view name.
     */
    @XmlElement(required = true)
    @Nullable
    public String getViewName() {
        return viewName;
    }

    /**
     * Setter for the UIMA view name.
     *
     * @param viewName string uima view name.
     */
    public void setViewName(@Nullable String viewName) {
        this.viewName = viewName;
    }
}
