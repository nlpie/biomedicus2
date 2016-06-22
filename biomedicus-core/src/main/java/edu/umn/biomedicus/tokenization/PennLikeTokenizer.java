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

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.ParseToken;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DocumentScoped
public class PennLikeTokenizer implements DocumentProcessor {
    private final Document document;

    /**
     * Any sequence of 1 or more character that are not unicode whitespace.
     */
    private static final Pattern WORDS = Pattern.compile("([^\\p{Z}\\n\\t\\r]+)");

    /**
     * Break the unicode Ps (open brackets) and Pi (open quotation).
     * Break the unicode currency symbols Sc.
     */
    private static final Pattern BEGIN_BREAKS = Pattern.compile("^((\\p{Ps})|(\\p{Pi})|" +
            "(\\p{Sc}))");

    /**
     * Break words apart whenever the unicode Dash Punctuation group (Pd) appears in them.
     */
    private static final Pattern MID_BREAKS = Pattern.compile("([\\p{Pd}/\\\\])");

    /**
     * Break possessives and contractions ', 's, n't, 'll, 've, 're in both uppercase and lowercase forms.
     * Break the unicode Pe (close brackets) and Pf (final quotation).
     * Break all unicode punctuation (P) except period.
     * Break the unicode currency symbols Sc.
     */
    private static final Pattern END_BREAKS = Pattern.compile(
            "((')|('[SsDdMm])|(n't)|(N'T)|('ll)|('LL)|('ve)|('VE)|('re)|('RE)|" +
                    "(\\p{Pe})|(\\p{Pf})|" +
                    "([\\p{P}&&[^\\.]])|" +
                    "(\\p{Sc}))$"
    );

    private static final Pattern TRAILING_PERIOD = Pattern.compile("(\\.)$");
    private final Labeler<ParseToken> parseTokenLabeler;

    @Inject
    public PennLikeTokenizer(Document document,
                             Labeler<ParseToken> parseTokenLabeler) {
        this.document = document;
        this.parseTokenLabeler = parseTokenLabeler;
    }

    @Override
    public void process() throws BiomedicusException {
        for (Sentence sentence : document.getSentences()) {
            String text = sentence.getText();

            // phase 1, split by whitespace

            LinkedList<Span> splitByWhitespace = new LinkedList<>();

            Matcher words = WORDS.matcher(text);
            while (words.find()) {
                splitByWhitespace.addLast(new Span(words.start(), words.end()));
            }

            if (splitByWhitespace.size() == 0) {
                return;
            }

            // phase 1.1 split a trailing period

            Span lastTokenByWhiteSpace = splitByWhitespace.pollLast();
            Matcher matcher = TRAILING_PERIOD.matcher(lastTokenByWhiteSpace.getCovered(text));
            if (matcher.find()) {
                splitByWhitespace.addLast(lastTokenByWhiteSpace.derelativize(new Span(0, matcher.start())));
                splitByWhitespace.add(lastTokenByWhiteSpace.derelativize(new Span(matcher.start(), matcher.end())));
            } else {
                splitByWhitespace.addLast(lastTokenByWhiteSpace);
            }

            // phase 2 split by middle breaking characters

            LinkedList<Span> splitByWhitespaceAndMid = new LinkedList<>();

            while (!splitByWhitespace.isEmpty()) {
                Span poll = splitByWhitespace.pollFirst();

                CharSequence tokenText = poll.getCovered(text);

                Matcher midBreaksMatcher = MID_BREAKS.matcher(tokenText);
                if (midBreaksMatcher.find()) {
                    int begin = midBreaksMatcher.start();
                    int end = midBreaksMatcher.end();

                    Span beginSplit = poll.derelativize(new Span(0, begin));
                    splitByWhitespaceAndMid.addLast(beginSplit);

                    Span matchedSplit = poll.derelativize(new Span(begin, end));
                    splitByWhitespaceAndMid.addLast(matchedSplit);

                    while (midBreaksMatcher.find()) {
                        begin = midBreaksMatcher.start();
                        Span beforeSplit = poll.derelativize(new Span(end, begin));
                        splitByWhitespaceAndMid.addLast(beforeSplit);

                        end = midBreaksMatcher.end();
                        matchedSplit = poll.derelativize(new Span(begin, end));
                        splitByWhitespaceAndMid.addLast(matchedSplit);
                    }
                    Span lastSplit = poll.derelativize(new Span(end, tokenText.length()));
                    splitByWhitespaceAndMid.addLast(lastSplit);
                } else {
                    splitByWhitespaceAndMid.add(poll);
                }
            }

            // phase 3 split off begin-breaking sequences

            LinkedList<Span> splitByWhitespaceBeginAndMid = new LinkedList<>();

            while (!splitByWhitespaceAndMid.isEmpty()) {
                Span poll = splitByWhitespaceAndMid.pollFirst();

                CharSequence tokenText = poll.getCovered(text);

                Matcher beginBreaksMatcher = BEGIN_BREAKS.matcher(tokenText);
                if (beginBreaksMatcher.find()) {
                    assert beginBreaksMatcher.start() == 0;
                    Span begin = poll.derelativize(new Span(beginBreaksMatcher.start(), beginBreaksMatcher.end()));
                    splitByWhitespaceBeginAndMid.addLast(begin);

                    Span rest = poll.derelativize(new Span(beginBreaksMatcher.end(), tokenText.length()));
                    splitByWhitespaceAndMid.addFirst(rest);
                } else {
                    splitByWhitespaceBeginAndMid.addLast(poll);
                }
            }

            // phase 4 split off end-breaking sequences

            LinkedList<Span> allSplit = new LinkedList<>();

            while (!splitByWhitespaceBeginAndMid.isEmpty()) {
                Span poll = splitByWhitespaceBeginAndMid.pollLast();
                CharSequence tokenText = poll.getCovered(text);

                Matcher endBreaksMatcher = END_BREAKS.matcher(tokenText);
                if (endBreaksMatcher.find()) {
                    Span rest = poll.derelativize(new Span(0, endBreaksMatcher.start()));
                    splitByWhitespaceBeginAndMid.addLast(rest);
                    Span endSplit = poll.derelativize(new Span(endBreaksMatcher.start(), endBreaksMatcher.end()));
                    allSplit.addFirst(endSplit);
                } else {
                    allSplit.addFirst(poll);
                }
            }

            // phase 5 label the splits created through the previous phases

            Span last = null;
            while (!allSplit.isEmpty()) {
                Span span = allSplit.pollFirst();
                if (span.length() == 0) {
                    continue;
                }
                if (last != null) {
                    String tokenText = text.substring(last.getBegin(), last.getEnd());
                    String trailingText = text.substring(last.getEnd(), span.getBegin());
                    ParseToken parseToken = new ParseToken(tokenText, trailingText);
                    parseTokenLabeler.value(parseToken).label(sentence.derelativize(last));
                }
                last = span;
            }
            if (last != null) {
                Span derelativized = sentence.derelativize(last);
                parseTokenLabeler.value(new ParseToken(text.substring(last.getBegin(), last.getEnd()), ""))
                        .label(derelativized);
            }
        }
    }
}
