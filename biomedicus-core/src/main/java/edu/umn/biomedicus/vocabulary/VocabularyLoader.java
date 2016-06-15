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
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
@Singleton
public class VocabularyLoader extends DataLoader<Vocabulary> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyLoader.class);

    private final Path wordsPath;

    @Inject
    public VocabularyLoader(@Setting("vocabulary.wordIndex.path") Path wordsPath) {
        this.wordsPath = wordsPath;
    }

    @Override
    protected Vocabulary loadModel() throws BiomedicusException {
        try {
            LOGGER.info("Loading words into term index from path: {}", wordsPath);

            TermIndex wordIndex = new TermIndex();
            Files.lines(wordsPath).forEach(wordIndex::addTerm);

            return new Vocabulary(wordIndex);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
