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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.*;

/**
 * Abstract class for a LabelIndex implementation. Provides sensible defaults for methods using an adapter pattern.
 * Falls back to {@link StandardLabelIndex} for methods which may not be implementable under other instances.
 *
 * @param <T>
 */
public abstract class AbstractLabelIndex<T> implements LabelIndex<T> {
    @Override
    public LabelIndex<T> containing(TextLocation textLocation) {
        return new StandardLabelIndex<>(this).containing(textLocation);
    }

    @Override
    public LabelIndex<T> insideSpan(TextLocation textLocation) {
        return new StandardLabelIndex<>(new FilteredLabelIndex<>(this, textLocation::contains));
    }

    @Override
    public LabelIndex<T> leftwardsFrom(TextLocation span) {
        return new StandardLabelIndex<>(this).leftwardsFrom(span);
    }

    @Override
    public LabelIndex<T> rightwardsFrom(TextLocation span) {
        return new StandardLabelIndex<>(this).leftwardsFrom(span);
    }

    @Override
    public LabelIndex<T> reverse() {
        return new StandardLabelIndex<>(this).reverse();
    }

    @Override
    public LabelIndex<T> ascendingBegin() {
        return new StandardLabelIndex<>(this).ascendingBegin();
    }

    @Override
    public LabelIndex<T> descendingBegin() {
        return new StandardLabelIndex<>(this).descendingBegin();
    }

    @Override
    public LabelIndex<T> increasingSize() {
        return new StandardLabelIndex<>(this).increasingSize();
    }

    @Override
    public LabelIndex<T> decreasingSize() {
        return new StandardLabelIndex<>(this).decreasingSize();
    }

    @Override
    public LabelIndex<T> limit(int max) {
        return new LimitedLabelIndex<>(this, max);
    }

    @Override
    public LabelIndex<T> filter(Predicate<Label<T>> predicate) {
        return new FilteredLabelIndex<>(this, predicate);
    }

    @Override
    public Optional<Label<T>> withSpan(Span span) {
        return matching(span);
    }

    @Override
    public Optional<Label<T>> withTextLocation(TextLocation textLocation) {
        return matching(textLocation);
    }

    @Override
    public Optional<Label<T>> matching(TextLocation textLocation) {
        Iterator<Label<T>> it = insideSpan(textLocation).filter(textLocation::spanEquals).iterator();
        if (it.hasNext()) {
            return Optional.of(it.next());
        }
        return Optional.empty();
    }

    @Override
    public List<Label<T>> all() {
        return stream().collect(Collectors.toList());
    }

    @Override
    public Stream<Label<T>> stream() {
        Iterator<Label<T>> iterator = iterator();
        Spliterator<Label<T>> spliterator = Spliterators.spliteratorUnknownSize(iterator,
                ORDERED | DISTINCT | IMMUTABLE | NONNULL | SORTED);
        return StreamSupport.stream(spliterator, false);
    }

    @Override
    public List<T> values() {
        return stream().map(Label::value).collect(Collectors.toList());
    }

    @Override
    public Optional<Label<T>> firstOptionally() {
        Iterator<Label<T>> iterator = iterator();
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        }
        return Optional.empty();
    }
}
