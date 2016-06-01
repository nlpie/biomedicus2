package edu.umn.biomedicus.tokenization;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.ValueLabeler;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.TermToken;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@DocumentScoped
public class TermTokenMerger implements DocumentProcessor {

    private final Document document;

    private final ValueLabeler termTokenLabeler;

    private static final Set<Character> MERGE = new HashSet<>(Arrays.asList('-', '/', '\\', '\''));

    @Inject
    public TermTokenMerger(Document document, Labeler<TermToken> termTokenLabeler) {
        this.document = document;
        this.termTokenLabeler = termTokenLabeler.value(TermToken.TERM_TOKEN);
    }

    @Override
    public void process() throws BiomedicusException {
        String text = document.getText();
        Iterator<Token> iterator = document.getTokens().iterator();

        Span prev = new Span(0, 0);
        while (iterator.hasNext()) {
            Token token = iterator.next();
            if (prev.length() == 0) {
                prev = token.span();
                continue;
            }
            char last = text.charAt(prev.getEnd() - 1);
            char first = text.charAt(token.getBegin());
            if (prev.getEnd() == token.getBegin() &&
                    ((Character.isLetterOrDigit(last) && MERGE.contains(first)) ||
                    (MERGE.contains(last) && Character.isLetterOrDigit(first)) ||
                            (Character.isLetterOrDigit(last) && Character.isLetterOrDigit(first)))
                    ) {
                prev = new Span(prev.getBegin(), token.getEnd());
            } else {
                termTokenLabeler.label(prev);
                prev = token.span();
            }
        }
        termTokenLabeler.label(prev);
    }
}
