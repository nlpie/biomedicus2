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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DefaultLabelIndex<T> extends AbstractLabelIndex<T> {
    private final SpansMap<T> tree;

    public DefaultLabelIndex(SpansMap<T> tree) {
        this.tree = tree;
    }

    public DefaultLabelIndex(Iterable<Label<T>> labels) {
        OrderedSpanMap<T> orderedSpanMap = new OrderedSpanMap<>();
        for (Label<T> label : labels) {
            orderedSpanMap.put(label, label.value());
        }
        tree = orderedSpanMap;
    }

    @Override
    public LabelIndex<T> containing(TextLocation textLocation) {
        return new DefaultLabelIndex<>(tree.containing(textLocation));
    }

    @Override
    public LabelIndex<T> insideSpan(TextLocation textLocation) {
        return new DefaultLabelIndex<>(tree.insideSpan(textLocation));
    }

    @Override
    public LabelIndex<T> leftwardsFrom(TextLocation span) {
        return new DefaultLabelIndex<>(tree.toTheLeftOf(span).descendingBegin()
                .descendingEnd());
    }

    @Override
    public LabelIndex<T> rightwardsFrom(TextLocation span) {
        return new DefaultLabelIndex<>(tree.toTheRightOf(span));
    }

    @Override
    public LabelIndex<T> ascendingBegin() {
        return new DefaultLabelIndex<>(tree.ascendingBegin());
    }

    @Override
    public LabelIndex<T> descendingBegin() {
        return new DefaultLabelIndex<>(tree.descendingBegin());
    }

    @Override
    public LabelIndex<T> ascendingEnd() {
        return new DefaultLabelIndex<>(tree.ascendingEnd());
    }

    @Override
    public LabelIndex<T> descendingEnd() {
        return new DefaultLabelIndex<>(tree.descendingBegin());
    }

    @Override
    public LabelIndex<T> filter(Predicate<Label<T>> predicate) {
        return new FilteredLabelIndex<>(this, predicate);
    }

    @Override
    public Iterator<Label<T>> iterator() {
        return tree.entries().iterator();
    }

    @Override
    public Stream<Label<T>> stream() {
        return tree.entries().stream();
    }

    @Override
    public Set<Span> spans() {
        return tree.spans();
    }

    @Override
    public Collection<T> values() {
        return tree.values();
    }

    @Override
    public boolean isEmpty() {
        return tree.isEmpty();
    }

    @Override
    public List<Label<T>> asList() {
        return tree.asList();
    }

    @Override
    public List<Span> spansAsList() {
        return tree.spansAsList();
    }

    @Override
    public List<T> valuesAsList() {
        return tree.valuesAsList();
    }
}
