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

import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.exc.BiomedicusException;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class RocksDbVocabularyBuilder extends VocabularyBuilder {

  private final Options options;

  private final RocksDbTermIndexBuilder words;
  private final RocksDbTermIndexBuilder terms;
  private final RocksDbTermIndexBuilder norms;

  @Inject
  public RocksDbVocabularyBuilder(@Setting("vocabulary.db.path") Path dbPath) {
    try {
      Files.createDirectories(dbPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    options = new Options().setCreateIfMissing(true).prepareForBulkLoad();
    try {
      words = new RocksDbTermIndexBuilder(
          RocksDB.open(options, dbPath.resolve("words-terms.db").toString()),
          RocksDB.open(options, dbPath.resolve("words-indices.db").toString()));
      terms = new RocksDbTermIndexBuilder(
          RocksDB.open(options, dbPath.resolve("terms-terms.db").toString()),
          RocksDB.open(options, dbPath.resolve("terms-indices.db").toString()));
      norms = new RocksDbTermIndexBuilder(
          RocksDB.open(options, dbPath.resolve("norms-terms.db").toString()),
          RocksDB.open(options, dbPath.resolve("norms-indices.db").toString()));
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  TermIndexBuilder createWordsIndexBuilder() {
    return words;
  }

  @Override
  TermIndexBuilder createTermsIndexBuilder() {
    return terms;
  }

  @Override
  TermIndexBuilder createNormsIndexBuilder() {
    return norms;
  }

  @Override
  public void doShutdown() throws BiomedicusException {
    try {
      words.close();
    } catch (IOException e) {
      throw new BiomedicusException();
    } finally {
      try {
        terms.close();
      } catch (IOException e) {
        throw new BiomedicusException(e);
      } finally {
        try {
          norms.close();
        } catch (IOException e) {
          throw new BiomedicusException(e);
        } finally {
          options.close();
        }
      }
    }
  }

  private static class RocksDbTermIndexBuilder implements TermIndexBuilder, Closeable {

    private final RocksDB terms;
    private final RocksDB indices;

    private int size = 0;

    public RocksDbTermIndexBuilder(RocksDB terms, RocksDB indices) {
      this.terms = terms;
      this.indices = indices;
    }

    @Override
    public void addTerm(String term) throws BiomedicusException {
      int id = size++;
      byte[] idAsBytes = ByteBuffer.allocate(4).putInt(id).array();
      byte[] termAsBytes = term.getBytes(StandardCharsets.UTF_8);
      try {
        terms.put(idAsBytes, termAsBytes);
        indices.put(termAsBytes, idAsBytes);
      } catch (RocksDBException e) {
        throw new BiomedicusException(e);
      }
    }

    @Override
    public void close() throws IOException {
      terms.close();
      indices.close();
    }
  }
}
