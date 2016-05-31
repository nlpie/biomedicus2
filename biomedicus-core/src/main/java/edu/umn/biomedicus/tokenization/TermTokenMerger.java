package edu.umn.biomedicus.tokenization;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.ValueLabeler;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.TermToken;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;

@DocumentScoped
public class TermTokenMerger implements DocumentProcessor {

    private final Document document;

    private final ValueLabeler termTokenLabeler;

    @Inject
    public TermTokenMerger(Document document, Labeler<TermToken> termTokenLabeler) {
        this.document = document;
        this.termTokenLabeler = termTokenLabeler.value(TermToken.TERM_TOKEN);
    }

    @Override
    public void process() throws BiomedicusException {
        Iterable<Token> tokens = document.getTokens();
        int runningBegin = 0;
        int wordEnd = 0;
        int symbolEnd = 0;
        for (Token token : tokens) {
            int begin = token.getBegin();
            String text = token.getText();
            if (begin != symbolEnd) {
                // is a space between this and previous token.
                termTokenLabeler.label(runningBegin, wordEnd);
                runningBegin = begin;
                wordEnd = symbolEnd = token.getEnd();
            } else if (text.equals("-") || text.equals(".")) {
                // this token is a hyphen or period
                symbolEnd = token.getEnd();
            } else if (text.equals("'s") || text.equals("'")){
                wordEnd = symbolEnd = token.getEnd();
            }
        }
        termTokenLabeler.label(runningBegin, wordEnd);
    }
}
