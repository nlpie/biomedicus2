/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.tools.newinfo;

/**
 * Represents a line in the token file for the new information writer.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class TokenLine {
    /**
     * The number of the sentence relative to the document.
     */
    private final int sentenceNumber;

    /**
     * The number of the word relative to the document.
     */
    private final int wordNumber;

    /**
     * The text of the word.
     */
    private final String word;

    /**
     * Initializes the token line
     *
     * @param sentenceNumber index of sentence relative to the document
     * @param wordNumber index of the word relative to the document
     * @param word the word
     */
    TokenLine(int sentenceNumber, int wordNumber, String word) {
        this.sentenceNumber = sentenceNumber;
        this.wordNumber = wordNumber;
        this.word = word;
    }

    /**
     * The line for output in the writer.
     *
     * @return token line for output
     */
    String line() {
        return sentenceNumber + "\t" + wordNumber + "\t" + word.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n") + "\n";
    }
}
