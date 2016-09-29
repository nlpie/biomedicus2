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

import com.google.inject.Inject;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.types.semantics.DictionaryTerm;
import edu.umn.biomedicus.common.types.semantics.Historical;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class HistoryModificationDetector implements DocumentProcessor {
    private static final ContextCues CONTEXT_CUES_MATCHER = ContextCues.builder()
            .addLeftPhrase("History")
            .addLeftPhrase("history")
            .addLeftPhrase("Historical")
            .addLeftPhrase("historical")
            .addLeftPhrase("Histories")
            .addLeftPhrase("histories")
            .addLeftPhrase("Status", "Post")
            .addLeftPhrase("status", "post")
            .addLeftPhrase("S/P")
            .addLeftPhrase("s/p")
            .addLeftPhrase("S-P")
            .addLeftPhrase("s-p")
            .addLeftPhrase("S.P.")
            .addLeftPhrase("s.p.")
            .addLeftPhrase("SP")
            .addLeftPhrase("sp")
            .addRightPhrase("History")
            .addRightPhrase("history")
            .addScopeDelimitingPos(PartOfSpeech.WDT)
            .addScopeDelimitingPos(PartOfSpeech.PRP)
            .addScopeDelimitingPos(PartOfSpeech.VBZ)
            .addScopeDelimitingWord("but")
            .addScopeDelimitingWord("however")
            .addScopeDelimitingWord("therefore")
            .addScopeDelimitingWord("otherwise")
            .addScopeDelimitingWord(";")
            .addScopeDelimitingWord(":")
            .build();

    private final Labels<Sentence> sentences;
    private final Labels<DictionaryTerm> dictionaryTerms;
    private final Labels<TermToken> termTokens;
    private final Labels<PartOfSpeech> partsOfSpeech;
    private final Labeler<Historical> labeler;

    @Inject
    public HistoryModificationDetector(Document document) {
        this.sentences = document.labels(Sentence.class);
        this.dictionaryTerms = document.labels(DictionaryTerm.class);
        this.termTokens = document.labels(TermToken.class);
        this.partsOfSpeech = document.labels(PartOfSpeech.class);
        labeler = document.labeler(Historical.class);
    }

    @Override
    public void process() throws BiomedicusException {
        ContextSearch contextSearch = new ContextSearch.ContextSearchBuilder()
                .setContextCues(CONTEXT_CUES_MATCHER)
                .setSentences(sentences)
                .setModifiableTerms(dictionaryTerms)
                .setTokens(termTokens)
                .setPartOfSpeechLabels(partsOfSpeech)
                .createContextSearch();
        Map<Span, List<Label<TermToken>>> matches = contextSearch.findMatches();
        for (Map.Entry<Span, List<Label<TermToken>>> entry : matches.entrySet()) {
            List<Label<TermToken>> cues = entry.getValue();
            List<Span> cuesList = cues.stream().map(Label::toSpan).collect(Collectors.toList());
            labeler.value(new Historical(cuesList)).label(entry.getKey());
        }
    }
}
