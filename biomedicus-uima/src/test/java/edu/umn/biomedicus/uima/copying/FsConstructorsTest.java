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

package edu.umn.biomedicus.uima.copying;

import static org.junit.jupiter.api.Assertions.assertEquals;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.apache.uima.cas.BooleanArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link FsConstructors}.
 *
 * @author Ben Knoll
 */
public class FsConstructorsTest {

  @Tested
  FsConstructors fsConstructors;

  @Injectable
  CAS targetCas;

  @Injectable
  TypeSystem targetTypeSystem;

  @Injectable
  Type type, targetType;

  @Injectable
  FeatureStructure newFs;

  @Injectable
  BooleanArrayFS newBooleanArray;

  @Injectable
  BooleanArrayFS booleanArrayFS;

  @Injectable
  FeatureStructure featureStructure;

  @Test
  public void testCreateArrayFs() {
    new Expectations() {{
      booleanArrayFS.getType();
      result = type;
      type.getName();
      result = "uima.cas.BooleanArray";
      booleanArrayFS.size();
      result = 4;
      targetCas.createBooleanArrayFS(4);
      result = newBooleanArray;
    }};

    assertEquals(newBooleanArray, fsConstructors.createNewInstanceOfSameType(booleanArrayFS));
  }

  @Test
  public void testCreateGenericFs() {
    new Expectations() {{
      featureStructure.getType();
      result = type;
      type.getName();
      result = "someType";
      targetCas.getTypeSystem();
      result = targetTypeSystem;
      targetTypeSystem.getType("someType");
      result = targetType;
      targetCas.createFS(targetType);
      result = newFs;
    }};

    assertEquals(newFs, fsConstructors.createNewInstanceOfSameType(featureStructure));
  }
}