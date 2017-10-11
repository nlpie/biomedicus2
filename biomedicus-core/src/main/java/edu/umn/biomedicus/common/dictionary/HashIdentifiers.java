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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

/**
 * An implementation of {@link edu.umn.biomedicus.common.terms.BidirectionalDictionary.Identifiers}
 * that is backed by a hash map from strings to integers.
 */
public final class HashIdentifiers extends AbstractIdentifiers {
  private final Map<String, Integer> map;

  public HashIdentifiers() {
    map = new HashMap<>();
  }

  public HashIdentifiers(int size) {
    map = new HashMap<>(size);
  }

  void addMapping(String string, int identifier) {
    map.put(string, identifier);
  }

  @Override
  protected int getIdentifier(@Nullable CharSequence term) {
    if (term == null) {
      return -1;
    }
    Integer identifier = map.get(term.toString());
    return identifier == null ? -1 : identifier;
  }

  @Override
  public boolean contains(@Nullable String string) {
    return string != null && map.containsKey(string);
  }

  @Override
  public MappingIterator mappingIterator() {
    Iterator<Entry<String, Integer>> iterator = map.entrySet().iterator();
    return new MappingIterator() {
      @Nullable
      private Entry<String, Integer> value = null;

      {
        if (iterator.hasNext()) {
          value = iterator.next();
        }
      }

      @Override
      public boolean isValid() {
        return value != null;
      }

      @Override
      public int identifier() {
        return value.getValue();
      }

      @Override
      public String string() {
        return value.getKey();
      }

      @Override
      public void next() {
        value = iterator.hasNext() ? iterator.next() : null;
      }

      @Override
      public void close() throws IOException {

      }
    };
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public void close() throws IOException {

  }
}
