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

package edu.umn.biomedicus.common.tuples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link PosCap}.
 */
public class PosCapTest {

  @Test
  public void testGetCapitalized() {
    PosCap posCap = PosCap.getCapitalized(PartOfSpeech.BOS);

    assertEquals(posCap.getPartOfSpeech(), PartOfSpeech.BOS);
    assertEquals(posCap.isCapitalized(), true);
  }

  @Test
  public void testGetNotCapitalized() {
    PosCap posCap = PosCap.getNotCapitalized(PartOfSpeech.BOS);

    assertEquals(posCap.getPartOfSpeech(), PartOfSpeech.BOS);
    assertEquals(posCap.isCapitalized(), false);
  }

  @Test
  public void testCreate() {
    PosCap posCap = PosCap.create(PartOfSpeech.BOS, true);

    assertEquals(posCap.getPartOfSpeech(), PartOfSpeech.BOS);
    assertEquals(posCap.isCapitalized(), true);
  }

  @Test
  public void testGetPartOfSpeech() {
    PosCap posCap = PosCap.create(PartOfSpeech.BOS, true);

    assertEquals(posCap.getPartOfSpeech(), PartOfSpeech.BOS);
  }

  @Test
  public void testIsCapitalized() {
    PosCap posCap = PosCap.create(PartOfSpeech.BOS, true);

    assertEquals(posCap.isCapitalized(), true);
  }

  @Test
  public void testOrdinal() {
    PosCap posCap = PosCap.create(PartOfSpeech.BOS, true);
    int ordinal = posCap.ordinal();
    PosCap fromOrdinal = PosCap.createFromOrdinal(ordinal);
    assertEquals(fromOrdinal, posCap);
  }

  @Test
  public void testEqualsSameObject() {
    PosCap posCap = PosCap.create(PartOfSpeech.CD, true);
    assertTrue(posCap.equals(posCap));
  }

  @Test
  public void testEquals() {
    PosCap posCap = PosCap.create(PartOfSpeech.BOS, true);
    PosCap second = PosCap.getCapitalized(PartOfSpeech.BOS);

    assertTrue(posCap.equals(second));
  }

  @Test
  public void testEqualsNull() {
    PosCap posCap = PosCap.create(PartOfSpeech.BOS, true);

    assertFalse(posCap.equals(null));
  }

  @Test
  public void testEqualsNotObject() {
    PosCap posCap = PosCap.create(PartOfSpeech.BOS, true);

    assertFalse(posCap.equals("string"));
  }

  @Test
  public void testEqualsFalse() {
    PosCap posCap = PosCap.create(PartOfSpeech.BOS, true);
    PosCap second = PosCap.getCapitalized(PartOfSpeech.BBS);

    assertFalse(posCap.equals(second));
  }

  @Test
  public void testEqualsFalseCapitalization() {

    PosCap posCap = PosCap.create(PartOfSpeech.BBS, false);
    PosCap second = PosCap.getCapitalized(PartOfSpeech.BBS);

    assertFalse(posCap.equals(second));
  }

  @Test
  public void testHashCodeEqual() {
    PosCap posCap = PosCap.create(PartOfSpeech.BOS, true);
    PosCap second = PosCap.getCapitalized(PartOfSpeech.BOS);

    assertTrue(posCap.hashCode() == second.hashCode());
  }

  @Test
  public void testHashCodeUnequal() {
    PosCap posCap = PosCap.create(PartOfSpeech.BOS, true);
    PosCap second = PosCap.getCapitalized(PartOfSpeech.BBS);

    assertFalse(posCap.hashCode() == second.hashCode());
  }

  @Test
  public void testCompareTo() {
    PosCap posCap = PosCap.create(PartOfSpeech.BOS, true);
    PosCap second = PosCap.getCapitalized(PartOfSpeech.BBS);

    assertEquals(posCap.compareTo(second), PartOfSpeech.BOS.compareTo(PartOfSpeech.BBS));
  }

  @Test
  public void testCompareToCapitalization() {

    PosCap posCap = PosCap.create(PartOfSpeech.BOS, false);
    PosCap second = PosCap.getCapitalized(PartOfSpeech.BOS);

    assertEquals(posCap.compareTo(second), Boolean.compare(false, true));
  }

  @Test
  public void testToString() {
    PosCap posCap = PosCap.create(PartOfSpeech.BOS, false);

    assertEquals(posCap.toString(), "PosCap{partOfSpeech=BOS, capitalized=false}");
  }
}