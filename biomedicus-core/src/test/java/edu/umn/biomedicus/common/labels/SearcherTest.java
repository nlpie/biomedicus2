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
import mockit.Mocked;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.*;

/**
 *
 */
public class SearcherTest {
    static class Blah {

    }

    static class Foo {
        private String value;
        private int baz;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getBaz() {
            return baz;
        }

        public void setBaz(int baz) {
            this.baz = baz;
        }
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

        Search search = blah.createSearcher(document);
        search.search();

        assertEquals(search.getSpan().get(), span);
    }

    @Test
    public void testNoMatchType() throws Exception {
        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
            labelIndex.first(); result = Optional.empty();
            labelAliases.getLabelable("Blah"); result = Blah.class;
        }};

        Searcher blah = Searcher.parse(labelAliases, "Blah");

        Search search = blah.createSearcher(document);
        search.search();

        assertFalse(search.found());
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

        Search search = blah.createSearcher(document);
        search.search();

        assertEquals(search.getSpan().get(), span);
    }

    @Test
    public void testNoMatchPin() throws Exception {
        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
            labelIndex.first(); returns(Optional.of(label), Optional.empty());
            labelAliases.getLabelable("Blah"); result = Blah.class;
        }};

        Searcher blah = Searcher.parse(labelAliases, "[Blah Blah]");


        Search search = blah.createSearcher(document);
        search.search();

        assertFalse(search.found());
    }

    @Test
    public void testStringPropertyMatch() throws Exception {
        Foo foo = new Foo();
        foo.setValue("bar");

        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
            labelIndex.first(); returns(Optional.of(new Label<>(new Span(0, 5), foo)));
            labelAliases.getLabelable("Foo"); result = Foo.class;
        }};

        Searcher blah = Searcher.parse(labelAliases,
                "Foo{value=\"bar\"}");

        Search search = blah.createSearcher(document);
        search.search();

        assertEquals(search.getSpan().get(), new Span(0, 5));
    }

    @Test
    public void testStringPropertyNoMatch() throws Exception {
        Foo foo = new Foo();
        foo.setValue("baz");

        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
            labelIndex.first(); returns(Optional.of(new Label<>(new Span(0, 5), foo)));
            labelAliases.getLabelable("Foo"); result = Foo.class;
        }};

        Searcher blah = Searcher.parse(labelAliases,
                "Foo{value=\"bar\"}");

        Search search = blah.createSearcher(document);
        search.search();

        assertFalse(search.found());
    }

    @Test
    public void testMultiProperties() throws Exception {
        Foo foo = new Foo();
        foo.setValue("baz");
        foo.setBaz(42);

        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
            labelIndex.first(); returns(Optional.of(new Label<>(new Span(0, 5), foo)));
            labelAliases.getLabelable("Foo"); result = Foo.class;
        }};

        Searcher blah = Searcher.parse(labelAliases,
                "Foo{value=\"baz\",baz=42}");

        Search search = blah.createSearcher(document);
        search.search();

        assertEquals(search.getSpan().get(), new Span(0, 5));
    }

    @Test
    public void testAlternations() throws Exception {
        Optional<Label<Blah>> label = Optional
                .of(new Label<>(new Span(0, 5), new Blah()));

        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
            labelIndex.first(); returns(label);
            labelAliases.getLabelable("Foo"); result = Foo.class;
            labelAliases.getLabelable("Blah"); result = Blah.class;
        }};

        Searcher blah = Searcher.parse(labelAliases,
                "Foo | Blah");

        Search search = blah.createSearcher(document);
        search.search();

        assertEquals(search.getSpan().get(), new Span(0, 5));
    }

    @Test
    public void testEmpty() throws Exception {
        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
        }};

        Searcher blah = Searcher.parse(labelAliases, "");

        Search search = blah.createSearcher(document);
        search.search();

        assertTrue(search.found());
        assertEquals(search.getSpan().get(), new Span(0, 0));
    }

    @Test
    public void testLabelVariable() throws Exception {
        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
            labelIndex.first(); result = Optional.of(label);
            labelAliases.getLabelable("Blah"); result = Blah.class;
        }};

        Searcher blah = Searcher.parse(labelAliases, "instance:Blah");

        Search search = blah.createSearcher(document);
        search.search();

        assertEquals(search.getLabel("instance").get(), label);
    }

    @Test
    public void testLabelGroup() throws Exception {
        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
            labelIndex.first(); result = Optional.of(label);
            labelAliases.getLabelable("Blah"); result = Blah.class;
        }};

        Searcher blah = Searcher.parse(labelAliases, "(?<instance>Blah)");

        Search search = blah.createSearcher(document);
        search.search();

        assertEquals(search.getSpan("instance").get(), label.toSpan());
    }

    enum AnEnum {
        VALUE;
    }

    @Test
    public void testAnEnum() throws Exception {
        Label<AnEnum> label = new Label<>(new Span(0,5), AnEnum.VALUE);

        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
            labelIndex.first(); result = Optional.of(label);
            labelAliases.getLabelable("AnEnum"); result = AnEnum.class;
        }};

        Searcher blah = Searcher.parse(labelAliases, "(instance:AnEnum=VALUE)");

        Search search = blah.createSearcher(document);
        search.search();

        assertEquals(search.getSpan("instance").get(), label.toSpan());
    }

    @Test
    public void testOptionMissing() throws Exception {
        new Expectations() {{
            document.getDocumentSpan(); result = new Span(0, 10);
            labelIndex.first(); result = Optional.empty();
            labelAliases.getLabelable("Blah"); result = Blah.class;
        }};

        Searcher blah = Searcher.parse(labelAliases, "Blah?");

        Search search = blah.createSearcher(document);
        search.search();

        assertTrue(search.found());
    }
}
