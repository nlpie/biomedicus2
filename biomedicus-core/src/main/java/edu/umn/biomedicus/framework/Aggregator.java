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

import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.nlpengine.Document;
import javax.annotation.Nonnull;

/**
 * A class that processes multiple documents and maintains state between them.
 *
 * @since 1.7.0
 */
public interface Aggregator {

  /**
   * Called to add documents to the aggregator.
   *
   * @param document the document data
   * @throws BiomedicusException in case of any exception during processing that prevents the
   * completion of the document.
   */
  void addDocument(@Nonnull Document document) throws BiomedicusException;

  /**
   * Called after the completion of all documents
   *
   * @throws BiomedicusException in case of any exception that prevents the aggregator from
   * completing its work.
   */
  void done() throws BiomedicusException;
}
