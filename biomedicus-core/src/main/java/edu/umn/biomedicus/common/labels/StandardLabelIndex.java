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

public class StandardLabelIndex<T> extends AbstractLabelIndex<T> {
    private final OrderedSpanMap<Label<T>> tree;

    private final Predicate<Label<T>> filter;

    private final boolean ascending;

    private final boolean sizeDecreasing;

    public StandardLabelIndex(OrderedSpanMap<Label<T>> tree, Predicate<Label<T>> filter, boolean ascending, boolean sizeDecreasing) {
        this.tree = tree;
        this.filter = filter;
        this.ascending = ascending;
        this.sizeDecreasing = sizeDecreasing;
    }

    public StandardLabelIndex(Iterable<Label<T>> labels) {
        tree = new OrderedSpanMap<>();
        filter = (label) -> true;
        ascending = true;
        sizeDecreasing = true;
        for (Label<T> label : labels) {
            tree.put(label, label);
        }
    }

    @Override
    public LabelIndex<T> containing(TextLocation textLocation) {
        return new StandardLabelIndex<>(tree.containing(textLocation), filter, ascending, sizeDecreasing);
    }

    @Override
    public LabelIndex<T> insideSpan(TextLocation textLocation) {
        return new StandardLabelIndex<>(tree.insideSpan(textLocation), filter, ascending, sizeDecreasing);
    }

    @Override
    public Optional<Label<T>> withTextLocation(TextLocation textLocation) {
        return Optional.ofNullable(tree.get(textLocation));
    }

    @Override
    public LabelIndex<T> leftwardsFrom(TextLocation span) {
        return new StandardLabelIndex<>(tree.toTheLeftOf(span), filter, false, sizeDecreasing);
    }

    @Override
    public LabelIndex<T> rightwardsFrom(TextLocation span) {
        return new StandardLabelIndex<>(tree.toTheRightOf(span), filter, true, false);
    }

    @Override
    public LabelIndex<T> reverse() {
        return new StandardLabelIndex<>(tree, filter, !ascending, sizeDecreasing);
    }

    @Override
    public LabelIndex<T> ascendingBegin() {
        return new StandardLabelIndex<>(tree, filter, true, sizeDecreasing);
    }

    @Override
    public LabelIndex<T> descendingBegin() {
        return new StandardLabelIndex<>(tree, filter, false, sizeDecreasing);
    }

    @Override
    public LabelIndex<T> increasingSize() {
        return new StandardLabelIndex<>(tree, filter, ascending, false);
    }

    @Override
    public LabelIndex<T> decreasingSize() {
        return new StandardLabelIndex<>(tree, filter, ascending, true);
    }

    @Override
    public LabelIndex<T> filter(Predicate<Label<T>> predicate) {
        return new StandardLabelIndex<>(tree, filter.and(predicate), ascending, sizeDecreasing);
    }

    @Override
    public Iterator<Label<T>> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<Label<T>> stream() {
        Stream<Label<T>> labelStream;
        if (ascending) {
            if (sizeDecreasing) {
                labelStream = tree.ascendingStartDecreasingSizeValuesStream();
            } else {
                labelStream = tree.ascendingStartIncreasingSizeValueStream();
            }
        } else {
            if (sizeDecreasing) {
                labelStream = tree.descendingStartDecreasingSizeValuesStream();
            } else {
                labelStream = tree.descendingStartIncreasingSizeValuesStream();
            }
        }
        return labelStream.filter(filter);
    }
}
