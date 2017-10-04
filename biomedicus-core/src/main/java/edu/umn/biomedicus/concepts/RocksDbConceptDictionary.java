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

package edu.umn.biomedicus.concepts;

import edu.umn.biomedicus.common.terms.TermsBag;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.LifecycleManaged;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

/**
 * An implementation of {@link ConceptDictionary} that uses RocksDB as a backend.
 *
 * @since 1.8.0
 */
class RocksDbConceptDictionary implements ConceptDictionary, LifecycleManaged {

  private final RocksDB phrases;

  private final RocksDB lowercase;

  private final RocksDB normsDB;

  RocksDbConceptDictionary(RocksDB phrases, RocksDB lowercase, RocksDB normsDB) {
    this.phrases = phrases;
    this.lowercase = lowercase;
    this.normsDB = normsDB;
  }

  @Nullable
  @Override
  public List<SuiCuiTui> forPhrase(String phrase) {
    try {
      byte[] bytes = phrases.get(phrase.getBytes(StandardCharsets.UTF_8));
      return bytes == null ? null : toList(bytes);
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  @Override
  public List<SuiCuiTui> forLowercasePhrase(String phrase) {
    try {
      byte[] bytes = lowercase.get(phrase.getBytes(StandardCharsets.UTF_8));
      return bytes == null ? null : toList(bytes);
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  @Override
  public List<SuiCuiTui> forNorms(TermsBag norms) {
    if (norms.uniqueTerms() == 0) {
      return null;
    }
    try {
      byte[] bytes = normsDB.get(norms.getBytes());
      return bytes == null ? null: toList(bytes);
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void doShutdown() throws BiomedicusException {

  }

  static List<SuiCuiTui> toList(byte[] bytes) {

    int len = SuiCuiTui.BYTES_LENGTH;
    int size = bytes.length / len;
    List<SuiCuiTui> list = new ArrayList<>(size);
    ByteBuffer buffer = ByteBuffer.wrap(bytes);

    byte[] localBytes = new byte[len];
    for (int i = 0; i < size; i++) {
      buffer.get(localBytes);
      list.add(new SuiCuiTui(localBytes));
    }

    return list;
  }
}
