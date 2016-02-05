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
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.*;
import java.io.InputStream;
import java.util.List;

/**
 * Mappings from RTF elements to UIMA CAS elements. Is a POJO/bean that is XML-serializable via JAXB.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
@XmlRootElement
@XmlType
public class CasMappings {
    /**
     * Mappings from control words to annotations.
     */
    @Nullable
    private List<ControlWordCasMapping> controlWordCasMappings = null;

    /**
     * Mappings from destination names to view names.
     */
    @Nullable
    private List<DestinationCasMapping> destinationCasMappings = null;

    /**
     * Mappings that determine when rtf properties should emit annotations.
     */
    @Nullable
    private List<PropertyCasMapping> propertyCasMappings = null;

    /**
     * Loads the cas mappings from an xml classpath resource.
     *
     * @param classpath the xml classpath resource
     * @return cas mappings containing all the data in specified xml resource.
     */
    public static CasMappings loadFromFile(String classpath) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpath);
        return JAXB.unmarshal(inputStream, CasMappings.class);
    }

    /**
     * Gets the mappings from rtf control words to UIMA annotations.
     *
     * @return list containing mappings
     */
    @XmlElementWrapper(required = true)
    @XmlElementRef(name = "controlWordCasMapping")
    @Nullable
    public List<ControlWordCasMapping> getControlWordCasMappings() {
        return controlWordCasMappings;
    }

    /**
     * Sets the mappings from rtf control words to UIMA annotations.
     *
     * @param controlWordCasMappings a list of the mappings from rtf control words to uima annotations.
     */
    public void setControlWordCasMappings(@Nullable List<ControlWordCasMapping> controlWordCasMappings) {
        this.controlWordCasMappings = controlWordCasMappings;
    }

    /**
     * Gets the mappings from rtf destinations to UIMA CAS views.
     *
     * @return list of the mappings.
     */
    @XmlElementWrapper(required = true)
    @XmlElementRef(name = "destinationCasMapping")
    @Nullable
    public List<DestinationCasMapping> getDestinationCasMappings() {
        return destinationCasMappings;
    }

    /**
     * Sets the mappings from rtf destinations to UIMA CAS views.
     *
     * @param destinationCasMappings list of the mapping objects.
     */
    public void setDestinationCasMappings(@Nullable List<DestinationCasMapping> destinationCasMappings) {
        this.destinationCasMappings = destinationCasMappings;
    }

    /**
     * Gets the mappings from rtf property values to UIMA annotation types.
     *
     * @return list of the mapping objects.
     */
    @XmlElementWrapper(required = true)
    @XmlElement(name = "propertyCasMapping")
    @Nullable
    public List<PropertyCasMapping> getPropertyCasMappings() {
        return propertyCasMappings;
    }

    /**
     * Sets the mappings from rtf property values to UIMA Annotation types.
     *
     * @param propertyCasMappings list of the mapping objects.
     */
    public void setPropertyCasMappings(@Nullable List<PropertyCasMapping> propertyCasMappings) {
        this.propertyCasMappings = propertyCasMappings;
    }


}
