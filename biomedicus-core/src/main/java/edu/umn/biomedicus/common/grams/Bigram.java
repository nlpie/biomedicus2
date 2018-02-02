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

/**
 * Two objects of the same type in a sequence.
 *
 * @since 1.3.0
 */
public interface Bigram<T> extends Iterable<T> {

  /**
   * Returns the first of the two objects in the bigram.
   *
   * @return the first object.
   */
  T getFirst();

  /**
   * Returns the second of the two objects in the bigram.
   *
   * @return the second object.
   */
  T getSecond();
}
