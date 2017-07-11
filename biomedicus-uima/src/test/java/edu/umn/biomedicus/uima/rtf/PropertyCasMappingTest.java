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

package edu.umn.biomedicus.uima.rtf;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import edu.umn.biomedicus.rtf.reader.State;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.testng.annotations.Test;

/**
 * Unit test for {@link PropertyCasMapping}.
 */
public class PropertyCasMappingTest {

  PropertyCasMapping propertyCasMapping;

  @Mocked
  TypeSystem typeSystem;
  @Mocked
  Type type;
  @Mocked
  Feature feature;
  @Mocked
  CAS cas;
  @Mocked
  AnnotationFS annotationFS;


  @Test
  public void testMatch() throws Exception {
    PropertyCasMapping propertyCasMapping = new PropertyCasMapping("group", "property",
        "annotation", 1, null, true,
        false);
    assertTrue(propertyCasMapping.test(1));
  }

  @Test
  public void testMatchWithMaximum() throws Exception {
    PropertyCasMapping propertyCasMapping = new PropertyCasMapping("group", "property",
        "annotation", 1, 3, true,
        false);
    assertTrue(propertyCasMapping.test(3));
  }

  @Test
  public void testNoMatch() throws Exception {
    PropertyCasMapping propertyCasMapping = new PropertyCasMapping("group", "property",
        "annotation", 1, null, true,
        false);
    assertFalse(propertyCasMapping.test(0));
  }

  @Test
  public void testNoMatchMaximum() throws Exception {
    PropertyCasMapping propertyCasMapping = new PropertyCasMapping("group", "property",
        "annotation", 1, 3, true,
        false);
    assertFalse(propertyCasMapping.test(4));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAnnotationBeginBeforeZero() throws Exception {
    PropertyCasMapping propertyCasMapping = new PropertyCasMapping("group", "property",
        "annotation", 1, 3, true,
        false);
    propertyCasMapping.getAnnotation(cas, -1, 3, 1);
    fail();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAnnotationEndBeforeBegin() throws Exception {
    PropertyCasMapping propertyCasMapping = new PropertyCasMapping("group", "property",
        "annotation", 1, 3, true,
        false);
    propertyCasMapping.getAnnotation(cas, 4, 3, 1);
    fail();
  }

  @Test
  public void testGetAnnotationZeroLengthNotEmitted() throws Exception {
    PropertyCasMapping propertyCasMapping = new PropertyCasMapping("group", "property",
        "annotation", 1, 3, true,
        false);
    assertNull(propertyCasMapping.getAnnotation(cas, 3, 3, 1));
  }

  @Test
  public void testGetAnnotationValueIncluded() throws Exception {
    new Expectations() {{
      cas.getTypeSystem();
      result = typeSystem;
      typeSystem.getType("annotation");
      result = type;
      cas.createAnnotation(type, 3, 3);
      result = annotationFS;
      type.getFeatureByBaseName("value");
      result = feature;
    }};

    PropertyCasMapping propertyCasMapping = new PropertyCasMapping("group", "property",
        "annotation", 1, 3, true,
        true);
    assertEquals(propertyCasMapping.getAnnotation(cas, 3, 3, 1), annotationFS);

    new Verifications() {{
      annotationFS.setIntValue(feature, 1);
    }};
  }

  @Test
  public void testGetAnnotation() throws Exception {
    new Expectations() {{
      cas.getTypeSystem();
      result = typeSystem;
      typeSystem.getType("annotation");
      result = type;
      cas.createAnnotation(type, 3, 3);
      result = annotationFS;
    }};

    PropertyCasMapping propertyCasMapping = new PropertyCasMapping("group", "property",
        "annotation", 1, 3, false,
        true);
    assertEquals(propertyCasMapping.getAnnotation(cas, 3, 3, 1), annotationFS);

    new Verifications() {{
      type.getFeatureByBaseName("value");
      times = 0;
      annotationFS.setIntValue(feature, 1);
      times = 0;
    }};
  }

  @Test
  public void testGetPropertyValue(@Mocked State state) throws Exception {
    PropertyCasMapping propertyCasMapping = new PropertyCasMapping("group", "property",
        "annotation", 1, 3, false,
        true);
    new Expectations() {{
      state.getPropertyValue("group", "property");
      result = 20;
    }};

    assertEquals(propertyCasMapping.getPropertyValue(state), 20);
  }
}