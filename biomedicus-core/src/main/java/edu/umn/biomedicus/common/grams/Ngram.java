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

package edu.umn.biomedicus.common.grams;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;

/**
 * An implementation of Bigram and Trigram which uses an subarray.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class Ngram<T extends Comparable<T> & Serializable> implements Bigram<T>, Trigram<T>,
    Comparable<Ngram<T>>,
    Serializable {

  /**
   * Serialization UID.
   */
  private static final long serialVersionUID = 3988360323326418189L;

  /**
   * Backing array.
   */
  private T[] backingArray;

  /**
   * Length of the gram.
   */
  private int length;

  /**
   * Index that the gram starts at in the array.
   */
  private int offset;

  /**
   * Constructs a new ngram which is a span of objects in an array.
   *
   * @param backingArray the array.
   * @param length the length of the gram
   * @param offset the starting index of the gram in the array.
   */
  public Ngram(T[] backingArray, int length, int offset) {
    this.backingArray = backingArray;
    this.length = length;
    this.offset = offset;
  }

  /**
   * Creates a bigram ngram.
   *
   * @param first the first object in the ngram.
   * @param second the second object in the ngram.
   * @param <S> the type of object in the ngram.
   * @return a newly created bigram ngram.
   */
  public static <S extends Comparable<S> & Serializable> Ngram<S> create(S first, S second) {
    @SuppressWarnings("unchecked")
    S[] array = (S[]) Array.newInstance(first.getClass(), 2);
    array[0] = first;
    array[1] = second;
    return new Ngram<>(array, 2, 0);
  }

  /**
   * Creates a trigram ngram.
   *
   * @param first the first object in the ngram.
   * @param second the second object in the ngram.
   * @param third the third object in the ngram.
   * @param <S> the object type.
   * @return the newly created trigram ngram.
   */
  public static <S extends Comparable<S> & Serializable> Ngram<S> create(S first, S second,
      S third) {
    @SuppressWarnings("unchecked")
    S[] array = (S[]) Array.newInstance(first.getClass(), 3);
    array[0] = first;
    array[1] = second;
    array[2] = third;
    return new Ngram<>(array, 3, 0);
  }

  /**
   * Creates a bigram ngram from a span in an array.
   *
   * @param array the array of objects.
   * @param index the index of the first object.
   * @param <S> the object type.
   * @return newly created bigram ngram.
   */
  public static <S extends Comparable<S> & Serializable> Ngram<S> bigram(S[] array, int index) {
    return new Ngram<>(array, 2, index);
  }

  /**
   * Creates a trigram ngram from a span in an array.
   *
   * @param array the array of objects.
   * @param index the index of the first object.
   * @param <S> the object type.
   * @return newly created trigram ngram.
   */
  public static <S extends Comparable<S> & Serializable> Ngram<S> trigram(S[] array, int index) {
    return new Ngram<>(array, 3, index);
  }

  @Override
  public Ngram<T> tail() {
    if (length != 3) {
      throw new UnsupportedOperationException();
    }
    return new Ngram<>(backingArray, 2, offset + 1);
  }

  @Override
  public Ngram<T> head() {
    if (length != 3) {
      throw new UnsupportedOperationException();
    }
    return new Ngram<>(backingArray, 2, offset);
  }

  @Override
  public T getFirst() {
    return atIndex(0);
  }

  @Override
  public T getSecond() {
    return atIndex(1);
  }

  @Override
  public T getThird() {
    if (length != 3) {
      throw new UnsupportedOperationException();
    }
    return atIndex(2);
  }

  private T atIndex(int index) {
    return backingArray[offset + index];
  }

  @Override
  public int compareTo(Ngram<T> o) {
    int compare = Integer.compare(length, o.length);
    if (compare != 0) {
      return compare;
    }
    for (int i = 0; i < length; i++) {
      compare = atIndex(i).compareTo(o.atIndex(i));
      if (compare != 0) {
        return compare;
      }
    }
    return 0;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      int index = 0;

      @Override
      public boolean hasNext() {
        return index != length;
      }

      @Override
      public T next() {
        if (index == length) {
          throw new NoSuchElementException();
        }
        return atIndex(index++);
      }
    };
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Ngram<?> ngram = (Ngram<?>) o;
    if (length != ngram.length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (!atIndex(i).equals(ngram.atIndex(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    for (int i = 0; i < length; i++) {
      hashCode = 31 * hashCode + atIndex(i).hashCode();
    }
    return hashCode;
  }

  private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
    offset = 0;
    Class<?> componentType = (Class<?>) is.readObject();
    length = is.readInt();

    @SuppressWarnings("unchecked")
    T[] array = (T[]) Array.newInstance(componentType, length);
    backingArray = array;
    for (int i = 0; i < length; i++) {
      @SuppressWarnings("unchecked")
      T t = (T) is.readObject();
      backingArray[i] = t;
    }
  }

  private void writeObject(ObjectOutputStream os) throws IOException {
    Class<?> componentType = backingArray.getClass().getComponentType();
    os.writeObject(componentType);
    os.writeInt(length);
    for (int i = 0; i < length; i++) {
      os.writeObject(atIndex(i));
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[");

    if (length > 0) {
      sb.append(atIndex(0).toString());
    }
    for (int i = 1; i < length; i++) {
      sb.append(", ").append(atIndex(i).toString());
    }

    return sb.append("]").toString();
  }
}
