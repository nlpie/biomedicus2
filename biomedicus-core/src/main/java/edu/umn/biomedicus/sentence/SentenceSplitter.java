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

package edu.umn.biomedicus.sentence;

import edu.umn.biomedicus.common.text.SpanLike;

import java.util.stream.Stream;

/**
 * A post processor that is responsible for taking sentence candidate spans generated in the
 * {@link SentenceDetector} and splitting them into more than one span if necessary.
 *
 * @see SentenceDetector
 * @since 1.1.0
 */
public interface SentenceSplitter {
    /**
     * Set the text for the entire document.
     *
     * @param documentText entire document's text.
     */
    void setDocumentText(String documentText);

    /**
     * Split the candidate into a stream of one or more candidates. Would normally be used like
     * <pre>{@code candidates.stream().flatMap(sentenceSplitter::splitCandidate)}</pre> to get a stream of all the split
     * candidates.
     *
     * @param candidate the candidate to split
     * @return a stream of split candidates
     */
    Stream<SpanLike> splitCandidate(SpanLike candidate);
}
