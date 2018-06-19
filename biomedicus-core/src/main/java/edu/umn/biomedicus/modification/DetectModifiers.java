/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.concepts.DictionaryTerm;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.tokenization.TermToken;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.DocumentTask;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import edu.umn.nlpengine.Span;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 *
 */
public class DetectModifiers implements DocumentTask {

  private static final ContextCues CUES = ContextCues
      .builder()
      .addLeftPhrase(HISTORICAL, "History")
      .addLeftPhrase(HISTORICAL, "history")
      .addLeftPhrase(HISTORICAL, "Historical")
      .addLeftPhrase(HISTORICAL, "historical")
      .addLeftPhrase(HISTORICAL, "Histories")
      .addLeftPhrase(HISTORICAL, "histories")
      .addLeftPhrase(HISTORICAL, "Status", "Post")
      .addLeftPhrase(HISTORICAL, "Status", "post")
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
  public void run(@Nonnull Document document) {
    LabelIndex<TermToken> tokenLabelIndex = document.labelIndex(TermToken.class);

    LabelIndex<DictionaryTerm> dictionaryTermLabelIndex = document
        .labelIndex(DictionaryTerm.class);

    LabelIndex<Sentence> sentenceLabelIndex = document.labelIndex(Sentence.class);
    LabelIndex<PosTag> partOfSpeechLabelIndex = document.labelIndex(PosTag.class);

    Labeler<Probable> probableLabeler = document.labeler(Probable.class);
    Labeler<Historical> historicalLabeler = document.labeler(Historical.class);
    Labeler<Negated> negatedLabeler = document.labeler(Negated.class);

    Labeler<ModificationCue> cueLabeler = document.labeler(ModificationCue.class);

    for (DictionaryTerm termLabel : dictionaryTermLabelIndex) {
      Sentence sentenceLabel = sentenceLabelIndex.containing(termLabel).first();

      if (sentenceLabel == null) {
        sentenceLabelIndex.containing(termLabel);
        throw new RuntimeException("Term outside of a sentence.");
      }

      LabelIndex<TermToken> sentenceTokenLabels = tokenLabelIndex.inside(sentenceLabel);

      List<TermToken> contextList = sentenceTokenLabels.backwardFrom(termLabel).asList();

      Pair<ModificationType, List<Span>> result = CUES.searchLeft(contextList, partOfSpeechLabelIndex);

      if (result != null) {
        List<ModificationCue> cues = result.second().stream().map(span -> {
          ModificationCue cue = new ModificationCue(span);
          cueLabeler.add(cue);
          return cue;
        }).collect(Collectors.toList());
        switch (result.first()) {
          case HISTORICAL:
            historicalLabeler.add(new Historical(termLabel, cues));
            break;
          case NEGATED:
            negatedLabeler.add(new Negated(termLabel, cues));
            break;
          case PROBABLE:
            probableLabeler.add(new Probable(termLabel, cues));
            break;
          default:
            throw new IllegalStateException();
        }
        continue;
      }

      contextList = sentenceTokenLabels.forwardFrom(termLabel).asList();

      result = CUES.searchRight(contextList, partOfSpeechLabelIndex);
      if (result != null) {
        List<ModificationCue> cues = result.second().stream().map(span -> {
          ModificationCue cue = new ModificationCue(span);
          cueLabeler.add(cue);
          return cue;
        }).collect(Collectors.toList());
        switch (result.first()) {
          case HISTORICAL:
            historicalLabeler.add(new Historical(termLabel, cues));
            break;
          case NEGATED:
            negatedLabeler.add(new Negated(termLabel, cues));
            break;
          case PROBABLE:
            probableLabeler.add(new Probable(termLabel, cues));
            break;
          default:
            throw new IllegalStateException();
        }
      }
    }
  }
}
