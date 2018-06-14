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

import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary.Strings;
import java.nio.ByteBuffer;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * An unordered bag of terms and their counts. Implemented as a counting map.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public final class StringsBag extends AbstractCollection<StringIdentifier> implements Comparable<StringsBag> {

  private final int[] terms;
  private final int[] counts;

  private transient int _total = -1;

  public StringsBag(int[] terms, int[] counts) {
    this.terms = terms;
    this.counts = counts;
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
    terms = new int[size];
    counts = new int[size];
    for (int i = 0; i < size; i++) {
      terms[i] = wrap.getInt();
      counts[i] = wrap.getInt();
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
    List<StringIdentifier> result = new ArrayList<>();
    for (int i = 0; i < terms.length; i++) {
      for (int j = 0; j < counts[i]; j++) {
        result.add(new StringIdentifier(terms[i]));
      }
    }
    return result;
  }

  /**
   * Tests whether this bag contains a term.
   *
   * @param stringIdentifier the term to test
   * @return true if the bag contains the term, false otherwise.
   */
  public boolean contains(StringIdentifier stringIdentifier) {
    return Arrays.binarySearch(terms, stringIdentifier.value()) != -1;
  }

  /**
   * The number of times a specific term occurs in this bag.
   *
   * @param stringIdentifier the term to get
   * @return integer count
   */
  public int countOf(StringIdentifier stringIdentifier) {
    int index = Arrays.binarySearch(terms, stringIdentifier.value());
    return index < 0 ? 0 : counts[index];
  }

  /**
   * The number of unique terms that this bag holds.
   *
   * @return integer count
   */
  public int uniqueTerms() {
    return terms.length;
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

    return Arrays.equals(terms, that.terms) && Arrays.equals(counts, that.counts);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(terms) * 31 + Arrays.hashCode(counts);
  }

  public byte[] getBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(4 * 2 * terms.length);
    for (int i = 0; i < terms.length; i++) {
      buffer.putInt(terms[i]);
      buffer.putInt(counts[i]);
    }
    return buffer.array();
  }

  @Override
  public Iterator<StringIdentifier> iterator() {
    return new Iterator<StringIdentifier>() {
      int index = -1;
      int identifier;
      int instances = 0;

      {
        advance();
      }

      void advance() {
        if (instances > 0) {
          instances--;
        } else {
          index++;
          if (index < terms.length) {
            identifier = terms[index];
            instances = counts[index];
          }
        }
      }

      @Override
      public boolean hasNext() {
        return instances > 0 || index < terms.length;
      }

      @Override
      public StringIdentifier next() {
        if (instances == 0) {
          throw new NoSuchElementException();
        }

        int tmp = identifier;

        advance();

        return new StringIdentifier(tmp);
      }
    };
  }

  @Override
  public int size() {
    int total = this._total;
    if (total != -1) {
      return total;
    }
    total = 0;
    for (int count : counts) {
      total += count;
    }

    return this._total = total;
  }

  @Override
  public int compareTo(@NotNull StringsBag o) {
    for (int i = 0; i < Math.max(terms.length, o.terms.length); i++) {
      if (terms.length == i) return -1;
      if (o.terms.length == i) return 1;
      int compare = Integer.compare(terms[i], o.terms[i]);
      if (compare != 0) return compare;
      compare = Integer.compare(counts[i], o.counts[i]);
      if (compare != 0) return compare;
    }
    return 0;
  }

  public static class Builder {

    private final TreeMap<Integer, Integer> identifierToCount;

    public Builder() {
      identifierToCount = new TreeMap<>();
    }

    public Builder(Map<StringIdentifier, Integer> m) {
      identifierToCount = new TreeMap<>();
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
      int[] terms = new int[identifierToCount.size()];
      int[] counts = new int[identifierToCount.size()];

      Iterator<Entry<Integer, Integer>> it = identifierToCount.entrySet().iterator();
      for (int i = 0; i < identifierToCount.size(); i++) {
        Entry<Integer, Integer> next = it.next();
        terms[i] = next.getKey();
        counts[i] = next.getValue();
      }

      return new StringsBag(terms, counts);
    }
  }
}
