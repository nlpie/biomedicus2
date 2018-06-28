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

import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * A word and part of speech.
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
public class WordPos implements Comparable<WordPos>, Serializable {

  /**
   * The serial UID.
   */
  private static final long serialVersionUID = 996488464101266546L;

  /**
   * The word.
   */
  private String word;

  /**
   * The part of speech.
   */
  private PartOfSpeech tag;

  public WordPos() {

  }

  /**
   * Default constructor. Takes the word and part of speech tag that this
   *
   * @param word the word, must not be null
   * @param tag the part of speech tag, must not be null
   */
  public WordPos(String word, PartOfSpeech tag) {
    this.word = Objects.requireNonNull(word);
    this.tag = Objects.requireNonNull(tag);
  }

  /**
   * Moves the word forward one letter. This is used in suffix iteration.
   *
   * @return a new word pos with the word of substring(1)
   */
  public WordPos forward() {
    return new WordPos(word.substring(1), tag);
  }

  /**
   * Gets the word component of this tuple.
   *
   * @return word stored in this tuple
   */
  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }

  /**
   * Gets the tag component of this tuple
   *
   * @return tag component of the tuple
   */
  public PartOfSpeech getTag() {
    return tag;
  }

  public void setTag(PartOfSpeech tag) {
    this.tag = tag;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    WordPos that = (WordPos) o;

    if (!word.equals(that.word)) {
      return false;
    }
    return tag == that.tag;

  }

  @Override
  public int hashCode() {
    int result = word.hashCode();
    result = 31 * result + tag.hashCode();
    return result;
  }

  @Override
  public int compareTo(WordPos o) {
    int result = word.compareTo(o.word);
    if (result == 0) {
      result = tag.compareTo(o.tag);
    }
    return result;
  }

  @Override
  public String toString() {
    return "WordPos{"
        + "word='" + word + '\''
        + ", tag=" + tag.toString()
        + '}';
  }
}
