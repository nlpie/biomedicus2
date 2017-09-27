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

package edu.umn.biomedicus.tokenization;

import static org.testng.Assert.assertEquals;

import edu.umn.biomedicus.framework.store.Span;
import java.util.List;
import java.util.stream.Collectors;
import org.testng.annotations.Test;

public class PennLikePhraseTokenizerTest {

  private PennLikePhraseTokenizer pennLikePhraseTokenizer
      = new PennLikePhraseTokenizer(
      "This test's logic will confirm that the tokenizer (P.T.B.-like) is well-behaved.");

  @Test
  public void testWords() throws Exception {
    List<PennLikePhraseTokenizer.TokenCandidate> list = pennLikePhraseTokenizer
        .startStreamWithWords()
        .collect(Collectors.toList());

    assertEquals(list.size(), 11);
    assertEquals(list.get(0).toSpan(), new Span(0, 4)); // this
    assertEquals(list.get(10).isLast(), true);
  }

  @Test
  public void testDoesSplitZWSP() {
    PennLikePhraseTokenizer pennLikePhraseTokenizer
        = new PennLikePhraseTokenizer("This sentence has some zero-width spaces.\u200b\u200b");

    List<PennLikePhraseTokenizer.TokenCandidate> tokenCandidates = pennLikePhraseTokenizer
        .startStreamWithWords()
        .collect(Collectors.toList());

    PennLikePhraseTokenizer.TokenCandidate tokenCandidate = tokenCandidates
        .get(tokenCandidates.size() - 1);
    assertEquals(tokenCandidate.getEnd(), 41);
  }

  @Test
  public void testWordsEmptySentence() throws Exception {
    PennLikePhraseTokenizer pennLikePhraseTokenizer = new PennLikePhraseTokenizer("");

    List<PennLikePhraseTokenizer.TokenCandidate> list = pennLikePhraseTokenizer
        .startStreamWithWords()
        .collect(Collectors.toList());

    assertEquals(list.size(), 0);
  }

  @Test
  public void testWordsWhitespaceSentence() throws Exception {
    PennLikePhraseTokenizer pennLikePhraseTokenizer = new PennLikePhraseTokenizer("\n \t   ");

    List<PennLikePhraseTokenizer.TokenCandidate> list = pennLikePhraseTokenizer
        .startStreamWithWords()
        .collect(Collectors.toList());

    assertEquals(list.size(), 0);
  }

  @Test
  public void testSplitEndPossessive() throws Exception {
    PennLikePhraseTokenizer.TokenCandidate tokenCandidate = pennLikePhraseTokenizer.new TokenCandidate(
        5, 11, false); // test's
    List<PennLikePhraseTokenizer.TokenCandidate> list = pennLikePhraseTokenizer
        .splitWordByEndBreaks(tokenCandidate)
        .collect(Collectors.toList());

    assertEquals(list.size(), 2);
    assertEquals(list.get(0).toSpan(), new Span(5, 9)); // test
    assertEquals(list.get(1).toSpan(), new Span(9, 11)); // 's
  }

  @Test
  public void testSplitBeginParen() throws Exception {
    PennLikePhraseTokenizer.TokenCandidate tokenCandidate = pennLikePhraseTokenizer.new TokenCandidate(
        50, 63, false); // (P.T.B.-like)
    List<PennLikePhraseTokenizer.TokenCandidate> list = pennLikePhraseTokenizer
        .splitWordByBeginBreaks(tokenCandidate)
        .collect(Collectors.toList());

    assertEquals(list.size(), 2);
    assertEquals(list.get(0).toSpan(), new Span(50, 51)); // (
    assertEquals(list.get(1).toSpan(), new Span(51, 63)); // P.T.B.-like)
  }

  @Test
  public void testSplitEndParen() throws Exception {
    PennLikePhraseTokenizer.TokenCandidate tokenCandidate = pennLikePhraseTokenizer.new TokenCandidate(
        51, 63, false); // P.T.B.-like)
    List<PennLikePhraseTokenizer.TokenCandidate> list = pennLikePhraseTokenizer
        .splitWordByEndBreaks(tokenCandidate)
        .collect(Collectors.toList());

    assertEquals(list.size(), 2);
    assertEquals(list.get(0).toSpan(), new Span(51, 62)); // P.T.B.-like
    assertEquals(list.get(1).toSpan(), new Span(62, 63)); // )
  }

  @Test
  public void testSplitMidBreaks() throws Exception {
    PennLikePhraseTokenizer.TokenCandidate tokenCandidate = pennLikePhraseTokenizer.new TokenCandidate(
        51, 62, false); // P.T.B.-like
    List<PennLikePhraseTokenizer.TokenCandidate> list = pennLikePhraseTokenizer
        .splitWordByMiddleBreaks(tokenCandidate)
        .collect(Collectors.toList());

    assertEquals(list.size(), 3);
    assertEquals(list.get(0).toSpan(), new Span(51, 57)); // P.T.B.
    assertEquals(list.get(1).toSpan(), new Span(57, 58)); // -
    assertEquals(list.get(2).toSpan(), new Span(58, 62)); // like
  }

  @Test
  public void testSplitTrailingPeriod() throws Exception {
    PennLikePhraseTokenizer.TokenCandidate tokenCandidate = pennLikePhraseTokenizer.new TokenCandidate(
        67, 80, true); // well-behaved.
    List<PennLikePhraseTokenizer.TokenCandidate> list = pennLikePhraseTokenizer
        .splitTrailingPeriod(tokenCandidate)
        .collect(Collectors.toList());

    assertEquals(list.size(), 2);
    assertEquals(list.get(0).toSpan(), new Span(67, 79)); // well-behaved
    assertEquals(list.get(1).toSpan(), new Span(79, 80)); // .
  }

  @Test
  public void testDoNotSplitInternalPeriods() throws Exception {
    PennLikePhraseTokenizer.TokenCandidate tokenCandidate = pennLikePhraseTokenizer.new TokenCandidate(
        51, 57, false); // P.T.B.
    List<PennLikePhraseTokenizer.TokenCandidate> list = pennLikePhraseTokenizer
        .splitTrailingPeriod(tokenCandidate)
        .collect(Collectors.toList());

    assertEquals(list.size(), 1);
    assertEquals(list.get(0).toSpan(), new Span(51, 57)); // P.T.B.
  }

  @Test
  public void testDoNotSplitCommaNumbers() throws Exception {
    List<Span> spanList = PennLikePhraseTokenizer.tokenizePhrase("42,000,000")
        .collect(Collectors.toList());

    assertEquals(spanList.size(), 1);
    assertEquals(spanList.get(0).getBegin(), 0);
    assertEquals(spanList.get(0).getEnd(), 10);
  }

  @Test
  public void testSplitTrailingComma() throws Exception {
    List<Span> list = PennLikePhraseTokenizer.tokenizePhrase("first,")
        .collect(Collectors.toList());

    assertEquals(list.size(), 2);
    assertEquals(list.get(0).getBegin(), 0);
    assertEquals(list.get(0).getEnd(), 5);
  }

  @Test
  public void testSplitPercent() throws Exception {
    List<Span> spans = PennLikePhraseTokenizer.tokenizePhrase("42%")
        .collect(Collectors.toList());

    assertEquals(spans.size(), 2);
    assertEquals(spans.get(0), Span.create(0, 2));
    assertEquals(spans.get(1), Span.create(2, 3));
  }

  @Test
  public void testParenSplitMid() throws Exception {
    List<Span> spans = PennLikePhraseTokenizer.tokenizePhrase("abc(asf")
        .collect(Collectors.toList());

    assertEquals(spans.size(), 3);
    assertEquals(spans.get(0), Span.create(0, 3));
    assertEquals(spans.get(1), Span.create(3, 4));
    assertEquals(spans.get(2), Span.create(4, 7));
  }
}
