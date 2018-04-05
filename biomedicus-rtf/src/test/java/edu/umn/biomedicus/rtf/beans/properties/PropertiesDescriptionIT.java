/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link PropertiesDescription}.
 */
class PropertiesDescriptionIT {

  @Test
  void testLoadProperties() {
    PropertiesDescription propertiesDescription = PropertiesDescription
        .loadFromFile("edu/umn/biomedicus/rtf/PropertiesDescription.xml");
    List<PropertyGroupDescription> propertyGroupDescriptions = propertiesDescription
        .getPropertyGroupDescriptions();
    assertTrue(propertyGroupDescriptions.size() > 0);
    PropertyGroupDescription firstPropertyGroup = propertyGroupDescriptions.get(0);
    assertTrue(firstPropertyGroup.getPropertyDescriptions().size() > 0);
  }

  @Test
  void testCreateDescription() {
    PropertiesDescription propertiesDescription = PropertiesDescription
        .loadFromFile("edu/umn/biomedicus/rtf/PropertiesDescription.xml");
    Map<String, Map<String, Integer>> properties = propertiesDescription.createProperties();
    Map<String, Integer> characterFormatting = properties.get("CharacterFormatting");
    assertNotNull(characterFormatting);
    assertNotNull(characterFormatting.get("Bold"));
  }
}