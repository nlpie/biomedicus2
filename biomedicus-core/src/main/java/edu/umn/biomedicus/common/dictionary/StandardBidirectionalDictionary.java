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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

public final class StandardBidirectionalDictionary implements BidirectionalDictionary {
  private final Identifiers identifiers;

  private final Strings strings;

  public StandardBidirectionalDictionary(Identifiers identifiers, Strings strings) {
    this.identifiers = identifiers;
    this.strings = strings;
  }

  @Override
  public Strings getStrings() {
    return strings;
  }

  @Override
  public Identifiers getIdentifiers() {
    return identifiers;
  }

  @Nullable
  @Override
  public String getTerm(StringIdentifier stringIdentifier) {
    return strings.getTerm(stringIdentifier);
  }

  @Override
  public StringIdentifier getTermIdentifier(@Nullable CharSequence term) {
    return identifiers.getTermIdentifier(term);
  }

  @Override
  public List<String> getTerms(StringsVector terms) {
    return strings.getTerms(terms);
  }

  @Override
  public Collection<String> getTerms(StringsBag stringsBag) {
    return strings.getTerms(stringsBag);
  }

  @Override
  public boolean contains(String string) {
    return identifiers.contains(string);
  }

  @Override
  public StringsVector getTermVector(Iterable<? extends CharSequence> terms) {
    return identifiers.getTermVector(terms);
  }

  @Override
  public StringsBag getTermsBag(Iterable<? extends CharSequence> terms) {
    return identifiers.getTermsBag(terms);
  }

  @Override
  public int size() {
    return identifiers.size();
  }

  public StandardBidirectionalDictionary inMemory(boolean inMemory) {
    if (inMemory) {
      int size = identifiers.size();
      MappingIterator mappingIterator = identifiers.mappingIterator();
      HashIdentifiers hashIdentifiers = new HashIdentifiers(size);
      String[] strings = new String[size];
      while (mappingIterator.isValid()) {
        int identifier = mappingIterator.identifier();
        String string = mappingIterator.string();
        hashIdentifiers.addMapping(string, identifier);
        strings[identifier] = string;
        mappingIterator.next();
      }

      return new StandardBidirectionalDictionary(hashIdentifiers, new ArrayStrings(strings));
    }
    return this;
  }

  @Override
  public void close() throws IOException {
    identifiers.close();
    strings.close();
  }
}
