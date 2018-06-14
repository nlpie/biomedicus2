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

package edu.umn.biomedicus.concepts;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.common.dictionary.StringsBag;
import java.util.List;
import javax.annotation.Nullable;

/**
 * An interface for the dictionary of concepts used by the DictionaryConceptRecognizer.
 */
@ProvidedBy(ConceptDictionaryLoader.class)
public interface ConceptDictionary {
  /**
   * Finds the applicable concepts for a given phase in text.
   *
   * @param phrase the phrase in text
   * @return a list of all the concepts that apply to the given text
   */
  @Nullable
  List<ConceptRow> forPhrase(String phrase);

  /**
   * Finds the applicable concepts for a given lowercased phrase in text.
   *
   * @param phrase a lowercased phrase in text
   * @return a list of all the concepts that apply
   */
  @Nullable
  List<ConceptRow> forLowercasePhrase(String phrase);

  /**
   * Finds the applicable concepts for a bag of token norms.
   *
   * @param norms the bag of token norms
   * @return a list of all the concepts that apply
   */
  @Nullable
  List<ConceptRow> forNorms(StringsBag norms);

  /**
   * Returns the full source name for the identifier.
   *
   * @param identifier the source identifier
   * @return String full source name as it appears in the UMLS, or null if it's not found
   */
  @Nullable
  String source(int identifier);
}
