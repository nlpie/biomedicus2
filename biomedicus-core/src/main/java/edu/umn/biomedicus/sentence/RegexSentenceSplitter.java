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

package edu.umn.biomedicus.sentence;

import edu.umn.biomedicus.common.simple.Spans;
import edu.umn.biomedicus.common.text.Span;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Splits the candidate spans based on a {@link java.util.regex.Pattern}. It assumes that the pattern matches a sequence
 * of characters that match an end of a sentence. So if for the candidate span [0, 15), the pattern matches the span
 * [6, 7), we will create two new candidates [0, 7) and [7, 15).
 *
 * @author Ben Knoll
 * @since 1.1.0
 */
public class RegexSentenceSplitter implements SentenceSplitter {

    private final Pattern splitPattern;

    private String documentText;

    /**
     * Default constructor. Initializes with the {@link java.util.regex.Pattern} that we are splitting based on.
     *
     * @param splitPattern pattern to split based on.
     */
    public RegexSentenceSplitter(Pattern splitPattern) {
        this.splitPattern = splitPattern;
    }

    @Override
    public void setDocumentText(String documentText) {
        this.documentText = documentText;
    }

    @Override
    public Stream<Span> splitCandidate(Span candidate) {
        String covered = candidate.getCovered(documentText);
        int offset = candidate.getBegin();
        Matcher matcher = splitPattern.matcher(covered);
        int begin = 0;
        int end;
        Stream.Builder<Span> builder = Stream.<Span>builder();
        while (matcher.find()) {
            end = matcher.end();
            builder.add(Spans.spanning(offset + begin, offset + end));
            begin = end;
        }
        if (begin < covered.length()) {
            builder.add(Spans.spanning(offset + begin, offset + covered.length()));
        }
        return builder.build();
    }
}
