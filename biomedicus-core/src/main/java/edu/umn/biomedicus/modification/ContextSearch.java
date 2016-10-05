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
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ContextSearch {
    private final Labels<Sentence> sentences;
    private final Labels<TermToken> tokens;
    private final Labels<PartOfSpeech> partOfSpeechLabels;
    private final Labels<?> modifiableTerms;
    private final ContextCues contextCues;

    ContextSearch(ContextCues contextCues,
                  Labels<Sentence> sentences,
                  Labels<?> modifiableTerms,
                  Labels<TermToken> tokens,
                  Labels<PartOfSpeech> partOfSpeechLabels) {
        this.contextCues = contextCues;
        this.sentences = sentences;
        this.modifiableTerms = modifiableTerms;
        this.tokens = tokens;
        this.partOfSpeechLabels = partOfSpeechLabels;
    }

    Map<Span, List<Label<TermToken>>> findMatches() {
        Map<Span, List<Label<TermToken>>> matchingSpans = new HashMap<>();
        for (Label<Sentence> sentence : sentences) {
            Labels<TermToken> sentenceTokens = tokens.insideSpan(sentence);
            for (Label<?> modifiableTerm : modifiableTerms.insideSpan(sentence)) {
                List<Label<TermToken>> matches = matches(sentenceTokens, modifiableTerm);
                if (matches != null) {
                    matchingSpans.put(modifiableTerm.toSpan(), matches);
                }
            }
        }

        return matchingSpans;
    }

    private List<Label<TermToken>> matches(Labels<TermToken> sentenceTokens,
                                            Label<?> modifiableTerm) {
        Labels<TermToken> leftContextTokens = sentenceTokens.leftwardsFrom(modifiableTerm);
        List<Label<TermToken>> labels = contextCues.searchLeft(leftContextTokens.all(), partOfSpeechLabels);
        if (labels != null) {
            return labels;
        }


        Labels<TermToken> rightContextTokens = sentenceTokens.rightwardsFrom(modifiableTerm);
        labels = contextCues.searchRight(rightContextTokens.all(), partOfSpeechLabels);
        if (labels != null) {
            return labels;
        }

        return null;
    }

    static final class ContextSearchBuilder {
        private ContextCues contextCues;
        private Labels<Sentence> sentences;
        private Labels<?> modifiableTerms;
        private Labels<TermToken> tokens;
        private Labels<PartOfSpeech> partOfSpeechLabels;

        ContextSearchBuilder setContextCues(ContextCues contextCues) {
            this.contextCues = contextCues;
            return this;
        }

        ContextSearchBuilder setSentences(Labels<Sentence> sentences) {
            this.sentences = sentences;
            return this;
        }

        ContextSearchBuilder setModifiableTerms(Labels<?> modifiableTerms) {
            this.modifiableTerms = modifiableTerms;
            return this;
        }

        ContextSearchBuilder setTokens(Labels<TermToken> tokens) {
            this.tokens = tokens;
            return this;
        }

        ContextSearchBuilder setPartOfSpeechLabels(Labels<PartOfSpeech> partOfSpeechLabels) {
            this.partOfSpeechLabels = partOfSpeechLabels;
            return this;
        }

        ContextSearch createContextSearch() {
            return new ContextSearch(contextCues, sentences, modifiableTerms, tokens, partOfSpeechLabels);
        }
    }
}
