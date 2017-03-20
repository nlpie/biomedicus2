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
import edu.umn.biomedicus.common.collect.SpansMap;
import edu.umn.biomedicus.common.types.text.TextLocation;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StandardLabelIndex<T> extends AbstractLabelIndex<T> {
    private final SpansMap<T> tree;
    private final Predicate<Label<T>> filter;

    public StandardLabelIndex(SpansMap<T> tree,
                              Predicate<Label<T>> filter) {
        this.tree = tree;
        this.filter = filter;
    }

    public StandardLabelIndex(Iterable<Label<T>> labels) {
        OrderedSpanMap<T> orderedSpanMap = new OrderedSpanMap<>();
        filter = (label) -> true;
        for (Label<T> label : labels) {
            orderedSpanMap.put(label, label.value());
        }
        tree = orderedSpanMap;
    }

    @Override
    public LabelIndex<T> containing(TextLocation textLocation) {
        return new StandardLabelIndex<>(tree.containing(textLocation), filter);
    }

    @Override
    public LabelIndex<T> insideSpan(TextLocation textLocation) {
        return new StandardLabelIndex<>(tree.insideSpan(textLocation), filter);
    }

    @Override
    public Optional<Label<T>> matching(TextLocation textLocation) {
        return tree.get(textLocation)
                .map(t -> new Label<>(textLocation.toSpan(), t));
    }

    @Override
    public LabelIndex<T> leftwardsFrom(TextLocation span) {
        return new StandardLabelIndex<>(tree.toTheLeftOf(span).descendingBegin()
                .descendingEnd(), filter);
    }

    @Override
    public LabelIndex<T> rightwardsFrom(TextLocation span) {
        return new StandardLabelIndex<>(tree.toTheRightOf(span), filter);
    }

    @Override
    public LabelIndex<T> reverse() {
        return new StandardLabelIndex<>(tree, filter);
    }

    @Override
    public LabelIndex<T> ascendingBegin() {
        return new StandardLabelIndex<>(tree.ascendingBegin(), filter);
    }

    @Override
    public LabelIndex<T> descendingBegin() {
        return new StandardLabelIndex<>(tree.descendingBegin(), filter);
    }

    @Override
    public LabelIndex<T> ascendingEnd() {
        return new StandardLabelIndex<>(tree.descendingBegin(), filter);
    }

    @Override
    public LabelIndex<T> descendingEnd() {
        return new StandardLabelIndex<>(tree.descendingBegin(), filter);
    }

    @Override
    public LabelIndex<T> filter(Predicate<Label<T>> predicate) {
        return new StandardLabelIndex<>(tree, filter.and(predicate));
    }

    @Override
    public Iterator<Label<T>> iterator() {
        return tree.entries().iterator();
    }

    @Override
    public Stream<Label<T>> stream() {
        return tree.entries().stream().filter(filter);
    }
}
