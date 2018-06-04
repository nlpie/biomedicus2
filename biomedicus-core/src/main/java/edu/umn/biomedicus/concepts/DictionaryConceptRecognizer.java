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

package edu.umn.biomedicus.concepts;

import static edu.umn.biomedicus.common.types.syntax.PartOfSpeech.CC;
import static edu.umn.biomedicus.common.types.syntax.PartOfSpeech.CD;
import static edu.umn.biomedicus.common.types.syntax.PartOfSpeech.DT;
import static edu.umn.biomedicus.common.types.syntax.PartOfSpeech.EX;
import static edu.umn.biomedicus.common.types.syntax.PartOfSpeech.IN;
import static edu.umn.biomedicus.common.types.syntax.PartOfSpeech.MD;
import static edu.umn.biomedicus.common.types.syntax.PartOfSpeech.PRP;
import static edu.umn.biomedicus.common.types.syntax.PartOfSpeech.PRP$;
import static edu.umn.biomedicus.common.types.syntax.PartOfSpeech.TO;
import static edu.umn.biomedicus.common.types.syntax.PartOfSpeech.WDT;
import static edu.umn.biomedicus.common.types.syntax.PartOfSpeech.XX;

import edu.umn.biomedicus.acronyms.Acronym;
import edu.umn.biomedicus.common.dictionary.StringsBag;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.syntax.PartsOfSpeech;
import edu.umn.biomedicus.normalization.NormForm;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.tokenization.TermToken;
import edu.umn.biomedicus.tokenization.Token;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.DocumentOperation;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import edu.umn.nlpengine.Span;
import edu.umn.nlpengine.TextRange;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses a {@link ConceptDictionary} to recognize concepts in text. First, it will try to find direct
 * matches against all in-order sublists of tokens in a sentence. Then it will perform syntactic
 * permutations on any prepositional phrases in those sublists.
 *
 * @author Ben Knoll
 * @author Serguei Pakhomov
 * @since 1.0.0
 */
class DictionaryConceptRecognizer implements DocumentOperation {

  private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryConceptRecognizer.class);

  private static final Set<PartOfSpeech> TRIVIAL_POS = buildTrivialPos();

  private static final int SPAN_SIZE = 5;

  private final ConceptDictionary conceptDictionary;

  private Labeler<DictionaryTerm> termLabeler;

  private LabelIndex<PosTag> posTags;

  private LabelIndex<NormForm> normIndexes;

  private Labeler<DictionaryConcept> conceptLabeler;

  /**
   * Creates a dictionary concept recognizer from a concept dictionary and a document.
   *
   * @param conceptDictionary the dictionary to get concepts from.
   */
  @Inject
  DictionaryConceptRecognizer(ConceptDictionary conceptDictionary) {
    this.conceptDictionary = conceptDictionary;
  }

  private static Set<PartOfSpeech> buildTrivialPos() {
    Set<PartOfSpeech> builder = new HashSet<>();
    Collections.addAll(builder,
        DT,
        CD,
        WDT,
        TO,
        CC,
        PRP,
        PRP$,
        MD,
        EX,
        IN,
        XX);

    Set<PartOfSpeech> punctuationClass = PartsOfSpeech.getPunctuationClass();
    builder.addAll(punctuationClass);
    return Collections.unmodifiableSet(builder);
  }

  private boolean checkPhrase(Span span, String phrase, boolean oneToken, double confMod) {
    List<SuiCuiTui> phraseSUI = conceptDictionary.forPhrase(phrase);

    if (phraseSUI != null) {
      makeTerm(span, phraseSUI, 1 - confMod);
      return true;
    }

    if (oneToken) {
      return false;
    }

    phraseSUI = conceptDictionary
        .forLowercasePhrase(phrase.toLowerCase(Locale.ENGLISH));

    if (phraseSUI != null) {
      makeTerm(span, phraseSUI, 0.6 - confMod);
      return true;
    }

    return false;
  }

  private void checkTokenSet(List<TermToken> tokenSet) {
    if (tokenSet.size() <= 1) {
      return;
    }

    Span phraseAsSpan = new Span(tokenSet.get(0).getStartIndex(),
        tokenSet.get(tokenSet.size() - 1).getEndIndex());
    StringsBag.Builder builder = StringsBag.builder();
    for (NormForm normForm : normIndexes.inside(phraseAsSpan)) {

      PosTag posTag = posTags.firstAtLocation(normForm);

      if (posTag != null && TRIVIAL_POS.contains(posTag.getPartOfSpeech())) {
        continue;
      }

      builder.addTerm(normForm.normIdentifier());
    }
    StringsBag normBag = builder.build();

    List<SuiCuiTui> normsCUI = conceptDictionary.forNorms(normBag);
    if (normsCUI != null) {
      makeTerm(phraseAsSpan, normsCUI, .3);
    }
  }

  private void makeTerm(TextRange label, List<SuiCuiTui> cuis, double confidence) {
    for (SuiCuiTui cui : cuis) {
      conceptLabeler.add(cui.toConcept(label, confidence));
    }
    termLabeler.add(new DictionaryTerm(label));
  }

  @Override
  public void process(@NotNull Document document) {
    LOGGER.debug("Finding concepts in document.");

    LabelIndex<Sentence> sentences = document.labelIndex(Sentence.class);
    normIndexes = document.labelIndex(NormForm.class);
    termLabeler = document.labeler(DictionaryTerm.class);
    conceptLabeler = document.labeler(DictionaryConcept.class);
    posTags = document.labelIndex(PosTag.class);
    LabelIndex<TermToken> termTokenLabelIndex = document.labelIndex(TermToken.class);
    LabelIndex<Acronym> acronymLabelIndex = document.labelIndex(Acronym.class);

    String documentText = document.getText();
    for (Sentence sentence : sentences) {
      LOGGER.trace("Identifying concepts in a sentence");

      StringBuilder editedString = new StringBuilder();
      List<Span> editedStringSpans = new ArrayList<>();
      List<TermToken> sentenceTermTokens = termTokenLabelIndex.inside(sentence).asList();

      for (TermToken sentenceTermToken : sentenceTermTokens) {
        Acronym acronymForToken = acronymLabelIndex.firstAtLocation(sentenceTermToken);

        Token token;
        if (acronymForToken != null) {
          token = acronymForToken;
        } else {
          token = sentenceTermToken;
        }

        String tokenText = token.getText();
        Span span = new Span(editedString.length(), editedString.length() + tokenText.length());
        editedString.append(tokenText);
        if (token.getHasSpaceAfter()) {
          editedString.append(' ');
        }
        editedStringSpans.add(span);
      }

      for (int from = 0; from < sentenceTermTokens.size(); from++) {
        int to = Math.min(from + SPAN_SIZE, sentenceTermTokens.size());
        List<TermToken> window = sentenceTermTokens.subList(from, to);

        TermToken first = window.get(0);

        for (int subsetSize = 1; subsetSize <= window.size(); subsetSize++) {
          List<TermToken> windowSubset = window.subList(0, subsetSize);
          TermToken last = windowSubset.get(subsetSize - 1);
          Span entire = new Span(first.getStartIndex(), last.getEndIndex());

          if (posTags.inside(entire).stream()
              .map(PosTag::getPartOfSpeech).allMatch(TRIVIAL_POS::contains)) {
            continue;
          }

          if (checkPhrase(entire, entire.coveredString(documentText), subsetSize == 1, 0)) {
            continue;
          }

          int editedBegin = editedStringSpans.get(from).getStartIndex();
          int editedEnd = editedStringSpans.get(from + subsetSize - 1).getEndIndex();
          String editedSubstring = editedString.substring(editedBegin, editedEnd);
          if (checkPhrase(entire, editedSubstring, subsetSize == 1, .1)) {
            continue;
          }

          checkTokenSet(windowSubset);
        }
      }
    }
  }
}
