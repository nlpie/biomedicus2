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

package edu.umn.biomedicus.normalization;

import edu.umn.biomedicus.common.dictionary.StringIdentifier;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import java.nio.ByteBuffer;
import javax.annotation.Nonnull;

/**
 * A storage / hash map key object that is a tuple of a term and a part of speech.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
final class TermPos implements Comparable<TermPos> {

  private static final int BYTES = Integer.BYTES * 2;

  private final int indexedTerm;

  private final PartOfSpeech partOfSpeech;

  TermPos(StringIdentifier termIdentifier,
      PartOfSpeech partOfSpeech) {
    this.indexedTerm = termIdentifier.value();
    this.partOfSpeech = partOfSpeech;
  }

  TermPos(byte[] bytes) {
    ByteBuffer wrap = ByteBuffer.wrap(bytes);
    indexedTerm = wrap.getInt();
    partOfSpeech = PartOfSpeech.values()[wrap.getInt()];
  }

  StringIdentifier getIndexedTerm() {
    return new StringIdentifier(indexedTerm);
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
  public int compareTo(@Nonnull TermPos o) {
    int compare = Integer.compare(indexedTerm, o.indexedTerm);
    if (compare != 0) {
      return compare;
    }
    return partOfSpeech.compareTo(o.partOfSpeech);
  }

  byte[] getBytes() {
    return ByteBuffer.allocate(BYTES).putInt(indexedTerm).putInt(partOfSpeech.ordinal()).array();
  }
}
