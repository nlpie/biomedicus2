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
import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.text.*;

import java.util.ArrayList;
import java.util.List;

final class ContextSearch {
    private final int leftContextScope;
    private final List<String> leftContextCues;
    private final int rightContextScope;
    private final List<String> rightContextCues;
    private final List<PartOfSpeech> scopeDelimitersPos;
    private final List<String> scopeDelimitersTxt;
    private final String documentText;
    private final Labels<Sentence2> sentences;
    private final Labels<?> tokens;
    private final Labels<PartOfSpeech> partOfSpeechLabels;
    private final Labels<?> modifiableTerms;

    ContextSearch(ContextCues contextCues,
                  Document document,
                  Labels<Sentence2> sentences,
                  Labels<?> modifiableTerms,
                  Labels<?> tokens,
                  Labels<PartOfSpeech> partOfSpeechLabels) {
        leftContextScope = contextCues.getLeftContextScope();
        leftContextCues = contextCues.getLeftContextCues();
        rightContextScope = contextCues.getRightContextScope();
        rightContextCues = contextCues.getRightContextCues();
        scopeDelimitersPos = contextCues.getScopeDelimitersPos();
        scopeDelimitersTxt = contextCues.getScopeDelimitersTxt();
        documentText = document.getText();
        this.sentences = sentences;
        this.modifiableTerms = modifiableTerms;
        this.tokens = tokens;
        this.partOfSpeechLabels = partOfSpeechLabels;
    }

    List<Span> findMatches() {
        List<Span> matchingSpans = new ArrayList<>();
        for (Label<Sentence2> sentence : sentences) {
            Labels<?> sentenceTokens = tokens.insideSpan(sentence);
            for (Label<?> modifiableTerm : modifiableTerms.insideSpan(sentence)) {
                if (matches(sentenceTokens, modifiableTerm)) {
                    matchingSpans.add(modifiableTerm.toSpan());
                }
            }
        }

        return matchingSpans;
    }

    private boolean matches(Labels<?> sentenceTokens, Label<?> modifiableTerm) {
        Labels<?> leftContextTokens = sentenceTokens.leftwardsFrom(modifiableTerm);
        for (Label<?> leftContextToken : leftContextTokens.limit(leftContextScope)) {
            String tokenText = leftContextToken.getCovered(documentText).toString();
            if (scopeDelimitersTxt.contains(tokenText.toLowerCase())) {
                break;
            }
            if (partOfSpeechLabels.insideSpan(leftContextToken).stream().map(Label::value).anyMatch(scopeDelimitersPos::contains)) {
                break;
            }
            if (leftContextCues.contains(tokenText.toLowerCase())) {
                return true;
            }
        }

        Labels<?> rightContextTokens = sentenceTokens.rightwardsFrom(modifiableTerm);
        for (Label<?> rightContextToken : rightContextTokens.limit(rightContextScope)) {
            String tokenText = rightContextToken.getCovered(documentText).toString();
            if (scopeDelimitersTxt.contains(tokenText.toLowerCase())) {
                break;
            }
            if (partOfSpeechLabels.insideSpan(rightContextToken).stream().map(Label::value).anyMatch(scopeDelimitersPos::contains)) {
                break;
            }
            if (rightContextCues.contains(tokenText.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
