package edu.umn.biomedicus.tools.rtfrewriting;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for {@link SymbolIndexedDocument}.
 */
public class SymbolIndexedDocumentTest {
    @Tested SymbolIndexedDocument symbolIndexedDocument;

    @Injectable List<SymbolLocation> symbolLocations;

    @Injectable Map<String, Map<Integer, Integer>> destinationMap;

    @Injectable String document;

    @Mocked SymbolLocation symbolLocation;


    @Test
    public void testGetOriginalDocumentIndex() throws Exception {
        new Expectations() {{
            symbolLocation.getIndex(); result = 4;
            symbolLocations.get(anyInt); returns(symbolLocation, symbolLocation, symbolLocation, symbolLocation);
            symbolLocation.getOffset(); returns(4, 4, 4, 4, 4);
            symbolLocation.getLength(); returns(3, 3, 3, 3);
        }};

        int originalDocumentIndex = symbolIndexedDocument.getOriginalDocumentIndex(symbolLocation);

        assertEquals(originalDocumentIndex, 32);
    }
}