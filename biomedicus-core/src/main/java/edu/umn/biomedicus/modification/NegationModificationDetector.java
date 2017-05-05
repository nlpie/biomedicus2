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
import edu.umn.biomedicus.application.TextView;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.types.semantics.DictionaryTerm;
import edu.umn.biomedicus.common.types.semantics.ImmutableNegated;
import edu.umn.biomedicus.common.types.semantics.Negated;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class NegationModificationDetector implements DocumentProcessor {
    private static final ContextCues CONTEXT_CUES = ContextCues.builder()
            .addLeftPhrase("No")
            .addLeftPhrase("no")
            .addLeftPhrase("Deny")
            .addLeftPhrase("deny")
            .addLeftPhrase("Denies")
            .addLeftPhrase("denies")
            .addLeftPhrase("Denying")
            .addLeftPhrase("denying")
            .addLeftPhrase("Absent")
            .addLeftPhrase("absent")
            .addLeftPhrase("Negative")
            .addLeftPhrase("negative")
            .addLeftPhrase("Without")
            .addLeftPhrase("without")
            .addLeftPhrase("w/o")
            .addLeftPhrase("W/O")
            .addLeftPhrase("Never")
            .addLeftPhrase("never")
            .addLeftPhrase("Unremarkable")
            .addLeftPhrase("unremarkable")
            .addLeftPhrase("Un-remarkable")
            .addLeftPhrase("un-remarkable")
            .addRightPhrase("none")
            .addRightPhrase("negative")
            .addRightPhrase("absent")
            .addScopeDelimitingPos(PartOfSpeech.WDT)
            .addScopeDelimitingPos(PartOfSpeech.PRP)
            .addScopeDelimitingPos(PartOfSpeech.VBZ)
            .addScopeDelimitingWord("but")
            .addScopeDelimitingWord("however")
            .addScopeDelimitingWord("therefore")
            .addScopeDelimitingWord("otherwise")
            .addScopeDelimitingWord(";")
            .addScopeDelimitingWord(":")
            .addScopeDelimitingWord("except")
            .build();
    private final LabelIndex<Sentence> sentences;
    private final LabelIndex<DictionaryTerm> dictionaryTerms;
    private final LabelIndex<TermToken> termTokens;
    private final LabelIndex<PartOfSpeech> partsOfSpeech;
    private final Labeler<Negated> labeler;

    @Inject
    public NegationModificationDetector(TextView document) {
        sentences = document.getLabelIndex(Sentence.class);
        dictionaryTerms = document.getLabelIndex(DictionaryTerm.class);
        termTokens = document.getLabelIndex(TermToken.class);
        partsOfSpeech = document.getLabelIndex(PartOfSpeech.class);
        labeler = document.getLabeler(Negated.class);
    }

    @Override
    public void process() throws BiomedicusException {
        ContextSearch.ContextSearchBuilder contextSearchBuilder = new ContextSearch.ContextSearchBuilder();
        contextSearchBuilder.setContextCues(CONTEXT_CUES)
                .setSentences(sentences)
                .setModifiableTerms(dictionaryTerms)
                .setTokens(termTokens)
                .setPartOfSpeechLabelIndex(partsOfSpeech);
        ContextSearch contextSearch = contextSearchBuilder.createContextSearch();
        Map<Span, List<Label<TermToken>>> matches = contextSearch.findMatches();
        for (Map.Entry<Span, List<Label<TermToken>>> entry : matches.entrySet()) {
            List<Label<TermToken>> cues = entry.getValue();
            List<Span> cuesList = cues.stream().map(Label::toSpan).collect(Collectors.toList());
            labeler.value(ImmutableNegated.builder().addAllCueTerms(cuesList).build()).label(entry.getKey());
        }
    }
}
