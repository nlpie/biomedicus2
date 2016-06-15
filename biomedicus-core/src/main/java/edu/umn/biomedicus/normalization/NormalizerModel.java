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

package edu.umn.biomedicus.normalization;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.common.tuples.WordPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Token normalizer which uses a table of parts of speech and terms to provide a base term.
 * <p/>
 * <p>It provides functionality for a fallback table in potential cases where the part of speech has been incorrectly
 * labeled by the part of speech tagger.</p>
 *
 * @author Serguei Pakhomov
 * @author Ben Knoll
 * @since 0.3.0
 */
@ProvidedBy(NormalizerModelLoader.class)
class NormalizerModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(NormalizerModel.class);

    /**
     * A Table from the part of speech and entry to a base form.
     */
    private Map<WordPos, String> lexicon;

    /**
     * Same as lexicon, but only used if pos / entry is not found in lexicon.
     */
    private Map<WordPos, String> fallbackLexicon;

    /**
     * Default constructor. Initializes two tables.
     *
     * @param lexicon         the primary lexicon to use.
     * @param fallbackLexicon a lexicon to use if we fail to find the token in the first lexicon, this for cases like
     *                        mis-tagged tokens
     */
    NormalizerModel(Map<WordPos, String> lexicon, Map<WordPos, String> fallbackLexicon) {
        this.lexicon = lexicon;
        this.fallbackLexicon = fallbackLexicon;
    }

    /**
     * Normalizes the token.
     *
     * @param token token to normalize
     */
    public void normalize(Token token) {
        LOGGER.trace("Normalizing a token: {}", token.getText());
        String key = token.getText().trim().toLowerCase();
        PartOfSpeech partOfSpeech = token.getPartOfSpeech();
        String normalForm = null;
        if (partOfSpeech != null) {
            WordPos wordPos = new WordPos(key, partOfSpeech);
            normalForm = lexicon.get(wordPos);
            if (normalForm == null) {
                normalForm = fallbackLexicon.get(wordPos);
            }
        }
        if (normalForm == null) {
            normalForm = key;
        }
        token.setNormalForm(normalForm.toLowerCase());
    }
}
