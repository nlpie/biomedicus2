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

package edu.umn.biomedicus.modification;

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ContextSearch {
    private final LabelIndex<Sentence> sentences;
    private final LabelIndex<TermToken> tokens;
    private final LabelIndex<PartOfSpeech> partOfSpeechLabelIndex;
    private final LabelIndex<?> modifiableTerms;
    private final ContextCues contextCues;

    ContextSearch(ContextCues contextCues,
                  LabelIndex<Sentence> sentences,
                  LabelIndex<?> modifiableTerms,
                  LabelIndex<TermToken> tokens,
                  LabelIndex<PartOfSpeech> partOfSpeechLabelIndex) {
        this.contextCues = contextCues;
        this.sentences = sentences;
        this.modifiableTerms = modifiableTerms;
        this.tokens = tokens;
        this.partOfSpeechLabelIndex = partOfSpeechLabelIndex;
    }

    Map<Span, List<Label<TermToken>>> findMatches() {
        Map<Span, List<Label<TermToken>>> matchingSpans = new HashMap<>();
        for (Label<Sentence> sentence : sentences) {
            LabelIndex<TermToken> sentenceTokens = tokens.insideSpan(sentence);
            for (Label<?> modifiableTerm : modifiableTerms.insideSpan(sentence)) {
                List<Label<TermToken>> matches = matches(sentenceTokens, modifiableTerm);
                if (matches != null) {
                    matchingSpans.put(modifiableTerm.toSpan(), matches);
                }
            }
        }

        return matchingSpans;
    }

    private List<Label<TermToken>> matches(LabelIndex<TermToken> sentenceTokens,
                                            Label<?> modifiableTerm) {
        LabelIndex<TermToken> leftContextTokens = sentenceTokens.leftwardsFrom(modifiableTerm);
        List<Label<TermToken>> labels = contextCues.searchLeft(leftContextTokens.all(), partOfSpeechLabelIndex);
        if (labels != null) {
            return labels;
        }


        LabelIndex<TermToken> rightContextTokens = sentenceTokens.rightwardsFrom(modifiableTerm);
        labels = contextCues.searchRight(rightContextTokens.all(), partOfSpeechLabelIndex);
        if (labels != null) {
            return labels;
        }

        return null;
    }

    static final class ContextSearchBuilder {
        private ContextCues contextCues;
        private LabelIndex<Sentence> sentences;
        private LabelIndex<?> modifiableTerms;
        private LabelIndex<TermToken> tokens;
        private LabelIndex<PartOfSpeech> partOfSpeechLabelIndex;

        ContextSearchBuilder setContextCues(ContextCues contextCues) {
            this.contextCues = contextCues;
            return this;
        }

        ContextSearchBuilder setSentences(LabelIndex<Sentence> sentences) {
            this.sentences = sentences;
            return this;
        }

        ContextSearchBuilder setModifiableTerms(LabelIndex<?> modifiableTerms) {
            this.modifiableTerms = modifiableTerms;
            return this;
        }

        ContextSearchBuilder setTokens(LabelIndex<TermToken> tokens) {
            this.tokens = tokens;
            return this;
        }

        ContextSearchBuilder setPartOfSpeechLabelIndex(LabelIndex<PartOfSpeech> partOfSpeechLabelIndex) {
            this.partOfSpeechLabelIndex = partOfSpeechLabelIndex;
            return this;
        }

        ContextSearch createContextSearch() {
            return new ContextSearch(contextCues, sentences, modifiableTerms, tokens, partOfSpeechLabelIndex);
        }
    }
}
