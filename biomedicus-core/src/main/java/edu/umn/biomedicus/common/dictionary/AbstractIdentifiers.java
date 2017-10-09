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

import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary.Identifiers;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public abstract class AbstractIdentifiers implements Identifiers {
  protected abstract int getIdentifier(@Nullable CharSequence term);

  @Override
  public StringIdentifier getTermIdentifier(@Nullable CharSequence term) {
    return new StringIdentifier(getIdentifier(term));
  }

  @Override
  public StringsBag getTermsBag(Iterable<? extends CharSequence> terms) {
    StringsBag.Builder builder = StringsBag.builder();
    for (CharSequence term : terms) {
      StringIdentifier stringIdentifier = getTermIdentifier(term);
      builder.addTerm(stringIdentifier);
    }
    return builder.build();
  }

  @Override
  public StringsVector getTermVector(Iterable<? extends CharSequence> terms) {
    List<Integer> indexList = new ArrayList<>();
    for (CharSequence term : terms) {
      indexList.add(getIdentifier(term));
    }
    int[] arr = new int[indexList.size()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = indexList.get(i);
    }
    return new StringsVector(arr);
  }
}
