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

package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.common.types.text.Token;
import java.util.List;
import java.util.Set;

/**
 * Describes any generic acronym detection and normalization model
 * The essential capabilities of the model are
 * 1) to determine if something is an acronym, and
 * 2) to expand a given acronym Token given its context Tokens
 * Models are generally serializable as well so they can be trained ahead of time
 *
 * @author Greg Finley
 */
interface AcronymModel {

  boolean hasAcronym(Token token);

  String findBestSense(List<Token> allTokens, int forTokenIndex);

  /**
   * For deidentification: remove a single word from the model entirely
   *
   * @param word the word to remove
   */
  void removeWord(String word);

  /**
   * Remove all words except a determined set from the model
   *
   * @param words a set of the words to keep
   */
  void removeWordsExcept(Set<String> words);
}
