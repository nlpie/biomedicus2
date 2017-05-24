/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.types.text.ImmutableWordIndex;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.WordIndex;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 *
 */
public final class WordLabeler implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(WordLabeler.class);
    private final TermIndex wordIndex;
    private final LabelIndex<ParseToken> parseTokenLabelIndex;
    private final Labeler<WordIndex> wordIndexLabeler;

    @Inject
    public WordLabeler(Vocabulary vocabulary, TextView document) {
        wordIndex = vocabulary.getWordsIndex();
        parseTokenLabelIndex = document.getLabelIndex(ParseToken.class);
        wordIndexLabeler = document.getLabeler(WordIndex.class);
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.debug("Labeling word term index identifiers in a document.");
        for (Label<ParseToken> parseTokenLabel : parseTokenLabelIndex) {
            ParseToken token = parseTokenLabel.value();
            IndexedTerm indexedTerm = wordIndex
                    .getIndexedTerm(token.text().toLowerCase(Locale.ENGLISH));
            WordIndex wordIndex = ImmutableWordIndex.builder()
                    .term(indexedTerm)
                    .build();
            wordIndexLabeler.value(wordIndex).label(parseTokenLabel);
        }
    }
}
