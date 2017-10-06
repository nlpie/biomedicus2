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

package edu.umn.biomedicus.vocabulary;

import edu.umn.biomedicus.common.terms.AbstractTermIndex;
import edu.umn.biomedicus.common.terms.TermIdentifier;
import edu.umn.biomedicus.common.terms.TermIndex;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Term Index implemented using java hash maps.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
public class HashTermIndex extends AbstractTermIndex {

  private final Map<String, Integer> map = new HashMap<>();

  private final String[] terms;

  /**
   * Creates a hash term index from an array of string terms.
   *
   * @param terms the terms to create a term index from.
   */
  public HashTermIndex(String[] terms) {
    this.terms = terms;
    for (int i = 0; i < terms.length; i++) {
      map.put(terms[i], i);
    }
  }

  /**
   * Creates a hash term index by reading in all the strings in a collection.
   *
   * @param collection a collection of strings, each string representing a single term
   */
  public HashTermIndex(Collection<String> collection) {
    int size = collection.size();
    terms = new String[size];
    Iterator<String> iterator = collection.iterator();
    for (int i = 0; i < size; i++) {
      terms[i] = iterator.next();
      map.put(terms[i], i);
    }
  }

  /**
   * Creates a hash term index by reading in all the terms from an existing term index
   *
   * @param termIndex the term index to copy into a hash term index.
   */
  public HashTermIndex(TermIndex termIndex) {
    int size = termIndex.size();
    terms = new String[size];

    int i = 0;
    Iterator<TermIdentifier> iterator = termIndex.iterator();
    while (iterator.hasNext()) {
      TermIdentifier next = iterator.next();
      String string = termIndex.getTerm(next);
      terms[i] = string;
      map.put(string, i++);
    }
  }


  @Override
  public boolean contains(String string) {
    return map.containsKey(string);
  }

  @Override
  public int size() {
    return terms.length;
  }

  @Override
  protected String getTerm(int termIdentifier) {
    return terms[termIdentifier];
  }

  @Override
  protected int getIdentifier(CharSequence term) {
    Integer integer = map.get(term.toString());
    return integer != null ? integer : -1;
  }

  @Override
  public TermIndex inMemory(Boolean inMemory) {
    return inMemory ? new HashTermIndex(this) : this;
  }
}
