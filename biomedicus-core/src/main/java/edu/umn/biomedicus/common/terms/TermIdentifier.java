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

import javax.annotation.Nullable;

/**
 * An identifier for a unique string. Is associated with the specific vocabulary / term index that
 * it is retrieved from.
 */
public final class TermIdentifier implements Comparable<TermIdentifier> {

  private final int value;

  /**
   * Initializes a new indexed term with the given identifier
   *
   * @param value the identifier for a string
   */
  public TermIdentifier(int value) {
    this.value = value;
  }

  /**
   * Returns the identifier for unknown strings: -1.
   *
   * @return the identifier used for unknown strings.
   */
  public static TermIdentifier unknown() {
    return new TermIdentifier(-1);
  }

  /**
   * The integer term identifier for this indexed term.
   *
   * @return integer index / identifier
   */
  public int value() {
    return value;
  }

  /**
   * Whether or not this term is known in a specific vocabulary.
   *
   * @return true if the term, false if it is not known
   */
  public boolean isUnknown() {
    return value == -1;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TermIdentifier that = (TermIdentifier) o;

    return value == that.value;
  }

  @Override
  public int hashCode() {
    return value;
  }

  @Override
  public int compareTo(TermIdentifier o) {
    return Integer.compare(value, o.value);
  }
}
