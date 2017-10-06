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

package edu.umn.biomedicus.common.terms;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * A unordered bag of terms and their counts.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public final class TermsBag implements Comparable<TermsBag>, Iterable<TermIdentifier>, Serializable {

  /**
   * The term identifiers, sorted increasing.
   */
  private final int[] identifiers;

  /**
   * The number of times a term occurs in the bag.
   */
  private final int[] counts;

  private transient int total = -1;

  private TermsBag(int[] identifiers, int[] counts) {
    this.identifiers = identifiers;
    this.counts = counts;
  }

  /**
   * Creates a terms bag from a byte array.
   *
   * @param bytes the bytes used to initialize the terms and their counts
   * @see #getBytes()
   */
  public TermsBag(byte[] bytes) {
    int size = bytes.length / 4 / 2;
    ByteBuffer wrap = ByteBuffer.wrap(bytes);
    this.identifiers = new int[size];
    this.counts = new int[size];
    for (int i = 0; i < size; i++) {
      identifiers[i] = wrap.getInt();
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
   * Converts the bag to a new list of {@link TermIdentifier} objects. If a term occurs n times in the
   * bag it will get added to the list n times. The terms are sorted with identifiers ascending.
   *
   * @return newly allocated list of terms
   */
  List<TermIdentifier> toTerms() {
    List<TermIdentifier> termIdentifiers = new ArrayList<>(identifiers.length);
    for (int i = 0; i < identifiers.length; i++) {
      TermIdentifier termIdentifier = new TermIdentifier(identifiers[i]);
      for (int j = 0; j < counts[i]; j++) {
        termIdentifiers.add(termIdentifier);
      }
    }
    return termIdentifiers;
  }

  private int indexOf(TermIdentifier termIdentifier) {
    return Arrays.binarySearch(identifiers, termIdentifier.value());
  }

  /**
   * Tests whether this bag contains a term.
   *
   * @param termIdentifier the term to test
   * @return true if the bag contains the term, false otherwise.
   */
  public boolean contains(TermIdentifier termIdentifier) {
    return indexOf(termIdentifier) >= 0;
  }

  /**
   * The number of times a specific term occurs in this bag.
   *
   * @param termIdentifier the term to get
   * @return integer count
   */
  public int countOf(TermIdentifier termIdentifier) {
    int index = indexOf(termIdentifier);
    if (index < 0) {
      return 0;
    }
    return counts[index];
  }

  /**
   * The number of unique terms that this bag holds.
   *
   * @return integer count
   */
  public int uniqueTerms() {
    return identifiers.length;
  }

  public int size() {
    if (total != -1) {
      return total;
    }
    int total = 0;
    for (int count : counts) {
      total += count;
    }
    return this.total = total;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TermsBag that = (TermsBag) o;

    return Arrays.equals(identifiers, that.identifiers) && Arrays.equals(counts, that.counts);
  }

  @Override
  public int hashCode() {
    int value = Arrays.hashCode(identifiers);
    return value * 31 + Arrays.hashCode(counts);
  }

  @Override
  public int compareTo(TermsBag o) {
    int compare = Integer.compare(identifiers.length, o.identifiers.length);
    if (compare != 0) {
      return compare;
    }
    for (int i = 0; i < identifiers.length; i++) {
      compare = Integer.compare(identifiers[i], o.identifiers[i]);
      if (compare != 0) {
        return compare;
      }
      compare = Integer.compare(counts[i], o.counts[i]);
      if (compare != 0) {
        return compare;
      }
    }
    return 0;
  }

  @NotNull
  @Override
  public Iterator<TermIdentifier> iterator() {
    return new Iterator<TermIdentifier>() {
      int index = 0;

      @Override
      public boolean hasNext() {
        return index != identifiers.length;
      }

      @Override
      public TermIdentifier next() {
        if (index == identifiers.length) {
          throw new NoSuchElementException();
        }

        return new TermIdentifier(identifiers[index++]);
      }
    };
  }

  public byte[] getBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(4 * 2 * identifiers.length);
    for (int i = 0; i < identifiers.length; i++) {
      buffer.putInt(identifiers[i]);
      buffer.putInt(counts[i]);
    }
    return buffer.array();
  }

  public static class Builder {

    private final TreeMap<Integer, Integer> identifierToCount = new TreeMap<>();

    public Builder addTerm(TermIdentifier termIdentifier) {
      if (termIdentifier.isUnknown()) {
        return this;
      }

      return addIdentifier(termIdentifier.value());
    }

    Builder addIdentifier(int identifier) {
      identifierToCount.compute(identifier, (id, count) -> {
        if (count == null) {
          count = 0;
        }
        return count + 1;
      });
      return this;
    }

    public TermsBag build() {
      int size = identifierToCount.size();
      int[] identifiers = new int[size];
      int[] counts = new int[size];
      Iterator<Map.Entry<Integer, Integer>> entryIterator = identifierToCount.entrySet().iterator();
      for (int i = 0; i < size; i++) {
        Map.Entry<Integer, Integer> entry = entryIterator.next();
        identifiers[i] = entry.getKey();
        counts[i] = entry.getValue();
      }

      return new TermsBag(identifiers, counts);
    }
  }
}
