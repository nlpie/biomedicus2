/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

import static edu.umn.biomedicus.modification.ModificationType.HISTORICAL;
import static edu.umn.biomedicus.modification.ModificationType.NEGATED;
import static edu.umn.biomedicus.modification.ModificationType.PROBABLE;

import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.common.types.semantics.DictionaryTerm;
import edu.umn.biomedicus.common.types.semantics.Historical;
import edu.umn.biomedicus.common.types.semantics.ImmutableHistorical;
import edu.umn.biomedicus.common.types.semantics.ImmutableNegated;
import edu.umn.biomedicus.common.types.semantics.ImmutableProbable;
import edu.umn.biomedicus.common.types.semantics.Negated;
import edu.umn.biomedicus.common.types.semantics.Probable;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.DefaultLabelIndex;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextView;
import java.util.List;

/**
 *
 */
public class ModificationDetector implements DocumentProcessor {

  private static final ContextCues CUES = ContextCues
      .builder()
      .addLeftPhrase(HISTORICAL, "History")
      .addLeftPhrase(HISTORICAL, "history")
      .addLeftPhrase(HISTORICAL, "Historical")
      .addLeftPhrase(HISTORICAL, "historical")
      .addLeftPhrase(HISTORICAL, "Histories")
      .addLeftPhrase(HISTORICAL, "histories")
      .addLeftPhrase(HISTORICAL, "Status", "Post")
      .addLeftPhrase(HISTORICAL, "status", "post")
      .addLeftPhrase(HISTORICAL, "S/P")
      .addLeftPhrase(HISTORICAL, "s/p")
      .addLeftPhrase(HISTORICAL, "S-P")
      .addLeftPhrase(HISTORICAL, "s-p")
      .addLeftPhrase(HISTORICAL, "S.P.")
      .addLeftPhrase(HISTORICAL, "s.p.")
      .addLeftPhrase(HISTORICAL, "SP")
      .addLeftPhrase(HISTORICAL, "sp")
      .addRightPhrase(HISTORICAL, "History")
      .addRightPhrase(HISTORICAL, "history")
      .addLeftPhrase(NEGATED, "No")
      .addLeftPhrase(NEGATED, "no")
      .addLeftPhrase(NEGATED, "Deny")
      .addLeftPhrase(NEGATED, "deny")
      .addLeftPhrase(NEGATED, "Denies")
      .addLeftPhrase(NEGATED, "denies")
      .addLeftPhrase(NEGATED, "Denying")
      .addLeftPhrase(NEGATED, "denying")
      .addLeftPhrase(NEGATED, "Absent")
      .addLeftPhrase(NEGATED, "absent")
      .addLeftPhrase(NEGATED, "Negative")
      .addLeftPhrase(NEGATED, "negative")
      .addLeftPhrase(NEGATED, "Without")
      .addLeftPhrase(NEGATED, "without")
      .addLeftPhrase(NEGATED, "w/o")
      .addLeftPhrase(NEGATED, "W/O")
      .addLeftPhrase(NEGATED, "Never")
      .addLeftPhrase(NEGATED, "never")
      .addLeftPhrase(NEGATED, "Unremarkable")
      .addLeftPhrase(NEGATED, "unremarkable")
      .addLeftPhrase(NEGATED, "Un-remarkable")
      .addLeftPhrase(NEGATED, "un-remarkable")
      .addRightPhrase(NEGATED, "none")
      .addRightPhrase(NEGATED, "negative")
      .addRightPhrase(NEGATED, "absent")
      .addLeftPhrase(PROBABLE, "Possible")
      .addLeftPhrase(PROBABLE, "possible")
      .addLeftPhrase(PROBABLE, "Possibly")
      .addLeftPhrase(PROBABLE, "possibly")
      .addLeftPhrase(PROBABLE, "Probable")
      .addLeftPhrase(PROBABLE, "probable")
      .addLeftPhrase(PROBABLE, "Probably")
      .addLeftPhrase(PROBABLE, "probably")
      .addLeftPhrase(PROBABLE, "Might")
      .addLeftPhrase(PROBABLE, "might")
      .addLeftPhrase(PROBABLE, "likely")
      .addLeftPhrase(PROBABLE, "Likely")
      .addLeftPhrase(PROBABLE, "am", "not", "sure")
      .addLeftPhrase(PROBABLE, "Am", "not", "sure")
      .addLeftPhrase(PROBABLE, "Not", "sure")
      .addLeftPhrase(PROBABLE, "not", "sure")
      .addLeftPhrase(PROBABLE, "Differential")
      .addLeftPhrase(PROBABLE, "differential")
      .addLeftPhrase(PROBABLE, "Uncertain")
      .addLeftPhrase(PROBABLE, "uncertain")
      .addLeftPhrase(PROBABLE, "chance")
      .addLeftPhrase(PROBABLE, "Chance")
      .addRightPhrase(PROBABLE, "likely")
      .addRightPhrase(PROBABLE, "probable")
      .addRightPhrase(PROBABLE, "unlikely")
      .addRightPhrase(PROBABLE, "possible")
      .addRightPhrase(PROBABLE, "uncertain")
      .addScopeDelimitingPos(PartOfSpeech.WDT)
      .addScopeDelimitingPos(PartOfSpeech.PRP)
      .addScopeDelimitingPos(PartOfSpeech.VBZ)
      .addScopeDelimitingWord("but")
      .addScopeDelimitingWord("however")
      .addScopeDelimitingWord("therefore")
      .addScopeDelimitingWord("otherwise")
      .addScopeDelimitingWord("except")
      .addScopeDelimitingWord(";")
      .addScopeDelimitingWord(":")
      .build();

  @Override
  public void process(Document document) throws BiomedicusException {
    TextView textView = document.getTextView(StandardViews.SYSTEM)
        .orElseThrow(() -> new BiomedicusException("No system view"));

    LabelIndex<TermToken> tokenLabelIndex = new DefaultLabelIndex<>(
        textView.getLabelIndex(TermToken.class));
    LabelIndex<DictionaryTerm> dictionaryTermLabelIndex = textView
        .getLabelIndex(DictionaryTerm.class);
    LabelIndex<Sentence> sentenceLabelIndex = new DefaultLabelIndex<>(
        textView.getLabelIndex(Sentence.class));
    LabelIndex<PartOfSpeech> partOfSpeechLabelIndex = new DefaultLabelIndex<>(
        textView.getLabelIndex(PartOfSpeech.class));

    Labeler<Probable> probableLabeler = textView.getLabeler(Probable.class);
    Labeler<Historical> historicalLabeler = textView.getLabeler(Historical.class);
    Labeler<Negated> negatedLabeler = textView.getLabeler(Negated.class);

    for (Label<DictionaryTerm> termLabel : dictionaryTermLabelIndex) {
      Label<Sentence> sentenceLabel = sentenceLabelIndex.containing(termLabel).first()
          .orElseThrow(IllegalStateException::new);

      LabelIndex<TermToken> sentenceTokenLabels = tokenLabelIndex.insideSpan(sentenceLabel);

      List<Label<TermToken>> contextList = sentenceTokenLabels.leftwardsFrom(termLabel).asList();

      Pair<ModificationType, List<Span>> result = CUES.searchLeft(contextList,
          partOfSpeechLabelIndex);
      if (result != null) {
        switch (result.first()) {
          case HISTORICAL:
            historicalLabeler
                .value(ImmutableHistorical.builder().addAllCueTerms(result.second()).build())
                .label(termLabel);
            break;
          case NEGATED:
            negatedLabeler.value(ImmutableNegated.builder().addAllCueTerms(result.second()).build())
                .label(termLabel);
            break;
          case PROBABLE:
            probableLabeler
                .value(ImmutableProbable.builder().addAllCueTerms(result.second()).build())
                .label(termLabel);
            break;
          default:
            throw new IllegalStateException();
        }
        continue;
      }

      contextList = sentenceTokenLabels.rightwardsFrom(termLabel).asList();

      result = CUES.searchRight(contextList, partOfSpeechLabelIndex);
      if (result != null) {
        switch (result.first()) {
          case HISTORICAL:
            historicalLabeler
                .value(ImmutableHistorical.builder().addAllCueTerms(result.second()).build())
                .label(termLabel);
            break;
          case NEGATED:
            negatedLabeler.value(ImmutableNegated.builder().addAllCueTerms(result.second()).build())
                .label(termLabel);
            break;
          case PROBABLE:
            probableLabeler
                .value(ImmutableProbable.builder().addAllCueTerms(result.second()).build())
                .label(termLabel);
            break;
          default:
            throw new IllegalStateException();
        }
      }
    }
  }
}
