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

package edu.umn.biomedicus.uima.adapter;

import com.google.common.base.Preconditions;
import edu.umn.biomedicus.uima.labels.LabelAdapter;
import edu.umn.biomedicus.uima.labels.LabelAdapters;
import edu.umn.biomedicus.uima.labels.UimaLabelIndex;
import edu.umn.biomedicus.uima.labels.UimaLabeler;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import edu.umn.nlpengine.Span;
import edu.umn.nlpengine.LabeledText;
import edu.umn.nlpengine.TextRange;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.uima.cas.CAS;

/**
 * @author Ben Knoll
 * @since 1.6.0
 */
final class CASLabeledText extends LabeledText {

  private final CAS view;

  @Nullable
  private final LabelAdapters labelAdapters;

  private final Map<Class<?>, LabelIndex<?>> labelIndices = new HashMap<>();

  CASLabeledText(CAS view, @Nullable LabelAdapters labelAdapters) {
    this.view = view;
    this.labelAdapters = labelAdapters;
  }


  @Override
  public Reader getReader() {
    InputStream sofaDataStream = view.getSofaDataStream();
    return new BufferedReader(new InputStreamReader(sofaDataStream));
  }

  @Override
  public String getText() {
    return view.getDocumentText();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends TextRange> LabelIndex<T> labelIndex(Class<T> labelClass) {
    Preconditions.checkNotNull(labelAdapters);
    LabelIndex<T> labelIndex = (LabelIndex<T>) labelIndices.get(labelClass);
    if (labelIndex == null) {
      LabelAdapter<T> labelAdapter = labelAdapters.getLabelAdapterFactory(labelClass).create(view);
      labelIndices.put(labelClass, labelIndex = new UimaLabelIndex<>(view, labelAdapter));
    }
    return labelIndex;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends TextRange> Labeler<T> labeler(Class<T> labelClass) {
    Preconditions.checkNotNull(labelAdapters);

    LabelAdapter<T> labelAdapter = labelAdapters.getLabelAdapterFactory(labelClass).create(view);
    return new UimaLabeler<>(labelAdapter);
  }

  @Override
  public Span getDocumentSpan() {
    return new Span(0, getText().length());
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CASLabeledText that = (CASLabeledText) o;

    return view.equals(that.view);
  }

  @Override
  public int hashCode() {
    return view.hashCode();
  }
}
