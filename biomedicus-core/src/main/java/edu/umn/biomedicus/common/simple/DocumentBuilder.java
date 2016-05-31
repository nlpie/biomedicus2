package edu.umn.biomedicus.common.simple;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.SpanLike;

import java.util.ArrayList;

/**
 * Used to build simple documents.
 *
 * @author Ben Knoll
 * @since 1.4.0
 */
public class DocumentBuilder {
    /**
     * Builder for document text.
     */
    private final StringBuilder documentText = new StringBuilder();

    /**
     * Builder for tokens.
     */
    private final ArrayList<SpanLike> tokens = new ArrayList<>();

    /**
     * Builder for sentences.
     */
    private final ArrayList<SpanLike> sentences = new ArrayList<>();

    /**
     * The marker for a beginning of a sentence.
     */
    private int sentenceMarker = 0;

    /**
     * Adds the string as a token to the document.
     *
     * @param token adds a token
     * @return this builder.
     */
    public DocumentBuilder token(String token) {
        int begin = documentText.length();
        documentText.append(token);
        int end = documentText.length();
        tokens.add(Span.spanning(begin, end));
        return this;
    }

    /**
     * Inserts a space into the document.
     * @return this builder.
     */
    public DocumentBuilder space() {
        documentText.append(" ");
        return this;
    }

    /**
     * Marks the beginning of a sentence.
     *
     * @return this builder.
     */
    public DocumentBuilder beginSentence() {
        sentenceMarker = documentText.length();
        return this;
    }

    /**
     * Ends the sentence.
     *
     * @return this builder.
     */
    public DocumentBuilder endSentence() {
        sentences.add(Span.spanning(sentenceMarker, documentText.length()));
        return this;
    }

    /**
     * Builds the document given the tokens and sentences passed into the builder.
     *
     * @return document containing the tokens, spaces, and sentences.
     */
    public Document build() {
        SimpleDocument document = new SimpleDocument(documentText.toString());

        tokens.stream().forEach(document::createToken);
        sentences.stream().forEach(document::createSentence);
        return document;
    }
}
