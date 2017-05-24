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

import static org.testng.Assert.assertEquals;

import java.util.Iterator;
import org.testng.annotations.Test;

/**
 *
 */
public class ImmutableSpanMapTest {

  @Test
  public void testCeilingIndex() throws Exception {
    OrderedSpanMap<Object> spanMap = new OrderedSpanMap<>();
    spanMap.put(Span.create(0, 2), new Object());
    spanMap.put(Span.create(0, 10), new Object());
    spanMap.put(Span.create(3, 6), new Object());
    spanMap.put(Span.create(4, 5), new Object());
    spanMap.put(Span.create(7, 9), new Object());
    spanMap.put(Span.create(11, 20), new Object());
    ImmutableSpanMap<Object> immutableSpanMap = new ImmutableSpanMap<>(spanMap);
    assertEquals(immutableSpanMap.ceilingIndex(0, 2), 0);
    assertEquals(immutableSpanMap.ceilingIndex(0, 1), 0);
    assertEquals(immutableSpanMap.ceilingIndex(3, 6), 2);
    assertEquals(immutableSpanMap.ceilingIndex(4, 6), 4);
    assertEquals(immutableSpanMap.ceilingIndex(7, 9), 4);
    assertEquals(immutableSpanMap.ceilingIndex(11, 20), 5);
    assertEquals(immutableSpanMap.ceilingIndex(11, 21), -1);
  }

  @Test
  public void testFloorIndex() throws Exception {
    OrderedSpanMap<Object> spanMap = new OrderedSpanMap<>();
    spanMap.put(Span.create(0, 2), new Object());
    spanMap.put(Span.create(0, 10), new Object());
    spanMap.put(Span.create(3, 6), new Object());
    spanMap.put(Span.create(4, 5), new Object());
    spanMap.put(Span.create(7, 9), new Object());
    spanMap.put(Span.create(11, 20), new Object());
    ImmutableSpanMap<Object> immutableSpanMap = new ImmutableSpanMap<>(spanMap);
    assertEquals(immutableSpanMap.floorIndex(0, 8), 0);
    assertEquals(immutableSpanMap.floorIndex(0, 2), 0);
    assertEquals(immutableSpanMap.floorIndex(0, 1), -1);
    assertEquals(immutableSpanMap.floorIndex(3, 6), 2);
    assertEquals(immutableSpanMap.floorIndex(11, 20), 5);
    assertEquals(immutableSpanMap.floorIndex(11, 21), 5);

  }

  @Test
  public void testLowerIndex() throws Exception {
    OrderedSpanMap<Object> spanMap = new OrderedSpanMap<>();
    spanMap.put(Span.create(0, 2), new Object());
    spanMap.put(Span.create(0, 10), new Object());
    spanMap.put(Span.create(4, 5), new Object());
    spanMap.put(Span.create(3, 6), new Object());
    spanMap.put(Span.create(7, 9), new Object());
    spanMap.put(Span.create(11, 20), new Object());
    ImmutableSpanMap<Object> immutableSpanMap = new ImmutableSpanMap<>(spanMap);
    assertEquals(immutableSpanMap.lowerIndex(0, 10), 0);
    assertEquals(immutableSpanMap.lowerIndex(11, 20), 4);
    assertEquals(immutableSpanMap.lowerIndex(11, 21), 5);
    assertEquals(immutableSpanMap.lowerIndex(0, 11), 1);
    assertEquals(immutableSpanMap.lowerIndex(0, 2), -1);
  }

  @Test
  public void testContaining() throws Exception {
    OrderedSpanMap<Object> spanMap = new OrderedSpanMap<>();
    spanMap.put(Span.create(0, 10), new Object());
    spanMap.put(Span.create(7, 9), new Object());
    spanMap.put(Span.create(3, 6), new Object());
    spanMap.put(Span.create(11, 20), new Object());
    spanMap.put(Span.create(4, 5), new Object());
    spanMap.put(Span.create(0, 2), new Object());
    ImmutableSpanMap<Object> immutableSpanMap = new ImmutableSpanMap<>(spanMap);

    SpansMap<Object> containing = immutableSpanMap.containing(Span.of(7, 9));
    assertEquals(containing.size(), 2);
    Iterator<Span> iterator = containing.spans().iterator();
    assertEquals(iterator.next(), Span.of(0, 10));
    assertEquals(iterator.next(), Span.of(7, 9));
  }

  @Test
  public void testInsideSpan() throws Exception {
    OrderedSpanMap<Object> spanMap = new OrderedSpanMap<>();
    spanMap.put(Span.create(0, 10), new Object());
    spanMap.put(Span.create(7, 9), new Object());
    spanMap.put(Span.create(3, 6), new Object());
    spanMap.put(Span.create(11, 20), new Object());
    spanMap.put(Span.create(4, 5), new Object());
    spanMap.put(Span.create(0, 2), new Object());
    ImmutableSpanMap<Object> immutableSpanMap = new ImmutableSpanMap<>(spanMap);

    SpansMap<Object> containing = immutableSpanMap.insideSpan(Span.of(2, 9));
    assertEquals(containing.size(), 3);
    Iterator<Span> iterator = containing.spans().iterator();
    assertEquals(iterator.next(), Span.of(3, 6));
    assertEquals(iterator.next(), Span.of(4, 5));
    assertEquals(iterator.next(), Span.of(7, 9));
  }

  @Test
  public void testAscending() throws Exception {
    OrderedSpanMap<Object> spanMap = new OrderedSpanMap<>();
    spanMap.put(Span.create(0, 10), new Object());
    spanMap.put(Span.create(7, 9), new Object());
    spanMap.put(Span.create(3, 6), new Object());
    spanMap.put(Span.create(11, 20), new Object());
    spanMap.put(Span.create(4, 5), new Object());
    spanMap.put(Span.create(0, 2), new Object());
    ImmutableSpanMap<Object> immutableSpanMap = new ImmutableSpanMap<>(spanMap);

    SpansMap<Object> ascending = immutableSpanMap.ascendingBegin();

  }

  @Test
  public void testAscendingReversing() throws Exception {

  }
}