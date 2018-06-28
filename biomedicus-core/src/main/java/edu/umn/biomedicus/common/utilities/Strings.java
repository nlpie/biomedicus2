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

package edu.umn.biomedicus.common.utilities;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility class for working with strings.
 *
 * @since 1.2.0
 */
public final class Strings {

  /**
   * Private constructor to prevent instantiation of a utility class.
   */
  private Strings() {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a stream which generates all suffixes of a word up to a maximum suffix length in order
   * of decreasing size including a final empty string. The suffixes of "computer" of max length 4
   * would be "uter" "ter" "er" "r" "" in that order.
   *
   * @param string string to break apart into suffixes.
   * @param maxSuffixLength the number of characters from the end that should be broken into
   * suffixes.
   * @return a stream of the suffixes of the string.
   */
  public static Stream<String> generateSuffixes(String string, int maxSuffixLength) {
    int stringLength = string.length();
    int suffixes = Math.min(stringLength, maxSuffixLength);
    return IntStream.rangeClosed(stringLength - suffixes, stringLength).mapToObj(string::substring);
  }

  /**
   * Creates a stream which generates the suffixes of a word in order of decreasing size including a
   * final empty string. The suffixes of "computer" would be
   * "computer" "omputer" "mputer" ... "r" "".
   *
   * @param string string to break apart into suffixes.
   * @return a stream of the suffixes of the stream.
   */
  public static Stream<String> generateSuffixes(String string) {
    return IntStream.rangeClosed(0, string.length()).mapToObj(string::substring);
  }
}
