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

package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.model.semantics.PartOfSpeech;
import edu.umn.biomedicus.model.text.Token;
import edu.umn.biomedicus.type.TokenAnnotation;

import javax.annotation.Nullable;

/**
 * Adapter from the annotation type {@link edu.umn.biomedicus.type.TokenAnnotation} to the biomedicus model interface
 * {@link edu.umn.biomedicus.model.text.Token}.
 */
class TokenAdapter implements Token {
    private final TokenAnnotation tokenAnnotation;

    TokenAdapter(TokenAnnotation tokenAnnotation) {
        this.tokenAnnotation = tokenAnnotation;
    }

    @Override
    public String getText() {
        return tokenAnnotation.getCoveredText();
    }

    @Override
    public PartOfSpeech getPartOfSpeech() {
        return PartOfSpeech.MAP.get(tokenAnnotation.getPartOfSpeech());
    }

    @Override
    public void setPennPartOfSpeech(PartOfSpeech partOfSpeech) {
        tokenAnnotation.setPartOfSpeech(partOfSpeech.toString());
    }

    @Override
    public String getNormalForm() {
        return tokenAnnotation.getNormalForm();
    }

    @Override
    public void setNormalForm(String normalForm) {
        tokenAnnotation.setNormalForm(normalForm);
    }

    @Override
    public void setIsStopword(boolean isStopword) {
        tokenAnnotation.setIsStopword(isStopword);
    }

    @Override
    public boolean isStopword() {
        return tokenAnnotation.getIsStopword();
    }

    @Override
    public boolean isMisspelled() {
        return tokenAnnotation.getIsMisspelled();
    }

    @Override
    public void setIsMisspelled(boolean misspelled) {
        tokenAnnotation.setIsMisspelled(misspelled);
    }

    @Override
    public boolean isAcronym() {
        return tokenAnnotation.getIsAcronymAbbrev();
    }

    @Override
    public void setIsAcronym(boolean acronym) {
        tokenAnnotation.setIsAcronymAbbrev(acronym);
    }

    @Nullable
    @Override
    public String getLongForm() {
        return tokenAnnotation.getAcronymAbbrevExpansion();
    }

    @Override
    public void setLongForm(@Nullable String longForm) {
        tokenAnnotation.setAcronymAbbrevExpansion(longForm);
    }

    @Override
    public String correctSpelling() {
        return tokenAnnotation.getCorrectSpelling();
    }

    @Override
    public void setCorrectSpelling(String correctSpelling) {
        tokenAnnotation.setCorrectSpelling(correctSpelling);
    }

    @Override
    public int getBegin() {
        return tokenAnnotation.getBegin();
    }

    @Override
    public int getEnd() {
        return tokenAnnotation.getEnd();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TokenAdapter that = (TokenAdapter) o;

        if (!tokenAnnotation.equals(that.tokenAnnotation))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return tokenAnnotation.hashCode();
    }

    @Override
    public void beginEditing() {
        tokenAnnotation.removeFromIndexes();
    }

    @Override
    public void endEditing() {
        tokenAnnotation.addToIndexes();
    }
}
