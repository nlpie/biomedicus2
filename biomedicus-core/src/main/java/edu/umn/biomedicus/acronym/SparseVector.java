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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A simple implementation of sparse vectors
 *
 * @author Greg Finley
 * @since 1.5.0
 */
public class SparseVector {

  /**
   * Hash of values in this vector
   */
  private Map<Integer, Double> vector;

  public SparseVector() {
    vector = new HashMap<>();
  }

  public SparseVector(SparseVector other) {
    vector = new HashMap<>(other.getVector());
  }

  public SparseVector(Map<Integer, Double> vector) {
    this.vector = vector;
  }

  public Map<Integer, Double> getVector() {
    return vector;
  }

  public void setVector(Map<Integer, Double> vector) {
    this.vector = vector;
  }

  /**
   * Get the magnitude of this vector.
   *
   * @return the sqrt of the sum of all squared elements
   */
  public double magnitude() {
    double sqsum = 0;
    for (double x : vector.values()) {
      sqsum += Math.pow(x, 2);
    }
    return Math.pow(sqsum, 0.5);
  }

  /**
   * Normalize this vector to unit length.
   */
  public void normVector() {
    double mag = magnitude();
    for (Map.Entry<Integer, Double> e : vector.entrySet()) {
      e.setValue(e.getValue() / mag);
    }
  }

  /**
   * Add another vector to this one.
   *
   * @param v the vector to add (argument vector will not be changed)
   */
  public void add(SparseVector v) {
    v.getVector().entrySet().stream().forEach(e -> {
      vector.compute(e.getKey(), (key, val) -> {
        if (val == null) {
          return e.getValue();
        } else {
          return val + e.getValue();
        }
      });
    });
  }

  /**
   * Set this vector to the Hadamard/elementwise product of itself and another vector.
   *
   * @param v the vector to multiply against this one (argument vector will not be changed)
   */
  public void multiply(SparseVector v) {
    v.getVector().entrySet().stream()
        .forEach(e -> {
          vector.computeIfPresent(e.getKey(), (key, value) -> value * e.getValue());
        });
  }

  /**
   * Applies an operation to every element of this vector.
   *
   * @param operation a function that takes and outputs Double (Math::sqrt, e.g.)
   */
  public void applyOperation(Function<Double, Double> operation) {
    for (Map.Entry<Integer, Double> e : vector.entrySet()) {
      e.setValue(operation.apply(e.getValue()));
    }
  }

  /**
   * Return the dot product of this vector with another.
   *
   * @param v another vector
   * @return their dot product
   */
  public double dot(SparseVector v) {
    SparseVector small = v.getVector().size() > this.getVector().size() ? this : v;
    SparseVector big = small == v ? this : v;
    double sum = 0;
    for (int i : small.vector.keySet()) {
      sum += small.get(i) * big.get(i);
    }
    return sum;
  }

  public double get(int i) {
    return vector.getOrDefault(i, 0.);
  }

  public double set(int ind, double val) {
    double toReturn = get(ind);
    if (val == 0) {
      vector.remove(ind);
    } else {
      vector.put(ind, val);
    }
    return toReturn;
  }

  @Override
  public String toString() {
    return vector.toString();
  }

}
