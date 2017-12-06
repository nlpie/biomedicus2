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

import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An immutable ordered list of term indices.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
public final class StringsVector extends AbstractList<StringIdentifier> {

  private final int[] identifiers;

  StringsVector(int[] identifiers) {
    this.identifiers = identifiers;
  }

  StringsVector(byte[] bytes) {
    int size = bytes.length / 4;
    ByteBuffer wrap = ByteBuffer.wrap(bytes);
    identifiers = new int[size];
    for (int i = 0; i < size; i++) {
      identifiers[i] = wrap.getInt();
    }
  }

  /**
   * Creates a builder for creating terms vectors.
   *
   * @return newly created builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Gets the length of this term vector.
   *
   * @return the total number of elements in the vector
   */
  @Override
  public int size() {
    return identifiers.length;
  }

  /**
   * Returns the term identifier at the specified index.
   *
   * @param index the index to look up
   * @return the term identifier at the specified index
   */
  @Override
  public StringIdentifier get(int index) {
    if (index < 0 || index >= identifiers.length) {
      throw new IndexOutOfBoundsException("invalid index: " + index);
    }

    return new StringIdentifier(identifiers[index]);
  }

  /**
   * Reduces this terms vector to a terms bag where the ordering doesn't matter.
   *
   * @return turns this terms vector into a terms bag
   */
  public StringsBag toBag() {
    StringsBag.Builder builder = StringsBag.builder();
    for (int identifier : identifiers) {
      builder.addIdentifier(identifier);
    }

    return builder.build();
  }

  /**
   * Detects whether a list of terms prefixes this terms vector
   *
   * @param termList the list of terms to test
   * @return true if the list is a prefix for this terms vector, false otherwise
   */
  public boolean isPrefix(List<StringIdentifier> termList) {
    Iterator<StringIdentifier> it = termList.iterator();
    for (int identifier : identifiers) {
      if (!it.hasNext()) {
        return false;
      }
      StringIdentifier next = it.next();
      if (next.value() != identifier) {
        return false;
      }
    }
    return true;
  }

  /**
   * Detects whether a terms vector prefixes this terms vector.
   *
   * @param terms the terms vector to test
   * @return true if the terms vector parameter prefixes this terms vector, false otherwise
   */
  public boolean isPrefix(StringsVector terms) {
    return isPrefix(terms.asTermIdentifierList());
  }

  /**
   * Creates a list of TermIdentifiers view of this terms vector.
   *
   * @return immutable list that is a view of the elements in this terms vector
   */
  public List<StringIdentifier> asTermIdentifierList() {
    return new ListView();
  }

  byte[] getBytes() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(identifiers.length * 4);
    for (int identifier : identifiers) {
      byteBuffer.putInt(identifier);
    }
    return byteBuffer.array();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StringsVector that = (StringsVector) o;

    return Arrays.equals(identifiers, that.identifiers);

  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(identifiers);
  }

  @Override
  public Iterator<StringIdentifier> iterator() {
    return new Iterator<StringIdentifier>() {
      private int index = 0;

      @Override
      public boolean hasNext() {
        return index < identifiers.length;
      }

      @Override
      public StringIdentifier next() {
        if (index >= identifiers.length) {
          throw new NoSuchElementException();
        }
        return get(index++);
      }
    };
  }

  private class ListView extends AbstractList<StringIdentifier> {

    @Override
    public StringIdentifier get(int index) {
      return new StringIdentifier(identifiers[index]);
    }

    @Override
    public int size() {
      return identifiers.length;
    }
  }

  /**
   * A builder which collects terms in order to create a terms vector.
   */
  public static class Builder {

    private final ArrayList<Integer> identifiers = new ArrayList<>();

    /**
     *
     *
     * @param stringIdentifier
     * @return
     */
    public Builder addTerm(StringIdentifier stringIdentifier) {
      identifiers.add(stringIdentifier.value());
      return this;
    }

    /**
     *
     *
     * @param identifier
     * @return
     */
    public Builder addIdentifier(int identifier) {
      identifiers.add(identifier);
      return this;
    }

    /**
     *
     *
     * @return
     */
    public StringsVector build() {
      return new StringsVector(identifiers.stream().mapToInt(Integer::intValue).toArray());
    }
  }
}
