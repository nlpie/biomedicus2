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

package edu.umn.biomedicus.normalization;

import com.google.inject.Inject;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.ImmutableNormForm;
import edu.umn.biomedicus.common.types.text.NormForm;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.WordIndex;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs word normalization on the parse tokens in a document.
 *
 * @since 1.7.0
 */
final public class Normalizer implements DocumentProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(Normalizer.class);

  private final NormalizerModel normalizerStore;

  private final TermIndex normsIndex;

  /**
   * Creates a new normalizer for normalizing a document.
   *
   * @param normalizerStore the normalizer store to use.
   */
  @Inject
  public Normalizer(NormalizerModel normalizerStore, Vocabulary vocabulary) {
    this.normalizerStore = normalizerStore;
    normsIndex = vocabulary.getNormsIndex();
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    LOGGER.debug("Normalizing tokens in a document.");
    TextView textView = StandardViews.getSystemView(document);

    LabelIndex<WordIndex> wordIndexLabelIndex = textView.getLabelIndex(WordIndex.class);
    LabelIndex<PartOfSpeech> partOfSpeechLabelIndex = textView.getLabelIndex(PartOfSpeech.class);
    Labeler<NormForm> normFormLabeler = textView.getLabeler(NormForm.class);

    LabelIndex<ParseToken> parseTokenLabelIndex = textView.getLabelIndex(ParseToken.class);

    for (Label<WordIndex> wordIndexLabel : wordIndexLabelIndex) {
      PartOfSpeech partOfSpeech = partOfSpeechLabelIndex
          .withTextLocation(wordIndexLabel)
          .orElseThrow(BiomedicusException.supplier(
              "Part of speech label not found for word index"))
          .value();

      IndexedTerm wordTerm = wordIndexLabel.getValue().term();
      TermString normAndTerm = null;
      if (!wordTerm.isUnknown()) {
        normAndTerm = normalizerStore.get(new TermPos(wordTerm, partOfSpeech));
      }
      String norm;
      IndexedTerm normTerm;
      if (normAndTerm == null) {
        norm = parseTokenLabelIndex.withTextLocation(wordIndexLabel)
            .orElseThrow(BiomedicusException.supplier("parse token not found for word index"))
            .value()
            .text()
            .toLowerCase();
        normTerm = normsIndex.getIndexedTerm(norm);
      } else {
        norm = normAndTerm.getString();
        normTerm = normAndTerm.getTerm();
      }

      normFormLabeler.value(
          ImmutableNormForm.builder()
              .normalForm(norm)
              .normTermIdentifier(normTerm.termIdentifier())
              .build()
      ).label(wordIndexLabel);
    }
  }
}
