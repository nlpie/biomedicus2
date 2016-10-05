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

package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.common.types.text.Token;

import java.util.regex.Pattern;

/**
 *
 */
public final class Acronyms {
    private Acronyms() {
        throw new UnsupportedOperationException();
    }

    public static final String UNKNOWN = "(unknown)";

    private static final Pattern SINGLE_DIGIT = Pattern.compile("[0-9]");

    private static final Pattern DECIMAL_NUMBER = Pattern.compile("[0-9]*\\.[0-9]+");

    private static final Pattern BIG_NUMBER = Pattern.compile("[0-9][0-9,]+");

    /**
     * Gets a standardized form of a token, derived from Token.normalForm
     *
     * @param t the token to standardize
     * @return the standard form of the token
     */
    static String standardForm(Token t) {
        return standardFormString(t.text());
    }

    /**
     * Get a standardized form for the dictionary
     * Replace non-single-digit numerals (including decimals/commas) with a generic string
     * Collapse certain non-alphanumeric characters ('conjunction' symbols like /, &, +)
     *
     * @param charSequence the character sequence
     * @return the standardized form
     */
    static String standardFormString(CharSequence charSequence) {
        // Collapse numbers
        if (SINGLE_DIGIT.matcher(charSequence).matches()) {
            return "single_digit";
        }
        if (DECIMAL_NUMBER.matcher(charSequence).matches()) {
            return "decimal_number";
        }
        if (BIG_NUMBER.matcher(charSequence).matches()) {
            return "big_number";
        }
        // Collapse certain symbols
        return charSequence.toString().replace('&', '/').replace('+', '/');
    }
}
