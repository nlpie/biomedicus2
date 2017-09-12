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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import edu.umn.biomedicus.framework.LabelAliases;
import edu.umn.biomedicus.framework.Searcher;
import edu.umn.biomedicus.framework.SearchExpr;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextView;
import java.util.Iterator;
import java.util.Optional;
import mockit.Expectations;
import mockit.Mocked;
import org.testng.annotations.Test;

/**
 *
 */
public class SearchExprTest {

  @Mocked
  TextView document;

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
      document.getLabelIndex(Blah.class); result = labelIndex;
      labelIndex.first(); result = Optional.of(label);
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertEquals(searcher.getSpan().get(), span);
  }

  @Test
  public void testNoMatchType() throws Exception {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      document.getLabelIndex(Blah.class); result = labelIndex;
      labelIndex.first(); result = Optional.empty();
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
  }

  @Test
  public void testMatchPin() throws Exception {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      document.getLabelIndex(Blah.class); result = labelIndex;
      labelIndex.first(); returns(Optional.of(label), Optional.of(label1), Optional.of(label2));
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "[Blah Blah Blah]");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertEquals(searcher.getSpan().get(), span);
  }

  @Test
  public void testNoMatchPin() throws Exception {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      document.getLabelIndex(Blah.class); result = labelIndex;
      labelIndex.first(); returns(Optional.of(label), Optional.empty());
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "[Blah Blah]");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
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

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"bar\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan();
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), new Span(0, 5));
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

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"bar\">");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    assertFalse(searcher.found());
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

    SearchExpr blah = SearchExpr.parse(labelAliases, "Foo<getValue=\"baz\",getBaz=42>");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> span = searcher.getSpan();
    assertTrue(span.isPresent());
    assertEquals(span.get(), new Span(0, 5));
  }

  @Test
  public void testAlternations() throws Exception {
    Optional<Label<Blah>> label = Optional.of(new Label<>(new Span(0, 5), new Blah()));

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); returns(Optional.empty(), label);
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
  public void testEmpty() throws Exception {
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
  public void testLabelVariable() throws Exception {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = Optional.of(label);
      labelAliases.getLabelable("Blah"); result = Blah.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "instance:Blah");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Label<?>> opt = searcher.getLabel("instance");
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), label);
  }

  @Test
  public void testLabelGroup() throws Exception {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = Optional.of(label);
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
  public void testAnEnum() throws Exception {
    Label<AnEnum> label = new Label<>(new Span(0, 5), AnEnum.VALUE);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = Optional.of(label);
      labelAliases.getLabelable("AnEnum"); result = AnEnum.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "(instance:AnEnum=VALUE)");

    Searcher searcher = blah.createSearcher(document);
    searcher.search();

    Optional<Span> opt = searcher.getSpan("instance");
    assertTrue(opt.isPresent());
    assertEquals(opt.get(), label.toSpan());
  }

  @Test
  public void testOptionMissing() throws Exception {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); result = Optional.empty();
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
      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 8), new Foo()))
      );
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
  public void testPositiveLookaheadFail() throws Exception {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah()))
      );
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
  public void testNegativeLookaheadPass() throws Exception {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);
      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.empty()
      );
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
  public void testNegativeLookaheadFail() throws Exception {
    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 10);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 8), new Foo()))
      );

      labelAliases.getLabelable("Blah"); result = Blah.class;
      labelAliases.getLabelable("Foo"); result = Foo.class;
    }};

    SearchExpr blah = SearchExpr.parse(labelAliases, "Blah(?!Foo)");
    Searcher searcher = blah.createSearcher(document);
    boolean search = searcher.match();

    assertFalse(search);
  }

  @Test
  public void testFallback(@Mocked Iterator<Foo> fooIterator) throws Exception {
    Foo tenFoo = new Foo();
    tenFoo.setBaz(10);
    Foo fourteenFoo = new Foo();
    fourteenFoo.setBaz(14);

    new Expectations() {{
      document.getDocumentSpan(); result = new Span(0, 30);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), tenFoo)),
          Optional.of(Label.create(Span.of(6, 8), tenFoo)),
          Optional.of(Label.create(Span.of(9, 13), tenFoo)),
          Optional.of(Label.create(Span.of(14, 18), fourteenFoo))
      );

      labelIndex.iterator(); result = fooIterator;
      fooIterator.hasNext(); returns(true, true, true, true, false);

      fooIterator.next(); returns(
          Label.create(Span.of(0, 5), tenFoo),
          Label.create(Span.of(6, 8), tenFoo),
          Label.create(Span.of(9, 13), tenFoo),
          Label.create(Span.of(14, 18), fourteenFoo)
      );

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
  public void testAtomicLazyOptional() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(Optional.of(Label.create(Span.of(0, 5), new Blah())), Optional.empty());
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
  public void testAtomicPossessiveOptional() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah?+ Blah");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  public void testAtomicGreedyOptional() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.empty()
      );
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
  public void testAtomicPossessiveKleene() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah*+ Blah");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  public void testAtomicLazyKleene() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.empty());
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
  public void testAtomicGreedyKleene() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.empty());
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
  public void testAtomicPossessiveOnePlusFail() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah++ Blah");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  public void testAtomicPossessiveOnePlusMatch() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah++");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
  }

  @Test
  public void testAtomicLazyOnePlus() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah+?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  public void testAtomicPossessiveCurly() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,3}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
  }

  @Test
  public void testAtomicPossessiveCurlyNoMaxPass() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2,}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
  }

  @Test
  public void testAtomicPossessiveCurlyNoMaxFail() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2,}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  public void testAtomicPossessiveCurlyExactBelow() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  public void testAtomicPossessiveCurlyExact() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 10);
  }

  @Test
  public void testAtomicPossessiveCurlyExactAbove() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.of(Label.create(Span.of(11, 13), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{2}+");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertFalse(found);
  }

  @Test
  public void testAtomicLazyCurly() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.of(Label.create(Span.of(11, 13), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,3}?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  public void testAtomicLazyCurlyNoMax() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.of(Label.create(Span.of(11, 13), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,}?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  public void testAtomicLazyCurlyExact() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.of(Label.create(Span.of(11, 13), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1}?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  public void testAtomicGreedyCurly() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.of(Label.create(Span.of(11, 13), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,3}");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 13);
  }

  @Test
  public void testAtomicGreedyCurlyNoMax() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.of(Label.create(Span.of(11, 13), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,}");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 13);
  }

  @Test
  public void testAtomicGreedyCurlyExact() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 100);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.of(Label.create(Span.of(11, 13), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1}");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean found = searcher.search();

    assertTrue(found);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 5);
  }

  @Test
  public void testReluctanceAndMatch() throws Exception {
    new Expectations() {{
      labelAliases.getLabelable("Blah"); result = Blah.class;

      document.getDocumentSpan(); result = new Span(0, 13);

      labelIndex.first(); returns(
          Optional.of(Label.create(Span.of(0, 5), new Blah())),
          Optional.of(Label.create(Span.of(6, 10), new Blah())),
          Optional.of(Label.create(Span.of(11, 13), new Blah())),
          Optional.empty()
      );
    }};

    SearchExpr searchExpr = SearchExpr.parse(labelAliases, "Blah{1,}?");

    Searcher searcher = searchExpr.createSearcher(document);
    boolean match = searcher.match();

    assertTrue(match);
    assertEquals(searcher.getBegin(), 0);
    assertEquals(searcher.getEnd(), 13);
  }

  enum AnEnum {
    VALUE;
  }

  static class Blah {

  }

  public static class Foo {

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
}
