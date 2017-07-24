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

package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.types.semantics.Acronym;
import edu.umn.biomedicus.common.types.semantics.ImmutableAcronym;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.common.types.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.TextView;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will tag tokens as acronym/abbreviations or not
 *
 * @author Greg Finley
 * @since 1.5.0
 */
class AcronymProcessor implements DocumentProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(AcronymProcessor.class);

  /*
   * All part of speech tags to exclude from consideration as acronyms.
   * Some of the verbs may have to change, but PRP and CC are key (esp. for tokens like "it", "or")
   */
  private static final Set<PartOfSpeech> EXCLUDE_POS = EnumSet.of(
      PartOfSpeech.PRP,
      PartOfSpeech.DT,
      PartOfSpeech.CC,
      PartOfSpeech.IN,
      PartOfSpeech.UH,
      PartOfSpeech.TO,
      PartOfSpeech.RP,
      PartOfSpeech.PDT,
      PartOfSpeech.WP,
      PartOfSpeech.WP$,
      PartOfSpeech.WDT,
      PartOfSpeech.POS,
      PartOfSpeech.MD
  );

  private final AcronymModel model;

  @Nullable
  private final OrthographicAcronymModel orthographicModel;

  /**
   * Constructor to initialize the acronym detector
   *
   * @param model an AcronymModel that contains lists of acronyms and their senses
   * @param orthographicModel optional - an orthographic model for detecting unknown abbreviations
   */
  @Inject
  public AcronymProcessor(
      @Setting("acronym.model") AcronymModel model,
      @Nullable OrthographicAcronymModel orthographicModel
  ) {
    this.orthographicModel = orthographicModel;
    this.model = model;
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    LOGGER.debug("Detecting acronyms in a document.");

    TextView systemView = document.getTextView(StandardViews.SYSTEM)
        .orElseThrow(() -> new BiomedicusException("Missing system view"));

    LabelIndex<TermToken> termTokenLabels = systemView.getLabelIndex(TermToken.class);
    LabelIndex<PartOfSpeech> partOfSpeechLabels = systemView.getLabelIndex(PartOfSpeech.class);
    LabelIndex<ParseToken> parseTokenLabels = systemView.getLabelIndex(ParseToken.class);
    Labeler<Acronym> acronymLabeler = systemView.getLabeler(Acronym.class);

    List<Token> allTokens = null;
    List<Label<TermToken>> termTokenLabelsList = termTokenLabels.asList();

    int size = termTokenLabelsList.size();
    for (int i = 0; i < size; i++) {
      Label<TermToken> termTokenLabel = termTokenLabelsList.get(i);
      TermToken termToken = termTokenLabel.value();

      List<Label<PartOfSpeech>> partOfSpeechLabelsForToken = partOfSpeechLabels
          .insideSpan(termTokenLabel).asList();

      List<Label<? extends Token>> suspectedAcronyms = new ArrayList<>();

      boolean excludedPos = partOfSpeechLabelsForToken.stream().map(Label::value)
          .allMatch(EXCLUDE_POS::contains);

      if (!excludedPos) {
        if (model.hasAcronym(termToken)) {
          suspectedAcronyms.add(termTokenLabel);
          LOGGER.trace("Found potential acronym: {}", termToken);
        } else {
          // check the orthographic model AND all spanned ParseTokens
          if (orthographicSaysAcronym(termToken)) {
            suspectedAcronyms.add(termTokenLabel);
            LOGGER.trace("Found potential acronym: {}", termToken);
          }
          List<Label<ParseToken>> termParseTokens = parseTokenLabels.insideSpan(termTokenLabel)
              .asList();
          for (Label<ParseToken> parseTokenLabel : termParseTokens) {
            ParseToken parseToken = parseTokenLabel.value();

            Optional<Label<PartOfSpeech>> pos = partOfSpeechLabels
                .withTextLocation(parseTokenLabel);

            if ((!pos.isPresent() || !EXCLUDE_POS.contains(pos.get().value()))
                && (model.hasAcronym(parseToken) || orthographicSaysAcronym(parseToken))) {

              suspectedAcronyms.add(parseTokenLabel);
              LOGGER.trace("Found potential acronym: {}", parseToken);
            }
          }
        }
      }
      for (Label<? extends Token> acronymLabel : suspectedAcronyms) {
        // TODO: look at this and see if we can do without this list creation.
        if (allTokens == null) {
          allTokens = termTokenLabelsList.stream().map(Label::value).collect(Collectors.toList());
        }
        Token acronymToken = acronymLabel.value();
        // swap out the current TermToken for the ParseToken we just
        // found and find the sense
        Token tempToken = allTokens.set(i, acronymToken);
        String sense = model.findBestSense(allTokens, i);
        if (!Acronyms.UNKNOWN.equals(sense) && !sense
            .equalsIgnoreCase(acronymToken.text())) {
          LOGGER.trace("Labeling acronym expansion: {}", sense);
          acronymLabeler.value(ImmutableAcronym.builder()
              .text(sense)
              .hasSpaceAfter(acronymToken.hasSpaceAfter())
              .build())
              .label(acronymLabel);
          // If we just successfully set the term token, don't both
          // with ParseTokens
          if (acronymToken instanceof TermToken) {
            break;
          }
        }
        allTokens.set(i, tempToken);
      }
    }
  }

  private boolean orthographicSaysAcronym(Token token) {
    return orthographicModel != null && orthographicModel.seemsLikeAbbreviation(token);
  }
}
