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

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;


/**
 * A collection of term (string) to integer mappings in both directions.
 */
public interface BidirectionalDictionary extends Closeable {

  /**
   * Returns the mapping from identifiers to strings.
   *
   * @return object which maps from identifiers to strings.
   */
  Strings getStrings();

  /**
   * Returns the mappings from strings to identifiers.
   *
   * @return object which maps from strings to identifiers.
   */
  Identifiers getIdentifiers();

  /**
   * Attempts to retrieve the term associated with the term identifier.
   *
   * @param stringIdentifier the term identifier for the term
   * @return the string of the term or if not present null
   */
  @Nullable
  String getTerm(StringIdentifier stringIdentifier);

  /**
   * Gets the indexed term identifier from the full string.
   *
   * @param term the full character sequence of the term
   * @return the identifier for that unique string
   */
  StringIdentifier getTermIdentifier(@Nullable CharSequence term);

  /**
   * Converts a {@link StringsVector} to a list of the strings represented by the IndexedTerms
   *
   * @param terms a termsvector of indexed term identifiers
   * @return the list of the full strings for the terms vector identifiers
   */
  List<String> getTerms(StringsVector terms);

  /**
   * Gets the collection strings for the indexed term identifiers in a terms bag, the number of
   * times a term occurs in a bag will be the number of times it occurs in the output collection
   *
   * @param stringsBag a bag of terms
   * @return collection of the string expansions for those terms
   */
  Collection<String> getTerms(StringsBag stringsBag);

  /**
   * Whether or not this index contains the specified string
   *
   * @param string the string to check
   * @return true if this index contains the string, false otherwise
   */
  boolean contains(String string);

  /**
   * The terms vector for the iterable of Char Sequence terms.
   *
   * @param terms an iterable of terms
   * @return a term vector with identifiers in the order that terms were retrieved from the
   * iterable
   */
  StringsVector getTermVector(Iterable<? extends CharSequence> terms);

  /**
   * A terms bag for an iterable of Char Sequence terms.
   *
   * @param terms an iterable of terms
   * @return a terms bag with the identifiers for the iterable's terms and the number of times
   * they occurred
   */
  StringsBag getTermsBag(Iterable<? extends CharSequence> terms);

  /**
   * The total number of string to identifier (and vice versa) mappings in this dictionary.
   *
   * @return the size of this dictionary
   */
  int size();

  /**
   * A mapping from identifiers to strings.
   */
  interface Strings extends Closeable {

    /**
     * Attempts to retrieve the term associated with the term identifier.
     *
     * @param stringIdentifier the term identifier for the term
     * @return the string of the term or if not present null
     */
    @Nullable
    String getTerm(StringIdentifier stringIdentifier);

    /**
     * Converts a {@link StringsVector} to a list of the strings represented by the IndexedTerms
     *
     * @param terms a termsvector of indexed term identifiers
     * @return the list of the full strings for the terms vector identifiers
     */
    List<String> getTerms(StringsVector terms);

    /**
     * Gets the collection strings for the indexed term identifiers in a terms bag, the number of
     * times a term occurs in a bag will be the number of times it occurs in the output collection
     *
     * @param stringsBag a bag of terms
     * @return collection of the string expansions for those terms
     */
    Collection<String> getTerms(StringsBag stringsBag);

    /**
     * Returns a mapping iterator of all the identifiers to strings mapping stored in this object.
     *
     * @return newly created mapping iterator
     */
    MappingIterator mappingIterator();

    /**
     * Returns the number of mappings.
     *
     * @return count of the number of mappings
     */
    int size();
  }

  /**
   * A mapping from strings to identifiers.
   */
  interface Identifiers extends Closeable {
    /**
     * Whether or not this index contains the specified string
     *
     * @param string the string to check
     * @return true if this index contains the string, false otherwise
     */
    boolean contains(@Nullable String string);

    /**
     * Gets the indexed term identifier from the full string.
     *
     * @param term the full character sequence of the term
     * @return the identifier for that unique string
     */
    StringIdentifier getTermIdentifier(@Nullable CharSequence term);

    /**
     * The terms vector for the iterable of Char Sequence terms.
     *
     * @param terms an iterable of terms
     * @return a term vector with identifiers in the order that terms were retrieved from the
     * iterable
     */
    StringsVector getTermVector(Iterable<? extends CharSequence> terms);

    /**
     * A terms bag for an iterable of Char Sequence terms.
     *
     * @param terms an iterable of terms
     * @return a terms bag with the identifiers for the iterable's terms and the number of times
     * they occurred
     */
    StringsBag getTermsBag(Iterable<? extends CharSequence> terms);

    /**
     * Returns a mapping iterator of all the strings to identifiers mapping stored in this object.
     *
     * @return newly created mapping iterator
     */
    MappingIterator mappingIterator();

    /**
     * Returns the number of mappings.
     *
     * @return count of the number of mappings
     */
    int size();
  }
}
