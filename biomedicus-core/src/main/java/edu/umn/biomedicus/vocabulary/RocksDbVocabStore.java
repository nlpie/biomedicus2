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

import com.google.common.base.Preconditions;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.terms.TermIndex;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vocabulary store using RocksDB as the backend.
 */
public class RocksDbVocabStore extends VocabularyStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(RocksDbVocabStore.class);

  private final Path dbPath;

  private final Boolean inMemory;

  @Nullable
  private TermIndex words;

  @Nullable
  private TermIndex terms;

  @Nullable
  private TermIndex norms;

  @Inject
  public RocksDbVocabStore(@Setting("vocabulary.db.path") Path dbPath,
      @Setting("vocabulary.inMemory") Boolean inMemory) {
    this.dbPath = dbPath;
    this.inMemory = inMemory;
  }

  @Override
  void open() {
    LOGGER.info("Loading vocabularies: {}", dbPath);
    LOGGER.info("Opening words index. inMemory = {}.", inMemory);
    words = new RocksDbTermIndex(dbPath.resolve("wordsTerms"),
        dbPath.resolve("wordsIndices")).inMemory(inMemory);
    LOGGER.info("Opening terms index. inMemory = {}.", inMemory);
    terms = new RocksDbTermIndex(dbPath.resolve("termsTerms"),
        dbPath.resolve("termsIndices")).inMemory(inMemory);
    LOGGER.info("Opening norms index. inMemory = {}.", inMemory);
    norms = new RocksDbTermIndex(dbPath.resolve("normsTerms"),
        dbPath.resolve("normsIndices")).inMemory(inMemory);
    LOGGER.info("Done loading vocabularies.");
  }

  @Override
  TermIndex getWords() {
    Preconditions.checkNotNull(words);
    return words;
  }

  @Override
  TermIndex getTerms() {
    Preconditions.checkNotNull(terms);
    return terms;
  }

  @Override
  TermIndex getNorms() {
    Preconditions.checkNotNull(norms);
    return norms;
  }

  @Override
  public void close() throws IOException {
    if (words instanceof Closeable) {
      ((Closeable) words).close();
    }
    if (terms instanceof Closeable) {
      ((Closeable) terms).close();
    }
    if (norms instanceof Closeable) {
      ((Closeable) norms).close();
    }
  }
}
