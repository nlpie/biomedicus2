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

package edu.umn.biomedicus.vocabulary;

import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.exc.BiomedicusException;
import java.io.Closeable;

/**
 * Provides the term indexes for the vocabulary.
 *
 * @since 1.6.0
 */
abstract class VocabularyStore implements Closeable {

  /**
   * Opens the vocabulary store
   */
  abstract void open();

  /**
   * Gets the term index of words.
   *
   * @return the word index
   */
  abstract TermIndex getWords();

  /**
   * Gets the term index of terms (one or more words with a single semantic meaning).
   *
   * @return the terms
   */
  abstract TermIndex getTerms();

  /**
   * Gets the term index of norms (normalized forms of words)
   *
   * @return term index of norms
   */
  abstract TermIndex getNorms();
}
