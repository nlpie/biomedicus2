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

import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.TextSpan;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A simple, immutable implementation of {@link TextSpan}.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class SimpleTextSpan implements TextSpan {
    /**
     * The span the text covers.
     */
    private final Span span;

    /**
     * The overall text.
     */
    private final String text;

    /**
     * Default constructor, sets fields to their final values.
     *
     * @param span The span the text covers.
     * @param text The overall text.
     */
    public SimpleTextSpan(Span span, String text) {
        this.span = span;
        this.text = text;
    }

    @Override
    public String getText() {
        return text.substring(getBegin(), getEnd());
    }

    @Override
    public int getBegin() {
        return span.getBegin();
    }

    @Override
    public int getEnd() {
        return span.getEnd();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleTextSpan that = (SimpleTextSpan) o;
        return Objects.equals(span, that.span) &&
                Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(span, text);
    }

    @Override
    public String toString() {
        return "SimpleTextSpan{"
                + "span=" + span
                + ", text='" + text
                + "'}";
    }
}
