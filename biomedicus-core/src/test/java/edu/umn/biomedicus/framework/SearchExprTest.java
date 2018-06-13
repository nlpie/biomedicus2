/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.framework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.Label;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.LabelMetadata;
import edu.umn.nlpengine.Span;
import edu.umn.nlpengine.StandardLabelIndex;
import edu.umn.nlpengine.TextRange;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.jupiter.api.Test;

/**
 *
 */
class SearchExprTest {

  @Mocked
  Document document;

  @Mocked
  LabelAliases labelAliases;

  @Mocked
  LabelIndex labelIndex;

  private Span span = new Span(5, 10);

  private Span span1 = new Span(5, 7);

  private Span span2 = new Span(7, 10);

  private Blah label = new Blah(span);

  private Blah label1 = new Blah(span1);

  private Blah label2 = new Blah(span2);

  @Test
  void testMatchType() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      document.labelIndex(Blah.class); result = labelIndex;
      labelIndex.first(); result = label;
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertEquals(searcher.getSpan().get(), span);
  }

  @Test
  void testNoMatchType() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      document.labelIndex(Blah.class); result = labelIndex;
      labelIndex.first(); result = null;
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  void testNoTextBeforeMatch() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 13;
      document.getText(); result = "this is text.";
      document.labelIndex(Blah.class); result = labelIndex;
      labelIndex.first(); returns(
          new Blah(0, 4),
          new Blah(5, 7)
      );
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah -> Blah");

    Searcher searcher = blah.createSearcher(document);
    assertTrue(searcher.search());
  }

  @Test
  void testNoTextBeforeNoMatch() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 13;
      document.getText(); result = "this is text.";
      document.labelIndex(Blah.class); result = labelIndex;
      labelIndex.first(); returns(
          new Blah(0, 4),
          new Blah(8, 12)
      );
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah -> Blah");

    Searcher searcher = blah.createSearcher(document);
    assertFalse(searcher.search());
  }

  @Test
  void testMatchPin() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      document.labelIndex(Blah.class); result = labelIndex;
      labelIndex.first(); returns(label, label1, label2);
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "[Blah Blah Blah]");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertEquals(searcher.getSpan().get(), span);
  }

  @Test
  void testNoMatchPin() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      document.labelIndex(Blah.class); result = labelIndex;
      labelIndex.first(); returns(label, null);
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "[Blah Blah]");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  void testStringPropertyMatch() {
    Foo foo = new Foo(0, 5);
    foo.setValue("bar");

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"bar\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));
  }

  @Test
  void testStringPropertyNoMatch() {
    Foo foo = new Foo(0, 5);
    foo.setValue("baz");

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"bar\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  void testStringPropertyAlternation() {
    Foo foo = new Foo(0, 5);
    foo.setValue("bar");

    Foo foo2 = new Foo(6, 10);
    foo2.setValue("baz");

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); returns(foo, foo2);
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"baz\"|\"bar\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));

    searcher.search();
    opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(6, 10));
  }

  @Test
  void testStringPropertyAlternationMiss() {
    Foo foo = new Foo(0, 5);
    foo.setValue("baz");

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"bar\"|\"abc\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  void testRegexPropertyMatch() {
    Foo foo = new Foo(0, 5);
    foo.setValue("aaa");

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=r\"a*\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));
  }

  @Test
  void testRegexPropertyMiss() {
    Foo foo = new Foo(0, 5);
    foo.setValue("baz");

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=r\"a*\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  void testRegexAlternations() {
    Foo foo = new Foo(0, 5);
    foo.setValue("aaa");

    Foo foo2 = new Foo(6, 10);
    foo2.setValue("baz");

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); returns(foo, foo2);
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"baz\"|r\"a*\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));

    searcher.search();
    opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(6, 10));
  }

  @Test
  void testCaseInsensitiveMatch() {
    Foo foo = new Foo(0, 5);
    foo.setValue("BAZ");

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=i\"baz\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));
  }

  @Test
  void testCaseInsensitiveNoMatch() {
    Foo foo = new Foo(0, 5);
    foo.setValue("baz");

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=i\"bar\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  void testCaseInsensitiveAlternationsFirst() {
    Foo foo = new Foo(0, 5);
    foo.setValue("aaa");

    Foo foo2 = new Foo(6, 10);
    foo2.setValue("BAZ");

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); returns(foo, foo2);
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=i\"baz\"|r\"a*\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));

    searcher.search();
    opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(6, 10));
  }

  @Test
  void testCaseInsensitiveAlternationsOther() {
    Foo foo = new Foo(0, 5);
    foo.setValue("bar");

    Foo foo2 = new Foo(6, 10);
    foo2.setValue("baz");

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); returns(foo, foo2);
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"baz\"|i\"bar\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));

    searcher.search();
    opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(6, 10));
  }



  @Test
  void testNumberPropertyMatch() {
    Foo foo = new Foo(0, 5);
    foo.setBaz(5);

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getBaz=5>");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));
  }

  @Test
  void testNumberPropertyNegative() {
    Foo foo = new Foo(0, 5);
    foo.setBaz(-5);

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getBaz=-5>");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));
  }

  @Test
  void testNumberPropertyNoMatch() {
    Foo foo = new Foo(0, 5);
    foo.setBaz(3);

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getBaz=4>");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  void testNumberPropertyAlternation() {
    Foo foo = new Foo(0, 5);
    foo.setBaz(3);

    Foo foo2 = new Foo(6, 10);
    foo2.setBaz(4);

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); returns(foo, foo2);
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getBaz=4|3>");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));

    searcher.search();
    opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(6, 10));
  }

  @Test
  void testNumberPropertyAlternationMiss() {
    Foo foo = new Foo(0, 5);
    foo.setBaz(5);

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getBaz=3|4>");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  enum BAZ {
    FOO,
    BAR
  }

  @LabelMetadata(classpath = "test")
  static class HasEnum extends Label {

    private final int startIndex;
    private final int endIndex;

    BAZ baz;

    HasEnum(int startIndex, int endIndex) {
      this.startIndex = startIndex;
      this.endIndex = endIndex;
    }

    public BAZ getBaz() {
      return baz;
    }

    @Override
    public int getStartIndex() {
      return startIndex;
    }

    @Override
    public int getEndIndex() {
      return endIndex;
    }
  }

  @Test
  void testEnumPropertyMatch() {
    HasEnum hasEnum = new HasEnum(0, 5);
    hasEnum.baz = BAZ.FOO;

    LabelIndex<HasEnum> index = StandardLabelIndex.create(HasEnum.class, hasEnum);

    new Expectations() {{
      labelAliases.getLabelable("HasEnum"); result = HasEnum.class;


      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      document.labelIndex(HasEnum.class); result = index;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "HasEnum<getBaz=eFOO>");

    Searcher searcher = blah.createSearcher(document);

    assertTrue(searcher.search());
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testEnumPropertyMiss() {
    HasEnum hasEnum = new HasEnum(0, 5);
    hasEnum.baz = BAZ.BAR;

    LabelIndex<HasEnum> index = StandardLabelIndex.create(HasEnum.class, hasEnum);

    new Expectations() {{
      labelAliases.getLabelable("HasEnum"); result = HasEnum.class;


      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      document.labelIndex(HasEnum.class); result = index;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "HasEnum<getBaz=eFOO>");

    Searcher searcher = blah.createSearcher(document);

    assertFalse(searcher.search());
  }

  @Test
  void testPropertyMatchNull() {
    Foo foo = new Foo(0, 5);

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"abc\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  void testMultiProperties() {
    Foo foo = new Foo(0, 5);
    foo.setValue("baz");
    foo.setBaz(42);

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"baz\",getBaz=42>");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> span = searcher.getSpan();
    assertTrue(span.isPresent());
    assertEquals(span.get(), new Span(0, 5));
  }

  @Test
  void testAlternations() {
    Blah label = new Blah(0, 5);

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); returns(null, label);
      labelAliases.getLabelable("Foo"); result = Foo.class;
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo Foo | Foo | Blah");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));
  }

  @Test
  void testEmpty() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertTrue(searcher.found());
    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 0));
  }

  @Test
  void testLabelVariable() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = label;
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "instance:Blah");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Label opt = searcher.getLabel("instance");
    assertNotNull(opt);
    assertEquals(opt, label);
  }

  @Test
  void testRepeatLabelVariable() {
    Foo fooLabel = new Foo(0, 3);

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); returns(label, null, fooLabel);
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "instance:Blah | instance:Foo");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    TextRange opt = searcher.getLabel("instance");
    assertNotNull(opt);
    assertEquals(opt, label);

    searcher = blah.createSearcher(document);
    searcher.search();

    opt = searcher.getLabel("instance");
    assertNotNull(opt);
    assertEquals(opt, fooLabel);
  }

  @Test
  void testRepeatingGroupName() {
    Foo fooLabel = new Foo(0, 3);

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); returns(label, null, fooLabel);
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "(?<instance> Blah) | (?<instance> Foo)");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Span instance = searcher.getSpan("instance");
    assertNotNull(instance);
    assertTrue(instance.locationEquals(label));

    searcher = blah.createSearcher(document);
    searcher.search();

    instance = searcher.getSpan("instance");
    assertNotNull(instance);
    assertTrue(instance.locationEquals(fooLabel));
  }

  @Test
  void testLabelGroup() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = label;
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "(?<instance>Blah)");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Span opt = searcher.getSpan("instance");
    assertNotNull(opt);
    assertEquals(opt, label.toSpan());
  }

  @Test
  void testOptionMissing() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = null;
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah?");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertTrue(searcher.found());
  }

  @Test
  void testPositiveLookaheadPass() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); returns(new Blah(0, 5), new Foo(6, 8));
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah(?=Foo)");
    Searcher searcher = blah.createSearcher(document);
    boolean search = searcher.search();

    assertTrue(search);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testPositiveLookaheadFail() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); result = new Blah(0, 5);
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah(?=Foo)");
    Searcher searcher = blah.createSearcher(document);
    boolean search = searcher.search();

    assertTrue(search);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testNegativeLookaheadPass() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;
      labelIndex.first(); returns(new Blah(0, 5), null);
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah(?!Foo)");
    Searcher searcher = blah.createSearcher(document);
    boolean search = searcher.search();

    assertTrue(search);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testNegativeLookaheadFail() {
    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 10;

      labelIndex.first(); returns(new Blah(0, 5), new Foo(6, 8));

      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah(?!Foo)");
    Searcher searcher = blah.createSearcher(document);
    boolean search = searcher.match();

    assertFalse(search);
  }

  @Test
  void testFallback() {
    Foo foo1 = new Foo(0, 5);
    foo1.setBaz(10);

    Foo foo2 = new Foo(6, 8);
    foo2.setBaz(10);

    Foo foo3 = new Foo(9, 13);
    foo3.setBaz(10);

    Foo fourteenFoo = new Foo(14, 18);
    fourteenFoo.setBaz(14);

    StandardLabelIndex<Foo> foos = new StandardLabelIndex<>(Foo.class, foo1, foo2, foo3,
        fourteenFoo);

    new Expectations() {{
      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 30;

      document.labelIndex(Foo.class); result = foos; minTimes = 1;

      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "[?Foo<getBaz=10>] Foo<getBaz=14>");
    Searcher searcher = blah.createSearcher(document);
    boolean search = searcher.search();

    assertTrue(search);
    assertEquals(searcher.getBegin(), 9);
    assertEquals(searcher.getEnd(), 18);
  }

  @Test
  void testAtomicLazyOptional() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "opt:Blah?? Blah*");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
    Span span = searcher.getSpan("opt");
    assertNull(span);
  }

  @Test
  void testAtomicPossessiveOptional() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(0, 5), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah?+ Blah");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  void testAtomicGreedyOptional() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), null, new Blah(0, 5), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah? opt:Blah*");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
    Span span = searcher.getSpan("opt");
    assertNull(span);
  }

  @Test
  void testAtomicPossessiveKleene() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah*+ Blah");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  void testAtomicLazyKleene() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "opt:Blah*? Blah*");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
    Span span = searcher.getSpan("opt");
    assertNull(span);
  }

  @Test
  void testAtomicGreedyKleene() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah* opt:Blah*");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
    Span span = searcher.getSpan("opt");
    assertNull(span);
  }

  @Test
  void testAtomicPossessiveOnePlusFail() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah++ Blah");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  void testAtomicPossessiveOnePlusMatch() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah++");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
  }

  @Test
  void testAtomicLazyOnePlus() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah+?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testAtomicPossessiveCurly() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,3}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
  }

  @Test
  void testAtomicPossessiveCurlyNoMaxPass() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2,}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
  }

  @Test
  void testAtomicPossessiveCurlyNoMaxFail() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2,}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  void testAtomicPossessiveCurlyExactBelow() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  void testAtomicPossessiveCurlyExact() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
  }

  @Test
  void testAtomicPossessiveCurlyExactAbove() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), new Blah(11, 13), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  void testAtomicLazyCurly() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), new Blah(11, 13), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,3}?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testAtomicLazyCurlyNoMax() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), new Blah(11, 13), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,}?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testAtomicLazyCurlyExact() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), new Blah(11, 13), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1}?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testAtomicGreedyCurly() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), new Blah(11, 13), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,3}");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 13);
  }

  @Test
  void testAtomicGreedyCurlyNoMax() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), new Blah(11, 13), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,}");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 13);
  }

  @Test
  void testAtomicGreedyCurlyExact() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 100;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), new Blah(11, 13), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1}");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testReluctanceAndMatch() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 13;

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), new Blah(11, 13), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,}?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean match = searcher.match();

    assertTrue(match);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 13);
  }

  @Test
  void testContainsTrue() {
    List<Foo> foos = Collections.singletonList(new Foo(0, 6));

    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 13;

      labelIndex.first(); result = new Blah(2, 5);

      labelIndex.iterator(); result = foos.iterator();
    }};

    SearchExpr expr = SearchExpr.parse(labelAliases, "Blah[^Foo]");

    Searcher searcher = expr.createSearcher(document);
    boolean find = searcher.search();
    assertTrue(find);
    assertEquals(searcher.getBegin(), 2);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testContainsFalse() {
    List<Foo> foos = Collections.emptyList();

    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 13;

      labelIndex.first(); result = new Blah(2, 5);

      labelIndex.iterator(); result = foos.iterator();
    }};

    SearchExpr expr = SearchExpr.parse(labelAliases, "Blah[^Foo]");

    Searcher searcher = expr.createSearcher(document);
    boolean find = searcher.search();
    assertFalse(find);
  }

  @Test
  void testOptionalFindsFirst() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 13;

      labelIndex.first(); returns(new Foo(3, 4), new Blah(6, 7), new Foo(8, 9), new Blah(6, 7),
          new Foo(8, 9));
    }};

    SearchExpr expr = SearchExpr.parse(labelAliases, "Blah? Foo");

    Searcher searcher = expr.createSearcher(document);

    boolean find = searcher.search();
    assertTrue(find);
    assertEquals(searcher.getBegin(), 3);
    assertEquals(searcher.getEnd(), 4);

    assertTrue(searcher.search());
    assertEquals(searcher.getBegin(), 6);
    assertEquals(searcher.getEnd(), 9);
  }

  @Test
  void testGreedyLoopPreemption() {
    LabelIndex<Foo> fooLabelIndex = StandardLabelIndex.create(Foo.class, new Foo(0, 5), new Foo(20, 25));
    LabelIndex<Blah> blahs = StandardLabelIndex.create(Blah.class, new Blah(10, 14), new Blah(15, 20));

    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 25;

      document.labelIndex(Foo.class); result = fooLabelIndex; minTimes = 1;
      document.labelIndex(Blah.class); result = blahs; minTimes = 1;
    }};

    SearchExpr expr = SearchExpr.parse(labelAliases, "Blah* Foo");

    Searcher searcher = expr.createSearcher(document);

    assertTrue(searcher.search());
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);

    assertTrue(searcher.search());
    assertEquals(searcher.getBegin(), 10);
    assertEquals(searcher.getEnd(), 25);

    assertFalse(searcher.search());
  }

  @Test
  void testPossessiveLoopPreemption() {
    LabelIndex<Foo> fooLabelIndex = StandardLabelIndex.create(Foo.class, new Foo(0, 5), new Foo(20, 25));
    LabelIndex<Blah> blahs = StandardLabelIndex.create(Blah.class, new Blah(10, 14), new Blah(15, 20));

    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 25;

      document.labelIndex(Foo.class); result = fooLabelIndex; minTimes = 1;
      document.labelIndex(Blah.class); result = blahs; minTimes = 1;
    }};

    SearchExpr expr = SearchExpr.parse(labelAliases, "Blah*+ Foo");

    Searcher searcher = expr.createSearcher(document);

    assertTrue(searcher.search());
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);

    assertTrue(searcher.search());
    assertEquals(searcher.getBegin(), 10);
    assertEquals(searcher.getEnd(), 25);

    assertFalse(searcher.search());
  }

  @Test
  void testAlternationsProperOrdering() {
    LabelIndex<Foo> fooLabelIndex = StandardLabelIndex.create(Foo.class, new Foo(0, 5), new Foo(20, 25));
    LabelIndex<Blah> blahs = StandardLabelIndex.create(Blah.class, new Blah(10, 14), new Blah(15, 20));

    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;

      document.getStartIndex(); result = 0;
      document.getEndIndex(); result = 25;

      document.labelIndex(Foo.class); result = fooLabelIndex; minTimes = 1;
      document.labelIndex(Blah.class); result = blahs; minTimes = 1;
    }};

    SearchExpr expr = SearchExpr.parse(labelAliases, "Blah | Foo");

    Searcher searcher = expr.createSearcher(document);

    assertTrue(searcher.search());
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);

    assertTrue(searcher.search());
    assertEquals(searcher.getBegin(), 10);
    assertEquals(searcher.getEnd(), 14);

    assertTrue(searcher.search());
    assertEquals(searcher.getBegin(), 15);
    assertEquals(searcher.getEnd(), 20);

    assertTrue(searcher.search());
    assertEquals(searcher.getBegin(), 20);
    assertEquals(searcher.getEnd(), 25);

    assertFalse(searcher.search());
  }



  @LabelMetadata(classpath = "biomedicus.v2")
  static class Blah extends Label {

    private int startIndex;
    private int endIndex;

    Blah(int startIndex, int endIndex) {
      this.startIndex = startIndex;
      this.endIndex = endIndex;
    }

    Blah(TextRange label) {
      startIndex = label.getStartIndex();
      endIndex = label.getEndIndex();
    }

    @Override
    public int getStartIndex() {
      return startIndex;
    }

    @Override
    public int getEndIndex() {
      return endIndex;
    }
  }

  @LabelMetadata(classpath = "biomedicus.v2")
  public static class Foo extends Label {

    private String value;
    private int baz;
    private int startIndex;
    private int endIndex;

    Foo(int startIndex, int endIndex) {
      this.startIndex = startIndex;
      this.endIndex = endIndex;
    }

    public Foo(TextRange label) {
      startIndex = label.getStartIndex();
      endIndex = label.getEndIndex();
    }

    public String getValue() {
      return value;
    }

    void setValue(String value) {
      this.value = value;
    }

    public int getBaz() {
      return baz;
    }

    void setBaz(int baz) {
      this.baz = baz;
    }

    @Override
    public int getStartIndex() {
      return startIndex;
    }

    @Override
    public int getEndIndex() {
      return endIndex;
    }
  }
}
