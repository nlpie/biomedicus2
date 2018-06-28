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
 * Unit test for {@link WordPosCap}.
 */
public class WordPosCapTest {

  @Test
  public void testGetWord() {
    WordPosCap wordPosCap = new WordPosCap("aWord", PartOfSpeech.NN, false);

    assertEquals(wordPosCap.getWord(), "aWord");
  }

  @Test
  public void testGetPartOfSpeech() {
    WordPosCap wordPosCap = new WordPosCap("aWord", PartOfSpeech.NN, false);

    assertEquals(wordPosCap.getPartOfSpeech(), PartOfSpeech.NN);
  }

  @Test
  public void testIsCapitalized() {
    WordPosCap wordPosCap = new WordPosCap("aWord", PartOfSpeech.NN, false);

    assertEquals(wordPosCap.isCapitalized(), false);
  }

  @Test
  public void testToPosCap() {
    WordPosCap wordPosCap = new WordPosCap("aWord", PartOfSpeech.NN, false);
    PosCap posCap = PosCap.create(PartOfSpeech.NN, false);

    assertEquals(wordPosCap.toPosCap(), posCap);
  }

  @Test
  public void testEqualsSameObject() {
    WordPosCap wordPosCap = new WordPosCap("aWord", PartOfSpeech.NN, false);

    assertTrue(wordPosCap.equals(wordPosCap));
  }

  @Test
  public void testEqualsNull() {
    WordPosCap wordPosCap = new WordPosCap("aWord", PartOfSpeech.NN, false);

    assertFalse(wordPosCap.equals(null));
  }

  @Test
  public void testEqualsDifferentType() {
    WordPosCap wordPosCap = new WordPosCap("aWord", PartOfSpeech.NN, false);

    assertFalse(wordPosCap.equals("string"));
  }

  @Test
  public void testEqualsDifferentWord() {
    WordPosCap first = new WordPosCap("aWord", PartOfSpeech.NN, false);
    WordPosCap second = new WordPosCap("word", PartOfSpeech.NN, false);

    assertFalse(first.equals(second));
  }

  @Test
  public void testEqualsDifferentPos() {
    WordPosCap first = new WordPosCap("aWord", PartOfSpeech.NN, false);
    WordPosCap second = new WordPosCap("aWord", PartOfSpeech.NNS, false);

    assertFalse(first.equals(second));
  }

  @Test
  public void testEqualsDifferentCapitalization() {
    WordPosCap first = new WordPosCap("aWord", PartOfSpeech.NN, false);
    WordPosCap second = new WordPosCap("aWord", PartOfSpeech.NN, true);

    assertFalse(first.equals(second));
  }

  @Test
  public void testEquals() {
    WordPosCap first = new WordPosCap("aWord", PartOfSpeech.NN, false);
    WordPosCap second = new WordPosCap("aWord", PartOfSpeech.NN, false);

    assertTrue(first.equals(second));
  }

  @Test
  public void testHashCodeDifferentWord() {
    WordPosCap first = new WordPosCap("aWord", PartOfSpeech.NN, false);
    WordPosCap second = new WordPosCap("word", PartOfSpeech.NN, false);

    assertFalse(first.hashCode() == second.hashCode());
  }

  @Test
  public void testHashCodeDifferentPos() {
    WordPosCap first = new WordPosCap("aWord", PartOfSpeech.NN, false);
    WordPosCap second = new WordPosCap("aWord", PartOfSpeech.NNS, false);

    assertFalse(first.hashCode() == second.hashCode());
  }

  @Test
  public void testHashCodeDifferentCapitalization() {
    WordPosCap first = new WordPosCap("aWord", PartOfSpeech.NN, false);
    WordPosCap second = new WordPosCap("aWord", PartOfSpeech.NN, true);

    assertFalse(first.hashCode() == second.hashCode());
  }

  @Test
  public void testHashCodeEqual() {
    WordPosCap first = new WordPosCap("aWord", PartOfSpeech.NN, false);
    WordPosCap second = new WordPosCap("aWord", PartOfSpeech.NN, false);

    assertTrue(first.hashCode() == second.hashCode());
  }

  @Test
  public void testCompareToDifferentWord() {
    WordPosCap first = new WordPosCap("aWord", PartOfSpeech.NN, false);
    WordPosCap second = new WordPosCap("word", PartOfSpeech.NN, false);

    assertEquals(first.compareTo(second), "aWord".compareTo("word"));
  }

  @Test
  public void testCompareToDifferentPos() {
    WordPosCap first = new WordPosCap("word", PartOfSpeech.NN, false);
    WordPosCap second = new WordPosCap("word", PartOfSpeech.NNS, false);

    assertEquals(first.compareTo(second), PartOfSpeech.NN.compareTo(PartOfSpeech.NNS));
  }

  @Test
  public void testCompareToDifferentCapitalization() {
    WordPosCap first = new WordPosCap("word", PartOfSpeech.NN, false);
    WordPosCap second = new WordPosCap("word", PartOfSpeech.NN, true);

    assertEquals(first.compareTo(second), Boolean.compare(false, true));
  }

  @Test
  public void testCompareTo() {
    WordPosCap first = new WordPosCap("word", PartOfSpeech.NN, false);
    WordPosCap second = new WordPosCap("word", PartOfSpeech.NN, false);

    assertEquals(first.compareTo(second), 0);
  }

  @Test
  public void testToString() {
    WordPosCap wordPosCap = new WordPosCap("aWord", PartOfSpeech.NN, false);

    assertEquals(wordPosCap.toString(),
        "WordPosCap{word='aWord', partOfSpeech=NN, isCapitalized=false}");
  }
}