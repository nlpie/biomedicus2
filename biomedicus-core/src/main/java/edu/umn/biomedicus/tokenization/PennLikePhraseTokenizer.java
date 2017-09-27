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

import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextLocation;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class PennLikePhraseTokenizer {

  /**
   * Any sequence of 1 or more character that are not unicode whitespace.
   */
  private static final Pattern WORDS = Pattern.compile("([^\\p{Z}\\p{C}]+)");

  private static final Pattern TRAILING_PERIOD = Pattern.compile("([.])$");

  /**
   * Break words apart whenever the unicode Dash Punctuation group (Pd) appears in them.
   * Unicode ps (open brackets) and Pi (open quotation).
   */
  private static final Pattern MID_BREAKS = Pattern.compile("([\\p{Ps}\\p{Pi}:;\\p{Pd}/\\\\])");

  /**
   * Break the unicode Ps (open brackets) and Pi (open quotation).
   * Break the unicode currency symbols Sc.
   */
  private static final Pattern BEGIN_BREAKS = Pattern.compile("^[\\p{Ps}\\p{Pi}\\p{Sc}#]");

  /**
   * Break possessives and contractions ', 's, n't, 'll, 've, 're in both uppercase and lowercase
   * forms. Break the unicode Pe (close brackets) and Pf (final quotation). Break all unicode
   * punctuation (P) except period. Break the unicode currency symbols Sc.
   */
  private static final Pattern END_BREAKS = Pattern.compile(
      "((')|('[SsDdMm])|(n't)|(N'T)|('ll)|('LL)|('ve)|('VE)|('re)|('RE)|" +
          "(\\p{Pe})|(\\p{Pf})|" +
          "([\\p{P}&&[^.]])|" +
          "(\\p{Sc}))$"
  );

  private final CharSequence sentenceText;

  PennLikePhraseTokenizer(CharSequence sentenceText) {
    this.sentenceText = sentenceText;
  }

  public static Stream<Span> tokenizeSentence(CharSequence sentenceText) {
    PennLikePhraseTokenizer sentenceTokenizer = new PennLikePhraseTokenizer(sentenceText);

    return sentenceTokenizer.startStreamWithWords()
        .flatMap(sentenceTokenizer::splitTrailingPeriod)
        .flatMap(sentenceTokenizer::splitWordByMiddleBreaks)
        .flatMap(sentenceTokenizer::splitWordByBeginBreaks)
        .flatMap(sentenceTokenizer::splitWordByEndBreaks)
        .map(TokenCandidate::toSpan);
  }

  public static Stream<Span> tokenizePhrase(CharSequence phraseText) {
    PennLikePhraseTokenizer tokenizer = new PennLikePhraseTokenizer(phraseText);

    return tokenizer.startStreamWithWords()
        .flatMap(tokenizer::splitWordByMiddleBreaks)
        .flatMap(tokenizer::splitWordByBeginBreaks)
        .flatMap(tokenizer::splitWordByEndBreaks)
        .map(TokenCandidate::toSpan);
  }

  Stream<TokenCandidate> startStreamWithWords() {
    Stream.Builder<TokenCandidate> builder = Stream.builder();

    Matcher characters = WORDS.matcher(sentenceText);
    int last = -1;
    while (characters.find()) {
      last = characters.end();
    }

    if (last == -1) {
      return Stream.empty();
    }

    Matcher words = WORDS.matcher(sentenceText);
    while (words.find()) {
      int start = words.start();
      int end = words.end();
      boolean isLast = end == last;
      TokenCandidate tokenCandidate = new TokenCandidate(start, end, isLast);
      builder.add(tokenCandidate);
    }
    return builder.build();
  }

  Stream<TokenCandidate> splitTrailingPeriod(TokenCandidate tokenCandidate) {
    if (!tokenCandidate.isLast) {
      return Stream.of(tokenCandidate);
    } else {
      Matcher matcher = TRAILING_PERIOD.matcher(tokenCandidate.getCovered(sentenceText));
      if (matcher.find()) {
        return Stream
            .of(new TokenCandidate(tokenCandidate.derelativize(new Span(0, matcher.start())),
                    false),
                new TokenCandidate(
                    tokenCandidate.derelativize(new Span(matcher.start(), matcher.end())), true));
      } else {
        return Stream.of(tokenCandidate);
      }
    }
  }

  Stream<TokenCandidate> splitWordByMiddleBreaks(TokenCandidate tokenCandidate) {
    CharSequence tokenText = tokenCandidate.getCovered(sentenceText);

    Stream.Builder<TokenCandidate> builder = Stream.builder();

    Matcher midBreaksMatcher = MID_BREAKS.matcher(tokenText);
    if (midBreaksMatcher.find()) {
      int begin = midBreaksMatcher.start();
      int end = midBreaksMatcher.end();

      Span beginSplit = tokenCandidate.derelativize(new Span(0, begin));
      builder.add(new TokenCandidate(beginSplit, false));

      Span matchedSplit = tokenCandidate.derelativize(new Span(begin, end));
      builder.add(new TokenCandidate(matchedSplit, false));

      while (midBreaksMatcher.find()) {
        begin = midBreaksMatcher.start();
        Span beforeSplit = tokenCandidate.derelativize(new Span(end, begin));
        builder.add(new TokenCandidate(beforeSplit, false));

        end = midBreaksMatcher.end();
        matchedSplit = tokenCandidate.derelativize(new Span(begin, end));
        builder.add(new TokenCandidate(matchedSplit, false));
      }
      Span lastSplit = tokenCandidate.derelativize(new Span(end, tokenText.length()));
      builder.add(new TokenCandidate(lastSplit, tokenCandidate.isLast));
    } else {
      builder.add(tokenCandidate);
    }
    return builder.build();
  }

  Stream<TokenCandidate> splitWordByBeginBreaks(TokenCandidate tokenCandidate) {
    Stream.Builder<TokenCandidate> builder = Stream.builder();
    while (true) {
      CharSequence tokenText = tokenCandidate.getCovered(sentenceText);
      Matcher beginBreaksMatcher = BEGIN_BREAKS.matcher(tokenText);
      if (beginBreaksMatcher.find()) {
        assert beginBreaksMatcher.start() == 0;
        Span begin = tokenCandidate
            .derelativize(new Span(beginBreaksMatcher.start(), beginBreaksMatcher.end()));
        builder.add(new TokenCandidate(begin, false));

        Span rest = tokenCandidate
            .derelativize(new Span(beginBreaksMatcher.end(), tokenText.length()));
        tokenCandidate = new TokenCandidate(rest, tokenCandidate.isLast);
      } else {
        builder.add(tokenCandidate);
        return builder.build();
      }
    }
  }

  Stream<TokenCandidate> splitWordByEndBreaks(TokenCandidate tokenCandidate) {
    LinkedList<TokenCandidate> candidates = new LinkedList<>();

    while (true) {
      CharSequence tokenText = tokenCandidate.getCovered(sentenceText);
      Matcher endBreaksMatcher = END_BREAKS.matcher(tokenText);
      if (endBreaksMatcher.find()) {
        int start = endBreaksMatcher.start();
        Span rest = tokenCandidate.derelativize(new Span(0, start));
        Span endSplit = tokenCandidate.derelativize(new Span(start, endBreaksMatcher.end()));
        candidates.addFirst(new TokenCandidate(endSplit, tokenCandidate.isLast));
        tokenCandidate = new TokenCandidate(rest, false);
      } else {
        if (tokenCandidate.getBegin() != tokenCandidate.getEnd()) {
          candidates.addFirst(tokenCandidate);
        }
        return candidates.stream();
      }
    }
  }

  class TokenCandidate implements TextLocation {

    private final int begin;
    private final int end;
    private final boolean isLast;

    TokenCandidate(Span span, boolean isLast) {
      begin = span.getBegin();
      end = span.getEnd();
      this.isLast = isLast;
    }

    TokenCandidate(int begin, int end, boolean isLast) {
      this.begin = begin;
      this.end = end;
      this.isLast = isLast;
    }

    @Override
    public int getBegin() {
      return begin;
    }

    @Override
    public int getEnd() {
      return end;
    }

    public boolean isLast() {
      return isLast;
    }
  }
}
