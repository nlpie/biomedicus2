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
import edu.umn.nlpengine.TextRange;
import edu.umn.nlpengine.Span;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Search results from TagEx.
 *
 * @since 1.6.0
 */
public interface Searcher extends TextRange {

  /**
   * Returns the named label if it matched against anything.
   *
   * @param name the variable name assigned to the named label.
   * @return an optional containing the label matched against, or else empty if nothing matched.
   */
  @Nullable
  Label getLabel(@Nonnull String name);

  /**
   * Gets the span of any named group or label.
   *
   * @param name the variable name assigned to the group or label
   * @return an optional containing either the name group or label's span, or else empty if the
   * named group or label did not match anything
   */
  @Nullable
  Span getSpan(@Nonnull String name);

  /**
   * True after a search or match if the pattern was matched or found, false otherwise.
   */
  boolean found();

  /**
   * Performs the search on the entire document, continuing from the last match if there was one.
   *
   * @return true if the search found a match in the document, false otherwise.
   */
  boolean search();

  /**
   * Performs the search on a specific section of the document, continuing from the last match if
   * there was one.
   *
   * @param begin the begin index of the section of the document to search
   * @param end the end index of the section of the document to search
   * @return true if it found a match, false otherwise
   */
  boolean search(int begin, int end);

  /**
   * Performs the search on a specific section of the document, continuing from the last match if
   * there was one.
   *
   * @param span span to search for the pattern in it.
   * @return true if it found a match, false otherwise.
   */
  boolean search(Span span);

  /**
   * Performs the search on a specific section of the document, continuing from the last match if
   * there was one.
   *
   * @param textRange the range of the document to search
   * @return true if it found a match, false otherwise
   */
  default boolean search(TextRange textRange) {
    return search(textRange.getStartIndex(), textRange.getEndIndex());
  }

  /**
   * Attempts to the document against the pattern, returning true if it is a match, false
   * otherwise.
   *
   * @return true if the document matches, false otherwise.
   */
  boolean match();

  /**
   * Attempts to match a span in a text view against the pattern.
   *
   * @param begin the begin of the span
   * @param end the end of the span
   * @return true if there was a match, false otherwise
   */
  boolean match(int begin, int end);

  /**
   * Attempts to match a span in a text view against the pattern.
   *
   * @param textRange the span of the document to check
   * @return true if there was a match, false otherwise
   */
  default boolean match(TextRange textRange) {
    return match(textRange.getStartIndex(), textRange.getEndIndex());
  }

  /**
   * Attempts to match a given span for the pattern.
   *
   * @param span the span of text and labels to attempt to match against
   * @return true if there was a match, false otherwise
   */
  boolean match(Span span);

  /**
   * An optional of the span matched by the entire pattern, if there is no match this span will be
   * empty.
   *
   * @return an optional, empty if no match found, otherwise containing a span for the match.
   */
  Optional<Span> getSpan();

  /**
   * Returns a collection of the group names defined in the original pattern.
   */
  Collection<String> getGroupNames();

  /**
   * The beginning index of the span matched by the entire pattern.
   */
  int getBegin();

  /**
   * The end index of the span matched by the entire pattern.
   */
  int getEnd();

  /**
   * Converts this searcher into a {@link SearchResult}
   * @return search result object, or null if this is not a match
   */
  @Nullable
  SearchResult toSearchResult();
}
