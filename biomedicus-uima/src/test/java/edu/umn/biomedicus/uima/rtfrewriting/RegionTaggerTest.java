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

package edu.umn.biomedicus.uima.rtfrewriting;

import edu.umn.biomedicus.uima.rtfrewriting.RegionTagger;
import edu.umn.biomedicus.uima.rtfrewriting.RtfRewriterCursor;
import edu.umn.biomedicus.uima.rtfrewriting.SymbolIndexedDocument;
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