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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.Label;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.LabelMetadata;
import edu.umn.nlpengine.Span;
import edu.umn.nlpengine.StandardLabelIndex;
import edu.umn.nlpengine.TextRange;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

/**
 *
 */
class SearchExprTest {

  Document document;

  LabelAliases labelAliases;

  Span span = new Span(5, 10);

  Span span1 = new Span(5, 7);

  Span span2 = new Span(7, 10);

  Blah label = new Blah(span);

  Blah label1 = new Blah(span1);

  Blah label2 = new Blah(span2);

  @BeforeEach
  void setUp() {
    document = mock(Document.class);
    labelAliases = mock(LabelAliases.class);
    Answer<Class<? extends Label>> blahAnswer = invocationOnMock -> Blah.class;
    when(labelAliases.getLabelable("Blah")).thenAnswer(blahAnswer);
    Answer<Class<? extends Label>> fooAnswer = invocationOnMock -> Foo.class;
    when(labelAliases.getLabelable("Foo")).thenAnswer(fooAnswer);
    Answer<Class<? extends Label>> hasEnumAnswer = invocationOnMock -> HasEnum.class;
    when(labelAliases.getLabelable("HasEnum")).thenAnswer(hasEnumAnswer);
  }

  @Test
  void testMatchType() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Blah.class)).thenReturn(StandardLabelIndex.create(Blah.class, label));

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertEquals(searcher.getSpan().get(), span);
  }

  @Test
  void testNoMatchType() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Blah.class)).thenReturn(StandardLabelIndex.create(Blah.class));

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  void testNoTextBeforeMatch() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(13);
    when(document.getText()).thenReturn("this is text.");
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 4), new Blah(5, 7)));

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah -> Blah");

    Searcher searcher = blah.createSearcher(document);
    assertTrue(searcher.search());
  }

  @Test
  void testNoTextBeforeNoMatch() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(13);
    when(document.getText()).thenReturn("this is text.");
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 4), new Blah(8, 12)));

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah -> Blah");

    Searcher searcher = blah.createSearcher(document);
    assertFalse(searcher.search());
  }

  @Test
  void testMatchPin() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(5, 10)));
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class, new Foo(5, 7), new Foo(7, 10)));

    SearchExpr blah = SearchExpr.parse(labelAliases, "[Blah Foo Foo]");

    Searcher searcher = blah.createSearcher(document);
    assertTrue(searcher.search());

    assertEquals(searcher.getSpan().get(), Span.create(5, 10));
  }

  @Test
  void testNoMatchPin() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Blah.class)).thenReturn(StandardLabelIndex.create(Blah.class));

    SearchExpr blah = SearchExpr.parse(labelAliases, "[Blah Blah]");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  void testStringPropertyMatch() {
    Foo foo = new Foo(0, 5);
    foo.setValue("bar");
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class, foo, foo2));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"bar\"|\"abc\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  void testRegexPropertyMatch() {
    Foo foo = new Foo(0, 5);
    foo.setValue("aaa");

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class, foo, foo2));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class, foo, foo2));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class, foo, foo2));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class, foo, foo2));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getBaz=3|4>");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  void testEnumPropertyMatch() {
    HasEnum hasEnum = new HasEnum(0, 5);
    hasEnum.baz = BAZ.FOO;

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(HasEnum.class))
        .thenReturn(StandardLabelIndex.create(HasEnum.class, hasEnum));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(HasEnum.class))
        .thenReturn(StandardLabelIndex.create(HasEnum.class, hasEnum));

    SearchExpr blah = SearchExpr.parse(labelAliases, "HasEnum<getBaz=eFOO>");

    Searcher searcher = blah.createSearcher(document);

    assertFalse(searcher.search());
  }

  @Test
  void testPropertyMatchNull() {
    Foo foo = new Foo(0, 5);

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class)).thenReturn(StandardLabelIndex.create(Foo.class, foo));

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"baz\",getBaz=42>");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> span = searcher.getSpan();
    assertTrue(span.isPresent());
    assertEquals(span.get(), new Span(0, 5));
  }

  @Test
  void testAlternations() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class));
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5)));

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo Foo | Foo | Blah");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));
  }

  @Test
  void testEmpty() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);

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
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Blah.class)).thenReturn(StandardLabelIndex.create(Blah.class, label));

    SearchExpr blah = SearchExpr.parse(labelAliases, "instance:Blah");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Label opt = searcher.getLabel("instance");
    assertNotNull(opt);
    assertEquals(opt, label);
  }

  @Test
  void testLabelGroup() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Blah.class)).thenReturn(StandardLabelIndex.create(Blah.class, label));

    SearchExpr blah = SearchExpr.parse(labelAliases, "(?<instance>Blah)");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Span opt = searcher.getSpan("instance");
    assertNotNull(opt);
    assertEquals(opt, label.toSpan());
  }

  @Test
  void testOptionMissing() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Blah.class)).thenReturn(StandardLabelIndex.create(Blah.class));

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah?");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertTrue(searcher.found());
  }

  @Test
  void testPositiveLookaheadPass() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5)));
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class, new Foo(6, 8)));

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah(?=Foo)");
    Searcher searcher = blah.createSearcher(document);
    boolean search = searcher.search();

    assertTrue(search);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testPositiveLookaheadFail() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5)));
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class));

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah(?=Foo)");
    Searcher searcher = blah.createSearcher(document);
    boolean search = searcher.search();

    assertFalse(search);
  }

  @Test
  void testNegativeLookaheadPass() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5)));
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class));

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah(?!Foo)");
    Searcher searcher = blah.createSearcher(document);
    boolean search = searcher.search();

    assertTrue(search);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testNegativeLookaheadFail() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(10);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5)));
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class, new Foo(6, 8)));

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

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(25);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5)));
    when(document.labelIndex(Foo.class)).thenReturn(foos);

    SearchExpr blah = SearchExpr.parse(labelAliases, "[?Foo<getBaz=10>] Foo<getBaz=14>");
    Searcher searcher = blah.createSearcher(document);
    boolean search = searcher.search();

    assertTrue(search);
    assertEquals(searcher.getBegin(), 9);
    assertEquals(searcher.getEnd(), 18);
  }

  @Test
  void testAtomicLazyOptional() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5)));

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
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5), new Blah(0, 5)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah?+ Blah");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  void testAtomicGreedyOptional() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5), new Blah(0, 5)));

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
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5), new Blah(6, 10)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah*+ Blah");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  void testAtomicLazyKleene() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5), new Blah(6, 10)));

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
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5), new Blah(6, 10)));

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
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5), new Blah(6, 10)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah++ Blah");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  void testAtomicPossessiveOnePlusMatch() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5), new Blah(6, 10)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah++");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
  }

  @Test
  void testAtomicLazyOnePlus() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5), new Blah(6, 10)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah+?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testAtomicPossessiveCurly() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5), new Blah(6, 10)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,3}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
  }

  @Test
  void testAtomicPossessiveCurlyNoMaxPass() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5), new Blah(6, 10)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2,}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
  }

  @Test
  void testAtomicPossessiveCurlyNoMaxFail() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2,}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  void testAtomicPossessiveCurlyExactBelow() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  void testAtomicPossessiveCurlyExact() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(0, 5), new Blah(6, 10)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
  }

  @Test
  void testAtomicPossessiveCurlyExactAbove() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex
            .create(Blah.class, new Blah(0, 5), new Blah(6, 10), new Blah(11, 13)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  void testAtomicLazyCurly() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex
            .create(Blah.class, new Blah(0, 5), new Blah(6, 10), new Blah(11, 13)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,3}?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testAtomicLazyCurlyNoMax() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex
            .create(Blah.class, new Blah(0, 5), new Blah(6, 10), new Blah(11, 13)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,}?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testAtomicLazyCurlyExact() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex
            .create(Blah.class, new Blah(0, 5), new Blah(6, 10), new Blah(11, 13)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1}?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testAtomicGreedyCurly() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex
            .create(Blah.class, new Blah(0, 5), new Blah(6, 10), new Blah(11, 13)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,3}");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 13);
  }

  @Test
  void testAtomicGreedyCurlyNoMax() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex
            .create(Blah.class, new Blah(0, 5), new Blah(6, 10), new Blah(11, 13)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,}");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 13);
  }

  @Test
  void testAtomicGreedyCurlyExact() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(100);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex
            .create(Blah.class, new Blah(0, 5), new Blah(6, 10), new Blah(11, 13)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1}");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testReluctanceAndMatch() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(13);
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex
            .create(Blah.class, new Blah(0, 5), new Blah(6, 10), new Blah(11, 13)));

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,}?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean match = searcher.match();

    assertTrue(match);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 13);
  }

  @Test
  void testContainsTrue() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(13);
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class, new Foo(0, 6)));
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(2, 5)));

    SearchExpr expr = SearchExpr.parse(labelAliases, "Blah[^Foo]");

    Searcher searcher = expr.createSearcher(document);
    boolean find = searcher.search();
    assertTrue(find);
    assertEquals(searcher.getBegin(), 2);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  void testContainsFalse() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(13);
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class));
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(2, 5)));

    SearchExpr expr = SearchExpr.parse(labelAliases, "Blah[^Foo]");

    Searcher searcher = expr.createSearcher(document);
    boolean find = searcher.search();
    assertFalse(find);
  }

  @Test
  void testOptionalFindsFirst() {
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(13);
    when(document.labelIndex(Foo.class))
        .thenReturn(StandardLabelIndex.create(Foo.class, new Foo(3, 4), new Foo(8, 9)));
    when(document.labelIndex(Blah.class))
        .thenReturn(StandardLabelIndex.create(Blah.class, new Blah(6, 7)));

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
    LabelIndex<Foo> fooLabelIndex = StandardLabelIndex
        .create(Foo.class, new Foo(0, 5), new Foo(20, 25));
    LabelIndex<Blah> blahs = StandardLabelIndex
        .create(Blah.class, new Blah(10, 14), new Blah(15, 20));

    when(document.labelIndex(Foo.class)).thenReturn(fooLabelIndex);
    when(document.labelIndex(Blah.class)).thenReturn(blahs);
    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(25);

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
    LabelIndex<Foo> fooLabelIndex = StandardLabelIndex
        .create(Foo.class, new Foo(0, 5), new Foo(20, 25));
    LabelIndex<Blah> blahs = StandardLabelIndex
        .create(Blah.class, new Blah(10, 14), new Blah(15, 20));

    when(document.labelIndex(Blah.class)).thenReturn(blahs);
    when(document.labelIndex(Foo.class)).thenReturn(fooLabelIndex);

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(25);

    SearchExpr expr = SearchExpr.parse(labelAliases, "Blah*+ Foo");

    Searcher searcher = expr.createSearcher(document);

    assertTrue(searcher.search());
    assertEquals(0, searcher.getBegin());
    assertEquals(5, searcher.getEnd());

    assertTrue(searcher.search());
    assertEquals(10, searcher.getBegin());
    assertEquals(25, searcher.getEnd());

    assertFalse(searcher.search());
  }

  @Test
  void testAlternationsProperOrdering() {
    LabelIndex<Foo> fooLabelIndex = StandardLabelIndex
        .create(Foo.class, new Foo(0, 5), new Foo(20, 25));
    LabelIndex<Blah> blahs = StandardLabelIndex
        .create(Blah.class, new Blah(10, 14), new Blah(15, 20));

    when(document.labelIndex(Blah.class)).thenReturn(blahs);
    when(document.labelIndex(Foo.class)).thenReturn(fooLabelIndex);

    when(document.getStartIndex()).thenReturn(0);
    when(document.getEndIndex()).thenReturn(25);

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
