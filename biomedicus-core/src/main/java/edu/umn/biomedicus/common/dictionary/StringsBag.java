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

import java.nio.ByteBuffer;
import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * An unordered bag of terms and their counts. Implemented as a counting map.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public final class StringsBag extends AbstractCollection<StringIdentifier> {

  private final HashMap<Integer, Integer> backingMap;

  private transient int _total = -1;

  StringsBag(HashMap<Integer, Integer> backingMap) {
    this.backingMap = backingMap;
  }



  /**
   * Creates a terms bag from a byte array.
   *
   * @param bytes the bytes used to initialize the terms and their counts
   * @see #getBytes()
   */
  public StringsBag(byte[] bytes) {
    int size = bytes.length / 4 / 2;
    ByteBuffer wrap = ByteBuffer.wrap(bytes);
    backingMap = new HashMap<>(size);
    for (int i = 0; i < size; i++) {
      backingMap.put(wrap.getInt(), wrap.getInt());
    }
  }

  /**
   * Creates a builder for creating terms bags.
   *
   * @return a new builder that which terms can be added to
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Converts the bag to a new list of {@link StringIdentifier} objects. If a term occurs n times in the
   * bag it will get added to the list n times. The terms are sorted with identifiers ascending.
   *
   * @return newly allocated list of terms
   */
  List<StringIdentifier> toTerms() {
    return backingMap.entrySet().stream().flatMap(entry -> {
      Integer key = entry.getKey();
      StringIdentifier identifier = new StringIdentifier(key);
      Stream.Builder<StringIdentifier> termIdentifierBuilder = Stream.builder();
      for (int i = 0; i < entry.getValue(); i++) {
        termIdentifierBuilder.add(identifier);
      }
      return termIdentifierBuilder.build();
    }).collect(Collectors.toList());
  }

  /**
   * Tests whether this bag contains a term.
   *
   * @param stringIdentifier the term to test
   * @return true if the bag contains the term, false otherwise.
   */
  public boolean contains(StringIdentifier stringIdentifier) {
    return backingMap.containsKey(stringIdentifier.value());
  }

  /**
   * The number of times a specific term occurs in this bag.
   *
   * @param stringIdentifier the term to get
   * @return integer count
   */
  public int countOf(StringIdentifier stringIdentifier) {
    Integer count = backingMap.get(stringIdentifier.value());
    return count == null ? 0 : count;
  }

  /**
   * The number of unique terms that this bag holds.
   *
   * @return integer count
   */
  public int uniqueTerms() {
    return backingMap.size();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StringsBag that = (StringsBag) o;

    return backingMap.equals(that.backingMap);
  }

  @Override
  public int hashCode() {
    return backingMap.hashCode();
  }

  public byte[] getBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(4 * 2 * backingMap.size());
    for (Entry<Integer, Integer> entry : backingMap.entrySet()) {
      buffer.putInt(entry.getKey());
      buffer.putInt(entry.getValue());
    }
    return buffer.array();
  }

  @Override
  public Iterator<StringIdentifier> iterator() {
    Set<Entry<Integer, Integer>> entries = backingMap.entrySet();
    Iterator<Entry<Integer, Integer>> iterator = entries.iterator();

    return new Iterator<StringIdentifier>() {
      int identifier;
      int instances;
      int count;

      @Override
      public boolean hasNext() {
        return instances != -1 && (count != instances || iterator.hasNext());
      }

      @Override
      public StringIdentifier next() {
        if (instances == -1) {
          throw new NoSuchElementException();
        }

        if (count == instances) {
          if (!iterator.hasNext()) {
            throw new NoSuchElementException();
          }

          Entry<Integer, Integer> next = iterator.next();
          identifier = next.getKey();
          instances = next.getValue();
          count = 0;
        }

        count++;
        return new StringIdentifier(identifier);
      }
    };
  }

  @Override
  public int size() {
    int total = this._total;
    if (total != -1) {
      return total;
    }
    total = backingMap.values().stream().mapToInt((i) -> i).sum();
    return this._total = total;
  }

  public static class Builder {

    private final HashMap<Integer, Integer> identifierToCount;

    public Builder() {
      identifierToCount = new HashMap<>();
    }

    public Builder(int capacity) {
      identifierToCount = new HashMap<>(capacity);
    }

    public Builder(Map<StringIdentifier, Integer> m) {
      identifierToCount = new HashMap<>(m.size());
      for (Entry<StringIdentifier, Integer> entry : m.entrySet()) {
        identifierToCount.put(entry.getKey().value(), entry.getValue());
      }

    }

    public Builder addTerm(StringIdentifier stringIdentifier) {
      if (stringIdentifier.isUnknown()) {
        return this;
      }

      return addIdentifier(stringIdentifier.value());
    }

    public Builder addIdentifier(int identifier) {
      identifierToCount.compute(identifier, (id, count) -> {
        if (count == null) {
          count = 0;
        }
        return count + 1;
      });
      return this;
    }

    public StringsBag build() {
      return new StringsBag(identifierToCount);
    }
  }
}
