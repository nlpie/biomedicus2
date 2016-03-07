/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.common.simple;

import edu.umn.biomedicus.common.semantics.SubstanceUsage;
import edu.umn.biomedicus.common.semantics.SubstanceUsageBuilder;
import edu.umn.biomedicus.common.semantics.SubstanceUsageType;
import edu.umn.biomedicus.common.text.*;

import javax.annotation.Nullable;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple {@link AbstractDocument} implementation.
 *
 * @author Ben Knoll
 * @since 1.1.0
 */
public class SimpleDocument extends AbstractDocument {
    /**
     * The text of the document.
     */
    private final String documentText;

    /**
     * The list of the tokens in the document.
     */
    private final List<Token> tokenList;

    /**
     * The sentence identifier.
     */
    private final List<Sentence> sentenceList;

    /**
     * A document identifier.
     */
    private final String identifier = UUID.randomUUID().toString();

    /**
     * Terms list.
     */
    private final List<Term> terms = new ArrayList<>();

    /**
     * The category of the document.
     */
    @Nullable
    private String category;

    /**
     * Creates a document with the given data.
     *
     * @param documentText The text of the document.
     * @param tokenList    The list of the tokens in the document.
     * @param sentenceList The sentence identifier.
     */
    public SimpleDocument(String documentText, List<Token> tokenList, List<Sentence> sentenceList) {
        this.documentText = documentText;
        this.tokenList = tokenList;
        this.sentenceList = sentenceList;
    }

    /**
     * Creates a simple document with only the document text.
     *
     * @param documentText the document text.
     */
    public SimpleDocument(String documentText) {
        this(documentText, new ArrayList<>(), new ArrayList<>());
    }

    @Override
    public Iterable<Token> getTokens() {
        return tokenList;
    }

    @Override
    public Iterable<Sentence> getSentences() {
        return sentenceList;
    }

    @Override
    public Sentence createSentence(int begin, int end) {
        List<Token> sentenceTokens = tokenList.stream()
                .filter(token -> token.getBegin() >= begin && token.getEnd() <= end)
                .collect(Collectors.toList());
        SimpleSentence sentence = new SimpleSentence(documentText, begin, end, sentenceTokens);
        sentenceList.add(sentence);
        return sentence;
    }

    @Override
    public Iterable<Term> getTerms() {
        return terms;
    }

    @Override
    public void addTerm(Term term) {
        terms.add(term);
    }

    @Override
    public Reader getReader() {
        return new StringReader(documentText);
    }

    @Override
    public Token createToken(int begin, int end) {
        Token token = new SimpleToken(documentText, begin, end);
        tokenList.add(token);
        return token;
    }

    @Override
    public String getText() {
        return documentText;
    }

    @Override
    public void setCategory(String category) {
        this.category = category;
    }

    @Nullable
    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Stream<TextSpan> textSegments() {
        return Stream.of(new SimpleTextSpan(new SimpleSpan(0, documentText.length()), documentText));
    }

    @Override
    public SectionBuilder createSection(Span span) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Section> getSections() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Section> getSectionsAtLevel(int level) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SubstanceUsageBuilder createSubstanceUsage(Sentence sentence, SubstanceUsageType substanceUsageType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<SubstanceUsage> getSubstanceUsages() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void beginEditing() {

    }

    @Override
    public void endEditing() {

    }


}
