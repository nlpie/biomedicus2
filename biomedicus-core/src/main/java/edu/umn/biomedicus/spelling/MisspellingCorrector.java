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

package edu.umn.biomedicus.spelling;

import com.google.inject.Inject;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.grams.Ngram;
import edu.umn.biomedicus.common.types.semantics.ImmutableSpellCorrection;
import edu.umn.biomedicus.common.types.semantics.Misspelling;
import edu.umn.biomedicus.common.types.semantics.SpellCorrection;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.common.utilities.Patterns;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.TextView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class MisspellingCorrector implements DocumentProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingCorrector.class);

  private final SpellingModel spellingModel;

  @Inject
  public MisspellingCorrector(SpellingModel spellingModel) {
    this.spellingModel = spellingModel;
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    LOGGER.debug("Correcting any misspelled words in a document.");

    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<Sentence> sentence2LabelIndex = systemView.getLabelIndex(Sentence.class);
    LabelIndex<ParseToken> parseTokenLabelIndex = systemView.getLabelIndex(ParseToken.class);
    LabelIndex<Misspelling> misspellingLabelIndex = systemView.getLabelIndex(Misspelling.class);
    Labeler<SpellCorrection> spellCorrectionLabeler = systemView.getLabeler(SpellCorrection.class);

    for (Label<Sentence> sentence : sentence2LabelIndex) {
      String first = "<NONE>";
      String prev = "<NONE>";
      for (Label<ParseToken> tokenLabel : parseTokenLabelIndex.insideSpan(sentence)) {
        ParseToken token = tokenLabel.value();
        String text = token.text();
        if (Patterns.ALPHABETIC_WORD.matcher(text).matches()) {
          return;
        }
        if (misspellingLabelIndex.withTextLocation(tokenLabel).isPresent()) {
          String suggested = spellingModel.suggestCorrection(text, Ngram.create(first, prev));
          if (suggested != null) {
            LOGGER.trace("Correcting word: {} with {}", text, suggested);
            SpellCorrection spellCorrection = ImmutableSpellCorrection.builder()
                .text(suggested)
                .hasSpaceAfter(token.hasSpaceAfter())
                .build();
            spellCorrectionLabeler.value(spellCorrection)
                .label(tokenLabel);
          }
        }
        first = prev;
        prev = text;
      }
    }
  }
}
