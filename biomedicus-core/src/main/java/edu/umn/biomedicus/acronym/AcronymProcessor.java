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

import edu.umn.biomedicus.annotations.ProcessorSetting;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
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

  private final boolean checkParseTokens;

  private Labeler<Acronym> acronymLabeler;

  private List<TermToken> termTokens;

  /**
   * Constructor to initialize the acronym detector
   *
   * @param model an AcronymModel that contains lists of acronyms and their senses
   * @param orthographicModel optional - an orthographic model for detecting unknown abbreviations
   */
  @Inject
  public AcronymProcessor(
      @Setting("acronym.model") AcronymModel model,
      @ProcessorSetting("acronym.checkParseTokens") Boolean checkParseTokens,
      @Nullable OrthographicAcronymModel orthographicModel
  ) {
    this.orthographicModel = orthographicModel;
    this.model = model;
    this.checkParseTokens = checkParseTokens;
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    LOGGER.debug("Detecting acronyms in a document.");

    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<TermToken> termTokenLabels = systemView.getLabelIndex(TermToken.class);
    LabelIndex<PartOfSpeech> partOfSpeechLabels = systemView.getLabelIndex(PartOfSpeech.class);
    LabelIndex<ParseToken> parseTokenLabels = systemView.getLabelIndex(ParseToken.class);
    acronymLabeler = systemView.getLabeler(Acronym.class);

    List<Label<TermToken>> termTokenLabelList = termTokenLabels.asList();
    termTokens = termTokenLabels.valuesAsList();

    int size = termTokenLabels.size();
    TERM_TOKENS:
    for (int i = 0; i < size; i++) {
      Label<TermToken> termTokenLabel = termTokenLabelList.get(i);

      List<Label<PartOfSpeech>> partOfSpeechLabelsForToken = partOfSpeechLabels
          .insideSpan(termTokenLabel).asList();

      if (!partOfSpeechLabelsForToken.stream().map(Label::value)
          .allMatch(EXCLUDE_POS::contains)) {
        if (!checkAndLabel(i, termTokenLabel) && checkParseTokens) {
          for (Label<ParseToken> parseTokenLabel : parseTokenLabels.insideSpan(termTokenLabel)) {
            if (!EXCLUDE_POS.contains(partOfSpeechLabels.withTextLocation(parseTokenLabel)
                .orElseThrow(() -> new BiomedicusException("Parse token without POS")).value())) {
              if (checkAndLabel(i, parseTokenLabel)) {
                continue TERM_TOKENS;
              }
            }
          }
        }
      }
    }
  }

  private boolean checkAndLabel(int i, Label<? extends Token> tokenLabel)
      throws BiomedicusException {
    boolean found = false;
    Token token = tokenLabel.value();
    if (model.hasAcronym(token)
        || (orthographicModel != null && orthographicModel.seemsLikeAbbreviation(token))) {
      String sense = model.findBestSense(termTokens, i);
      if (!Acronyms.UNKNOWN.equals(sense) && !sense.equalsIgnoreCase(token.text())) {
        LOGGER.trace("Labeling acronym expansion: {}", sense);
        acronymLabeler.value(ImmutableAcronym.builder()
            .text(sense)
            .hasSpaceAfter(token.hasSpaceAfter())
            .build())
            .label(tokenLabel);
        found = true;
      }
    }
    return found;
  }
}
