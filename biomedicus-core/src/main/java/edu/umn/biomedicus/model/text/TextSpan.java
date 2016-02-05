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

package edu.umn.biomedicus.model.text;

import edu.umn.biomedicus.common.utilities.Patterns;

/**
 * A span or substring of text within a greater string of text.
 *
 * @since 1.3.0
 */
public interface TextSpan extends Span {
    /**
     * Returns the text covered by the span.
     *
     * @return string of text covered by this span.
     */
    String getText();

    /**
     * Returns true if the span contains characters that are not whitespace characters.
     *
     * @return true for containing non-whitespace, false if text span is only whitespace.
     */
    default boolean containsNonWhitespace() {
        return Patterns.NON_WHITESPACE.matcher(getText()).find();
    }
}
