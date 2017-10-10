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

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Interface for the mapping of strings expansions to the sparse vector of their contexts.
 *
 * @since 1.8.0
 */
public interface SenseVectors extends Closeable {

  /**
   * Checks if the sense has an associated sparse vector.
   *
   * @param sense String sense to check
   * @return true if this contains the sense, false if it does not
   */
  boolean containsSense(String sense);

  /**
   * Retrieves the context vector for the given sense.
   *
   * @param sense the sense to retrieve
   * @return a sparse vector for the sense's context, or null if this does not contain the sense.
   */
  @Nullable
  SparseVector get(@Nullable String sense);

  /**
   * Returns a set of all the senses in this map.
   *
   * @return immutable set view of all the senses
   */
  Set<String> senses();

  /**
   * Returns a collection of all the context vectors in this map.
   *
   * @return immutable collection view of all the vectors.
   */
  Collection<SparseVector> vectors();

  /**
   * Removes the given word from this sense vectors object. May not be supported in certain
   * implementations
   *
   * @param index the index/identifier of the word to remove from each context
   */
  void removeWord(int index);

  /**
   * Removes a collection of words from this sense vectors object. May not be supported in certain
   * implementations.
   *
   * @param indexes the indexes/identifiers of the words to remove from each context
   */
  void removeWords(Collection<Integer> indexes);

  /**
   * Returns the total number of sense -> context mappings in this object.
   *
   * @return integer count of the number of mappings.
   */
  int size();
}
