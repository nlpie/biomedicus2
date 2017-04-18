/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TextLocation;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A collection of {@link Label} objects. By default, a typed injectable of
 * labels should map to the collection of all such labels for a document. Labels
 * are unique per span, meaning that for each unique span at most one instance
 * of T will be labeled.
 * <br />
 * Most transformations, functions which return another instance of LabelIndex,
 * should be performed lazily, meaning that the computation involved isn't
 * performed until the labels are iterated, and in many cases, their limits can
 * be combined, for example {@link #insideSpan(TextLocation)} and
 * {@link #rightwardsFrom(TextLocation)} chained should only cost as much as
 * one insideSpan call.
 *
 * @param <T> the type that is labeled
 * @since 1.5.0
 */
public interface LabelIndex<T> extends Iterable<Label<T>> {
    /**
     * Returns a collection of all the labels that contain the specified
     * {@link TextLocation} parameter.
     *
     * @param textLocation the text location which should be contained by any
     *                     label returned.
     * @return new LabelIndex in which all labels contain the specified text
     * location.
     */
    LabelIndex<T> containing(TextLocation textLocation);

    /**
     * Returns a collection of these labels only inside the span parameter. All
     * labels in the returned objects will have a begin greater than or equal to
     * the argument's begin and an end less than or equal to the arguments end.
     *
     * @param textLocation the boundaries.
     * @return LabelIndex object filtered down so that all labels meet the requirement
     */
    LabelIndex<T> insideSpan(TextLocation textLocation);

    /**
     * The collection of labels where the begin and end are less than or equal
     * to the begin of the span argument. Iterator order is such that we start
     * with the first to the immediate left of the span, and continue onwards.
     *
     * @param span span to work leftwards from
     * @return labels leftwards from the specified span.
     */
    LabelIndex<T> leftwardsFrom(TextLocation span);

    /**
     * The collection of labels where the begin and end are greater than or
     * equal to the end of the span argument.
     *
     * @param span span to work rightwards from
     * @return labels rightwards after the specified span.
     */
    LabelIndex<T> rightwardsFrom(TextLocation span);

    /**
     * Reverses the iteration order of this collection of labels. By default labels iterate in order from left to right.
     *
     * @return labels collection, reversed
     */
    LabelIndex<T> reverse();

    /**
     * Returns a LabelIndex which ascends based on the begin value of label objects.
     *
     * @return a view of this label index
     */
    LabelIndex<T> ascendingBegin();

    /**
     * Returns a LabelIndex which descends based on the begin value of label
     * objects.
     *
     * @return a view of this label index
     */
    LabelIndex<T> descendingBegin();

    /**
     * Returns a LabelIndex which, after the begin behavior, ascends based on
     * the end value of the labels.
     *
     * @return
     */
    LabelIndex<T> ascendingEnd();

    LabelIndex<T> descendingEnd();

    /**
     * Limits the the number of labels returned to a specific count. Should be
     * used as close as possible to the consumption of the stream, since
     * limiting forces segmentation between transformation calls.
     *
     * @param max the number of labels to limit to
     * @return a view of this label index
     */
    LabelIndex<T> limit(int max);

    /**
     * Applies the predicate, return a collection of the labels that match that predicate.
     *
     * @param predicate the testing predicate
     * @return a view of this label index.
     */
    LabelIndex<T> filter(Predicate<Label<T>> predicate);

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
    Optional<Label<T>> withSpan(Span span);

    /**
     * Optionally return any label that matches a text location exactly.
     *
     * @param textLocation text location to find matches for
     * @return an optional label that matches the text location.
     */
    Optional<Label<T>> matching(TextLocation textLocation);


    /**
     * All the labels in this Label Index placed into a list.
     *
     * @return new list with all the labels in this label index.
     */
    List<Label<T>> all();

    /**
     * Stream of all the labels in this label index.
     *
     * @return stream
     */
    Stream<Label<T>> stream();

    /**
     * List of all the label values in this label index.
     *
     * @return list of all the label values
     */
    List<T> values();

    LabelIndex<T> inflate();
}
