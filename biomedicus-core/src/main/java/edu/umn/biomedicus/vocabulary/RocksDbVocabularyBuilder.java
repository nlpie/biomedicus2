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

import edu.umn.biomedicus.common.utilities.Patterns;
import edu.umn.biomedicus.exc.BiomedicusException;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class RocksDbVocabularyBuilder extends VocabularyBuilder {
  private static final Pattern MORE_THAN_TWO_NUMBERS_IN_A_ROW = Pattern.compile("[\\p{Nd}]{3,}");

  private RocksDbTermIndexBuilder words;

  private RocksDbTermIndexBuilder terms;

  private RocksDbTermIndexBuilder norms;

  @Override
  void setOutputPath(Path outputPath) {
    try {
      Files.createDirectories(outputPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    try (Options options = new Options().setCreateIfMissing(true).prepareForBulkLoad()){
      try {
        words = new RocksDbTermIndexBuilder(
            RocksDB.open(options, outputPath.resolve("wordsTerms").toString()),
            RocksDB.open(options, outputPath.resolve("wordsIndices").toString()));
        terms = new RocksDbTermIndexBuilder(
            RocksDB.open(options, outputPath.resolve("termsTerms").toString()),
            RocksDB.open(options, outputPath.resolve("termsIndices").toString()));
        norms = new RocksDbTermIndexBuilder(
            RocksDB.open(options, outputPath.resolve("normsTerms").toString()),
            RocksDB.open(options, outputPath.resolve("normsIndices").toString()));
      } catch (RocksDBException e) {
        throw new RuntimeException(e);
      }
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
    BiomedicusException exception = null;
    for (RocksDbTermIndexBuilder builder : Arrays.asList(words, terms, norms)) {
      try {
        builder.close();
      } catch (IOException e) {
        if (exception == null) {
          exception = new BiomedicusException("Unable to close one or more builders.");
        }
        exception.addSuppressed(e);
      }
    }

    if (exception != null) {
      throw exception;
    }
  }

  private static class RocksDbTermIndexBuilder implements TermIndexBuilder, Closeable {
    private final TreeSet<String> termSet = new TreeSet<>();
    private final RocksDB terms;
    private final RocksDB indices;

    public RocksDbTermIndexBuilder(RocksDB terms, RocksDB indices) {
      this.terms = terms;
      this.indices = indices;
    }

    @Override
    public void addTerm(String term) throws BiomedicusException {
      if (MORE_THAN_TWO_NUMBERS_IN_A_ROW.matcher(term).find()) {
        return;
      }

      if (!termSet.contains(term) && Patterns.A_LETTER_OR_NUMBER.matcher(term).find()) {
        termSet.add(term);
      }
    }

    @Override
    public void doWrite() {
      int i = 0;
      for (String s : termSet) {
        byte[] termBytes = s.getBytes(StandardCharsets.UTF_8);
        byte[] indexBytes = ByteBuffer.allocate(4).putInt(i).array();
        try {
          terms.put(indexBytes, termBytes);
          indices.put(termBytes, indexBytes);
        } catch (RocksDBException e) {
          throw new RuntimeException(e);
        }
        i = Math.incrementExact(i);
      }
    }

    @Override
    public void close() throws IOException {
      terms.close();
      indices.close();
    }
  }
}
