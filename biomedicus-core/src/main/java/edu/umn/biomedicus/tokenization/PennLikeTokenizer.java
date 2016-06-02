package edu.umn.biomedicus.tokenization;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.text.Document;
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

    @Inject
    public PennLikeTokenizer(Document document) {
        this.document = document;
    }

    @Override
    public void process() throws BiomedicusException {
        for (Sentence sentence : document.getSentences()) {
            String text = sentence.getText();

            LinkedList<Span> splitByWhitespace = new LinkedList<>();

            Matcher words = WORDS.matcher(text);
            while (words.find()) {
                splitByWhitespace.addLast(new Span(words.start(), words.end()));
            }
            Span lastTokenByWhiteSpace = splitByWhitespace.pollLast();
            Matcher matcher = TRAILING_PERIOD.matcher(lastTokenByWhiteSpace.getCovered(text));
            if (matcher.find()) {
                splitByWhitespace.addLast(lastTokenByWhiteSpace.relativize(new Span(0, matcher.start())));
                splitByWhitespace.add(lastTokenByWhiteSpace.relativize(new Span(matcher.start(), matcher.end())));
            } else {
                splitByWhitespace.addLast(lastTokenByWhiteSpace);
            }

            LinkedList<Span> splitByWhitespaceAndMid = new LinkedList<>();

            while (!splitByWhitespace.isEmpty()) {
                Span poll = splitByWhitespace.pollFirst();

                CharSequence tokenText = poll.getCovered(text);

                Matcher midBreaksMatcher = MID_BREAKS.matcher(tokenText);
                if (midBreaksMatcher.find()) {
                    int begin = midBreaksMatcher.start();
                    int end = midBreaksMatcher.end();

                    Span beginSplit = poll.relativize(new Span(0, begin));
                    splitByWhitespaceAndMid.addLast(beginSplit);

                    Span matchedSplit = poll.relativize(new Span(begin, end));
                    splitByWhitespaceAndMid.addLast(matchedSplit);

                    while (midBreaksMatcher.find()) {
                        begin = midBreaksMatcher.start();
                        Span beforeSplit = poll.relativize(new Span(end, begin));
                        splitByWhitespaceAndMid.addLast(beforeSplit);

                        end = midBreaksMatcher.end();
                        matchedSplit = poll.relativize(new Span(begin, end));
                        splitByWhitespaceAndMid.addLast(matchedSplit);
                    }
                    Span lastSplit = poll.relativize(new Span(end, tokenText.length()));
                    splitByWhitespaceAndMid.addLast(lastSplit);
                } else {
                    splitByWhitespaceAndMid.add(poll);
                }
            }

            LinkedList<Span> splitByWhitespaceBeginAndMid = new LinkedList<>();

            while (!splitByWhitespaceAndMid.isEmpty()) {
                Span poll = splitByWhitespaceAndMid.pollFirst();

                CharSequence tokenText = poll.getCovered(text);

                Matcher beginBreaksMatcher = BEGIN_BREAKS.matcher(tokenText);
                if (beginBreaksMatcher.find()) {
                    assert beginBreaksMatcher.start() == 0;
                    Span begin = poll.relativize(new Span(beginBreaksMatcher.start(), beginBreaksMatcher.end()));
                    splitByWhitespaceBeginAndMid.addLast(begin);

                    Span rest = poll.relativize(new Span(beginBreaksMatcher.end(), tokenText.length()));
                    splitByWhitespaceAndMid.addFirst(rest);
                } else {
                    splitByWhitespaceBeginAndMid.addLast(poll);
                }
            }

            LinkedList<Span> allSplit = new LinkedList<>();

            while (!splitByWhitespaceBeginAndMid.isEmpty()) {
                Span poll = splitByWhitespaceBeginAndMid.pollLast();
                CharSequence tokenText = poll.getCovered(text);

                Matcher endBreaksMatcher = END_BREAKS.matcher(tokenText);
                if (endBreaksMatcher.find()) {
                    Span rest = poll.relativize(new Span(0, endBreaksMatcher.start()));
                    splitByWhitespaceBeginAndMid.addLast(rest);
                    Span endSplit = poll.relativize(new Span(endBreaksMatcher.start(), endBreaksMatcher.end()));
                    allSplit.addFirst(endSplit);
                } else {
                    allSplit.addFirst(poll);
                }
            }

            while (!allSplit.isEmpty()) {
                Span span = allSplit.pollFirst();
                if (span.length() == 0) {
                    continue;
                }
                document.createToken(sentence.toSpan().relativize(span));
            }
        }
    }
}
