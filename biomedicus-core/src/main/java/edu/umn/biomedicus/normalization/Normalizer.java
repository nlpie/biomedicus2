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

package edu.umn.biomedicus.normalization;

import com.google.inject.Inject;
import edu.umn.biomedicus.common.TextIdentifiers;
import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary;
import edu.umn.biomedicus.common.dictionary.StringIdentifier;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.LabeledText;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.biomedicus.tokenization.WordIndex;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
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

  private final BidirectionalDictionary normsIndex;

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
    LabeledText labeledText = TextIdentifiers.getSystemLabeledText(document);

    LabelIndex<WordIndex> wordIndexLabelIndex = labeledText.labelIndex(WordIndex.class);
    LabelIndex<PosTag> posTagIndex = labeledText.labelIndex(PosTag.class);
    Labeler<NormForm> normFormLabeler = labeledText.labeler(NormForm.class);

    LabelIndex<ParseToken> parseTokenLabelIndex = labeledText.labelIndex(ParseToken.class);

    for (WordIndex wordIndex : wordIndexLabelIndex) {
      PartOfSpeech partOfSpeech = posTagIndex
          .firstAtLocation(wordIndex)
          .getPartOfSpeech();
      StringIdentifier wordTerm = wordIndex.getStringIdentifier();
      TermString normAndTerm = null;
      if (!wordTerm.isUnknown()) {
        normAndTerm = normalizerStore.get(new TermPos(wordTerm, partOfSpeech));
      }
      String norm;
      StringIdentifier normTerm;
      if (normAndTerm == null) {
        norm = parseTokenLabelIndex.firstAtLocation(wordIndex)
            .getText()
            .toLowerCase();
        normTerm = normsIndex.getTermIdentifier(norm);
      } else {
        norm = normAndTerm.getString();
        normTerm = normAndTerm.getTerm();
      }

      normFormLabeler.add(new NormForm(wordIndex, norm, normTerm));
    }
  }
}
