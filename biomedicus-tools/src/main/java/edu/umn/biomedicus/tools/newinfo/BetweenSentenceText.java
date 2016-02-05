package edu.umn.biomedicus.tools.newinfo;

import edu.umn.biomedicus.model.semantics.SubstanceUsageType;
import edu.umn.biomedicus.model.text.Sentence;
import edu.umn.biomedicus.model.text.Term;
import edu.umn.biomedicus.model.text.Token;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Contains the text that exists between two sentences for the purpose of writing new information output and
 * re-writing rtf documents.
 *
 * @author Ben Knoll
 * @since 1.4
 */
class BetweenSentenceText implements Sentence {
    /**
     * The text between the two sentences.
     */
    private final String text;

    /**
     * The begin index.
     */
    private final int begin;

    /**
     * The end index.
     */
    private final int end;

    /**
     * Constructor for the text between two sentences.
     *
     * @param text the text itself.
     * @param begin the begin index of the text in the document.
     * @param end the end index of the text in the document.
     */
    BetweenSentenceText(String text, int begin, int end) {
        this.text = text;
        this.begin = begin;
        this.end = end;
    }

    /**
     * Creates from the total document text.
     *
     * @param documentText the document text.
     * @param begin the begin.
     * @param end the end.
     * @return new {@code BetweenSentenceText}.
     */
    static BetweenSentenceText fromDocumentText(String documentText, int begin, int end) {
        return new BetweenSentenceText(documentText.substring(begin, end), begin, end);
    }

    @Override
    public Stream<Token> tokens() {
        return Stream.empty();
    }

    @Override
    public Stream<Term> terms() {
        return Stream.empty();
    }

    @Override
    public String getDependencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDependencies(String dependencies) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getParseTree() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParseTree(String parseTree) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSocialHistoryCandidate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIsSocialHistoryCandidate(boolean isSocialHistoryCandidate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<SubstanceUsageType> getSubstanceUsageTypes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addSubstanceUsageType(SubstanceUsageType substanceUsageType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public int getBegin() {
        return begin;
    }

    @Override
    public int getEnd() {
        return end;
    }
}
