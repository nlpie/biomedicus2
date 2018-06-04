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

package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.acronyms.Acronym;
import edu.umn.biomedicus.acronyms.OtherAcronymSense;
import edu.umn.biomedicus.acronyms.ScoredSense;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.biomedicus.tokenization.TermToken;
import edu.umn.biomedicus.tokenization.Token;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.DocumentOperation;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import edu.umn.nlpengine.TextRange;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
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
class LabelAcronyms implements DocumentOperation {

  private static final Logger LOGGER = LoggerFactory.getLogger(LabelAcronyms.class);

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

  private final Boolean labelOtherSenses;

  private Labeler<Acronym> acronymLabeler;

  private List<TermToken> termTokens;

  private Labeler<OtherAcronymSense> otherSenseLabeler;

  /**
   * Constructor to initialize the acronym detector
   *
   * @param model an AcronymModel that contains lists of acronyms and their senses
   * @param orthographicModel optional - an orthographic model for detecting unknown abbreviations
   */
  @Inject
  public LabelAcronyms(
      @Setting("acronym.model") AcronymModel model,
      @ProcessorSetting("acronym.checkParseTokens") Boolean checkParseTokens,
      @ProcessorSetting("acronym.labelOtherSenses") Boolean labelOtherSenses,
      @Nullable OrthographicAcronymModel orthographicModel
  ) {
    this.orthographicModel = orthographicModel;
    this.model = model;
    this.checkParseTokens = checkParseTokens;
    this.labelOtherSenses = labelOtherSenses;
  }

  @Override
  public void process(@Nonnull Document document) {
    LOGGER.debug("Detecting acronyms in a document.");

    LabelIndex<TermToken> termTokenLabels = document.labelIndex(TermToken.class);
    LabelIndex<PosTag> partOfSpeechLabels = document.labelIndex(PosTag.class);
    LabelIndex<ParseToken> parseTokenLabels = document.labelIndex(ParseToken.class);
    acronymLabeler = document.labeler(Acronym.class);
    otherSenseLabeler = document.labeler(OtherAcronymSense.class);

    List<TermToken> termTokenLabelList = termTokenLabels.asList();
    termTokens = termTokenLabels.asList();

    int size = termTokenLabels.size();
    TERM_TOKENS:
    for (int i = 0; i < size; i++) {
      TermToken termToken = termTokenLabelList.get(i);

      List<PosTag> partOfSpeechLabelsForToken = partOfSpeechLabels.inside(termToken).asList();

      if (!partOfSpeechLabelsForToken.stream().map(PosTag::getPartOfSpeech).allMatch(EXCLUDE_POS::contains)) {
        if (!checkAndLabel(i, termToken) && checkParseTokens) {
          for (ParseToken parseToken : parseTokenLabels.inside(termToken)) {
            if (!EXCLUDE_POS.contains(partOfSpeechLabels.atLocation(parseToken).iterator().next().getPartOfSpeech())) {
              if (checkAndLabel(i, parseToken)) {
                continue TERM_TOKENS;
              }
            }
          }
        }
      }
    }
  }

  private <T extends Token& TextRange> boolean checkAndLabel(int i, T token) {
    boolean found = false;
    if (model.hasAcronym(token)
        || (orthographicModel != null && orthographicModel.seemsLikeAbbreviation(token))) {
      List<ScoredSense> senses = model.findBestSense(termTokens, i);
      if (senses.size() > 0) {
        ScoredSense first = senses.get(0);
        acronymLabeler.add(new Acronym(token, first.getSense(), token.getHasSpaceAfter(),
            first.getScore()));
        if (labelOtherSenses) {
          for (int j = 1; j < senses.size(); j++) {
            ScoredSense scoredSense = senses.get(j);
            otherSenseLabeler.add(new OtherAcronymSense(token, scoredSense.getSense(),
                token.getHasSpaceAfter(), scoredSense.getScore()));
          }
        }
        found = true;
      }
    }
    return found;
  }
}
