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

import edu.umn.biomedicus.common.collect.SlidingWindow;
import edu.umn.biomedicus.common.types.text.TextLocation;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.*;

/**
 * A collection of {@link Label} objects. By default, a typed injectable of labels should map to the collection of all
 * such labels for a document. Labels are unique per span, meaning that for each unique span at most one instance of T
 * will be labeled.
 * <br />
 * Most transformations, functions which return another instance of Labels, should be performed lazily, meaning that the
 * computation involved isn't performed until the labels are iterated, and in many cases, their limits can be combined,
 * for example {@link #insideSpan(TextLocation)} and {@link #rightwardsFrom(TextLocation)} chained should only cost as much as
 * one insideSpan call.
 *
 * @param <T> the type that is labeled
 * @since 1.5.0
 */
public interface Labels<T> extends Iterable<Label<T>> {
    /**
     * Returns a collection of all the labels that contain the specified span parameter.
     *
     * @param textLocation
     * @return
     */
    Labels<T> containing(TextLocation textLocation);

    /**
     * Returns a collection of these labels only inside the span parameter. All labels in the returned objects will have
     * a begin greater than or equal to the argument's begin and an end less than or equal to the arguments end.
     *
     * @param textLocation the boundaries.
     * @return Labels object filtered down so that all labels meet the requirement
     */
    Labels<T> insideSpan(TextLocation textLocation);

    /**
     * The collection of labels where the begin and end are less than or equal to the begin of the span argument.
     * Iterator order is such that we start with the first to the immediate left of the span, and continue onwards.
     *
     * @param span span to work leftwards from
     * @return labels leftwards from the specified span.
     */
    Labels<T> leftwardsFrom(TextLocation span);

    /**
     * The collection of labels where the begin and end are greater than or equal to the end of the span argument.
     *
     * @param span span to work rightwards from
     * @return labels rightwards after the specified span.
     */
    Labels<T> rightwardsFrom(TextLocation span);

    /**
     * Reverses the iteration order of this collection of labels. By default labels iterate in order from left to right.
     *
     * @return
     */
    Labels<T> reverse();

    /**
     * Limits the the number of labels returned to a specific count. Should be used as close as possible to the
     * consumption of the stream, since limiting forces segmentation between transformation calls.
     *
     * @param max
     * @return
     */
    Labels<T> limit(int max);

    /**
     * Applies the predicate, return a collection of the labels that match that predicate.
     *
     * @param predicate
     * @return
     */
    Labels<T> filter(Predicate<Label<T>> predicate);

    /**
     * Returns an optional of the first label in this labels list.
     *
     * @return
     */
    default Optional<Label<T>> firstOptionally() {
        Iterator<Label<T>> iterator = iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        }
        return Optional.empty();
    }

    default Optional<Label<T>> withSpan(TextLocation textLocation) {
        Iterator<Label<T>> it = insideSpan(textLocation).filter(textLocation::spanEquals).iterator();
        if (it.hasNext()) {
            return Optional.of(it.next());
        }
        return Optional.empty();
    }

    default Iterable<List<Label<T>>> slidingWindowsOfSize(int size) {
        return new SlidingWindow<>(this, size);
    }


    /**
     * @return
     */
    default List<Label<T>> all() {
        return stream().collect(Collectors.toList());
    }

    default Stream<Label<T>> stream() {
        Iterator<Label<T>> iterator = iterator();
        Spliterator<Label<T>> spliterator = Spliterators.spliteratorUnknownSize(iterator,
                ORDERED | DISTINCT | IMMUTABLE | NONNULL | SORTED);
        return StreamSupport.stream(spliterator, false);
    }

    default List<T> values() {
        return stream().map(Label::value).collect(Collectors.toList());
    }
}
