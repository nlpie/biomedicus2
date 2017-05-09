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
import edu.umn.biomedicus.common.utilities.Patterns;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.common.grams.Ngram;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.common.types.semantics.ImmutableSpellCorrection;
import edu.umn.biomedicus.common.types.semantics.Misspelling;
import edu.umn.biomedicus.common.types.semantics.SpellCorrection;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class MisspellingCorrector implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MisspellingCorrector.class);
    private final SpellingModel spellingModel;
    private final LabelIndex<Sentence> sentence2LabelIndex;
    private final LabelIndex<ParseToken> parseTokenLabelIndex;
    private final LabelIndex<Misspelling> misspellingLabelIndex;
    private final Labeler<SpellCorrection> spellCorrectionLabeler;

    @Inject
    public MisspellingCorrector(SpellingModel spellingModel,
                                TextView document) {
        this.spellingModel = spellingModel;
        this.sentence2LabelIndex = document.getLabelIndex(Sentence.class);
        this.parseTokenLabelIndex = document.getLabelIndex(ParseToken.class);
        this.misspellingLabelIndex = document.getLabelIndex(Misspelling.class);
        this.spellCorrectionLabeler = document
                .getLabeler(SpellCorrection.class);
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Correcting any misspelled words in a document.");
        for (Label<Sentence> sentence : sentence2LabelIndex) {
            String first = "<NONE>";
            String prev = "<NONE>";
            for (Label<ParseToken> tokenLabel : parseTokenLabelIndex
                    .insideSpan(sentence)) {
                ParseToken token = tokenLabel.value();
                String text = token.text();
                if (Patterns.ALPHABETIC_WORD.matcher(text)
                        .matches()) {
                    return;
                }
                if (misspellingLabelIndex.withTextLocation(tokenLabel)
                        .isPresent()) {
                    String suggested = spellingModel
                            .suggestCorrection(text, Ngram.create(first, prev));
                    if (suggested != null) {
                        LOGGER.debug("Correcting word: {} with {}", text,
                                suggested);
                        SpellCorrection spellCorrection
                                = ImmutableSpellCorrection.builder()
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
