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

import edu.umn.biomedicus.common.collect.OrderedSpanMap;
import edu.umn.biomedicus.common.types.text.TextLocation;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StandardLabels<T> extends AbstractLabels<T> {
    private final OrderedSpanMap<Label<T>> tree;

    private final Predicate<Label<T>> filter;

    private final boolean ascending;

    public StandardLabels(OrderedSpanMap<Label<T>> tree, Predicate<Label<T>> filter, boolean ascending) {
        this.tree = tree;
        this.filter = filter;
        this.ascending = ascending;
    }

    public StandardLabels(Iterable<Label<T>> labels) {
        tree = new OrderedSpanMap<>();
        filter = (label) -> true;
        ascending = true;
        for (Label<T> label : labels) {
            tree.put(label, label);
        }
    }

    @Override
    public Labels<T> containing(TextLocation textLocation) {
        return new StandardLabels<>(tree.containing(textLocation), filter, ascending);
    }

    @Override
    public Labels<T> insideSpan(TextLocation textLocation) {
        return new StandardLabels<>(tree.insideSpan(textLocation), filter, ascending);
    }

    @Override
    public Optional<Label<T>> withSpan(TextLocation textLocation) {
        return Optional.ofNullable(tree.get(textLocation));
    }

    @Override
    public Labels<T> leftwardsFrom(TextLocation span) {
        return new StandardLabels<>(tree.toTheLeftOf(span), filter, false);
    }

    @Override
    public Labels<T> rightwardsFrom(TextLocation span) {
        return new StandardLabels<>(tree.toTheRightOf(span), filter, true);
    }

    public Labels<T> reverse() {
        return new StandardLabels<>(tree, filter, !ascending);
    }

    @Override
    public Labels<T> filter(Predicate<Label<T>> predicate) {
        return new StandardLabels<>(tree, filter.and(predicate), ascending);
    }

    @Override
    public Iterator<Label<T>> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<Label<T>> stream() {
        Stream<Label<T>> labelStream = ascending ? tree.valuesStream() : tree.descendingValuesStream();
        return labelStream.filter(filter);
    }
}
