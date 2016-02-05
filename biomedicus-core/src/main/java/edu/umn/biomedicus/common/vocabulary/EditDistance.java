/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.common.vocabulary;

/**
 * Class for computing the edit distance between two words.
 *
 * @author Arun Kumar
 * @author Ben Knoll
 * @since 1.3.0
 */
public class EditDistance {
    private final Costs costs;

    private final String firstWord;

    private final String secondWord;

    public EditDistance(Costs costs, String firstWord, String secondWord) {
        this.costs = costs;
        this.firstWord = firstWord;
        this.secondWord = secondWord;
    }

    /**
     * Computes the edit distance using a DP method, since we're not returning the traceback of operations, we don't
     * need to store anything more than two rows at a time.
     *
     * @return edit distance for transforming first word into the second.
     */
    public int compute() {
        int firstLength = firstWord.length();
        int secondLength = secondWord.length();

        if (firstLength == 0) return secondLength * costs.getInsert();
        if (secondLength == 0) return firstLength * costs.getInsert();

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
                boolean isMatch = firstWord.charAt(row) == secondWord.charAt(column);
                int matchOrReplace = (isMatch ? costs.getMatch() : costs.getReplace()) + lastRow[column - 1];
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