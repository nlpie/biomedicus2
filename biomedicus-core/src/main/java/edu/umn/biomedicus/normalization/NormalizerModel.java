/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.normalization;

import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.model.semantics.PartOfSpeech;
import edu.umn.biomedicus.model.text.Token;
import edu.umn.biomedicus.model.tuples.WordPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
@Singleton
class NormalizerModel {
    private static final Logger LOGGER = LogManager.getLogger(NormalizerModel.class);

    /**
     * A Table from the part of speech and entry to a base form.
     */
    private final Map<WordPos, String> lexicon;

    /**
     * Same as lexicon, but only used if pos / entry is not found in lexicon.
     */
    private final Map<WordPos, String> fallbackLexicon;

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

    @Inject
    NormalizerModel(BiomedicusConfiguration biomedicusConfiguration) {
        Path lexiconFile = biomedicusConfiguration.resolveDataFile("normalization.lexicon.path");
        Path fallbackLexiconFile = biomedicusConfiguration.resolveDataFile("normalization.fallback.path");

        Yaml yaml = new Yaml();
        try {
            LOGGER.info("Loading normalization lexicon file: {}", lexiconFile);
            @SuppressWarnings("unchecked")
            Map<WordPos, String> lexicon = (Map<WordPos, String>) yaml.load(Files.newInputStream(lexiconFile));
            this.lexicon = lexicon;

            LOGGER.info("Loading normalization fallback lexicon file: {}", fallbackLexiconFile);
            @SuppressWarnings("unchecked")
            Map<WordPos, String> fallbackLexicon = (Map<WordPos, String>) yaml.load(Files.newInputStream(fallbackLexiconFile));
            this.fallbackLexicon = fallbackLexicon;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load Normalizer model", e);
        }
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
        token.setNormalForm(normalForm);
    }
}
