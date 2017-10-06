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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;


/**
 * A collection of term (string) to integer mappings.
 */
public interface TermIndex {

  /**
   * Whether or not this index contains the specified string
   *
   * @param string the string to check
   * @return true if this index contains the string, false otherwise
   */
  boolean contains(String string);

  /**
   * Gets the term (string) from the identifier.
   *
   * @param termIdentifier an indexed term identifier
   * @return the string that it represents
   */
  @Nullable
  String getTerm(TermIdentifier termIdentifier);

  /**
   * Gets the indexed term identifier from the full string.
   *
   * @param term the full character sequence of the term
   * @return the identifier for that unique string
   */
  TermIdentifier getIndexedTerm(@Nullable CharSequence term);

  /**
   * Converts a {@link TermsVector} to a list of the strings represented by the IndexedTerms
   *
   * @param terms a termsvector of indexed term identifiers
   * @return the list of the full strings for the terms vector identifiers
   */
  List<String> getTerms(TermsVector terms);

  /**
   * The terms vector for the iterable of Char Sequence terms.
   *
   * @param terms an iterable of terms
   * @return a term vector with identifiers in the order that terms were retrieved from the iterable
   */
  TermsVector getTermVector(Iterable<? extends CharSequence> terms);

  /**
   * A terms bag for an iterable of Char Sequence terms.
   *
   * @param terms an iterable of terms
   * @return a terms bag with the identifiers for the iterable's terms and the number of times they
   * occurred
   */
  TermsBag getTermsBag(Iterable<? extends CharSequence> terms);

  /**
   * Gets the collection strings for the indexed term identifiers in a terms bag, the number of
   * times a term occurs in a bag will be the number of times it occurs in the output collection
   *
   * @param termsBag a bag of terms
   * @return collection of the string expansions for those terms
   */
  Collection<String> getTerms(TermsBag termsBag);

  /**
   * An iterator for all of the term identifiers in this term index.
   *
   * @return iterator of all term identifiers
   */
  Iterator<TermIdentifier> iterator();

  /**
   * A stream of all the term identifiers in this index
   *
   * @return new stream
   */
  Stream<TermIdentifier> stream();

  /**
   * Counts the number of terms in this index. Implementations are not necessarily fast.
   *
   * @return the size of the index, number of term - identifier pairs
   */
  int size();
}
