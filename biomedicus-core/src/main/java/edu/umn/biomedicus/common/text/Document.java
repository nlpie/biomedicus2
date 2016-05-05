package edu.umn.biomedicus.common.text;

import edu.umn.biomedicus.common.semantics.SubstanceUsage;
import edu.umn.biomedicus.common.semantics.SubstanceUsageBuilder;
import edu.umn.biomedicus.common.semantics.SubstanceUsageType;
import edu.umn.biomedicus.exc.BiomedicusException;

import javax.annotation.Nullable;
import java.io.Reader;
import java.util.stream.Stream;

/**
 * A biomedicus basic unit for a document of text.
 * <p>This class will be implemented for each backend, so the biomedicus pipeline is designed to be agnostic
 * about how the data is stored. The UIMA example is edu.umn.biomedicus.adapter.JCasDocument</p>
 */
public interface Document extends Editable {
    /**
     * Get an {@link Iterable} over all the {@link Token} in a document in no
     * specific order.
     *
     * @return iterable of the tokens in a document.
     */
    Iterable<Token> getTokens();

    /**
     * Creates a new token and adds to index. The implementation should check to make sure that the span is not just
     * whitespace.
     *
     * @param span to create a token from
     * @return the newly created token.
     */
    Token createToken(Span span);

    /**
     * Creates a new token and adds to index. The implementation should check to make sure that the span is not just
     * whitespace.
     *
     * @param begin the begin of the token
     * @param end   the end of the token
     * @return the newly created token
     */
    Token createToken(int begin, int end);

    /**
     * Get an {@link Iterable} over all the {@link Sentence} in a document.
     *
     * @return iterable of the sentences in a document.
     */
    Iterable<Sentence> getSentences();

    /**
     * Add a sentence occurring over the span to this document.
     *
     * @param span a {@link Span} indicating where the sentence occurs.
     */
    Sentence createSentence(Span span);

    /**
     * Add a sentence occurring between begin and end to this document.
     *
     * @param begin the begin index of the sentence in this document
     * @param end the end index of the sentence in this document
     * @return newly created sentence.
     */
    Sentence createSentence(int begin, int end);

    /**
     * Get an {@link Iterable} over all of the {@link Term} objects in this document.
     *
     * @return iterable of the terms in this document
     */
    Iterable<Term> getTerms();

    /**
     * Adds a copy of the {@link Term} to the document.
     *
     * @param term the term to copy and add to the document.
     */
    void addTerm(Term term);

    /**
     * Returns a reader for the document text
     *
     * @return a java reader for the document text
     */
    Reader getReader();

    /**
     * Gets the entire text of the document
     *
     * @return document text
     */
    String getText();

    /**
     * Sets the category of the document.
     *
     * @param category string identifier for a category
     */
    void setCategory(String category);

    /**
     * Returns the category of the document.
     *
     * @return string identifier for a category.
     */
    @Nullable
    String getCategory();

    /**
     * Returns an identifier specific to the document.
     *
     * @return identifier
     */
    String getIdentifier();

    /**
     * Gets a stream of the discrete text segments within the document. These are defined as spans which sentences and
     * smaller segments will not bridge the gap between.
     *
     * @return stream of text segments
     */
    Stream<TextSpan> textSegments();

    /**
     * Creates a section builder for a section covering the span.
     *
     * @param span span the section covers.
     * @return section builder for a new section.
     */
    SectionBuilder createSection(Span span);

    /**
     * Gets an iterable of all the sections in the document.
     *
     * @return sections.
     */
    Iterable<Section> getSections();

    /**
     * Returns an iterable of all the sections with the specified level.
     *
     * @param level level of sections.
     * @return iterable of sections.
     */
    Iterable<Section> getSectionsAtLevel(int level);

    /**
     * Creates a new substance usage builder.
     *
     * @param sentence the sentence the substance usage occurs in.
     * @param substanceUsageType the substance usage type.
     * @return substance usage candidate builder.
     */
    SubstanceUsageBuilder createSubstanceUsage(Sentence sentence, SubstanceUsageType substanceUsageType);

    /**
     * Returns an iterable of all the substance usages in the document.
     *
     * @return substance usages.
     */
    Iterable<SubstanceUsage> getSubstanceUsages();

    /**
     *
     * @param key
     * @return
     */
    @Nullable
    String getMetadata(String key) throws BiomedicusException;

    /**
     *
     * @param key
     * @param value
     */
    void setMetadata(String key, String value) throws BiomedicusException;

    void createNewInformationAnnotation(Span span, String kind);

    boolean hasNewInformationAnnotation(Span span, String kind);

    boolean hasNewInformationAnnotation(Span span);

    Document getSiblingDocument(String identifier) throws BiomedicusException;
}
