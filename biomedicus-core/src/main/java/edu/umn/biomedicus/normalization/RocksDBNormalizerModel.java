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

package edu.umn.biomedicus.normalization;

import edu.umn.biomedicus.exc.BiomedicusException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

/**
 * Normalizer model which uses RocksDB as a backing map.
 */
public class RocksDBNormalizerModel implements NormalizerModel {

  private final RocksDB db;

  RocksDBNormalizerModel(Path dbPath) {
    RocksDB.loadLibrary();

    try {
      db = RocksDB.openReadOnly(dbPath.toString());
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  @Override
  public TermString get(@Nullable TermPos termPos) {
    if (termPos == null) {
      return null;
    }
    byte[] key = termPos.getBytes();
    try {
      byte[] bytes = db.get(key);
      return bytes == null ? null : new TermString(bytes);
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doShutdown() throws BiomedicusException {
    db.close();
  }

  NormalizerModel inMemory(boolean inMemory) {
    if (!inMemory) {
      return this;
    }

    Map<TermPos, TermString> map = new HashMap<>();
    try (RocksIterator rocksIterator = db.newIterator()) {
      rocksIterator.seekToFirst();
      while (rocksIterator.isValid()) {
        TermPos termPos = new TermPos(rocksIterator.key());
        TermString termString = new TermString(rocksIterator.value());
        map.put(termPos, termString);
        rocksIterator.next();
      }
    }

    return new HashNormalizerModel(map);
  }
}
