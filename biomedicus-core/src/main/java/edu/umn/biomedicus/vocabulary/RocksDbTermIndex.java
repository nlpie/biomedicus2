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

import edu.umn.biomedicus.common.terms.AbstractTermIndex;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.annotation.Nullable;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class RocksDbTermIndex extends AbstractTermIndex implements Closeable {

  /**
   * integer indices -> string terms
   */
  private final RocksDB terms;

  /**
   * string terms -> integer indices
   */
  private final RocksDB indices;

  private transient int size = -1;

  public RocksDbTermIndex(Path termsPath, Path indicesPath) {
    RocksDB.loadLibrary();

    try {
      terms = RocksDB.openReadOnly(termsPath.toString());
      indices = RocksDB.openReadOnly(indicesPath.toString());
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
  protected int getIdentifier(@Nullable CharSequence term) {
    if (term == null) {
      return -1;
    }

    byte[] bytes = term.toString().getBytes(StandardCharsets.UTF_8);
    try {
      byte[] idBytes = indices.get(bytes);
      return ByteBuffer.wrap(idBytes).getInt();
    } catch (RocksDBException e) {
      // says "if error happens in underlying native library", can't possible hope to handle that.
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean contains(String string) {
    return false;
  }

  @Override
  public int size() {
    if (size == -1) {
      int size = 0;
      RocksIterator rocksIterator = terms.newIterator();
      while (rocksIterator.isValid()) {
        rocksIterator.next();
        size++;
      }
      return this.size = size;
    }
    return size;
  }

  @Override
  public void close() throws IOException {
    terms.close();
    indices.close();
  }
}
