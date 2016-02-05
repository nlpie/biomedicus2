package edu.umn.biomedicus.tools.rtfrewriting;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for {@link SpecialTableMarker}.
 */
public class SpecialTableMarkerTest {

    @Test
    public void testInsertInTable() throws Exception {
        String document = "{\\atbl;1;2;3;}";

        SpecialTableMarker specialTableMarker = new SpecialTableMarker(document, "\\u2222221B", "\\atbl");

        assertEquals(specialTableMarker.insertInTable(), "{\\atbl;1;2;3;\\u2222221B 4}");
    }
}