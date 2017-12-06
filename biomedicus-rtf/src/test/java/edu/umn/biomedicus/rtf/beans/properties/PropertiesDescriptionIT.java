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

import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Integration test for {@link PropertiesDescription}.
 */
public class PropertiesDescriptionIT {

  @Test
  public void testLoadProperties() throws Exception {
    PropertiesDescription propertiesDescription = PropertiesDescription
        .loadFromFile("edu/umn/biomedicus/rtf/PropertiesDescription.xml");
    List<PropertyGroupDescription> propertyGroupDescriptions = propertiesDescription
        .getPropertyGroupDescriptions();
    Assert.assertTrue(propertyGroupDescriptions.size() > 0);
    PropertyGroupDescription firstPropertyGroup = propertyGroupDescriptions.get(0);
    Assert.assertTrue(firstPropertyGroup.getPropertyDescriptions().size() > 0);
  }

  @Test
  public void testCreateDescription() throws Exception {
    PropertiesDescription propertiesDescription = PropertiesDescription
        .loadFromFile("edu/umn/biomedicus/rtf/PropertiesDescription.xml");
    Map<String, Map<String, Integer>> properties = propertiesDescription.createProperties();
    Map<String, Integer> characterFormatting = properties.get("CharacterFormatting");
    Assert.assertNotNull(characterFormatting);
    Assert.assertNotNull(characterFormatting.get("Bold"));
  }
}