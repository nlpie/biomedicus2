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

package edu.umn.biomedicus.uima.labels;

import com.google.inject.Inject;
import edu.umn.biomedicus.framework.store.AbstractLabelIndex;
import edu.umn.biomedicus.framework.store.DefaultLabelIndex;
import edu.umn.biomedicus.framework.store.ImmutableDistinctSpanMap;
import edu.umn.biomedicus.framework.store.ImmutableSpanMap;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.OrderedSpanMap;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.SpansMap;
import edu.umn.biomedicus.framework.store.TextLocation;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;

public final class UimaLabelIndex<T> extends AbstractLabelIndex<T> {

  private final CAS cas;
  private final AnnotationIndex<AnnotationFS> index;
  private final LabelAdapter<T> labelAdapter;
  private final Type annotationType;
  private final Type type;
  @Nullable
  private transient LabelIndex<T> inflated;

  @Inject
  public UimaLabelIndex(CAS cas, LabelAdapter<T> labelAdapter) {
    this.cas = cas;
    this.labelAdapter = labelAdapter;
    annotationType = cas.getTypeSystem()
        .getType("uima.tcas.Annotation");
    type = labelAdapter.getType();
    index = cas.getAnnotationIndex(type);
  }

  @Override
  public LabelIndex<T> containing(TextLocation textLocation) {
    return inflate().containing(textLocation);
  }

  @Override
  public LabelIndex<T> insideSpan(TextLocation textLocation) {
    AnnotationFS bound = cas.createAnnotation(annotationType,
        textLocation.getBegin() - 1, textLocation.getEnd() + 1);
    FSIterator<AnnotationFS> subiterator = index.subiterator(bound);
    SpansMap<T> spansMap;
    if (labelAdapter.isDistinct()) {
      ImmutableDistinctSpanMap.Builder<T> builder
          = ImmutableDistinctSpanMap.builder();
      while (subiterator.hasNext()) {
        Label<T> label = labelAdapter
            .annotationToLabel(subiterator.next());
        if (textLocation.contains(label)) {
          builder.add(label);
        }
      }
      spansMap = builder.build();
    } else {
      OrderedSpanMap<T> orderedSpanMap = new OrderedSpanMap<>();
      while (subiterator.hasNext()) {
        Label<T> label = labelAdapter
            .annotationToLabel(subiterator.next());
        if (textLocation.contains(label)) {
          orderedSpanMap.put(label, label.getValue());
        }
      }
      spansMap = new ImmutableSpanMap<>(orderedSpanMap);
    }
    return new DefaultLabelIndex<>(spansMap);
  }

  @Override
  public LabelIndex<T> leftwardsFrom(TextLocation span) {
    return inflate().leftwardsFrom(span);
  }

  @Override
  public LabelIndex<T> rightwardsFrom(TextLocation span) {
    return inflate().rightwardsFrom(span);
  }

  @Override
  public LabelIndex<T> ascendingBegin() {
    return inflate().ascendingBegin();
  }

  @Override
  public LabelIndex<T> descendingBegin() {
    return inflate().descendingBegin();
  }

  @Override
  public LabelIndex<T> ascendingEnd() {
    return inflate().ascendingEnd();
  }

  @Override
  public LabelIndex<T> descendingEnd() {
    return inflate().descendingEnd();
  }

  @Override
  public Optional<Label<T>> first() {
    FSIterator<AnnotationFS> it = index.iterator();
    if (it.hasNext()) {
      return Optional.of(labelAdapter.annotationToLabel(it.next()));
    }
    return Optional.empty();
  }

  @Override
  public Optional<Label<T>> withTextLocation(TextLocation textLocation) {
    AnnotationFS bound = cas.createAnnotation(annotationType,
        textLocation.getBegin() - 1, textLocation.getEnd() + 1);
    FSIterator<AnnotationFS> subiterator = index.subiterator(bound);
    while (subiterator.hasNext()) {
      AnnotationFS next = subiterator.next();
      if (next.getBegin() == textLocation.getBegin()
          && next.getEnd() == textLocation.getEnd()) {
        return Optional.of(labelAdapter.annotationToLabel(next));
      }
    }
    return Optional.empty();
  }

  @Override
  public Set<Span> spans() {
    return inflate().spans();
  }

  @Override
  public Collection<T> values() {
    return inflate().values();
  }

  @Override
  public List<Label<T>> asList() {
    return inflate().asList();
  }

  @Override
  public List<Span> spansAsList() {
    return inflate().spansAsList();
  }

  @Override
  public List<T> valuesAsList() {
    return inflate().valuesAsList();
  }

  @Override
  public Iterator<Label<T>> iterator() {
    return new FSIteratorAdapter<>(index, labelAdapter::annotationToLabel);
  }

  @Override
  public int size() {
    return index.size();
  }

  public LabelIndex<T> inflate() {
    if (inflated != null) {
      return inflated;
    }

    SpansMap<T> spansMap;
    if (labelAdapter.isDistinct()) {
      spansMap = ImmutableDistinctSpanMap.<T>builder()
          .addAll(this)
          .build();
    } else {
      OrderedSpanMap<T> orderedSpanMap = new OrderedSpanMap<>();
      for (Label<T> tLabel : this) {
        orderedSpanMap.put(tLabel, tLabel.getValue());
      }
      spansMap = new ImmutableSpanMap<>(orderedSpanMap);
    }
    return (inflated = new DefaultLabelIndex<>(spansMap));
  }
}
