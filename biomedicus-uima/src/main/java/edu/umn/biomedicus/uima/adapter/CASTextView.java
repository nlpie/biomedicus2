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

package edu.umn.biomedicus.uima.adapter;

import com.google.common.base.Preconditions;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.uima.labels.LabelAdapter;
import edu.umn.biomedicus.uima.labels.LabelAdapters;
import edu.umn.biomedicus.uima.labels.UimaLabelIndex;
import edu.umn.biomedicus.uima.labels.UimaLabeler;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.annotation.Nullable;
import org.apache.uima.cas.CAS;

/**
 * @author Ben Knoll
 * @since 1.6.0
 */
final class CASTextView implements TextView {

  private final CAS view;
  @Nullable
  private final LabelAdapters labelAdapters;

  CASTextView(CAS view, @Nullable LabelAdapters labelAdapters) {
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

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CASTextView that = (CASTextView) o;

    return view.equals(that.view);
  }

  @Override
  public int hashCode() {
    return view.hashCode();
  }

  @Override
  public <T> LabelIndex<T> getLabelIndex(Class<T> labelClass) {
    Preconditions.checkNotNull(labelAdapters);
    LabelAdapter<T> labelAdapter = labelAdapters
        .getLabelAdapterFactory(labelClass).create(view);
    return new UimaLabelIndex<>(view, labelAdapter);
  }

  @Override
  public <T> Labeler<T> getLabeler(Class<T> labelClass) {
    Preconditions.checkNotNull(labelAdapters);
    LabelAdapter<T> labelAdapter = labelAdapters
        .getLabelAdapterFactory(labelClass).create(view);
    return new UimaLabeler<>(labelAdapter);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void label(Label<T> label) {
    Preconditions.checkNotNull(labelAdapters);
    LabelAdapter<?> labelAdapter = labelAdapters
        .getLabelAdapterFactory(label.value().getClass()).create(view);
    new UimaLabeler<>(labelAdapter).label((Label) label);
  }

  @Override
  public Span getDocumentSpan() {
    return new Span(0, getText().length());
  }
}
