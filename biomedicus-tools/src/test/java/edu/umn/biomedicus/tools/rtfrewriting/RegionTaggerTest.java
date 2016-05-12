package edu.umn.biomedicus.tools.rtfrewriting;

import mockit.Expectations;
import mockit.FullVerificationsInOrder;
import mockit.Mocked;
import org.testng.annotations.Test;

public class RegionTaggerTest {
    @Mocked
    SymbolIndexedDocument symbolIndexedDocument;

    @Mocked
    RtfRewriterCursor rtfRewriterCursor;

    @Test
    public void testOutsideDestination() throws Exception {
        new Expectations() {{
            symbolIndexedDocument.symbolIndex(12, "dest"); result = 3;
            symbolIndexedDocument.symbolIndex(42, "dest"); result = 5;
            new RtfRewriterCursor(symbolIndexedDocument); result = rtfRewriterCursor;
            rtfRewriterCursor.setSymbolIndex(3);
            rtfRewriterCursor.insertBefore("bTag");
            rtfRewriterCursor.getSymbolIndex(); returns(3, 5);
            rtfRewriterCursor.nextIsOutsideDestination("dest"); result = true;
            rtfRewriterCursor.insertAfter("eTag");
            rtfRewriterCursor.forward();
            rtfRewriterCursor.advanceToDestination("dest");
            rtfRewriterCursor.insertBefore("bTag");
        }};

        RegionTagger regionTagger = new RegionTagger(symbolIndexedDocument, "dest", 12, 43, "bTag", "eTag");
        regionTagger.tagRegion();

        new FullVerificationsInOrder() {{
            rtfRewriterCursor.insertAfter("eTag");
        }};
    }
}