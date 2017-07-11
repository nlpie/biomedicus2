/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.rtf.beans.properties;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An XML descriptor file for the rtf properties
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
@XmlRootElement
@XmlType
public class PropertiesDescription {

  private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesDescription.class);

  /**
   * The descriptions of the individual property groups
   */
  private List<PropertyGroupDescription> propertyGroupDescriptions;

  /**
   * Loads a description from a classpath resource.
   *
   * @param classpath the name of the classpath resource
   * @return initialized property groups from the XML file.
   */
  public static PropertiesDescription loadFromFile(String classpath) {
    LOGGER.debug("Loading properties description from: {}", classpath);
    InputStream inputStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(classpath);
    return JAXB.unmarshal(inputStream, PropertiesDescription.class);
  }

  /**
   * Getter for the list of property groups
   *
   * @return property groups
   */
  @XmlElementWrapper(name = "propertyGroups")
  @XmlElementRef
  public List<PropertyGroupDescription> getPropertyGroupDescriptions() {
    return propertyGroupDescriptions;
  }

  /**
   * Setter for the list of property groups
   *
   * @param propertyGroupDescriptions descriptions of the property groups.
   */
  public void setPropertyGroupDescriptions(
      List<PropertyGroupDescription> propertyGroupDescriptions) {
    this.propertyGroupDescriptions = propertyGroupDescriptions;
  }

  /**
   * Creates a new state properties map using the description store in this file.
   *
   * @return newly created {@code StateProperties} object
   */
  public Map<String, Map<String, Integer>> createProperties() {
    LOGGER.debug("Creating state properties from description.");
    return propertyGroupDescriptions.stream()
        .collect(Collectors.toMap(PropertyGroupDescription::getName,
            PropertyGroupDescription::createPropertyGroup));
  }
}
