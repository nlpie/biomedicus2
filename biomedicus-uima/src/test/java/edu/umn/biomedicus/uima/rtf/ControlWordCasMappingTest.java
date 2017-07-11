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

import mockit.Deencapsulation;
import mockit.Tested;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ControlWordCasMapping}.
 */
public class ControlWordCasMappingTest {

  @Tested
  ControlWordCasMapping controlWordCasMapping;

  @Test
  public void testGetAnnotationName() throws Exception {
    Deencapsulation.setField(controlWordCasMapping, "annotationName", "anno");

    assertEquals(controlWordCasMapping.getAnnotationName(), "anno");
  }

  @Test
  public void testSetAnnotationName() throws Exception {
    controlWordCasMapping.setAnnotationName("annot");

    assertEquals(Deencapsulation.getField(controlWordCasMapping, "annotationName"), "annot");
  }

  @Test
  public void testGetControlWord() throws Exception {
    Deencapsulation.setField(controlWordCasMapping, "controlWord", "cw");

    assertEquals(controlWordCasMapping.getControlWord(), "cw");
  }

  @Test
  public void testSetControlWord() throws Exception {
    controlWordCasMapping.setControlWord("cw2");

    assertEquals(Deencapsulation.getField(controlWordCasMapping, "controlWord"), "cw2");
  }
}