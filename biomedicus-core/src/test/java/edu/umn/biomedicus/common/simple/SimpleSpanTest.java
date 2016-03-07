package edu.umn.biomedicus.common.simple;

import edu.umn.biomedicus.common.text.Span;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Unit test for {@link SimpleSpan} and default methods on {@link Span}.
 */
public class SimpleSpanTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCreateIllegalSpan() throws Exception {
        new SimpleSpan(10, 8);
        fail("That constructor should fail");
    }

    @Test
    public void testGetBegin() throws Exception {
        Span tested = new SimpleSpan(10, 15);
        assertEquals(tested.getBegin(), 10, "Begin should be equal to value passed to constructor.");
    }

    @Test
    public void testGetEnd() throws Exception {
        Span tested = new SimpleSpan(10, 15);
        assertEquals(tested.getEnd(), 15, "End should be equal to value passed to constructor.");
    }

    @Test
    public void testEqualsSameObject() throws Exception {
        Span tested = new SimpleSpan(10, 15);
        assertTrue(tested.equals(tested), "Same object should be equal");
    }

    @Test
    public void testEqualsNull() throws Exception {
        Span tested = new SimpleSpan(10, 15);
        assertFalse(tested.equals(null), "Should not equal null");
    }

    @Test
    public void testEqualsOtherClass() throws Exception {
        Span tested = new SimpleSpan(10, 15);
        assertFalse(tested.equals(new Span() {
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
        Span tested = new SimpleSpan(10, 15);
        Span other = new SimpleSpan(5, 10);
        assertFalse(tested.equals(other), "Different spans should not be equal");
    }

    @Test
    public void testEquals() throws Exception {
        Span tested = new SimpleSpan(10, 15);
        Span other = new SimpleSpan(10, 15);
        assertTrue(tested.equals(other), "objects with same values should be equal");
    }

    @Test
    public void testHashCode() throws Exception {
        Span tested = new SimpleSpan(10, 15);
        Span other = new SimpleSpan(10, 15);
        assertTrue(tested.hashCode() == other.hashCode(), "Objects with same values should have same hashCode");
    }

    @Test
    public void testHashCodeDifferent() throws Exception {
        Span tested = new SimpleSpan(10, 15);
        Span other = new SimpleSpan(5, 10);
        assertFalse(tested.hashCode() == other.hashCode(),
                "Objects with different values should have different hashCodes");
    }

    @Test
    public void testToString() throws Exception {
        Span tested = new SimpleSpan(10, 15);
        assertEquals(tested.toString(), "SimpleSpan{begin=10, end=15}",
                "ToString should return a descriptive string containing values");
    }

    @Test
    public void testContainsBeginLessThan() throws Exception {
        Span tested = new SimpleSpan(10, 15);
        Span other = new SimpleSpan(6, 10);

        assertFalse(tested.contains(other));
    }

    @Test
    public void testContainsOverlappingFalse() throws Exception {
        Span tested = new SimpleSpan(6, 10);
        Span other = new SimpleSpan(8, 13);

        assertFalse(tested.contains(other));
    }

    @Test
    public void testContainsEqualsTrue() throws Exception {
        Span tested = new SimpleSpan(6, 10);
        Span other = new SimpleSpan(6, 10);

        assertTrue(tested.contains(other));
    }

    @Test
    public void testContainsTrue() throws Exception {
        Span tested = new SimpleSpan(6, 10);
        Span other = new SimpleSpan(7, 9);

        assertTrue(tested.contains(other));
    }

    @Test
    public void testLength() throws Exception {
        Span tested = new SimpleSpan(6, 10);

        assertEquals(tested.length(), 4);
    }

    @Test
    public void testGetCovered() throws Exception {
        String string = "This is a sentence.";
        Span tested = new SimpleSpan(5, 9);

        assertEquals(tested.getCovered(string), "is a");
    }

    @Test
    public void testIndicesEmpty() throws Exception {
        Span tested = new SimpleSpan(0, 0);

        int[] ints = tested.indices().toArray();
        assertEquals(ints.length, 0);
    }

    @Test
    public void testIndices() throws Exception {
        Span tested = new SimpleSpan(0, 4);

        int[] ints = tested.indices().toArray();
        assertEquals(ints, new int[]{0, 1, 2, 3});
    }

    @Test
    public void testAllIndicesAreInFalseMiddle() throws Exception {
        int[] array = new int[]{1, 2, 4};
        Span tested = new SimpleSpan(1, 5);

        assertFalse(tested.allIndicesAreIn(array));
    }

    @Test
    public void testAllIndicesAreInFalseLeft() throws Exception {
        int[] array = new int[]{2, 3, 4};
        Span tested = new SimpleSpan(1, 5);

        assertFalse(tested.allIndicesAreIn(array));
    }

    @Test
    public void testAllIndicesAreInFalseRight() throws Exception {
        int[] array = new int[]{1, 2, 3};
        Span tested = new SimpleSpan(1, 5);

        assertFalse(tested.allIndicesAreIn(array));
    }

    @Test
    public void testAllIndicesAreInTrue() throws Exception {
        int[] array = new int[]{1, 2, 3, 4};
        Span tested = new SimpleSpan(1, 5);

        assertTrue(tested.allIndicesAreIn(array));
    }

    @Test
    public void testAllIndicesAreInFalseEmpty() throws Exception {
        int[] array = new int[]{1,2,3,4};
        Span tested = new SimpleSpan(0,0);

        assertFalse(tested.allIndicesAreIn(array));
    }

    @Test
    public void testCompareToDifferentBegin() throws Exception {
        Span tested = new SimpleSpan(10, 15);
        Span other = new SimpleSpan(6, 10);
        assertNotEquals(tested.compareTo(other), 0, "Comparing spans with different values should not return 0");
    }

    @Test
    public void testCompareToEquals() throws Exception {
        Span tested = new SimpleSpan(10, 15);
        Span other = new SimpleSpan(10, 15);
        assertEquals(tested.compareTo(other), 0, "Comparing spans with same values should return 0");
    }
}