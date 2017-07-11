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

package edu.umn.biomedicus.framework;

import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.store.Document;

/**
 * A source for new documents.
 *
 * @since 1.7.0
 */
public interface DocumentSource {

  /**
   * Whether the document source has another document.
   *
   * @return true if we have another document, false otherwise
   */
  boolean hasNext();

  /**
   * Calls on the document source to next the document.
   *
   * @param factory a factory that can be used to create documents.
   */
  Document next(DocumentBuilder factory) throws BiomedicusException;

  /**
   * Returns an estimate of the total number of documents to be created by this document source.
   *
   * @return long estimate of total number of documents.
   */
  long estimateTotal();
}
