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

import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.framework.DataLoader;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.Token;
import edu.umn.biomedicus.common.tuples.WordPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
@ProvidedBy(NormalizerModel.Loader.class)
class NormalizerModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(NormalizerModel.class);
    private Map<WordPos, String> lexicon;
    private Map<WordPos, String> fallbackLexicon;

    /**
     * Default constructor. Initializes two tables.
     *
     * @param lexicon         the primary lexicon to use.
     * @param fallbackLexicon a lexicon to use if we fail to find the token in the first lexicon, this for cases like
     *                        mis-tagged tokens
     */
    private NormalizerModel(Map<WordPos, String> lexicon, Map<WordPos, String> fallbackLexicon) {
        this.lexicon = lexicon;
        this.fallbackLexicon = fallbackLexicon;
    }

    /**
     * Normalizes the token.
     *
     * @param token token to normalize
     */
    public String normalize(Token token, @Nullable PartOfSpeech partOfSpeech) {
        String text = token.text();
        LOGGER.trace("Normalizing a token: {}", text);
        String key = text.trim().toLowerCase();
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
        return normalForm.toLowerCase();
    }

    /**
     *
     */
    @Singleton
    public static class Loader extends DataLoader<NormalizerModel> {
        private final Logger LOGGER = LoggerFactory.getLogger(Loader.class);
        private final Path lexiconFile;
        private final Path fallbackLexiconFile;

        @Inject
        public Loader(@Setting("normalization.lexicon.path") Path lexiconFile,
                      @Setting("normalization.fallback.path") Path fallbackLexiconFile) {
            this.lexiconFile = lexiconFile;
            this.fallbackLexiconFile = fallbackLexiconFile;
        }

        @Override
        protected NormalizerModel loadModel() {
            Yaml yaml = new Yaml();
            try {
                LOGGER.info("Loading normalization lexicon file: {}", lexiconFile);
                @SuppressWarnings("unchecked")
                Map<WordPos, String> lexicon = (Map<WordPos, String>) yaml.load(Files.newInputStream(lexiconFile));

                LOGGER.info("Loading normalization fallback lexicon file: {}", fallbackLexiconFile);
                @SuppressWarnings("unchecked")
                Map<WordPos, String> fallbackLexicon = (Map<WordPos, String>) yaml.load(Files.newInputStream(fallbackLexiconFile));

                return new NormalizerModel(lexicon, fallbackLexicon);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load Normalizer model", e);
            }
        }
    }
}
