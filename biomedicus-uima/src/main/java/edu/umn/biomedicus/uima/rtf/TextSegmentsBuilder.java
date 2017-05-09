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

package edu.umn.biomedicus.uima.rtf;

import edu.umn.biomedicus.framework.store.Span;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;

import java.util.ArrayList;
import java.util.Collection;

import static edu.umn.biomedicus.common.utilities.Patterns.NON_WHITESPACE;

/**
 * Builds text segments by taking all splits and checking if spans between them
 * contain characters that are not whitespace.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class TextSegmentsBuilder {
    /**
     * The text segment splitting indices.
     */
    private final Collection<Integer> splits;

    /**
     * View to build in.
     */
    private final CAS cas;
    private final Type textSegmentAnnotation;

    /**
     * Initializes with an empty collection.
     *
     * @param cas the view to build in.
     */
    TextSegmentsBuilder(CAS cas) {
        this.splits = new ArrayList<>();
        splits.add(0);
        splits.add(cas.getDocumentText().length());
        this.cas = cas;
        textSegmentAnnotation = cas.getTypeSystem()
                .getType("edu.umn.biomedicus.type.TextSegmentAnnotation");
    }

    /**
     * Adds an annotation the represents a structural component of a document.
     *
     * @param annotationType the annotation type.
     */
    void addAnnotations(Type annotationType) {
        for (AnnotationFS annotation : cas.getAnnotationIndex(annotationType)) {
            splits.add(annotation.getBegin());
            splits.add(annotation.getEnd());
        }
    }

    /**
     * Builds text segments from the added annotations.
     */
    void buildInView() {
        String documentText = cas.getDocumentText();
        int[] sortedSplits = splits.stream().mapToInt(i -> i).sorted()
                .distinct().toArray();

        int prev = 0;
        for (int currentSplit : sortedSplits) {
            if (currentSplit != prev) {
                Span span = new Span(0, currentSplit);
                CharSequence segmentText = span.getCovered(documentText);
                if (NON_WHITESPACE.matcher(segmentText).find()) {
                    cas.addFsToIndexes(
                            cas.createAnnotation(textSegmentAnnotation, prev,
                                    currentSplit));
                }
                prev = currentSplit;
            }
        }
    }
}
