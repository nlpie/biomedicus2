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

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.TextView;

import javax.annotation.Nonnull;

/**
 * A document processor which hides the implementation of retrieving a specific view. Useful for
 * document processors that do not need to access multiple views or document metadata.
 *
 * @since 1.8.0
 */
public abstract class TextViewProcessor implements DocumentProcessor {

  @Nonnull
  private String viewName = StandardViews.SYSTEM;

  private Document document;

  @Inject(optional = true)
  public void setViewName(@ProcessorSetting("viewName") String viewName) {
    this.viewName = viewName;
  }

  @Override
  public final void process(@Nonnull Document document) throws BiomedicusException {
    this.document = document;
    process(this.document.getTextView(viewName)
        .orElseThrow(() -> new BiomedicusException("Could not locate view" + viewName)));
  }

  /**
   * To be implemented by subclass. Performs the necessary processing.
   */
  protected abstract void process(TextView textView) throws BiomedicusException;

  protected Document getDocument() {
    return this.document;
  }
}
