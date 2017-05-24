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

package edu.umn.biomedicus.common.tuples;

import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * A triplet of a string (word), a part of speech, and a capitalization boolean value.
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
public class WordPosCap implements Comparable<WordPosCap>, Serializable {
    /**
     * Serial UID.
     */
    private static final long serialVersionUID = 8141457560938728230L;

    /**
     * Word.
     */
    private final String word;

    /**
     * Part of speech.
     */
    private final PartOfSpeech partOfSpeech;

    /**
     * Capitalization.
     */
    private final boolean isCapitalized;

    /**
     * Default constructor. Initializes the three values in the triplet.
     *
     * @param word          a word or any string
     * @param partOfSpeech  a part of speech
     * @param isCapitalized true for capitalized, false otherwise. Is not necessarily whether the word is capitalized
     */
    public WordPosCap(String word, PartOfSpeech partOfSpeech, boolean isCapitalized) {
        this.word = word;
        this.partOfSpeech = partOfSpeech;
        this.isCapitalized = isCapitalized;
    }

    /**
     * Returns the word part of the (word, pos, cap) triplet.
     *
     * @return String word
     */
    public String getWord() {
        return word;
    }

    /**
     * Gets the part of speech of the triplet
     *
     * @return a part of speech
     */
    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    /**
     * The capitalization value of the triplet. Not necessarily whether the word value is capitalized, since the word
     * value could be a suffix.
     *
     * @return true if capitalized, false otherwise.
     */
    public boolean isCapitalized() {
        return isCapitalized;
    }

    /**
     * Converts this to a double of part of speech and capitalization.
     *
     * @return newly created double of part of speech and capitalization
     */
    public PosCap toPosCap() {
        return PosCap.create(partOfSpeech, isCapitalized);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WordPosCap that = (WordPosCap) o;

        if (!word.equals(that.word)) {
            return false;
        }
        if (!partOfSpeech.equals(that.partOfSpeech)) {
            return false;
        }
        return isCapitalized == that.isCapitalized;

    }

    @Override
    public int hashCode() {
        int result = word.hashCode();
        result = 31 * result + partOfSpeech.hashCode();
        result = 31 * result + Boolean.hashCode(isCapitalized);
        return result;
    }

    @Override
    public int compareTo(WordPosCap o) {
        int result = word.compareTo(o.word);
        if (result == 0) {
            result = partOfSpeech.compareTo(o.partOfSpeech);
        }
        if (result == 0) {
            result = Boolean.compare(isCapitalized, o.isCapitalized);
        }
        return result;
    }

    @Override
    public String toString() {
        return "WordPosCap{"
                + "word='" + word + '\''
                + ", partOfSpeech=" + partOfSpeech.toString()
                + ", isCapitalized=" + isCapitalized
                + '}';
    }
}
