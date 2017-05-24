/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.common.labels;

import org.testng.annotations.Test;

public class DefaultLabelIndexTest {
    @Test
    public void testContaining() throws Exception {

    }

    @Test
    public void testInsideSpan() throws Exception {

    }

    @Test
    public void testWithTextLocation() throws Exception {

    }

    @Test
    public void testLeftwardsFrom() throws Exception {

    }

    @Test
    public void testRightwardsFrom() throws Exception {

    }

    @Test
    public void testReverse() throws Exception {

    }
    /**

    @Test
    public void testAscendingBegin() throws Exception {
        Object object = new Object();
        List<Label<Object>> labels = Arrays.asList(new Label<>(Span.create(12, 14), object),
                new Label<>(Span.create(12, 18), object), new Label<>(Span.create(0, 3), object),
                new Label<>(Span.create(5, 7), object));
        DefaultLabelIndex<Object> standardLabelIndex = new DefaultLabelIndex<>(labels);
        List<Label<Object>> labelList = standardLabelIndex.ascendingBegin().all();
        assertEquals(labelList.get(0).getBegin(), 0);
        assertEquals(labelList.get(1).getBegin(), 5);
        assertEquals(labelList.get(2).getBegin(), 12);
    }

    @Test
    public void testDescendingBegin() throws Exception {
        Object object = new Object();
        List<Label<Object>> labels = Arrays.asList(new Label<>(Span.create(12, 14), object),
                new Label<>(Span.create(12, 18), object), new Label<>(Span.create(0, 3), object),
                new Label<>(Span.create(5, 7), object));
        DefaultLabelIndex<Object> standardLabelIndex = new DefaultLabelIndex<>(labels);
        List<Label<Object>> labelList = standardLabelIndex.descendingBegin().all();
        assertEquals(labelList.get(0).getBegin(), 12);
        assertEquals(labelList.get(1).getBegin(), 12);
        assertEquals(labelList.get(2).getBegin(), 5);
        assertEquals(labelList.get(3).getBegin(), 0);
    }**/

    @Test
    public void testFilter() throws Exception {

    }

    @Test
    public void testIterator() throws Exception {

    }

    @Test
    public void testStream() throws Exception {

    }

}