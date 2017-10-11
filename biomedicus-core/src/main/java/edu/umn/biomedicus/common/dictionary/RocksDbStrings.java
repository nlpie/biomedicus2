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

package edu.umn.biomedicus.common.dictionary;

import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary.Strings;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public final class RocksDbStrings extends AbstractStrings {

  /**
   * integer indices -> string terms
   */
  private final RocksDB terms;

  private transient int _size = -1;

  public RocksDbStrings(Path termsPath) {
    RocksDB.loadLibrary();

    try {
      terms = RocksDB.openReadOnly(termsPath.toString());
    } catch (RocksDBException e) {
      // says "if error happens in underlying native library", can't possible hope to handle that.
      throw new RuntimeException(e);
    }
  }

  @Override
  protected String getTerm(int termIdentifier) {
    byte[] bytes = ByteBuffer.allocate(4).putInt(termIdentifier).array();
    try {
      byte[] termBytes = terms.get(bytes);
      return new String(termBytes, StandardCharsets.UTF_8);
    } catch (RocksDBException e) {
      // says "if error happens in underlying native library", can't possible hope to handle that.
      throw new RuntimeException(e);
    }
  }

  @Override
  public MappingIterator mappingIterator() {
    RocksIterator rocksIterator = terms.newIterator();
    rocksIterator.seekToFirst();
    return new MappingIterator() {
      @Override
      public boolean isValid() {
        return rocksIterator.isValid();
      }

      @Override
      public int identifier() {
        byte[] key = rocksIterator.key();
        return ByteBuffer.wrap(key).getInt();
      }

      @Override
      public String string() {
        byte[] value = rocksIterator.value();
        return new String(value, StandardCharsets.UTF_8);
      }

      @Override
      public void next() {
        rocksIterator.next();
      }

      @Override
      public void close() throws IOException {
        rocksIterator.close();
      }
    };
  }

  @Override
  public int size() {

    int size = _size;
    if (size != -1) {
      return size;
    }

    size = 0;
    MappingIterator mappingIterator = mappingIterator();
    while (mappingIterator.isValid()) {
      size++;
      mappingIterator.next();
    }

    return (_size = size);
  }

  public Strings inMemory(boolean inMemory) throws IOException {
    if (inMemory) {
      TreeMap<Integer, String> treeMap = new TreeMap<>();

      MappingIterator mappingIterator = mappingIterator();
      while (mappingIterator.isValid()) {
        treeMap.put(mappingIterator.identifier(), mappingIterator.string());
        mappingIterator.next();
      }
      mappingIterator.close();

      String[] strings = new String[treeMap.size()];
      for (Entry<Integer, String> entry : treeMap.entrySet()) {
        strings[entry.getKey()] = entry.getValue();
      }
      return new ArrayStrings(strings);
    }
    return this;
  }

  @Override
  public void close() throws IOException {
    terms.close();
  }
}
