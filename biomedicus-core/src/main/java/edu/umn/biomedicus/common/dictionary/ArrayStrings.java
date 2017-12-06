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

public final class ArrayStrings extends AbstractStrings {
  private final String[] strings;

  ArrayStrings(String[] strings) {
    this.strings = strings;
  }

  @Override
  protected String getTerm(int termIdentifier) {
    return strings[termIdentifier];
  }

  @Override
  public MappingIterator mappingIterator() {
    return new MappingIterator() {
      int index = 0;

      @Override
      public boolean isValid() {
        return index < strings.length;
      }

      @Override
      public int identifier() {
        return index;
      }

      @Override
      public String string() {
        return strings[index];
      }

      @Override
      public void next() {
        index++;
      }

      @Override
      public void close() throws IOException {

      }
    };
  }

  @Override
  public int size() {
    return strings.length;
  }

  @Override
  public void close() throws IOException {

  }
}
