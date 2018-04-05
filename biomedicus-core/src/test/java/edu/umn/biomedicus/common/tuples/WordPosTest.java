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
 * Unit test for {@link WordPos}.
 */
public class WordPosTest {

  @Test
  public void testForward() {
    WordPos wordPos = new WordPos("aWord", PartOfSpeech.NN).forward();

    assertEquals(wordPos.getWord(), "Word");
  }

  @Test
  public void testGetWord() {
    WordPos wordPos = new WordPos("aWord", PartOfSpeech.NN);

    assertEquals(wordPos.getWord(), "aWord");
  }

  @Test
  public void testGetTag() {
    WordPos wordPos = new WordPos("aWord", PartOfSpeech.NN);

    assertEquals(wordPos.getTag(), PartOfSpeech.NN);
  }

  @Test
  public void testEqualsSameObject() {
    WordPos wordPos = new WordPos("aWord", PartOfSpeech.NN);

    assertTrue(wordPos.equals(wordPos));
  }

  @Test
  public void testEqualsNull() {
    WordPos wordPos = new WordPos("aWord", PartOfSpeech.NN);

    assertFalse(wordPos.equals(null));
  }

  @Test
  public void testEqualsDifferentType() {
    WordPos wordPos = new WordPos("aWord", PartOfSpeech.NN);

    assertFalse(wordPos.equals("string"));
  }

  @Test
  public void testEqualsDifferentWord() {
    WordPos first = new WordPos("first", PartOfSpeech.NN);
    WordPos second = new WordPos("second", PartOfSpeech.NN);

    assertFalse(first.equals(second));
  }

  @Test
  public void testEqualsDifferentPos() {
    WordPos first = new WordPos("aWord", PartOfSpeech.NN);
    WordPos second = new WordPos("aWord", PartOfSpeech.NNS);

    assertFalse(first.equals(second));
  }

  @Test
  public void testEquals() {
    WordPos first = new WordPos("aWord", PartOfSpeech.NN);
    WordPos second = new WordPos("aWord", PartOfSpeech.NN);

    assertTrue(first.equals(second));
  }

  @Test
  public void testHashCodeEqual() {
    WordPos first = new WordPos("aWord", PartOfSpeech.NN);
    WordPos second = new WordPos("aWord", PartOfSpeech.NN);

    assertTrue(first.hashCode() == second.hashCode());
  }

  @Test
  public void testHashCodeNotEqual() {
    WordPos first = new WordPos("aWord", PartOfSpeech.NN);
    WordPos second = new WordPos("aWord", PartOfSpeech.NNS);
    WordPos third = new WordPos("Word", PartOfSpeech.NNS);

    assertFalse(first.hashCode() == second.hashCode());
    assertFalse(second.hashCode() == third.hashCode());
  }

  @Test
  public void testCompareToDifferentWord() {
    WordPos first = new WordPos("first", PartOfSpeech.NN);
    WordPos second = new WordPos("second", PartOfSpeech.NN);

    assertEquals(first.compareTo(second), "first".compareTo("second"));
  }

  @Test
  public void testCompareToDifferentTag() {
    WordPos first = new WordPos("aWord", PartOfSpeech.NN);
    WordPos second = new WordPos("aWord", PartOfSpeech.NNS);

    assertEquals(first.compareTo(second), PartOfSpeech.NN.compareTo(PartOfSpeech.NNS));
  }

  @Test
  public void testCompareToEqual() {
    WordPos first = new WordPos("aWord", PartOfSpeech.NNS);
    WordPos second = new WordPos("aWord", PartOfSpeech.NNS);

    assertEquals(first.compareTo(second), 0);
  }

  @Test
  public void testToString() {
    WordPos wordPos = new WordPos("aWord", PartOfSpeech.NNS);

    assertEquals(wordPos.toString(), "WordPos{word='aWord', tag=NNS}");
  }
}