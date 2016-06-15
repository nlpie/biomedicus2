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

package edu.umn.biomedicus.common.simple;

import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.SpanLike;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for {@link Span} and default methods on {@link SpanLike}.
 */
public class SpanTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateIllegalSpan() throws Exception {
        new Span(10, 8);
        fail("That constructor should fail");
    }

    @Test
    public void testGetBegin() throws Exception {
        SpanLike tested = new Span(10, 15);
        assertEquals(tested.getBegin(), 10, "Begin should be equal to value passed to constructor.");
    }

    @Test
    public void testGetEnd() throws Exception {
        SpanLike tested = new Span(10, 15);
        assertEquals(tested.getEnd(), 15, "End should be equal to value passed to constructor.");
    }

    @Test
    public void testEqualsSameObject() throws Exception {
        SpanLike tested = new Span(10, 15);
        assertTrue(tested.equals(tested), "Same object should be equal");
    }

    @Test
    public void testEqualsNull() throws Exception {
        SpanLike tested = new Span(10, 15);
        assertFalse(tested.equals(null), "Should not equal null");
    }

    @Test
    public void testEqualsOtherClass() throws Exception {
        SpanLike tested = new Span(10, 15);
        assertFalse(tested.equals(new SpanLike() {
            @Override
            public int getBegin() {
                return 10;
            }

            @Override
            public int getEnd() {
                return 15;
            }
        }), "Should not equal another class");
    }

    @Test
    public void testEqualsDifferent() throws Exception {
        SpanLike tested = new Span(10, 15);
        SpanLike other = new Span(5, 10);
        assertFalse(tested.equals(other), "Different spans should not be equal");
    }

    @Test
    public void testEquals() throws Exception {
        SpanLike tested = new Span(10, 15);
        SpanLike other = new Span(10, 15);
        assertTrue(tested.equals(other), "objects with same values should be equal");
    }

    @Test
    public void testHashCode() throws Exception {
        SpanLike tested = new Span(10, 15);
        SpanLike other = new Span(10, 15);
        assertTrue(tested.hashCode() == other.hashCode(), "Objects with same values should have same hashCode");
    }

    @Test
    public void testHashCodeDifferent() throws Exception {
        SpanLike tested = new Span(10, 15);
        SpanLike other = new Span(5, 10);
        assertFalse(tested.hashCode() == other.hashCode(),
                "Objects with different values should have different hashCodes");
    }

    @Test
    public void testContainsBeginLessThan() throws Exception {
        SpanLike tested = new Span(10, 15);
        SpanLike other = new Span(6, 10);

        assertFalse(tested.contains(other));
    }

    @Test
    public void testContainsOverlappingFalse() throws Exception {
        SpanLike tested = new Span(6, 10);
        SpanLike other = new Span(8, 13);

        assertFalse(tested.contains(other));
    }

    @Test
    public void testContainsEqualsTrue() throws Exception {
        SpanLike tested = new Span(6, 10);
        SpanLike other = new Span(6, 10);

        assertTrue(tested.contains(other));
    }

    @Test
    public void testContainsTrue() throws Exception {
        SpanLike tested = new Span(6, 10);
        SpanLike other = new Span(7, 9);

        assertTrue(tested.contains(other));
    }

    @Test
    public void testLength() throws Exception {
        SpanLike tested = new Span(6, 10);

        assertEquals(tested.length(), 4);
    }

    @Test
    public void testGetCovered() throws Exception {
        String string = "This is a sentence.";
        SpanLike tested = new Span(5, 9);

        assertEquals(tested.getCovered(string), "is a");
    }

    @Test
    public void testIndicesEmpty() throws Exception {
        SpanLike tested = new Span(0, 0);

        int[] ints = tested.indices().toArray();
        assertEquals(ints.length, 0);
    }

    @Test
    public void testIndices() throws Exception {
        SpanLike tested = new Span(0, 4);

        int[] ints = tested.indices().toArray();
        assertEquals(ints, new int[]{0, 1, 2, 3});
    }

    @Test
    public void testAllIndicesAreInFalseMiddle() throws Exception {
        int[] array = new int[]{1, 2, 4};
        SpanLike tested = new Span(1, 5);

        assertFalse(tested.allIndicesAreIn(array));
    }

    @Test
    public void testAllIndicesAreInFalseLeft() throws Exception {
        int[] array = new int[]{2, 3, 4};
        SpanLike tested = new Span(1, 5);

        assertFalse(tested.allIndicesAreIn(array));
    }

    @Test
    public void testAllIndicesAreInFalseRight() throws Exception {
        int[] array = new int[]{1, 2, 3};
        SpanLike tested = new Span(1, 5);

        assertFalse(tested.allIndicesAreIn(array));
    }

    @Test
    public void testAllIndicesAreInTrue() throws Exception {
        int[] array = new int[]{1, 2, 3, 4};
        SpanLike tested = new Span(1, 5);

        assertTrue(tested.allIndicesAreIn(array));
    }

    @Test
    public void testAllIndicesAreInFalseEmpty() throws Exception {
        int[] array = new int[]{1,2,3,4};
        SpanLike tested = new Span(0,0);

        assertFalse(tested.allIndicesAreIn(array));
    }
}