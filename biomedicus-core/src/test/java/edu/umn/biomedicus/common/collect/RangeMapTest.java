package edu.umn.biomedicus.common.collect;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for {@link RangeMap}.
 */
public class RangeMapTest {

    @Test
    public void testMap() throws Exception {
        RangeMap rangeMap = RangeMap.builder()
                .add('a')
                .add('b')
                .add('c')
                .add('d')
                .add('e')
                .add('g')
                .add('h')
                .build();
        assertEquals(rangeMap.map('h'), 6);
    }

    @Test
    public void testReverseMap() throws Exception {
        RangeMap rangeMap = RangeMap.builder()
                .add('a')
                .add('b')
                .add('c')
                .add('d')
                .add('e')
                .add('g')
                .add('h')
                .build();
        assertEquals(rangeMap.reverseMap(6), 'h');
    }

    @Test
    public void testSize() throws Exception {
        RangeMap rangeMap = RangeMap.builder()
                .add('a')
                .add('b')
                .add('c')
                .add('d')
                .add('e')
                .add('g')
                .build();
        assertEquals(rangeMap.size(), 6);
    }
}