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
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.TextIdentifiers;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.LabeledText;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.biomedicus.tokenization.TermToken;
import edu.umn.biomedicus.tokenization.Token;
import edu.umn.nlpengine.TextRange;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
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

    LabeledText systemView = TextIdentifiers.getSystemLabeledText(document);

    LabelIndex<TermToken> termTokenLabels = systemView.labelIndex(TermToken.class);
    LabelIndex<PosTag> partOfSpeechLabels = systemView.labelIndex(PosTag.class);
    LabelIndex<ParseToken> parseTokenLabels = systemView.labelIndex(ParseToken.class);
    acronymLabeler = systemView.labeler(Acronym.class);

    List<TermToken> termTokenLabelList = termTokenLabels.asList();
    termTokens = termTokenLabels.asList();

    int size = termTokenLabels.size();
    TERM_TOKENS:
    for (int i = 0; i < size; i++) {
      TermToken termToken = termTokenLabelList.get(i);

      List<PosTag> partOfSpeechLabelsForToken = partOfSpeechLabels.insideSpan(termToken).asList();

      if (!partOfSpeechLabelsForToken.stream().map(PosTag::getPartOfSpeech).allMatch(EXCLUDE_POS::contains)) {
        if (!checkAndLabel(i, termToken) && checkParseTokens) {
          for (ParseToken parseToken : parseTokenLabels.insideSpan(termToken)) {
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

  private <T extends Token& TextRange> boolean checkAndLabel(int i, T token)
      throws BiomedicusException {
    boolean found = false;
    if (model.hasAcronym(token)
        || (orthographicModel != null && orthographicModel.seemsLikeAbbreviation(token))) {
      String sense = model.findBestSense(termTokens, i);
      if (!Acronyms.UNKNOWN.equals(sense) && !sense.equalsIgnoreCase(token.getText())) {
        LOGGER.trace("Labeling acronym expansion: {}", sense);
        acronymLabeler.add(new Acronym(token, token.getText(), token.getHasSpaceAfter()));
        found = true;
      }
    }
    return found;
  }
}
