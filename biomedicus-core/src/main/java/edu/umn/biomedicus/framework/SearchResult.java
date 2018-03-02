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

package edu.umn.biomedicus.framework;

import edu.umn.nlpengine.Label;
import edu.umn.nlpengine.Span;

/**
 * An immutable result of a search expression.
 *
 */
public interface SearchResult {
  /**
   * Returns the named label if it matched against anything.
   *
   * @param name the variable name assigned to the named label.
   * @return an optional containing the label matched against, or else empty if nothing matched.
   */
  Label getLabel(String name);

  /**
   * Gets the span of any named group or label.
   *
   * @param name the variable name assigned to the group or label
   * @return an optional containing either the name group or label's span, or else empty if the
   * named group or label did not match anything
   */
  Span getSpan(String name);

  /**
   * True after a search or match if the pattern was matched or found, false otherwise.
   */
  boolean found();

  /**
   * The beginning index of the span matched by the entire pattern.
   */
  int getBegin();

  /**
   * The end index of the span matched by the entire pattern.
   */
  int getEnd();
}
