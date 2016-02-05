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

package edu.umn.biomedicus.processing;

import edu.umn.biomedicus.model.text.Document;

/**
 * Interface responsible for breaking a document or string into word tokens.
 *
 * @since 1.1.0
 */
public interface Tokenizer {
    /**
     * Takes the text from a biomedicus {@link Document} and finds word tokens.
     *
     * @param document the document to tokenize
     */
    void tokenize(Document document);
}
