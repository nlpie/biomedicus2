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
 * A base processor of a document. A single document processor is created and injected for each
 * document. Avoid time-consuming work in the constructor for implementations of document processor.
 *
 * <h2>Parallelism</h2>
 * <p>
 *   This class is designed to localize state to the processing of a single document. Because
 *   instances of DocumentProcessor are not shared between Document objects that they process, any
 *   state stored in the class will not affect the processing of other documents. Do not circumvent
 *   this by mutating Singleton injected resource models or using static fields to store data.
 * </p>
 *
 * @since 1.4.0
 */
public interface DocumentProcessor {

  /**
   * Performs the processing.
   *
   * @param document document to process
   */
  void process(@Nonnull Document document) throws BiomedicusException;
}
