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

package edu.umn.biomedicus.framework.store;

import static org.testng.Assert.*;

import edu.umn.biomedicus.framework.store.ImmutableDistinctSpanMap.Builder;
import java.util.Iterator;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ImmutableDistinctSpanMapTest {


  private ImmutableDistinctSpanMap<Integer> immutable;

  @BeforeMethod
  public void setUp() throws Exception {
    Builder<Integer> builder = ImmutableDistinctSpanMap.builder();
    builder.add(Span.create(1, 2), 1);
    builder.add(Span.create(3, 3), 2);
    builder.add(Span.create(7, 10), 3);
    builder.add(Span.create(12, 18), 4);
    immutable = builder.build();
  }

  @Test
  public void testLowerAll() throws Exception {
    assertEquals(immutable.lower(19), 3);
  }

  @Test
  public void testLowerNone() throws Exception {
    assertEquals(immutable.lower(-1), -1);
  }

  @Test
  public void testLowerMid() throws Exception {
    assertEquals(immutable.lower(4), 1);
  }

  @Test
  public void testLowerContains() throws Exception {
    assertEquals(immutable.lower(3), 1);
  }

  @Test
  public void testLowerTopContaining() throws Exception {
    assertEquals(immutable.lower(13), 2);
  }

  @Test
  public void testHigherAll() throws Exception {
    assertEquals(immutable.higher(-1), 0);
  }

  @Test
  public void testHigherNone() throws Exception {
    assertEquals(immutable.higher(13), 4);
  }

  @Test
  public void testHigherContains() throws Exception {
    assertEquals(immutable.higher(7), 2);
  }

  @Test
  public void testHigherMid() throws Exception {
    assertEquals(immutable.higher(6), 2);
  }

  @Test
  public void testSize() throws Exception {
    assertEquals(immutable.size(), 4);
  }

  @Test
  public void testViewContainsLabel() throws Exception {
    SpansMap<Integer> view = immutable.insideSpan(Span.create(3, 10));
    assertTrue(view.containsLabel(Label.create(Span.create(3, 3), 2)));
  }

  @Test
  public void testViewContainsLabelFalse() throws Exception {
    SpansMap<Integer> view = immutable.insideSpan(Span.create(3, 10));
    assertFalse(view.containsLabel(Label.create(Span.create(1, 2), 1)));
  }

  @Test
  public void testContaining() throws Exception {
    SpansMap<Integer> containing = immutable.containing(Span.create(8, 9));
    assertTrue(containing.containsLabel(Label.create(Span.create(7, 10), 3)));
  }

  @Test
  public void testContainingLast() throws Exception {
    assertTrue(immutable.containing(Span.create(13, 17))
        .containsLabel(Label.create(Span.create(12, 18), 4)));
  }

  @Test
  public void testContainingBefore() throws Exception {
    assertEquals(immutable.containing(Span.create(0, 0)).size(), 0);
  }

  @Test
  public void testContainingOverlap() throws Exception {
    assertEquals(immutable.containing(Span.create(13, 25)).size(), 0);
  }

  @Test
  public void testToTheLeftOfAsList() throws Exception {
    SpansMap<Integer> toTheLeft = immutable.toTheLeftOf(7);
    List<Label<Integer>> asList = toTheLeft.asList();
    assertEquals(asList.size(), 2);
    assertEquals(asList.get(0), Label.create(Span.create(1, 2), 1));
    assertEquals(asList.get(1), Label.create(Span.create(3, 3), 2));
  }

  @Test
  public void testToTheRightOfAsList() throws Exception {
    List<Label<Integer>> asList = immutable.toTheRightOf(6).asList();
    assertEquals(asList.size(), 2);
    assertEquals(asList.get(0), Label.create(Span.create(7, 10), 3));
    assertEquals(asList.get(1), Label.create(Span.create(12, 18), 4));
  }

  @Test
  public void testDescendingViewEntriesIterator() throws Exception {
    SpansMap<Integer> descending = immutable.descendingBegin();
    Iterator<Label<Integer>> it = descending.entries().iterator();
    assertEquals(it.next(), Label.create(Span.create(12, 18), 4));
    assertEquals(it.next(), Label.create(Span.create(7, 10), 3));
    assertEquals(it.next(), Label.create(Span.create(3, 3), 2));
    assertEquals(it.next(), Label.create(Span.create(1, 2), 1));
  }
}