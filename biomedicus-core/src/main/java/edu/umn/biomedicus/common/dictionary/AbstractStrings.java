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

import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary.Strings;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public abstract class AbstractStrings implements Strings {
  protected abstract String getTerm(int termIdentifier);

  @Override
  @Nullable
  public String getTerm(StringIdentifier stringIdentifier) {
    if (stringIdentifier.isUnknown()) {
      return null;
    }
    return getTerm(stringIdentifier.value());
  }

  @Override
  public List<String> getTerms(StringsBag stringsBag) {
    ArrayList<String> terms = new ArrayList<>(stringsBag.size());

    for (StringIdentifier stringIdentifier : stringsBag) {
      terms.add(this.getTerm(stringIdentifier));
    }

    return terms;
  }

  @Override
  public List<String> getTerms(StringsVector terms) {
    List<String> strings = new ArrayList<>(terms.size());
    for (StringIdentifier term : terms) {
      strings.add(getTerm(term));
    }
    return strings;
  }
}
