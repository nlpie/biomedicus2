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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public abstract class AbstractTermIndex implements TermIndex {

  protected abstract String getTerm(int termIdentifier);

  protected abstract int getIdentifier(@Nullable CharSequence term);

  @Override
  @Nullable
  public String getTerm(IndexedTerm indexedTerm) {
    if (indexedTerm.isUnknown()) {
      return null;
    }
    return getTerm(indexedTerm.termIdentifier());
  }

  @Override
  public IndexedTerm getIndexedTerm(@Nullable CharSequence term) {
    return new IndexedTerm(getIdentifier(term));
  }

  @Override
  public TermsBag getTermsBag(Iterable<? extends CharSequence> terms) {
    TermsBag.Builder builder = TermsBag.builder();
    for (CharSequence term : terms) {
      IndexedTerm indexedTerm = getIndexedTerm(term);
      builder.addTerm(indexedTerm);
    }
    return builder.build();
  }

  @Override
  public List<String> getTerms(TermsBag termsBag) {
    return termsBag.toTerms().stream().map(this::getTerm).collect(Collectors.toList());
  }

  @Override
  public List<String> getTerms(TermsVector terms) {
    List<String> strings = new ArrayList<>(terms.length());
    for (IndexedTerm term : terms) {
      strings.add(getTerm(term));
    }
    return strings;
  }

  @Override
  public TermsVector getTermVector(Iterable<? extends CharSequence> terms) {
    List<Integer> indexList = new ArrayList<>();
    for (CharSequence term : terms) {
      indexList.add(getIdentifier(term));
    }
    int[] arr = new int[indexList.size()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = indexList.get(i);
    }
    return new TermsVector(arr);
  }

  @Override
  public Iterator<IndexedTerm> iterator() {
    return stream().iterator();
  }

  @Override
  public Stream<IndexedTerm> stream() {
    return IntStream.range(0, size()).mapToObj(IndexedTerm::new);
  }

}
