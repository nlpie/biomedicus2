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

package edu.umn.biomedicus.common.tuples;

import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.text.Token;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

/**
 * A double of part of speech and capitalization.
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
public class PosCap implements Comparable<PosCap>, Serializable {
    /**
     * Serial UID.
     */
    private static final long serialVersionUID = -6706873411087752899L;

    /**
     * The part of speech of this object.
     */
    private final PartOfSpeech partOfSpeech;

    /**
     * Whether or not this object is capitalized.
     */
    private final boolean capitalized;

    /**
     * Default constructor, takes the final values for fields.
     *
     * @param partOfSpeech part of speech enumerated object value
     * @param capitalized  capitalization
     */
    private PosCap(PartOfSpeech partOfSpeech, boolean capitalized) {
        this.partOfSpeech = Objects.requireNonNull(partOfSpeech);
        this.capitalized = capitalized;
    }

    /**
     * Gets the Part of speech capitalization for the part of speech and capitalized = true.
     *
     * @param partOfSpeech part of speech
     * @return existing pos cap
     */
    public static PosCap getCapitalized(PartOfSpeech partOfSpeech) {
        return new PosCap(partOfSpeech, true);
    }

    /**
     * Gets the Part of speech capitalization for the part of speech and capitalized = false.
     *
     * @param partOfSpeech part of speech
     * @return existing pos cap
     */
    public static PosCap getNotCapitalized(PartOfSpeech partOfSpeech) {
        return new PosCap(partOfSpeech, false);
    }

    /**
     * Gets the part of speech capitalization for the parameters.
     *
     * @param partOfSpeech part of speech
     * @param capitalized  true for capitalized false otherwise
     * @return part of speech capitalization
     */
    public static PosCap create(PartOfSpeech partOfSpeech, boolean capitalized) {
        return new PosCap(partOfSpeech, capitalized);
    }

    /**
     * Gets the part of speech and capitalization of a token object.
     *
     * @param token the token object to return part of speech capitalization for.
     * @return part fo speech capitalization.
     */
    public static PosCap create(Token token) {
        PartOfSpeech partOfSpeech = token.getPartOfSpeech();
        if (partOfSpeech == null) {
            throw new IllegalArgumentException("Tokens in document have not been tagged with part of speech");
        }
        return PosCap.create(partOfSpeech, token.isCapitalized());
    }

    /**
     * Gets the part of speech component of this tuple.
     *
     * @return the part of speech
     */
    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    /**
     * Gets the capitalization component of this tuple.
     *
     * @return true if it is capitalized, false otherwise
     */
    public boolean isCapitalized() {
        return capitalized;
    }

    /**
     * The ordinal of the part of speech-capitalization, its integer location in the sequence of all part of speech
     * capitalizations.
     *
     * @return integer ordinal
     */
    public int ordinal() {
        return (capitalized ? PartOfSpeech.values().length : 0) + partOfSpeech.ordinal();
    }

    /**
     * The total number of part of speech capitalizations.
     *
     * @return integer count of the number of part of speech capitalizations.
     */
    public static int cardinality() {
        return PartOfSpeech.values().length * 2;
    }

    /**
     * Creates a Part of speech capitalization from its ordinal.
     *
     * @param ordinal the integer ordinal
     * @return part of speech capitalization with the specified ordinal.
     */
    public static PosCap createFromOrdinal(int ordinal) {
        int posOrdinal = ordinal % PartOfSpeech.values().length;
        boolean capitalized = ordinal != posOrdinal;
        return PosCap.create(PartOfSpeech.values()[posOrdinal], capitalized);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PosCap that = (PosCap) o;

        if (capitalized != that.capitalized) {
            return false;
        }
        return partOfSpeech == that.partOfSpeech;

    }

    @Override
    public int hashCode() {
        int result = partOfSpeech.hashCode();
        result = 31 * result + (capitalized ? 1 : 0);
        return result;
    }

    @Override
    public int compareTo(PosCap o) {
        int result = partOfSpeech.compareTo(o.getPartOfSpeech());
        if (result == 0) {
            result = Boolean.compare(capitalized, o.isCapitalized());
        }
        return result;
    }

    @Override
    public String toString() {
        return "PosCap{"
                + "partOfSpeech=" + partOfSpeech.toString()
                + ", capitalized=" + capitalized
                + '}';
    }
}
