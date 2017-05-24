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

import java.util.Iterator;
import java.util.Set;

import static org.testng.Assert.*;

/**
 *
 */
public class OrderedSpanMapTest {
    @Test
    public void testEntriesSize() throws Exception {

        OrderedSpanMap<String> spanMap = new OrderedSpanMap<>();
        spanMap.put(Span.create(0, 10), "");
        spanMap.put(Span.create(7, 9), "");
        spanMap.put(Span.create(3, 6), "");
        spanMap.put(Span.create(11, 20), "");
        spanMap.put(Span.create(4, 5), "");
        spanMap.put(Span.create(0, 2), "");

        assertEquals(spanMap.size(), 6);
    }

    @Test
    public void testEntriesIt() throws Exception {
        OrderedSpanMap<String> spanMap = new OrderedSpanMap<>();
        spanMap.put(Span.create(0, 10), "");
        spanMap.put(Span.create(7, 9), "");
        spanMap.put(Span.create(3, 6), "");
        spanMap.put(Span.create(11, 20), "");
        spanMap.put(Span.create(4, 5), "");
        spanMap.put(Span.create(0, 2), "");

        Set<Label<String>> entries = spanMap.entries();
        Iterator<Label<String>> it = entries.iterator();
        assertEquals(it.next(), new Label<>(Span.create(0, 2), ""));
        assertEquals(it.next(), new Label<>(Span.create(0, 10), ""));

    }
}