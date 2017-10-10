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

package edu.umn.biomedicus.acronym;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public class HashSenseVectors implements SenseVectors {

  private final Map<String, SparseVector> vectorMap;

  HashSenseVectors(
      Map<String, SparseVector> vectorMap) {
    this.vectorMap = vectorMap;
  }

  @Override
  public boolean containsSense(String sense) {
    return vectorMap.containsKey(sense);
  }

  @Nullable
  @Override
  public SparseVector get(String sense) {
    return vectorMap.get(sense);
  }

  @Override
  public Set<String> senses() {
    return vectorMap.keySet();
  }

  @Override
  public Collection<SparseVector> vectors() {
    return vectorMap.values();
  }

  @Override
  public void removeWord(int index) {
    throw new UnsupportedOperationException("Data is loaded from RocksDB can't edit");
  }

  @Override
  public void removeWords(Collection<Integer> indexes) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return vectorMap.size();
  }

  @Override
  public void close() throws IOException {

  }
}
