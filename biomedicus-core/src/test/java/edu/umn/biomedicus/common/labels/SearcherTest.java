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

package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.Span;
import mockit.Expectations;
import mockit.Mock;
import mockit.Mocked;
import mockit.Verifications;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.*;

/**
 *
 */
public class SearcherTest {
    static class Blah {

    }

    @Mocked
    Document document;

    @Mocked
    LabelAliases labelAliases;

    @Mocked
    LabelIndex labelIndex;

    Span span = new Span(5, 10);
    Span span1 = new Span(5, 7);
    Span span2 = new Span(7, 10);

    Label<Blah> label = new Label<>(span, new Blah());
    Label<Blah> label1 = new Label<>(span1, new Blah());
    Label<Blah> label2 = new Label<>(span2, new Blah());

    @Test
    public void testMatchType() throws Exception {
        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
            labelIndex.first(); result = Optional.of(label);
            labelAliases.getLabelable("Blah"); result = Blah.class;
        }};

        Searcher blah = Searcher.parse(labelAliases, "Blah");

        Search search = blah.search(document);
        assertEquals(search.getSpan(), span);
    }

    @Test
    public void testMatchPin() throws Exception {
        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
            labelIndex.first(); returns(Optional.of(label), Optional.of(label1),
                    Optional.of(label2));
            labelAliases.getLabelable("Blah"); result = Blah.class;
        }};

        Searcher blah = Searcher.parse(labelAliases, "[Blah Blah Blah]");

        Search search = blah.search(document);
        assertEquals(search.getSpan(), span);
    }
}