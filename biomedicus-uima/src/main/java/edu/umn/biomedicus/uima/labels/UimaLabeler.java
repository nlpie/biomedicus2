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

package edu.umn.biomedicus.uima.labels;

import com.google.inject.Inject;
import edu.umn.nlpengine.TextRange;
import edu.umn.nlpengine.Labeler;
import org.jetbrains.annotations.NotNull;

public final class UimaLabeler<T extends TextRange> implements Labeler<T> {

  private final LabelAdapter<T> labelAdapter;

  @Inject
  public UimaLabeler(LabelAdapter<T> labelAdapter) {
    this.labelAdapter = labelAdapter;
  }

  @Override
  public void add(@NotNull T label) {
    labelAdapter.labelToAnnotation(label);
  }

  @Override
  public void addAll(@NotNull Iterable<? extends T> elements) {
    elements.forEach(this::add);
  }
}
