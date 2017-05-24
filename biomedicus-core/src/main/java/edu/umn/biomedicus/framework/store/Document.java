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

package edu.umn.biomedicus.framework.store;

import java.util.Map;
import java.util.Optional;

/**
 * A single processing artifact and all the data that it contains
 *
 * @since 1.6.0
 */
public interface Document {

  /**
   * Returns the document identifier.
   *
   * @return string document identifier
   */
  String getDocumentId();

  /**
   * Returns an Optional of the metadata with the specified key.
   *
   * @param key the string identifier key for the metadata value
   * @return an optional of the value associated with the key, it will be empty if there is no value
   * associated with the specified key
   */
  Optional<String> getMetadata(String key);

  /**
   * Returns a map of all the metadata for this document.
   *
   * @return unmodifiable map of all the metadata for this document.
   */
  Map<String, String> getAllMetadata();

  /**
   * Adds the following metadata to the document.
   *
   * @param key the key for the metadata
   * @param value the value for the metadata.
   */
  void putMetadata(String key, String value);

  /**
   * Puts all the entries from the map into the document
   *
   * @param metadata a metadata map
   */
  void putAllMetadata(Map<String, String> metadata);

  /**
   * Gets an optional of the text view specified by the name.
   *
   * @param name the name identifier of the text view
   * @return an optional of the text view, it will be empty if there is no text view associated for
   * the specific text view.
   */
  Optional<TextView> getTextView(String name);

  /**
   * Returns a builder for a new text view.
   *
   * @return builder for a new text view
   */
  TextView.Builder newTextView();
}
