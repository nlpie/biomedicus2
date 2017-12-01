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

import edu.umn.nlpengine.Label;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import edu.umn.nlpengine.Span;
import java.io.Reader;

/**
 * A biomedicus basic unit for document text.
 *
 * @since 1.6.0
 */
public interface TextView {

  /**
   * Returns a reader for the document text
   *
   * @return a java reader for the document text
   */
  Reader getReader();

  /**
   * Gets the entire text of the document
   *
   * @return document text
   */
  String getText();

  /**
   * Returns the label index for the specific label class.
   *
   * @param labelClass the labelable class instance
   * @param <T> the type of the labelable class
   * @return label index for the labelable type
   */
  <T extends Label> LabelIndex<T> getLabelIndex(Class<T> labelClass);

  /**
   * Returns a labeler for the specific label class.
   *
   * @param labelClass the labelable class instance
   * @param <T> the type of the labelable class
   * @return labeler for the labelable type
   */
  <T extends Label> Labeler<T> getLabeler(Class<T> labelClass);

  /**
   * Returns the {@link Span} of the entire document.
   *
   * @return the Span of the entire document.
   */
  Span getDocumentSpan();

  /**
   * A builder for a new text view.
   */
  interface Builder {

    /**
     * Sets the text of the text view.
     *
     * @param text the text of the text view
     * @return this builder
     */
    Builder withText(String text);

    /**
     * Sets the name of the text view.
     *
     * @param name the name identifier of the text view.
     * @return this builder
     */
    Builder withName(String name);

    /**
     * Finalizes and builds the new text view.
     *
     * @return the finished text view.
     */
    TextView build();
  }
}
