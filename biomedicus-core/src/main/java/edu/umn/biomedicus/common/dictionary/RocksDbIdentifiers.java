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

package edu.umn.biomedicus.common.dictionary;

import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary.Identifiers;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.annotation.Nullable;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public final class RocksDbIdentifiers extends AbstractIdentifiers implements Closeable {
  /**
   * string terms -> integer indices
   */
  private final RocksDB indices;

  private transient int _size = -1;

  public RocksDbIdentifiers(Path identifiersPath) {
    RocksDB.loadLibrary();

    try {
      indices = RocksDB.openReadOnly(identifiersPath.toString());
    } catch (RocksDBException e) {
      // says "if error happens in underlying native library", can't possible hope to handle that.
      throw new RuntimeException(e);
    }
  }

  @Override
  protected int getIdentifier(@Nullable CharSequence term) {
    if (term == null) {
      return -1;
    }

    byte[] bytes = term.toString().getBytes(StandardCharsets.UTF_8);
    try {
      byte[] idBytes = indices.get(bytes);
      if (idBytes == null) {
        return -1;
      }
      return ByteBuffer.wrap(idBytes).getInt();
    } catch (RocksDBException e) {
      // says "if error happens in underlying native library", can't possible hope to handle that.
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    indices.close();
  }

  @Override
  public boolean contains(@Nullable String string) {
    if (string == null) {
      return false;
    }
    try {
      return indices.get(string.getBytes(StandardCharsets.UTF_8)) != null;
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public MappingIterator mappingIterator() {
    RocksIterator rocksIterator = indices.newIterator();
    return new MappingIterator() {
      @Override
      public void close() {
        rocksIterator.close();
      }

      @Override
      public boolean isValid() {
        return rocksIterator.isValid();
      }

      @Override
      public int identifier() {
        byte[] value = rocksIterator.value();
        return ByteBuffer.wrap(value).getInt();
      }

      @Override
      public String string() {
        byte[] key = rocksIterator.key();
        return new String(key, StandardCharsets.UTF_8);
      }

      @Override
      public void next() {
        rocksIterator.next();
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

  public Identifiers inMemory(boolean inMemory) {
    if (inMemory) {
      HashIdentifiers hashIdentifiers = new HashIdentifiers();
      try (RocksIterator rocksIterator = indices.newIterator()) {
        rocksIterator.seekToFirst();
        while (rocksIterator.isValid()) {
          byte[] key = rocksIterator.key();
          String stringKey = new String(key, StandardCharsets.UTF_8);

          byte[] value = rocksIterator.value();
          int intValue = ByteBuffer.wrap(value).getInt();

          hashIdentifiers.addMapping(stringKey, intValue);

          rocksIterator.next();
        }
      }
      close();
      return hashIdentifiers;
    }
    return this;
  }
}
