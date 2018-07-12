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

import com.google.common.base.Preconditions;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary;
import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary.Identifiers;
import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary.Strings;
import edu.umn.biomedicus.common.dictionary.RocksDbIdentifiers;
import edu.umn.biomedicus.common.dictionary.RocksDbStrings;
import edu.umn.biomedicus.common.dictionary.StandardBidirectionalDictionary;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vocabulary store using RocksDB as the backend.
 */
@Singleton
public class RocksDbVocabStore extends VocabularyStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(RocksDbVocabStore.class);

  private final Path dbPath;

  private final Boolean inMemory;

  @Nullable
  private BidirectionalDictionary words;

  @Nullable
  private BidirectionalDictionary terms;

  @Nullable
  private BidirectionalDictionary norms;

  @Inject
  public RocksDbVocabStore(
      @Setting("vocabulary.db.path") Path dbPath,
      @Setting("vocabulary.inMemory") Boolean inMemory
  ) {
    this.dbPath = dbPath;
    this.inMemory = inMemory;
  }

  @Override
  void open() {
    LOGGER.info("Loading vocabularies: {}", dbPath);

    LOGGER.info("Opening words index. inMemory = {}.", inMemory);
    Strings wordsTerms = new RocksDbStrings(dbPath.resolve("wordsTerms"));
    Identifiers wordsIndices = new RocksDbIdentifiers(dbPath.resolve("wordsIndices"));
    words = new StandardBidirectionalDictionary(wordsIndices, wordsTerms).inMemory(inMemory);

    LOGGER.info("Opening terms index. inMemory = {}.", inMemory);
    Strings termsTerms = new RocksDbStrings(dbPath.resolve("termsTerms"));
    Identifiers termsIndices = new RocksDbIdentifiers(dbPath.resolve("termsIndices"));
    terms = new StandardBidirectionalDictionary(termsIndices, termsTerms).inMemory(inMemory);

    LOGGER.info("Opening norms index. inMemory = {}.", inMemory);
    Strings normsTerms = new RocksDbStrings(dbPath.resolve("normsTerms"));
    Identifiers normsIndices = new RocksDbIdentifiers(dbPath.resolve("normsIndices"));
    norms = new StandardBidirectionalDictionary(normsIndices, normsTerms).inMemory(inMemory);
  }

  @Override
  BidirectionalDictionary getWords() {
    Preconditions.checkNotNull(words);
    return words;
  }

  @Override
  BidirectionalDictionary getTerms() {
    Preconditions.checkNotNull(terms);
    return terms;
  }

  @Override
  BidirectionalDictionary getNorms() {
    Preconditions.checkNotNull(norms);
    return norms;
  }

  @Override
  public void close() throws IOException {
    if (words != null) {
      words.close();
      words = null;
    }
    if (terms != null) {
      terms.close();
      terms = null;
    }
    if (norms != null) {
      norms.close();
      norms = null;
    }
  }
}
