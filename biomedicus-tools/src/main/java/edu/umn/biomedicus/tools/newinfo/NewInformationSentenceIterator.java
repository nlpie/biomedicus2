package edu.umn.biomedicus.tools.newinfo;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates through sentences for the new information. Includes the text between two sentences as a sentence.
 *
 * @author Ben Knoll
 * @since 1.4
 */
class NewInformationSentenceIterator implements Iterator<Sentence> {
    /**
     * The original iterator for sentences in the document.
     */
    private final Iterator<Sentence> documentSentences;

    /**
     * The text of the document.
     */
    private final String documentText;

    /**
     * The current sentence, if there is one.
     */
    @Nullable
    private Sentence currentSentence;

    /**
     * The next sentence, if there is one.
     */
    @Nullable
    private Sentence nextSentence;

    /**
     * The end index of the previous sentence.
     */
    private int prevEnd;

    /**
     * Creates a new information sentence iterator from the document sentences and the document text.
     *
     * @param documentSentences iterator over the document sentences.
     * @param documentText document text.
     */
    NewInformationSentenceIterator(Iterator<Sentence> documentSentences, String documentText) {
        this.documentSentences = documentSentences;
        this.documentText = documentText;

        prevEnd = 0;
        populateNextSentence();
    }

    private void populateNextSentence() {
        if (nextSentence != null) {
            currentSentence = nextSentence;
            nextSentence = null;
            return;
        }

        if (documentSentences.hasNext()) {
            Sentence sentence = documentSentences.next();
            int begin = sentence.getBegin();
            if (begin == prevEnd) {
                currentSentence = sentence;
                nextSentence = null;
            } else {
                currentSentence = BetweenSentenceText.fromDocumentText(documentText, prevEnd, begin);
                nextSentence = sentence;
            }
        } else {
            int length = documentText.length();
            if (prevEnd != length) {
                currentSentence = BetweenSentenceText.fromDocumentText(documentText, prevEnd, length);
            } else {
                currentSentence = null;
            }
            nextSentence = null;
        }
    }

    /**
     * Creates a new information sentence iterator from a document.
     *
     * @param document the document.
     * @return newly created {@code NewInformationSentenceIterator}.
     */
    static NewInformationSentenceIterator create(Document document) {
        return new NewInformationSentenceIterator(document.getSentences().iterator(), document.getText());
    }


    @Override
    public boolean hasNext() {
        return currentSentence != null;
    }

    @Override
    public Sentence next() {
        if (currentSentence == null) {
            throw new NoSuchElementException();
        }
        Sentence sentence = currentSentence;
        prevEnd = sentence.getEnd();
        populateNextSentence();
        return sentence;
    }
}
