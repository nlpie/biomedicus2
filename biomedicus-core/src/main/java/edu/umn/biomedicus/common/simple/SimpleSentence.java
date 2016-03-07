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

import edu.umn.biomedicus.common.semantics.SubstanceUsageType;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Term;
import edu.umn.biomedicus.common.text.Token;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A simple, immutable {@link Sentence} implementation.
 *
 * @since 1.1.0
 * @author Ben Knoll
 */
public class SimpleSentence implements Sentence {

    /**
     * The begin of the sentence relative to the document.
     */
    private final int begin;

    /**
     * The end index of the sentence relative to the document.
     */
    private final int end;

    /**
     * The tokens in the sentence.
     */
    private final List<Token> tokens;

    /**
     * The text of the document.
     */
    private final String documentText;

    /**
     * Creates a new sentence with the given parameters.
     *
     * @param documentText the text of the document.
     * @param begin the begin index of the sentence in the document.
     * @param end the end index of the sentence in the document.
     * @param tokens the tokens in the sentence.
     */
    public SimpleSentence(String documentText, int begin, int end, List<Token> tokens) {
        this.documentText = documentText;
        this.begin = begin;
        this.end = end;
        this.tokens = tokens;
    }

    @Override
    public List<Token> getTokens() {
        return tokens;
    }

    @Override
    public Stream<Token> tokens() {
        return tokens.stream();
    }

    @Override
    public Stream<Term> terms() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDependencies() {
        return null;
    }

    @Override
    public void setDependencies(String dependencies) {

    }

    @Override
    public String getParseTree() {
        return null;
    }

    @Override
    public void setParseTree(String parseTree) {

    }

    @Override
    public boolean isSocialHistoryCandidate() {
        return false;
    }

    @Override
    public void setIsSocialHistoryCandidate(boolean isSocialHistoryCandidate) {

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
    public int getBegin() {
        return begin;
    }

    @Override
    public int getEnd() {
        return end;
    }

    @Override
    public String getText() {
        return documentText.substring(begin, end);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleSentence that = (SimpleSentence) o;
        return Objects.equals(begin, that.begin) &&
                Objects.equals(end, that.end) &&
                Objects.equals(tokens, that.tokens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(begin, end, tokens);
    }

    @Override
    public String toString() {
        return "SimpleSentence{"
                + "begin=" + begin
                + ", end=" + end
                + ", tokens=" + tokens
                + ", documentText='" + documentText + '\''
                + '}';
    }
}
