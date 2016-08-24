/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

import edu.umn.biomedicus.common.types.text.Span;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

public class PennLikeSentenceTokenizerTest {
    private PennLikeSentenceTokenizer pennLikeSentenceTokenizer
            = new PennLikeSentenceTokenizer("This test's logic will confirm that the tokenizer (P.T.B.-like) is well-behaved.");

    @Test
    public void testWords() throws Exception {
        List<PennLikeSentenceTokenizer.TokenCandidate> list = pennLikeSentenceTokenizer.startStreamWithWords()
                .collect(Collectors.toList());

        assertEquals(list.size(), 11);
        assertEquals(list.get(0).toSpan(), new Span(0, 4)); // this
        assertEquals(list.get(10).isLast(), true);
    }

    @Test
    public void testWordsEmptySentence() throws Exception {
        PennLikeSentenceTokenizer pennLikeSentenceTokenizer = new PennLikeSentenceTokenizer("");

        List<PennLikeSentenceTokenizer.TokenCandidate> list = pennLikeSentenceTokenizer.startStreamWithWords()
                .collect(Collectors.toList());

        assertEquals(list.size(), 0);
    }

    @Test
    public void testWordsWhitespaceSentence() throws Exception {
        PennLikeSentenceTokenizer pennLikeSentenceTokenizer = new PennLikeSentenceTokenizer("\n \t   ");

        List<PennLikeSentenceTokenizer.TokenCandidate> list = pennLikeSentenceTokenizer.startStreamWithWords()
                .collect(Collectors.toList());

        assertEquals(list.size(), 0);
    }

    @Test
    public void testSplitEndPossessive() throws Exception {
        PennLikeSentenceTokenizer.TokenCandidate tokenCandidate = pennLikeSentenceTokenizer.new TokenCandidate(5, 11, false); // test's
        List<PennLikeSentenceTokenizer.TokenCandidate> list = pennLikeSentenceTokenizer.splitWordByEndBreaks(tokenCandidate)
                .collect(Collectors.toList());

        assertEquals(list.size(), 2);
        assertEquals(list.get(0).toSpan(), new Span(5, 9)); // test
        assertEquals(list.get(1).toSpan(), new Span(9, 11)); // 's
    }

    @Test
    public void testSplitBeginParen() throws Exception {
        PennLikeSentenceTokenizer.TokenCandidate tokenCandidate = pennLikeSentenceTokenizer.new TokenCandidate(50, 63, false); // (P.T.B.-like)
        List<PennLikeSentenceTokenizer.TokenCandidate> list = pennLikeSentenceTokenizer.splitWordByBeginBreaks(tokenCandidate)
                .collect(Collectors.toList());

        assertEquals(list.size(), 2);
        assertEquals(list.get(0).toSpan(), new Span(50, 51)); // (
        assertEquals(list.get(1).toSpan(), new Span(51, 63)); // P.T.B.-like)
    }

    @Test
    public void testSplitEndParen() throws Exception {
        PennLikeSentenceTokenizer.TokenCandidate tokenCandidate = pennLikeSentenceTokenizer.new TokenCandidate(51, 63, false); // P.T.B.-like)
        List<PennLikeSentenceTokenizer.TokenCandidate> list = pennLikeSentenceTokenizer.splitWordByEndBreaks(tokenCandidate)
                .collect(Collectors.toList());

        assertEquals(list.size(), 2);
        assertEquals(list.get(0).toSpan(), new Span(51, 62)); // P.T.B.-like
        assertEquals(list.get(1).toSpan(), new Span(62, 63)); // )
    }

    @Test
    public void testSplitMidBreaks() throws Exception {
        PennLikeSentenceTokenizer.TokenCandidate tokenCandidate = pennLikeSentenceTokenizer.new TokenCandidate(51, 62, false); // P.T.B.-like
        List<PennLikeSentenceTokenizer.TokenCandidate> list = pennLikeSentenceTokenizer.splitWordByMiddleBreaks(tokenCandidate)
                .collect(Collectors.toList());

        assertEquals(list.size(), 3);
        assertEquals(list.get(0).toSpan(), new Span(51, 57)); // P.T.B.
        assertEquals(list.get(1).toSpan(), new Span(57, 58)); // -
        assertEquals(list.get(2).toSpan(), new Span(58, 62)); // like
    }

    @Test
    public void testSplitTrailingPeriod() throws Exception {
        PennLikeSentenceTokenizer.TokenCandidate tokenCandidate = pennLikeSentenceTokenizer.new TokenCandidate(67, 80, true); // well-behaved.
        List<PennLikeSentenceTokenizer.TokenCandidate> list = pennLikeSentenceTokenizer.splitTrailingPeriod(tokenCandidate)
                .collect(Collectors.toList());

        assertEquals(list.size(), 2);
        assertEquals(list.get(0).toSpan(), new Span(67, 79)); // well-behaved
        assertEquals(list.get(1).toSpan(), new Span(79, 80)); // .
    }

    @Test
    public void testDoNotSplitInternalPeriods() throws Exception {
        PennLikeSentenceTokenizer.TokenCandidate tokenCandidate = pennLikeSentenceTokenizer.new TokenCandidate(51, 57, false); // P.T.B.
        List<PennLikeSentenceTokenizer.TokenCandidate> list = pennLikeSentenceTokenizer.splitTrailingPeriod(tokenCandidate)
                .collect(Collectors.toList());

        assertEquals(list.size(), 1);
        assertEquals(list.get(0).toSpan(), new Span(51, 57)); // P.T.B.
    }


}