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

package edu.umn.biomedicus.common.grams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link Ngram}.
 */
class NgramTest {

  private Ngram<String> bigram = Ngram.create("one", "two");
  private Ngram<String> trigram = Ngram.create("first", "second", "third");
  private Ngram<String> arrayBi = Ngram.bigram(new String[]{"a", "b", "c", "d", "e"}, 2);
  private Ngram<String> arrayTri = Ngram.trigram(new String[]{"a", "b", "c", "d", "e"}, 2);

  @Test
  void testTailBigram() {
    assertThrows(UnsupportedOperationException.class, () -> bigram.tail());
  }

  @Test
  void testTail() {
    Ngram<String> tail = arrayTri.tail();
    assertEquals(tail, Ngram.create("d", "e"),
        "Tail should return a bigram of the final two objects of a trigram");
  }

  @Test
  void testHeadBigram() {
    assertThrows(UnsupportedOperationException.class, () -> bigram.head());
  }

  @Test
  void testHead() {
    Ngram<String> head = arrayTri.head();
    assertEquals(head, Ngram.create("c", "d"),
        "Head should return a bigram of the first two objects of a trigram");
  }

  @Test
  void testGetFirst() {
    String first = arrayBi.getFirst();
    assertEquals(first, "c", "First should return the first object in a ngram");
  }

  @Test
  void testGetSecond() {
    String second = arrayBi.getSecond();
    assertEquals(second, "d", "Second should return the second object in a ngram");
  }

  @Test
  void testGetThird() {
    String third = trigram.getThird();
    assertEquals(third, "third", "Third should return the third object in a trigram");
  }

  @Test
  void testGetThirdBigram() {
    assertThrows(UnsupportedOperationException.class, () -> bigram.getThird());
  }

  @Test
  void testCompareToDifferentLength() {
    assertNotEquals(bigram.compareTo(trigram), 0,
        "Different length ngrams should not be equal when compared");
  }

  @Test
  void testCompareToDifferentValues() {
    assertNotEquals(bigram.compareTo(arrayBi), 0,
        "Ngrams containing different values should not be equal when compared");
  }

  @Test
  void testCompareToEquals() {
    assertEquals(bigram.compareTo(Ngram.create("one", "two")), 0,
        "Equal bigrams should return 0 when compared");
  }

  @Test
  void testIterator() {
    List<String> list = new ArrayList<>();
    arrayBi.iterator().forEachRemaining(list::add);
    assertEquals(list, Arrays.asList("c", "d"));
  }

  @Test
  void testIteratorBeyond() {
    Iterator<String> iterator = arrayBi.iterator();
    iterator.next();
    iterator.next();
    assertThrows(NoSuchElementException.class, iterator::next);
  }

  @Test
  void testEqualsSameObject() {
    assertEquals(arrayTri, arrayTri, "Object should be equal to itself");
  }

  @Test
  void testEqualsNull() {
    assertFalse(arrayTri.equals(null), "Object should not equal null.");
  }

  @Test
  void testEqualsOtherClass() {
    assertNotEquals("string", arrayTri, "Object should never equal another class");
  }

  @Test
  void testEqualsDifferentLength() {
    assertNotEquals(arrayTri, arrayBi, "Ngram should not equal ngram with different length");
  }

  @Test
  void testEqualsDifferentValues() {
    assertNotEquals(arrayTri, trigram, "Ngram should not equal ngram with different values");
  }

  @Test
  void testEquals() {
    assertEquals(arrayTri, Ngram.create("c", "d", "e"),
        "Ngram should be equal to ngram with same values");
  }

  @Test
  void testHashCodeNeq() {
    assertNotEquals(arrayTri.hashCode(), arrayBi.hashCode(),
        "hash code for ngram with different values should not be equal");
  }

  @Test
  void testHashCode() {
    assertEquals(arrayTri.hashCode(), Ngram.create("c", "d", "e").hashCode(),
        "hash code for objects with same value should be equal");
  }

  @Test
  void testSerialization() throws Exception {
    PipedOutputStream pipedOutputStream = new PipedOutputStream();
    PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(pipedOutputStream);
    ObjectInputStream objectInputStream = new ObjectInputStream(pipedInputStream);

    objectOutputStream.writeObject(arrayTri);
    @SuppressWarnings("unchecked")
    Ngram<String> ngram = (Ngram<String>) objectInputStream.readObject();

    assertEquals(ngram, arrayTri, "Deserialized object should be same as serialized object.");
  }
}