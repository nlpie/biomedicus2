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

/**
 * Utility class for creating spans.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public final class Spans {
    /**
     * Private no-arg constructor to prevent instantiation of utility class.
     */
    private Spans() {
        throw new UnsupportedOperationException();
    }

    /**
     * Takes a child span that is relative to the parent span and puts it in the same coordinate space as the parent
     * span.
     *
     * @param parent parent span containing the child
     * @param child  child span whose begin and end indexes are relative to the parents.
     * @return the child span in the same coordinate space as the parent.
     */
    public static Span normalizeChild(Span parent, Span child) {
        return new SimpleSpan(parent.getBegin() + child.getBegin(), parent.getBegin() + child.getEnd());
    }

    /**
     * Creates a new span between the begin and end.
     *
     * @param begin the begin of the span.
     * @param end   the end of the span.
     * @return newly initialized span.
     */
    public static Span spanning(int begin, int end) {
        return new SimpleSpan(begin, end);
    }

    /**
     * Creates a text span given the document text and a region in the document.
     *
     * @param documentText the document text.
     * @param begin the begin of the region.
     * @param end the end of a region.
     * @return newly created TextSpan object.
     */
    public static TextSpan textSpan(String documentText, int begin, int end) {
        return new SimpleTextSpan(spanning(begin, end), documentText);
    }

    /**
     * Creates a text span covering the entire contents of a document.
     *
     * @param documentText the document text.
     * @return a text span containing the entire document.
     */
    public static TextSpan textSpan(String documentText) {
        return textSpan(documentText, 0, documentText.length());
    }
}
