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

package edu.umn.biomedicus.vocabulary;

import com.google.inject.Inject;
import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary;
import edu.umn.biomedicus.common.dictionary.StringIdentifier;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.biomedicus.tokenization.WordIndex;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.DocumentProcessor;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
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
  public void process(@NotNull Document document) {
    LOGGER.debug("Labeling word term index identifiers in a document.");

    LabelIndex<ParseToken> parseTokenLabelIndex = document.labelIndex(ParseToken.class);
    Labeler<WordIndex> wordIndexLabeler = document.labeler(WordIndex.class);

    for (ParseToken parseToken : parseTokenLabelIndex) {
      String lowercase = parseToken.getText().toLowerCase(Locale.ENGLISH);
      StringIdentifier termIdentifier = wordIndex.getTermIdentifier(lowercase);
      wordIndexLabeler.add(new WordIndex(parseToken, termIdentifier));
    }
  }
}
