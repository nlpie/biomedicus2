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

package edu.umn.biomedicus.common.types.syntax;

public enum UniversalPartOfSpeech {
  /**
   * Adjective
   */
  ADJ,
  /**
   * Adverb
   */
  ADV,
  /**
   * Interjection
   */
  INTJ,
  /**
   * Noun
   */
  NOUN,
  /**
   * Proper Noun
   */
  PROPN,
  /**
   * Verb
   */
  VERB,
  /**
   * Adposition (prepositions, postpositions in other languages)
   */
  ADP,
  /**
   * Auxiliary verb
   */
  AUX,
  /**
   * Coordinating conjunction
   */
  CONJ,
  /**
   * Determiner
   */
  DET,
  /**
   * Numeral
   */
  NUM,
  /**
   * Particle
   */
  PART,
  /**
   * Pronoun
   */
  PRON,
  /**
   * Subordinating conjunction
   */
  SCONJ,
  /**
   * Punctuation
   */
  PUNCT,
  /**
   * Symbol
   */
  SYM,
  /**
   * Other / unknown
   */
  X
}
