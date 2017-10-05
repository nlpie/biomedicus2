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

package edu.umn.biomedicus.tnt;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.LifecycleManaged;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
class RocksDbDataStoreFactory implements DataStoreFactory, LifecycleManaged {

  private static final Logger LOGGER = LoggerFactory.getLogger(RocksDbDataStoreFactory.class);

  private final Collection<RocksDB> rocksDBS = new ArrayList<>();

  private final boolean inMemory;

  private Path dbPath;

  @Inject
  public RocksDbDataStoreFactory(@Setting("tnt.word.dbPath") Path dbPath,
      @Setting("tnt.word.inMemory") boolean inMemory) {
    this.dbPath = dbPath;
    this.inMemory = inMemory;
  }

  @Override
  public void setDbPath(Path dbPath) {
    this.dbPath = dbPath;
  }



  @Override
  public SuffixDataStore openSuffixDataStore(int id) {
    RocksDB.loadLibrary();
    try {
      LOGGER.info("Opening TnT suffix model: {}", id);
      RocksDB rocksDB = RocksDB.openReadOnly(dbPath.resolve(getSuffixesName(id)).toString());
      RocksDbSuffixDataStore rocksDbSuffixDataStore = new RocksDbSuffixDataStore(rocksDB);
      if (inMemory) {
        LOGGER.info("Loading TnT suffix model into memory: {}", id);
        InMemorySuffixDataStore inMemorySuffixDataStore = rocksDbSuffixDataStore.inMemory();
        LOGGER.info("Done loading TnT suffix model into memory: {}", id);
        rocksDB.close();
        return inMemorySuffixDataStore;
      }
      rocksDBS.add(rocksDB);
      return rocksDbSuffixDataStore;
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  private String getSuffixesName(int id) {
    return "" + id;
  }

  @Override
  public SuffixDataStore createSuffixDataStore(int id) {
    RocksDB.loadLibrary();
    try (Options options = new Options().setCreateIfMissing(true).prepareForBulkLoad()) {
      Files.createDirectories(dbPath);
      RocksDB rocksDB = RocksDB.open(options, dbPath.resolve(getSuffixesName(id)).toString());
      rocksDBS.add(rocksDB);

      return new RocksDbSuffixDataStore(rocksDB);
    } catch (RocksDBException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getWordsName(int id) {
    return id + "-words";
  }

  private String getCandidatesName(int id) {
    return id + "-candidates";
  }

  @Override
  public KnownWordsDataStore openKnownWordDataStore(int id) {
    RocksDB.loadLibrary();
    try {
      LOGGER.info("Opening TnT model known word model: {}", id);
      RocksDB rocksDB = RocksDB.openReadOnly(dbPath.resolve(getWordsName(id)).toString());
      RocksDB candidatesDB = RocksDB.openReadOnly(dbPath.resolve(getCandidatesName(id)).toString());

      RocksDbKnownWordsDataStore rocksDbKnownWordsDataStore = new RocksDbKnownWordsDataStore(
          rocksDB, candidatesDB);
      if (inMemory) {
        LOGGER.info("Loading TnT known word model into memory: {}", id);
        InMemoryKnownWordDataStore inMemoryKnownWordDataStore = rocksDbKnownWordsDataStore
            .inMemory();
        LOGGER.info("Done loading TnT known word model into memory: {}", id);
        rocksDB.close();
        candidatesDB.close();
        return inMemoryKnownWordDataStore;
      }
      rocksDBS.add(rocksDB);
      rocksDBS.add(candidatesDB);
      return rocksDbKnownWordsDataStore;
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public KnownWordsDataStore createKnownWordsDataStore(int id) {
    RocksDB.loadLibrary();
    try (Options options = new Options().setCreateIfMissing(true).prepareForBulkLoad()) {
      Files.createDirectories(dbPath);
      RocksDB rocksDB = RocksDB.open(options, dbPath.resolve(getWordsName(id)).toString());
      rocksDBS.add(rocksDB);
      RocksDB candidatesDB = RocksDB.open(options, dbPath.resolve(getCandidatesName(id)).toString());
      rocksDBS.add(candidatesDB);

      return new RocksDbKnownWordsDataStore(rocksDB, candidatesDB);
    } catch (RocksDBException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doShutdown() throws BiomedicusException {
    for (RocksDB rocksDB : rocksDBS) {
      rocksDB.close();
    }
  }

  private static class InMemorySuffixDataStore implements SuffixDataStore {

    private final Map<Pair<PartOfSpeech, String>, Double> probabilities = new HashMap<>();

    @Nullable
    @Override
    public Double getProbability(String suffix, PartOfSpeech candidate) {
      return probabilities.get(Pair.of(candidate, suffix));
    }

    @Override
    public void addAllProbabilities(TreeMap<Pair<PartOfSpeech, String>, Double> probabilities) {
      this.probabilities.putAll(probabilities);
    }

    void addProbability(String string, PartOfSpeech partOfSpeech, Double probability) {
      probabilities.put(Pair.of(partOfSpeech, string), probability);
    }

    @Override
    public void write() {

    }
  }

  private static class InMemoryKnownWordDataStore implements KnownWordsDataStore {

    private Map<Pair<PartOfSpeech, String>, Double> probabilities = new HashMap<>();

    private Map<String, List<PartOfSpeech>> candidates = new HashMap<>();

    @Nullable
    @Override
    public Double getProbability(String word, PartOfSpeech candidate) {
      return probabilities.get(Pair.of(candidate, word));
    }

    @Override
    public List<PartOfSpeech> getCandidates(String word) {
      return candidates.get(word);
    }

    @Override
    public boolean isKnown(String word) {
      return candidates.get(word) != null;
    }

    @Override
    public void addAllProbabilities(Map<Pair<PartOfSpeech, String>, Double> lexicalProbabilities) {
      probabilities.putAll(lexicalProbabilities);
      for (Entry<Pair<PartOfSpeech, String>, Double> entry : lexicalProbabilities.entrySet()) {
        candidates.compute(entry.getKey().getSecond(), mmCompute(entry.getKey().getFirst()));
      }
    }

    void addProbability(String string, PartOfSpeech partOfSpeech, Double probability) {
      probabilities.put(Pair.of(partOfSpeech, string), probability);
      candidates.compute(string, mmCompute(partOfSpeech));
    }

    static BiFunction<String, List<PartOfSpeech>, List<PartOfSpeech>> mmCompute(PartOfSpeech pos) {
      return (s, partOfSpeeches) -> {
        if (partOfSpeeches == null) {
          partOfSpeeches = new ArrayList<>();
        } else {
          if (partOfSpeeches.contains(pos)) {
            return partOfSpeeches;
          }
        }
        partOfSpeeches.add(pos);
        return partOfSpeeches;
      };

    }

    @Override
    public void write() {

    }
  }

  private static class RocksDbSuffixDataStore implements SuffixDataStore {

    private final RocksDB probabilitiesDB;

    public RocksDbSuffixDataStore(RocksDB probabilitiesDB) {
      this.probabilitiesDB = probabilitiesDB;
    }

    @Nullable
    @Override
    public Double getProbability(String suffix, PartOfSpeech candidate) {
      try {
        byte[] bytes = probabilitiesDB.get(getPosWordBytes(candidate, suffix));
        return bytes == null ? null : ByteBuffer.wrap(bytes).getDouble();
      } catch (RocksDBException e) {
        throw new RuntimeException(e);
      }
    }


    @Override
    public void addAllProbabilities(TreeMap<Pair<PartOfSpeech, String>, Double> probabilities) {
      for (Entry<Pair<PartOfSpeech, String>, Double> entry : probabilities.entrySet()) {
        PartOfSpeech partOfSpeech = entry.getKey().getFirst();
        String word = entry.getKey().getSecond();

        byte[] posWordBytes = getPosWordBytes(partOfSpeech, word);

        try {
          probabilitiesDB.put(posWordBytes,
              ByteBuffer.allocate(8).putDouble(entry.getValue()).array());
        } catch (RocksDBException e) {
          throw new RuntimeException(e);
        }
      }
    }

    InMemorySuffixDataStore inMemory() {
      InMemorySuffixDataStore inMemory = new InMemorySuffixDataStore();
      try (RocksIterator rocksIterator = probabilitiesDB.newIterator()) {
        rocksIterator.seekToFirst();
        while (rocksIterator.isValid()) {
          Pair<PartOfSpeech, String> posWord = getPosWordFromBytes(rocksIterator.key());
          double prob = ByteBuffer.wrap(rocksIterator.value()).getDouble();
          inMemory.addProbability(posWord.getSecond(), posWord.getFirst(), prob);
        }
      }
      return inMemory;
    }

    @Override
    public void write() {

    }
  }

  private static class RocksDbKnownWordsDataStore implements KnownWordsDataStore {

    private final RocksDB probabilitiesDB;

    private final RocksDB candidatesDB;


    public RocksDbKnownWordsDataStore(RocksDB probabilitiesDB, RocksDB candidatesDB) {
      this.probabilitiesDB = probabilitiesDB;
      this.candidatesDB = candidatesDB;
    }

    @Nullable
    @Override
    public Double getProbability(String word, PartOfSpeech candidate) {
      byte[] posWordBytes = getPosWordBytes(candidate, word);
      try {
        byte[] bytes = probabilitiesDB.get(posWordBytes);
        return bytes == null ? null : ByteBuffer.wrap(bytes).getDouble();
      } catch (RocksDBException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public List<PartOfSpeech> getCandidates(String word) {
      try {
        return getPartsOfSpeechFromBytes(
            candidatesDB.get(word.getBytes(StandardCharsets.UTF_8)));
      } catch (RocksDBException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isKnown(String word) {
      try {
        return candidatesDB.get(word.getBytes(StandardCharsets.UTF_8)) != null;
      } catch (RocksDBException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void addAllProbabilities(Map<Pair<PartOfSpeech, String>, Double> lexicalProbabilities) {
      Map<String, List<PartOfSpeech>> candidates = new TreeMap<>();

      for (Entry<Pair<PartOfSpeech, String>, Double> entry : lexicalProbabilities.entrySet()) {
        PartOfSpeech partOfSpeech = entry.getKey().getFirst();
        String word = entry.getKey().getSecond();

        byte[] posWordBytes = getPosWordBytes(partOfSpeech, word);

        try {
          probabilitiesDB.put(posWordBytes,
              ByteBuffer.allocate(8).putDouble(entry.getValue()).array());
        } catch (RocksDBException e) {
          throw new RuntimeException(e);
        }

        candidates.compute(word, (k, v) -> {
          if (v == null) {
            v = new ArrayList<>();
          }
          v.add(partOfSpeech);
          return v;
        });
      }

      for (Entry<String, List<PartOfSpeech>> entry : candidates.entrySet()) {
        String word = entry.getKey();
        List<PartOfSpeech> parts = entry.getValue();
        byte[] partsOfSpeechBytes = getPartsOfSpeechBytes(parts);
        try {
          candidatesDB.put(word.getBytes(StandardCharsets.UTF_8), partsOfSpeechBytes);
        } catch (RocksDBException e) {
          throw new RuntimeException(e);
        }
      }
    }

    InMemoryKnownWordDataStore inMemory() {
      InMemoryKnownWordDataStore inMemory = new InMemoryKnownWordDataStore();
      try (RocksIterator rocksIterator = probabilitiesDB.newIterator()) {
        rocksIterator.seekToFirst();
        while (rocksIterator.isValid()) {
          Pair<PartOfSpeech, String> posWord = getPosWordFromBytes(rocksIterator.key());
          double prob = ByteBuffer.wrap(rocksIterator.value()).getDouble();
          inMemory.addProbability(posWord.getSecond(), posWord.getFirst(), prob);
        }
      }
      return inMemory;
    }

    @Override
    public void write() {

    }
  }

  static byte[] getPosWordBytes(PartOfSpeech candidate, String word) {
    byte[] bytes = word.getBytes(StandardCharsets.UTF_8);
    ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length + 4);
    byteBuffer.putInt(candidate.ordinal()).put(word.getBytes(StandardCharsets.UTF_8));
    return byteBuffer.array();
  }

  static Pair<PartOfSpeech, String> getPosWordFromBytes(@Nullable byte[] bytes) {
    if (bytes == null) {
      return null;
    }
    ByteBuffer wrap = ByteBuffer.wrap(bytes);
    PartOfSpeech partOfSpeech = PartOfSpeech.values()[wrap.getInt()];
    byte[] stringBytes = new byte[wrap.remaining()];
    wrap.get(stringBytes);
    String s = new String(stringBytes, StandardCharsets.UTF_8);
    return Pair.of(partOfSpeech, s);
  }

  static byte[] getPartsOfSpeechBytes(List<PartOfSpeech> partsOfSpeech) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(4 * partsOfSpeech.size());
    for (PartOfSpeech candidate : partsOfSpeech) {
      byteBuffer.putInt(candidate.ordinal());
    }
    return byteBuffer.array();
  }

  static List<PartOfSpeech> getPartsOfSpeechFromBytes(@Nullable byte[] bytes) {
    if (bytes == null) {
      return Collections.emptyList();
    }

    int size = bytes.length / 4;
    List<PartOfSpeech> partsOfSpeech = new ArrayList<>(size);
    ByteBuffer wrap = ByteBuffer.wrap(bytes);
    while (wrap.hasRemaining()) {
      PartOfSpeech partOfSpeech = PartOfSpeech.values()[wrap.getInt()];
      partsOfSpeech.add(partOfSpeech);
    }
    return partsOfSpeech;
  }
}
