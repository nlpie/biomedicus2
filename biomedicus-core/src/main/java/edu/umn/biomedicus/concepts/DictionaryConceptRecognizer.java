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

import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.terms.TermsBag;
import edu.umn.biomedicus.common.types.semantics.Acronym;
import edu.umn.biomedicus.common.types.semantics.DictionaryTerm;
import edu.umn.biomedicus.common.types.semantics.ImmutableDictionaryTerm;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.syntax.PartsOfSpeech;
import edu.umn.biomedicus.common.types.text.NormIndex;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.common.types.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextLocation;
import edu.umn.biomedicus.framework.store.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
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
class DictionaryConceptRecognizer implements DocumentProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryConceptRecognizer.class);

  private static final Set<PartOfSpeech> TRIVIAL_POS = buildTrivialPos();

  private static final int SPAN_SIZE = 5;

  private final ConceptDictionary conceptDictionary;

  @Nullable
  private Labeler<DictionaryTerm> termLabeler;

  @Nullable
  private LabelIndex<PartOfSpeech> partOfSpeechLabelIndex;

  @Nullable
  private LabelIndex<NormIndex> normIndexes;

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

  private boolean checkPhrase(Span span, String phrase, boolean oneToken, double confMod)
      throws BiomedicusException {
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

  private void checkTokenSet(List<Label<TermToken>> tokenSet) throws BiomedicusException {

    assert normIndexes != null;
    assert partOfSpeechLabelIndex != null;

    if (tokenSet.size() <= 1) {
      return;
    }

    Span phraseAsSpan = new Span(tokenSet.get(0).getBegin(),
        tokenSet.get(tokenSet.size() - 1).getEnd());
    TermsBag.Builder builder = TermsBag.builder();
    for (Label<NormIndex> normIndexLabel : normIndexes.insideSpan(phraseAsSpan)) {

      Optional<Label<PartOfSpeech>> partOfSpeechLabel = partOfSpeechLabelIndex
          .withTextLocation(normIndexLabel);

      if (partOfSpeechLabel.isPresent() && TRIVIAL_POS.contains(partOfSpeechLabel.get().value())) {
        continue;
      }

      builder.addTerm(normIndexLabel.value().term());
    }
    TermsBag normVector = builder.build();

    List<SuiCuiTui> normsCUI = conceptDictionary.forNorms(normVector);
    if (normsCUI != null) {
      makeTerm(phraseAsSpan, normsCUI, .3);
    }
  }

  private void makeTerm(TextLocation textLocation, List<SuiCuiTui> cuis, double confidence)
      throws BiomedicusException {

    assert termLabeler != null;

    ImmutableDictionaryTerm.Builder builder = ImmutableDictionaryTerm.builder();

    for (SuiCuiTui cui : cuis) {
      builder.addConcepts(cui.toConcept(confidence));
    }

    DictionaryTerm dictionaryTerm = builder.build();

    termLabeler.value(dictionaryTerm).label(textLocation);
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    LOGGER.debug("Finding concepts in document.");

    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<Sentence> sentences = systemView.getLabelIndex(Sentence.class);
    normIndexes = systemView.getLabelIndex(NormIndex.class);
    termLabeler = systemView.getLabeler(DictionaryTerm.class);
    partOfSpeechLabelIndex = systemView.getLabelIndex(PartOfSpeech.class);
    LabelIndex<TermToken> termTokenLabelIndex = systemView.getLabelIndex(TermToken.class);
    LabelIndex<Acronym> acronymLabelIndex = systemView.getLabelIndex(Acronym.class);

    String documentText = systemView.getText();
    for (Label<Sentence> sentence : sentences) {
      LOGGER.trace("Identifying concepts in a sentence");

      StringBuilder editedString = new StringBuilder();
      List<Span> editedStringSpans = new ArrayList<>();
      List<Label<TermToken>> sentenceTermTokens = termTokenLabelIndex.insideSpan(sentence).asList();

      for (Label<TermToken> sentenceTermToken : sentenceTermTokens) {
        Optional<Label<Acronym>> acronymForToken = acronymLabelIndex
            .withTextLocation(sentenceTermToken);

        Token token = acronymForToken.<Token>map(Label::value).orElseGet(sentenceTermToken::value);
        String tokenText = token.text();
        Span span = new Span(editedString.length(), editedString.length() + tokenText.length());
        editedString.append(tokenText);
        if (token.hasSpaceAfter()) {
          editedString.append(' ');
        }
        editedStringSpans.add(span);
      }

      for (int from = 0; from < sentenceTermTokens.size(); from++) {
        int to = Math.min(from + SPAN_SIZE, sentenceTermTokens.size());
        List<Label<TermToken>> window = sentenceTermTokens.subList(from, to);

        Label<TermToken> firstTokenLabel = window.get(0);
        boolean firstTrivial = TRIVIAL_POS
            .containsAll(partOfSpeechLabelIndex.insideSpan(firstTokenLabel).values());

        for (int subsetSize = 1; subsetSize <= window.size(); subsetSize++) {
          List<Label<TermToken>> windowSubset = window.subList(0, subsetSize);
          int last = subsetSize - 1;
          Label<TermToken> lastTokenLabel = windowSubset.get(last);
          Span asSpan = new Span(firstTokenLabel.getBegin(), lastTokenLabel.getEnd());

          boolean phraseFound = checkPhrase(asSpan,
              documentText.substring(asSpan.getBegin(), asSpan.getEnd()),
              subsetSize == 1, 0);

          if (!phraseFound) {
            int editedBegin = editedStringSpans.get(from).getBegin();
            int editedEnd = editedStringSpans.get(from + last).getEnd();
            String editedSubstring = editedString.substring(editedBegin, editedEnd);
            phraseFound = checkPhrase(asSpan, editedSubstring, subsetSize == 1, .1);
          }
          if (!phraseFound) {
            boolean lastTrivial = TRIVIAL_POS
                .containsAll(partOfSpeechLabelIndex.insideSpan(lastTokenLabel).values());
            if (!firstTrivial && !lastTrivial) {
              checkTokenSet(windowSubset);
            }
          }
        }
      }
    }
  }
}
