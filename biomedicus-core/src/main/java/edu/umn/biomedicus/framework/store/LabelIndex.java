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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A collection of {@link Label} objects. By default, a typed injectable of labels should map to the
 * collection of all such labels for a document. Labels are unique per span, meaning that for each
 * unique span at most one instance of T will be labeled. <br /> Most transformations, functions
 * which return another instance of LabelIndex, should be performed lazily, meaning that the
 * computation involved isn't performed until the labels are iterated, and in many cases, their
 * limits can be combined, for example {@link #insideSpan(TextLocation)} and {@link
 * #rightwardsFrom(TextLocation)} chained should only cost as much as one insideSpan call.
 *
 * @param <T> the type that is labeled
 * @since 1.5.0
 */
public interface LabelIndex<T> extends Collection<Label<T>> {

  /**
   * Returns a collection of all the labels that contain the specified {@link TextLocation}
   * parameter.
   *
   * @param textLocation the text location which should be contained by any label returned.
   * @return new LabelIndex in which all labels contain the specified text location.
   */
  LabelIndex<T> containing(TextLocation textLocation);

  /**
   * Returns a collection of these labels only inside the span parameter. All labels in the returned
   * objects will have a begin greater than or equal to the argument's begin and an end less than or
   * equal to the arguments end.
   *
   * @param textLocation the boundaries.
   * @return LabelIndex object filtered down so that all labels meet the requirement
   */
  LabelIndex<T> insideSpan(TextLocation textLocation);

  /**
   * The collection of labels where the begin and end are less than or equal to the begin of the
   * span argument. Iterator order is such that we start with the first to the immediate left of the
   * span, and continue onwards.
   *
   * @param span span to work leftwards from
   * @return labels leftwards from the specified span.
   */
  LabelIndex<T> leftwardsFrom(TextLocation span);

  /**
   * The collection of labels where the begin and end are greater than or equal to the end of the
   * span argument.
   *
   * @param span span to work rightwards from
   * @return labels rightwards after the specified span.
   */
  LabelIndex<T> rightwardsFrom(TextLocation span);

  /**
   * Returns a LabelIndex which ascends based on the begin value of label objects.
   *
   * @return a view of this label index
   */
  LabelIndex<T> ascendingBegin();

  /**
   * Returns a LabelIndex which descends based on the begin value of label objects.
   *
   * @return a view of this label index
   */
  LabelIndex<T> descendingBegin();

  /**
   * Returns a LabelIndex which, after the begin behavior, ascends based on the end value of the
   * labels.
   *
   * @return view of this labelindex with ascending end
   */
  LabelIndex<T> ascendingEnd();

  /**
   * Returns a LabelIndex which, after the begin behavior, ascends based on the begin value of the
   * labels.
   *
   * @return view of this LabelIndex with descending end.
   */
  LabelIndex<T> descendingEnd();

  /**
   * Returns optionally the first label in this LabelIndex.
   *
   * @return an optional of the first label, empty if there are no labels in this label index.
   */
  Optional<Label<T>> first();

  /**
   * Returns optionally a label with the specified text location.
   *
   * @param textLocation the location
   * @return an optional of the label with the text location, empty if no label has that location
   */
  Optional<Label<T>> withTextLocation(TextLocation textLocation);

  /**
   * Returns optionally a label with the specified span.
   *
   * @param span the span
   * @return an optional of the label with the span, empty if no label has that span
   */
  default Optional<Label<T>> withSpan(Span span) {
    return withTextLocation(span);
  }

  /**
   * The spans contained in the label index. Iteration order is begin ascending than end ascending
   * by default, and can be modified through the {@link #descendingBegin()} and {@link
   * #descendingEnd()} methods.
   *
   * @return a view of the spans
   */
  Set<Span> spans();

  /**
   * List of all the label values in this label index.
   *
   * @return list of all the label values
   */
  Collection<T> values();

  /**
   * Either put all of the elements in a list or provide a list view of the elements. Unmodifiable.
   *
   * @return a list which contains all the labels in this index.
   */
  List<Label<T>> asList();

  /**
   * Either put all of the spans in a list or provide a list view of the spans.
   *
   * @return a list containing all the spans in this index.
   */
  List<Span> spansAsList();

  /**
   * Either put all of the values in a list or provide a list view of the values.
   *
   * @return a list containing all of the values in this index.
   */
  List<T> valuesAsList();
}
