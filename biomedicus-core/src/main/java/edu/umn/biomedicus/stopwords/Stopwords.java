/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.stopwords;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Token;

/**
 * Represents stop words in text, words that should be ignored before processing because they are too common or for some
 * other reason. It has methods for marking which tokens are stop words either on an individual case by token or by
 * looking at all tokens in a document.
 */
public interface Stopwords {
    /**
     * Checks and marks if a token is a stopword
     * @param token the token to mark whether is a stopword
     */
    void checkIfStopword(Token token);

    /**
     * Checks and marks all of the tokens in a document whether they are stop words.
     * @param document the document to iterate through all tokens and check whether are stopwords.
     */
    void annotateStopwords(Document document);
}
