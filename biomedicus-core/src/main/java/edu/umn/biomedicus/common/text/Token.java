/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.common.text;

import edu.umn.biomedicus.common.syntax.PartOfSpeech;

import javax.annotation.Nullable;

/**
 * Biomedicus basic unit for a single token in a document.
 * <p/>
 * <p>Implementations should override {@link #hashCode} and {@link #equals} to ensure correct Collection operation
 * and performance</p>
 *
 * @since 1.0.0
 */
public interface Token extends SpanLike, Editable {
    /**
     * Gets the text of this Token as it appears in the document, whether or not it is correct.
     *
     * @return string of token text
     */
    String getText();

    /**
     * Gets the text of this Token with correct spelling, whether it is correctly spelled in the document or it is
     * incorrectly spelled and has been corrected. This method should fail if the token has been marked as incorrectly
     * spelled but has not had a correct spelling set.
     *
     * @return the correct spelling of the token
     */
    default String getCorrectText() {
        return correctSpelling() != null ? correctSpelling() : getText();
    }

    /**
     * Gets the {@link PartOfSpeech} for this token.
     *
     * @return part of speech for token
     */
    @Nullable
    PartOfSpeech getPartOfSpeech();

    /**
     * Sets the part of speech for the token.
     *
     * @param partOfSpeech part of speech
     * @see PartOfSpeech
     */
    void setPennPartOfSpeech(PartOfSpeech partOfSpeech);

    /**
     * The normalized form of the token.
     *
     * @return the normal form of the token, or null if it hasn't been set
     */
    @Nullable
    String getNormalForm();

    /**
     * Set the normal form of the token
     *
     * @param normalForm the token's normal form
     */
    void setNormalForm(String normalForm);

    /**
     * Mark whether or not this token is a stopword.
     *
     * @param isStopword true if it is a stopword, false otherwise
     */
    void setIsStopword(boolean isStopword);

    /**
     * Returns whether this token has been marked as a stopword.
     *
     * @return true if the token is a stopword, false otherwise
     */
    boolean isStopword();

    /**
     * Returns whether or not this token is misspelled.
     *
     * @return true if the token is misspelled, false otherwise
     */
    boolean isMisspelled();

    /**
     * Sets whether or not this token is misspelled
     *
     * @param misspelled true if the token is misspelled, false otherwise
     */
    void setIsMisspelled(boolean misspelled);

    /**
     * Returns the corrected spelling of the token
     *
     * @return the correct spelling of the token, if it has been corrected, or null otherwise
     */
    @Nullable
    String correctSpelling();

    /**
     * Sets a correct spelling for the Token
     *
     * @param correctSpelling string correct spelling of this token
     */
    void setCorrectSpelling(String correctSpelling);

    /**
     * Whether or not the token is capitalized.
     *
     * @return true if it is capitalized, false otherwise.
     */
    default boolean isCapitalized() {
        return Character.isUpperCase(getText().charAt(0));
    }
}
