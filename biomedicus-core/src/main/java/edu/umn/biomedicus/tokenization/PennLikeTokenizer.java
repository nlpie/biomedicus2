package edu.umn.biomedicus.tokenization;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DocumentScoped
public class PennLikeTokenizer implements DocumentProcessor {
    private final Document document;

    /**
     * Any sequence of 1 or more character that are not unicode whitespace.
     */
    private static final Pattern WORDS = Pattern.compile("([^\\p{Z}]+)");

    /**
     * Break the unicode Ps (open brackets) and Pi (open quotation).
     * Break the unicode currency symbols Sc.
     */
    private static final Pattern BEGIN_BREAKS = Pattern.compile("(\\p{Ps})|(\\p{Pi})|" +
            "(\\p{Sc})");

    /**
     * Break words apart whenever the unicode Dash Punctuation group (Pd) appears in them.
     */
    private static final Pattern MID_BREAKS = Pattern.compile("(\\p{Pd})");

    /**
     * Break possessives and contractions ', 's, n't, 'll, 've, 're in both uppercase and lowercase forms.
     * Break the unicode Pe (close brackets) and Pf (final quotation).
     * Break all unicode punctuation (P) except period.
     * Break the unicode currency symbols Sc.
     */
    private static final Pattern END_BREAKS = Pattern.compile(
            "(')|('[SsDdMm])|(n't)|(N'T)|('ll)|('LL)|('ve)|('VE)|('re)|('RE)|" +
                    "(\\p{Pe})|(\\p{Pf})|" +
                    "([\\p{P}&&[^\\.]])|" +
                    "(\\p{Sc})"
    );

    @Inject
    public PennLikeTokenizer(Document document) {
        this.document = document;
    }

    @Override
    public void process() throws BiomedicusException {
        for (Sentence sentence : document.getSentences()) {
            String text = sentence.getText();
            int len = text.length();

            List<Span> spanList = new ArrayList<>();

            Matcher words = WORDS.matcher(text);
            while (words.find()) {
                spanList.add(new Span(words.start(), words.end()));
            }

            for (Span span : spanList) {

            }


        }
    }
}
