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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import edu.umn.nlpengine.AbstractTextRange;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Span;
import edu.umn.nlpengine.StandardLabelIndex;
import edu.umn.nlpengine.LabeledText;
import edu.umn.nlpengine.TextRange;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mockit.Expectations;
import mockit.Mocked;
import org.testng.annotations.Test;

/**
 *
 */
public class SearchExprTest {

  @Mocked
  LabeledText document;

  @Mocked
  LabelAliases labelAliases;

  @Mocked
  LabelIndex labelIndex;

  Span span = new Span(5, 10);

  Span span1 = new Span(5, 7);

  Span span2 = new Span(7, 10);

  Blah label = new Blah(span);

  Blah label1 = new Blah(span1);

  Blah label2 = new Blah(span2);

  @Test
  public void testMatchType() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testNoMatchType() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testNoTextBeforeMatch() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 13);
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
  public void testNoTextBeforeNoMatch() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 13);
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
  public void testMatchPin() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testNoMatchPin() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testStringPropertyMatch() {
    Foo foo = new Foo(0, 5);
    foo.setValue("bar");

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testStringPropertyNoMatch() {
    Foo foo = new Foo(0, 5);
    foo.setValue("baz");

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"bar\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  public void testStringPropertyAlternation() {
    Foo foo = new Foo(0, 5);
    foo.setValue("bar");

    Foo foo2 = new Foo(6, 10);
    foo2.setValue("baz");

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testStringPropertyAlternationMiss() {
    Foo foo = new Foo(0, 5);
    foo.setValue("baz");

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"bar\"|\"abc\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  public void testRegexPropertyMatch() {
    Foo foo = new Foo(0, 5);
    foo.setValue("aaa");

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testRegexPropertyMiss() {
    Foo foo = new Foo(0, 5);
    foo.setValue("baz");

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=r\"a*\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  public void testRegexAlternations() {
    Foo foo = new Foo(0, 5);
    foo.setValue("aaa");

    Foo foo2 = new Foo(6, 10);
    foo2.setValue("baz");

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testCaseInsensitiveMatch() {
    Foo foo = new Foo(0, 5);
    foo.setValue("BAZ");

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testCaseInsensitiveNoMatch() {
    Foo foo = new Foo(0, 5);
    foo.setValue("baz");

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=i\"bar\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  public void testCaseInsensitiveAlternationsFirst() {
    Foo foo = new Foo(0, 5);
    foo.setValue("aaa");

    Foo foo2 = new Foo(6, 10);
    foo2.setValue("BAZ");

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testCaseInsensitiveAlternationsOther() {
    Foo foo = new Foo(0, 5);
    foo.setValue("bar");

    Foo foo2 = new Foo(6, 10);
    foo2.setValue("baz");

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testNumberPropertyMatch() {
    Foo foo = new Foo(0, 5);
    foo.setBaz(5);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testNumberPropertyNegative() {
    Foo foo = new Foo(0, 5);
    foo.setBaz(-5);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testNumberPropertyNoMatch() {
    Foo foo = new Foo(0, 5);
    foo.setBaz(3);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getBaz=4>");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  public void testNumberPropertyAlternation() {
    Foo foo = new Foo(0, 5);
    foo.setBaz(3);

    Foo foo2 = new Foo(6, 10);
    foo2.setBaz(4);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testNumberPropertyAlternationMiss() {
    Foo foo = new Foo(0, 5);
    foo.setBaz(5);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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

  static class HasEnum extends AbstractTextRange {
    BAZ baz;

    public HasEnum(int startIndex, int endIndex) {
      super(startIndex, endIndex);
    }

    public BAZ getBaz() {
      return baz;
    }
  }

  @Test
  public void testEnumPropertyMatch() throws Exception {
    HasEnum hasEnum = new HasEnum(0, 5);
    hasEnum.baz = BAZ.FOO;

    LabelIndex<HasEnum> index = StandardLabelIndex.create(hasEnum);

    new Expectations() {{
      labelAliases.getLabelable("HasEnum"); result = HasEnum.class;

      document.getDocumentSpan(); result = new Span(0, 10);
      document.labelIndex(HasEnum.class); result = index;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "HasEnum<getBaz=eFOO>");

    Searcher searcher = blah.createSearcher(document);

    assertTrue(searcher.search());
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  public void testEnumPropertyMiss() throws Exception {
    HasEnum hasEnum = new HasEnum(0, 5);
    hasEnum.baz = BAZ.BAR;

    LabelIndex<HasEnum> index = StandardLabelIndex.create(hasEnum);

    new Expectations() {{
      labelAliases.getLabelable("HasEnum"); result = HasEnum.class;

      document.getDocumentSpan(); result = new Span(0, 10);
      document.labelIndex(HasEnum.class); result = index;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "HasEnum<getBaz=eFOO>");

    Searcher searcher = blah.createSearcher(document);

    assertFalse(searcher.search());
  }

  @Test
  public void testPropertyMatchNull() {
    Foo foo = new Foo(0, 5);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = foo;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"abc\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  public void testMultiProperties() {
    Foo foo = new Foo(0, 5);
    foo.setValue("baz");
    foo.setBaz(42);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testAlternations() {
    Blah label = new Blah(0, 5);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testEmpty() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testLabelVariable() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = label;
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "instance:Blah");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<TextRange> opt = searcher.getLabel("instance");
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), label);
  }

  @Test
  public void testRepeatLabelVariable() {
    Foo fooLabel = new Foo(0, 3);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); returns(label, null, fooLabel);
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "instance:Blah | instance:Foo");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<TextRange> opt = searcher.getLabel("instance");
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), label);

    searcher = blah.createSearcher(document);
    searcher.search();

    opt = searcher.getLabel("instance");
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), fooLabel);
  }

  @Test
  public void testRepeatingGroupName() {
    Foo fooLabel = new Foo(0, 3);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); returns(label, null, fooLabel);
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "(?<instance> Blah) | (?<instance> Foo)");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> instance = searcher.getSpan("instance");
    assertTrue(instance.isPresent());
    assertTrue(instance.get().locationEquals(label));

    searcher = blah.createSearcher(document);
    searcher.search();

    instance = searcher.getSpan("instance");
    assertTrue(instance.isPresent());
    assertTrue(instance.get().locationEquals(fooLabel));
  }

  @Test
  public void testLabelGroup() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = label;
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "(?<instance>Blah)");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan("instance");
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), label.toSpan());
  }

  @Test
  public void testOptionMissing() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = null;
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah?");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertTrue(searcher.found());
  }

  @Test
  public void testPositiveLookaheadPass() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testPositiveLookaheadFail() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testNegativeLookaheadPass() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
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
  public void testNegativeLookaheadFail() {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);

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
  public void testFallback() {
    Foo foo1 = new Foo(0, 5);
    foo1.setBaz(10);

    Foo foo2 = new Foo(6, 8);
    foo2.setBaz(10);

    Foo foo3 = new Foo(9, 13);
    foo3.setBaz(10);

    Foo fourteenFoo = new Foo(14, 18);
    fourteenFoo.setBaz(14);

    List<Foo> arr = new ArrayList<>();
    arr.add(foo1);
    arr.add(foo2);
    arr.add(foo3);
    arr.add(fourteenFoo);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 30);

      labelIndex.first(); returns(foo1, foo2, foo3, fourteenFoo);

      labelIndex.iterator(); result = arr.iterator();

      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getBaz=10> Foo<getBaz=14>");
    Searcher searcher = blah.createSearcher(document);
    boolean search = searcher.search();

    assertTrue(search);
    assertEquals(searcher.getBegin(), 9);
    assertEquals(searcher.getEnd(), 18);
  }

  @Test
  public void testAtomicLazyOptional() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(new Blah(0, 5), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "opt:Blah?? Blah*");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
    Optional<Span> span = searcher.getSpan("opt");
    assertFalse(span.isPresent());
  }

  @Test
  public void testAtomicPossessiveOptional() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(new Blah(0, 5), new Blah(0, 5), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah?+ Blah");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  public void testAtomicGreedyOptional() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(new Blah(0, 5), null, new Blah(0, 5), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah? opt:Blah*");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
    Optional<Span> span = searcher.getSpan("opt");
    assertFalse(span.isPresent());
  }

  @Test
  public void testAtomicPossessiveKleene() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah*+ Blah");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  public void testAtomicLazyKleene() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "opt:Blah*? Blah*");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
    Optional<Span> span = searcher.getSpan("opt");
    assertFalse(span.isPresent());
  }

  @Test
  public void testAtomicGreedyKleene() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah* opt:Blah*");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
    Optional<Span> span = searcher.getSpan("opt");
    assertFalse(span.isPresent());
  }

  @Test
  public void testAtomicPossessiveOnePlusFail() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), (Object) null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah++ Blah");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  public void testAtomicPossessiveOnePlusMatch() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

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
  public void testAtomicLazyOnePlus() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

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
  public void testAtomicPossessiveCurly() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

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
  public void testAtomicPossessiveCurlyNoMaxPass() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

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
  public void testAtomicPossessiveCurlyNoMaxFail() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(new Blah(0, 5), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2,}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  public void testAtomicPossessiveCurlyExactBelow() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(new Blah(0, 5), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  public void testAtomicPossessiveCurlyExact() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

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
  public void testAtomicPossessiveCurlyExactAbove() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(new Blah(0, 5), new Blah(6, 10), new Blah(11, 13), null);
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  public void testAtomicLazyCurly() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

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
  public void testAtomicLazyCurlyNoMax() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

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
  public void testAtomicLazyCurlyExact() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

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
  public void testAtomicGreedyCurly() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

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
  public void testAtomicGreedyCurlyNoMax() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

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
  public void testAtomicGreedyCurlyExact() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

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
  public void testReluctanceAndMatch() {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 13);

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
  public void testContainsTrue() throws Exception {
    List<Foo> foos = Collections.singletonList(new Foo(0, 6));

    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;

      document.getDocumentSpan(); result = new Span(0, 13);

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
  public void testContainsFalse() throws Exception {
    List<Foo> foos = Collections.emptyList();

    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;

      document.getDocumentSpan(); result = new Span(0, 13);

      labelIndex.first(); result = new Blah(2, 5);

      labelIndex.iterator(); result = foos.iterator();
    }};

    SearchExpr expr = SearchExpr.parse(labelAliases, "Blah[^Foo]");

    Searcher searcher = expr.createSearcher(document);
    boolean find = searcher.search();
    assertFalse(find);
  }

  @Test
  public void testOptionalFindsFirst() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;

      document.getDocumentSpan(); result = new Span(0, 13);

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
  public void testGreedyLoopPreemption() throws Exception {
    LabelIndex<Foo> fooLabelIndex = StandardLabelIndex.create(new Foo(0, 5), new Foo(20, 25));
    LabelIndex<Blah> blahs = StandardLabelIndex.create(new Blah(10, 14), new Blah(15, 20));

    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;

      document.getDocumentSpan(); result = new Span(0, 25);

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
  public void testPossessiveLoopPreemption() throws Exception {
    LabelIndex<Foo> fooLabelIndex = StandardLabelIndex.create(new Foo(0, 5), new Foo(20, 25));
    LabelIndex<Blah> blahs = StandardLabelIndex.create(new Blah(10, 14), new Blah(15, 20));

    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;

      document.getDocumentSpan(); result = new Span(0, 25);

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
  public void testAlternationsProperOrdering() throws Exception {
    LabelIndex<Foo> fooLabelIndex = StandardLabelIndex.create(new Foo(0, 5), new Foo(20, 25));
    LabelIndex<Blah> blahs = StandardLabelIndex.create(new Blah(10, 14), new Blah(15, 20));

    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;

      document.getDocumentSpan(); result = new Span(0, 25);

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



  static class Blah extends AbstractTextRange {

    public Blah(int startIndex, int endIndex) {
      super(startIndex, endIndex);
    }

    public Blah(TextRange label) {
      super(label);
    }
  }

  public static class Foo extends AbstractTextRange {

    private String value;
    private int baz;

    public Foo(int startIndex, int endIndex) {
      super(startIndex, endIndex);
    }

    public Foo(TextRange label) {
      super(label);
    }

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
}
