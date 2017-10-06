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

import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An ordered list of term indices.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
public class TermsVector implements Iterable<TermIdentifier> {

  private final int[] identifiers;

  public TermsVector(int[] identifiers) {
    this.identifiers = identifiers;
  }

  public TermsVector(byte[] bytes) {
    int size = bytes.length / 4;
    ByteBuffer wrap = ByteBuffer.wrap(bytes);
    identifiers = new int[size];
    for (int i = 0; i < size; i++) {
      identifiers[i] = wrap.getInt();
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public int length() {
    return identifiers.length;
  }

  public TermIdentifier get(int index) {
    return new TermIdentifier(identifiers[index]);
  }

  public TermsBag toBag() {
    TermsBag.Builder builder = TermsBag.builder();
    for (int identifier : identifiers) {
      builder.addIdentifier(identifier);
    }

    return builder.build();
  }

  public boolean isPrefix(List<TermIdentifier> termList) {
    Iterator<TermIdentifier> it = termList.iterator();
    for (int identifier : identifiers) {
      if (!it.hasNext()) {
        return false;
      }
      TermIdentifier next = it.next();
      if (next.value() != identifier) {
        return false;
      }
    }
    return true;
  }

  public boolean isPrefix(TermsVector terms) {
    return isPrefix(terms.asIndexedTermList());
  }

  public List<TermIdentifier> asIndexedTermList() {
    return new ListView(this);
  }

  public byte[] getBytes() {
    ByteBuffer byteBuffer = ByteBuffer.allocate(identifiers.length * 4);
    for (int identifier : identifiers) {
      byteBuffer.putInt(identifier);
    }
    return byteBuffer.array();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TermsVector that = (TermsVector) o;

    return Arrays.equals(identifiers, that.identifiers);

  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(identifiers);
  }

  @Override
  public Iterator<TermIdentifier> iterator() {
    return new Iterator<TermIdentifier>() {
      private int index = 0;

      @Override
      public boolean hasNext() {
        return index < identifiers.length;
      }

      @Override
      public TermIdentifier next() {
        if (index >= identifiers.length) {
          throw new NoSuchElementException();
        }
        return get(index++);
      }
    };
  }

  private static class ListView extends AbstractList<TermIdentifier> {

    private final TermsVector backing;

    private ListView(TermsVector backing) {
      this.backing = backing;
    }

    @Override
    public TermIdentifier get(int index) {
      return new TermIdentifier(backing.identifiers[index]);
    }

    @Override
    public int size() {
      return backing.identifiers.length;
    }
  }

  public static class Builder {

    private final ArrayList<Integer> identifiers = new ArrayList<>();

    public void addTerm(TermIdentifier termIdentifier) {
      identifiers.add(termIdentifier.value());
    }

    public void addIdentifier(int identifier) {
      identifiers.add(identifier);
    }

    public TermsVector build() {
      return new TermsVector(identifiers.stream().mapToInt(Integer::intValue).toArray());
    }
  }
}
