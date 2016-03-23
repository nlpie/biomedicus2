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

import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.Token;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A simple token implementation.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class SimpleToken extends SimpleTextSpan implements Token {
    /**
     * The corrected spelling.
     */
    @Nullable
    private String correctedSpelling;

    /**
     * The normalized form.
     */
    @Nullable
    private String normalForm;

    /**
     * The part of speech.
     */
    @Nullable
    private PartOfSpeech partOfSpeech;

    /**
     * Whether the token is misspelled.
     */
    private boolean isMisspelled = false;

    /**
     * Whether the token is a stopword.
     */
    private boolean isStopword = false;

    /**
     * Creates a new simple token.
     *
     * @param documentText all the document text.
     * @param begin        the index the token begins at in the document text.
     * @param end          the index the token ends at in the document text.
     */
    public SimpleToken(String documentText, int begin, int end) {
        super(new SimpleSpan(begin, end), documentText);
    }

    /**
     * Creates a simple token from a span and document text.
     *
     * @param span the span of the token.
     * @param documentText the overall document text.
     * @return newly created simple token.
     */
    public static Token fromSpan(Span span, String documentText) {
        return new SimpleToken(documentText, span.getBegin(), span.getEnd());
    }

    @Nullable
    @Override
    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    @Override
    public void setPennPartOfSpeech(PartOfSpeech partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    @Nullable
    @Override
    public String getNormalForm() {
        return normalForm;
    }

    @Override
    public void setNormalForm(@Nullable String normalForm) {
        this.normalForm = normalForm;
    }

    @Override
    public void setIsStopword(boolean isStopword) {
        this.isStopword = isStopword;
    }

    @Override
    public boolean isStopword() {
        return isStopword;
    }

    @Override
    public boolean isMisspelled() {
        return isMisspelled;
    }

    @Override
    public void setIsMisspelled(boolean misspelled) {
        isMisspelled = misspelled;
    }

    @Override
    public boolean isAcronym() {
        return false;
    }

    @Override
    public void setIsAcronym(boolean acronym) {

    }

    @Nullable
    @Override
    public String getLongForm() {
        return null;
    }

    @Override
    public void setLongForm(@Nullable String longForm) {

    }

    @Nullable
    @Override
    public List<String> getLongFormNorm() {
        return null;
    }

    @Override
    public void setLongFormNorm(List<String> longFormNorm) {

    }

    @Nullable
    @Override
    public String correctSpelling() {
        return correctedSpelling;
    }

    @Override
    public void setCorrectSpelling(String correctSpelling) {
        correctedSpelling = correctSpelling;
    }

    @Nullable
    @Override
    public IndexedTerm getWordTerm() {
        return null;
    }

    @Override
    public void setWordTerm(IndexedTerm wordTerm) {

    }

    @Nullable
    @Override
    public IndexedTerm getNormTerm() {
        return null;
    }

    @Override
    public void setNormTerm(IndexedTerm normTerm) {

    }

    @Override
    public void beginEditing() {

    }

    @Override
    public void endEditing() {

    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (this == o) {
            return true;
        }

        SimpleToken that = (SimpleToken) o;

        if (isMisspelled != that.isMisspelled) {
            return false;
        }
        if (isStopword != that.isStopword) {
            return false;
        }
        if (correctedSpelling != null ? !correctedSpelling.equals(that.correctedSpelling) : that.correctedSpelling != null) {
            return false;
        }
        if (normalForm != null ? !normalForm.equals(that.normalForm) : that.normalForm != null) {
            return false;
        }
        if (partOfSpeech != that.partOfSpeech) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (correctedSpelling != null ? correctedSpelling.hashCode() : 0);
        result = 31 * result + (normalForm != null ? normalForm.hashCode() : 0);
        result = 31 * result + (partOfSpeech != null ? partOfSpeech.hashCode() : 0);
        result = 31 * result + (isMisspelled ? 1 : 0);
        result = 31 * result + (isStopword ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SimpleToken{"
                + "super=" + super.toString()
                + "correctedSpelling='" + correctedSpelling + '\''
                + ", normalForm='" + normalForm + '\''
                + ", partOfSpeech=" + partOfSpeech
                + ", isMisspelled=" + isMisspelled
                + ", isStopword=" + isStopword
                + '}';
    }
}
