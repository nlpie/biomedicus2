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

package edu.umn.biomedicus.uima.rtf;

import static mockit.Deencapsulation.getField;
import static mockit.Deencapsulation.setField;
import static org.junit.jupiter.api.Assertions.assertEquals;

import mockit.Tested;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DestinationCasMapping}.
 */
public class DestinationCasMappingTest {

  @Tested
  DestinationCasMapping destinationCasMapping;

  @Test
  public void testGetDestinationName() {
    setField(destinationCasMapping, "destinationName", "dest");

    assertEquals(destinationCasMapping.getDestinationName(), "dest");
  }

  @Test
  public void testSetDestinationName() {
    destinationCasMapping.setDestinationName("destName");

    assertEquals(getField(destinationCasMapping, "destinationName"), "destName");
  }

  @Test
  public void testGetViewName() {
    setField(destinationCasMapping, "viewName", "view");

    assertEquals(destinationCasMapping.getViewName(), "view");
  }

  @Test
  public void testSetViewName() {
    destinationCasMapping.setViewName("viewName");

    assertEquals(getField(destinationCasMapping, "viewName"), "viewName");
  }
}