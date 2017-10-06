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

package edu.umn.biomedicus.normalization;

import edu.umn.biomedicus.common.terms.TermIdentifier;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import java.io.Serializable;
import org.jetbrains.annotations.NotNull;

/**
 * A storage / hash map key object that is a tuple of a term and a
 * part of speech.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
final class TermPos implements Serializable, Comparable<TermPos> {

  private final int indexedTerm;

  private final PartOfSpeech partOfSpeech;

  TermPos(TermIdentifier termIdentifier,
      PartOfSpeech partOfSpeech) {
    this.indexedTerm = termIdentifier.value();
    this.partOfSpeech = partOfSpeech;
  }

  TermIdentifier getIndexedTerm() {
    return new TermIdentifier(indexedTerm);
  }

  PartOfSpeech getPartOfSpeech() {
    return partOfSpeech;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TermPos termPos = (TermPos) o;

    if (indexedTerm != termPos.indexedTerm) {
      return false;
    }
    return partOfSpeech == termPos.partOfSpeech;
  }

  @Override
  public int hashCode() {
    int result = indexedTerm;
    result = 31 * result + partOfSpeech.hashCode();
    return result;
  }

  @Override
  public int compareTo(@NotNull TermPos o) {
    int compare = Integer.compare(indexedTerm, o.indexedTerm);
    if (compare != 0) {
      return compare;
    }
    return partOfSpeech.compareTo(o.partOfSpeech);
  }
}
