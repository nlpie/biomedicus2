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

package edu.umn.biomedicus.spelling;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.Biomedicus;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.application.TextView;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.labels.ValueLabeler;
import edu.umn.biomedicus.common.types.semantics.Misspelling;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class MisspellingDetector implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingDetector.class);
    private final TermIndex wordIndex;
    private final LabelIndex<ParseToken> parseTokenLabelIndex;
    private final ValueLabeler mispellingLabeler;

    @Inject
    public MisspellingDetector(Vocabulary vocabulary,
                               TextView document) {
        wordIndex = vocabulary.getWordsIndex();
        this.parseTokenLabelIndex = document.getLabelIndex(ParseToken.class);
        mispellingLabeler = document.getLabeler(Misspelling.class).value(new Misspelling());
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Finding any misspelled words in a document.");
        for (Label<ParseToken> parseTokenLabel : parseTokenLabelIndex) {
            ParseToken parseToken = parseTokenLabel.value();
            String text = parseToken.text();
            if (Biomedicus.Patterns.ALPHABETIC_WORD.matcher(text).matches() && !wordIndex.contains(text)) {
                mispellingLabeler.label(parseTokenLabel);
            }
        }
    }
}
