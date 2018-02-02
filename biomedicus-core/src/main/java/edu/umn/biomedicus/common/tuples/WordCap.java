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

package edu.umn.biomedicus.common.tuples;

import java.io.Serializable;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * Represents a pair of word and isCapitalized.
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
public class WordCap implements Comparable<WordCap>, Serializable {

  /**
   * Serialization UID.
   */
  private static final long serialVersionUID = -5981094615088473604L;

  /**
   * Pattern which matches digits.
   */
  private static final Pattern DIGITS = Pattern.compile("\\d");

  /**
   * The word in the word-capitalization.
   */
  private final String word;

  /**
   * Whether the word is capitalized.
   */
  private final boolean isCapitalized;

  /**
   * Default constructor. Takes a word and an isCapitalized.
   *
   * @param word what the word is
   * @param isCapitalized whether the word is capitalized.
   */
  public WordCap(String word, boolean isCapitalized) {
    this.word = word;
    this.isCapitalized = isCapitalized;
  }

  /**
   * Returns the word component of this tuple
   *
   * @return the word
   */
  public String getWord() {
    return word;
  }

  /**
   * Returns the isCapitalized component of this tuple
   *
   * @return true if it is capitalized, false otherwise
   */
  public boolean isCapitalized() {
    return isCapitalized;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    WordCap wordCap = (WordCap) o;

    if (isCapitalized != wordCap.isCapitalized) {
      return false;
    }
    return word.equals(wordCap.word);

  }

  @Override
  public int hashCode() {
    int result = word.hashCode();
    result = 31 * result + (isCapitalized ? 1 : 0);
    return result;
  }


  @Override
  public int compareTo(WordCap o) {
    int result = this.word.compareTo(o.word);
    if (result == 0) {
      result = Boolean.compare(this.isCapitalized, o.isCapitalized);
    }
    return result;
  }

  @Override
  public String toString() {
    return "WordCap{"
        + "word='" + word + '\''
        + ", isCapitalized=" + isCapitalized
        + '}';
  }
}
