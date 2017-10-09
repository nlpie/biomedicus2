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

package edu.umn.biomedicus.vocabulary;

import com.google.inject.Inject;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary;
import edu.umn.biomedicus.common.dictionary.StringIdentifier;
import edu.umn.biomedicus.common.types.text.ImmutableWordIndex;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.WordIndex;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.TextView;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes the text of words from parse tokens and labels their index value.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
public final class WordLabeler implements DocumentProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(WordLabeler.class);

  private final BidirectionalDictionary wordIndex;

  @Inject
  public WordLabeler(Vocabulary vocabulary) {
    wordIndex = vocabulary.getWordsIndex();
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    LOGGER.debug("Labeling word term index identifiers in a document.");

    TextView systemView = StandardViews.getSystemView(document);
    LabelIndex<ParseToken> parseTokenLabelIndex = systemView.getLabelIndex(ParseToken.class);
    Labeler<WordIndex> wordIndexLabeler = systemView.getLabeler(WordIndex.class);

    for (Label<ParseToken> parseTokenLabel : parseTokenLabelIndex) {
      ParseToken token = parseTokenLabel.value();
      StringIdentifier termIdentifier = wordIndex.getTermIdentifier(token.text().toLowerCase(Locale.ENGLISH));
      WordIndex wordIndex = ImmutableWordIndex.builder()
          .term(termIdentifier)
          .build();
      wordIndexLabeler.value(wordIndex).label(parseTokenLabel);
    }
  }
}
