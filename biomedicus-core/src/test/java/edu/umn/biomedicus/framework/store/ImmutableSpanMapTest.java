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

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 *
 */
public class ImmutableSpanMapTest {
    @Test
    public void testConstruct() throws Exception {
        OrderedSpanMap<Object> spanMap = new OrderedSpanMap<>();
        spanMap.put(Span.create(0, 10), new Object());
        spanMap.put(Span.create(7, 9), new Object());
        spanMap.put(Span.create(3, 6), new Object());
        spanMap.put(Span.create(11, 20), new Object());
        spanMap.put(Span.create(4, 5), new Object());
        spanMap.put(Span.create(0, 2), new Object());

        ImmutableSpanMap<Object> immutableSpanMap
                = new ImmutableSpanMap<>(spanMap);
        Label<Object> objectLabel = immutableSpanMap.labelForIndex(0);
    }
}