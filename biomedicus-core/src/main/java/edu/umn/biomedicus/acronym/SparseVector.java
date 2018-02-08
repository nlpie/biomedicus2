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

package edu.umn.biomedicus.acronym;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * A simple implementation of sparse vectors
 *
 * @author Greg Finley
 * @since 1.5.0
 */
public class SparseVector {

  private static final int ENTRY_BYTES = Integer.BYTES + Double.BYTES;

  private static final int DEFAULT_SIZE = 10;

  /**
   * Hash of values in this vector
   */
  private int[] keys;
  private double[] values;
  private int size;


  public SparseVector() {
    keys = new int[DEFAULT_SIZE];
    values = new double[DEFAULT_SIZE];
    size = 0;
  }

  public SparseVector(SparseVector other) {
    size = other.size;
    keys = new int[size];
    System.arraycopy(other.keys, 0, keys, 0, size);
    values = new double[size];
    System.arraycopy(other.values, 0, values, 0, size);
  }

  public SparseVector(Map<Integer, Double> vector) {
    setVector(vector);
  }

  public SparseVector(byte[] bytes) {
    size = bytes.length / ENTRY_BYTES;
    keys = new int[size];
    values = new double[size];

    ByteBuffer wrap = ByteBuffer.wrap(bytes);
    for (int i = 0; i < size; i++) {
      keys[i] = wrap.getInt();
      values[i] = wrap.getDouble();
    }
  }

  public void setVector(Map<Integer, Double> vector) {
    size = vector.size();
    keys = new int[size];
    values = new double[size];

    List<Entry<Integer, Double>> list = vector.entrySet().stream()
        .sorted(Comparator.comparing(Entry::getKey))
        .collect(Collectors.toList());

    for (int i = 0; i < list.size(); i++) {
      Entry<Integer, Double> entry = list.get(i);
      keys[i] = entry.getKey();
      values[i] = entry.getValue();
    }
  }

  /**
   * Get the magnitude of this vector.
   *
   * @return the sqrt of the sum of all squared elements
   */
  public double magnitude() {
    double sqsum = 0;
    for (double x : values) {
      sqsum += x * x;
    }
    return Math.sqrt(sqsum);
  }

  /**
   * Normalize this vector to unit length.
   */
  public void normVector() {
    double mag = magnitude();
    for (int i = 0; i < size; i++) {
      values[i] = values[i] / mag;
    }
  }

  /**
   * Add another vector to this one.
   *
   * @param other the vector to add (argument vector will not be changed)
   */
  public void add(SparseVector other) {
    int newSize = 0;
    int[] newKeys = new int[size + other.size];
    double[] newValues = new double[size + other.size];

    int ptr = 0;
    int otherPtr = 0;
    while (ptr < size && otherPtr < other.size) {
      int key = keys[ptr];
      int otherKey = other.keys[otherPtr];

      if (key == otherKey) {
        newKeys[newSize] = key;
        newValues[newSize++] = values[ptr++] + other.values[otherPtr++];
      } else if (key < otherKey) {
        newKeys[newSize] = key;
        newValues[newSize++] = values[ptr++];
      } else { // key > otherKey
        newKeys[newSize] = otherKey;
        newValues[newSize++] = other.values[otherPtr++];
      }
    }
    while (ptr < size) {
      newKeys[newSize] = keys[ptr];
      newValues[newSize++] = values[ptr++];
    }
    while (otherPtr < size) {
      newKeys[newSize] = other.keys[otherPtr];
      newValues[newSize++] = other.values[otherPtr++];
    }

    keys = newKeys;
    values = newValues;
    size = newSize;
  }

  /**
   * Set this vector to the Hadamard/elementwise product of itself and another vector.
   *
   * @param other the vector to multiply against this one (argument vector will not be changed)
   */
  public void multiply(SparseVector other) {
    int newSize = 0;
    int cap = Math.min(size, other.size);
    int[] newKeys = new int[cap];
    double[] newValues = new double[cap];

    int ptr = 0;
    int otherPtr = 0;
    while (ptr < size && otherPtr < other.size) {
      int key = keys[ptr];
      int otherKey = other.keys[otherPtr];

      if (key == otherKey) {
        newKeys[newSize] = key;
        newValues[newSize++] = values[ptr++] * other.values[otherPtr++];
      } else if (key < otherKey) {
        ptr++;
      } else { // key > otherKey
        otherPtr++;
      }
    }

    size = newSize;
    keys = newKeys;
    values = newValues;
  }

  /**
   * Applies an operation to every element of this vector.
   *
   * @param operation a function that takes and outputs Double (Math::sqrt, e.g.)
   */
  public void applyOperation(DoubleUnaryOperator operation) {
    for (int i = 0; i < size; i++) {
      values[i] = operation.applyAsDouble(values[i]);
    }
  }

  /**
   * Return the dot product of this vector with another.
   *
   * @param other another vector
   * @return their dot product
   */
  public double dot(SparseVector other) {
    double sum = 0;

    int ptr = 0;
    int otherPtr = 0;
    while (ptr < size && otherPtr < other.size) {
      int key = keys[ptr];
      int otherKey = other.keys[otherPtr];

      if (key == otherKey) {
        sum += values[ptr++] * other.values[otherPtr++];
      } else if (key < otherKey) {
        ptr++;
      } else { // key > otherKey
        otherPtr++;
      }
    }

    return sum;
  }

  public double get(int i) {
    int index = Arrays.binarySearch(keys, i);
    if (index >= 0 && index < size) {
      return keys[index];
    } else {
      return 0.0d;
    }
  }

  public double set(int ind, double val) {
    int insert = Arrays.binarySearch(keys, ind);
    if (insert >= 0 && insert < size) {
      double existing = values[insert];
      if (val == 0.0d) {
        removeInternal(insert);
      } else {
        values[insert] = val;
      }
      return existing;
    }

    if (val == 0.0d) {
      return 0.0d;
    }

    if (size + 1 > keys.length) {
      int newCapacity = keys.length + (keys.length >> 1);
      keys = Arrays.copyOf(keys, newCapacity);
      values = Arrays.copyOf(values, newCapacity);
    }

    System.arraycopy(keys, insert, keys, insert + 1, size - insert);
    System.arraycopy(values, insert, values, insert + 1, size - insert);
    keys[insert] = ind;
    values[insert] = val;

    size = size + 1;

    return 0.0d;
  }

  public void remove(@Nullable Integer index) {
    if (index != null) {
      int i = Arrays.binarySearch(keys, index);
      removeInternal(i);
    }
  }

  private void removeInternal(int i) {
    if (i == size - 1) {
      size = size - 1;
      return;
    }

    if (i >= 0 && i < size) {
      System.arraycopy(keys, i + 1, keys, i, size - i - 1);
    }
  }

  public void removeAll(@Nullable Collection<Integer> indexes) {
    if (indexes == null) {
      return;
    }

    int newSize = 0;
    int[] newKeys = new int[size];
    double[] newValues = new double[size];

    for (int i = 0; i < size; i++) {
      if (!indexes.contains(i)) {
        newKeys[newSize] = keys[i];
        newValues[newSize++] = values[i];
      }
    }

    size = newSize;
    keys = newKeys;
    values = newValues;
  }

  public byte[] toBytes() {
    ByteBuffer buffer = ByteBuffer.allocate(size * ENTRY_BYTES);
    for (int i = 0; i < size; i++) {
      buffer.putInt(keys[i]).putDouble(values[i]);
    }
    return buffer.array();
  }
}
