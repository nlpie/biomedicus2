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

package edu.umn.biomedicus.common.collect;

/**
 * Class for computing the edit distance between two words.
 *
 * @author Arun Kumar
 * @author Ben Knoll
 * @since 1.3.0
 */
public class StandardEditDistance<T extends CharSequence> implements Metric<T> {

  private final Costs costs;

  public StandardEditDistance(Costs costs) {
    this.costs = costs;
  }

  public static <T extends CharSequence> StandardEditDistance<T> levenstein() {
    return new StandardEditDistance<>(Costs.LEVENSHTEIN);
  }

  @Override
  public int compute(T first, T second) {
    int firstLength = first.length();
    int secondLength = second.length();

    if (firstLength == 0) {
      return secondLength * costs.getInsert();
    }
    if (secondLength == 0) {
      return firstLength * costs.getInsert();
    }

    int[] lastRow = new int[secondLength + 1];
    int[] thisRow = new int[secondLength + 1];

    for (int column = 0; column <= secondLength; column++) {
      lastRow[column] = column * costs.getDelete();
    }

    for (int row = 1; row <= firstLength; row++) {
      // last row is index row - 1, this row is index row
      int column = 0;
      thisRow[column] = row * costs.getDelete();

      for (column = 1; column <= secondLength; column++) {
        boolean isMatch = first.charAt(row - 1) == second.charAt(column - 1);
        int matchOrReplace =
            (isMatch ? costs.getMatch() : costs.getReplace()) + lastRow[column - 1];
        int insert = costs.getInsert() + thisRow[column - 1];
        int delete = costs.getDelete() + lastRow[column];
        thisRow[column] = Math.min(Math.min(matchOrReplace, insert), delete);
      }

      // swap so we can recycle old lastRow
      int[] swap = lastRow;
      lastRow = thisRow;
      thisRow = swap;
    }

    return lastRow[secondLength];
  }
}