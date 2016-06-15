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
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.common.tuples.WordPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 *
 */
@Singleton
public class NormalizerModelLoader extends DataLoader<NormalizerModel> {
    private final Logger LOGGER = LoggerFactory.getLogger(NormalizerModelLoader.class);

    private final Path lexiconFile;

    private final Path fallbackLexiconFile;

    @Inject
    public NormalizerModelLoader(@Setting("normalization.lexicon.path") Path lexiconFile,
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
