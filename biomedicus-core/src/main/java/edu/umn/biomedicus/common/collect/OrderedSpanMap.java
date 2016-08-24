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

package edu.umn.biomedicus.common.collect;

import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TextLocation;
import edu.umn.biomedicus.common.tuples.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Stream;

public class OrderedSpanMap<T> implements SpansMap<T> {
    private final NavigableMap<Integer, NavigableMap<Integer, T>> backingTree;
    private final int endMin;
    private final int endMax;

    public OrderedSpanMap() {
        this.backingTree = new TreeMap<>();
        this.endMin = 0;
        this.endMax = Integer.MAX_VALUE;
    }

    private OrderedSpanMap(NavigableMap<Integer, NavigableMap<Integer, T>> backingTree, int endMin, int endMax) {
        this.backingTree = backingTree;
        this.endMin = endMin;
        this.endMax = endMax;
    }

    public int size() {
        return backingTree.values().stream().map(endMap -> endMap.subMap(endMin, true, endMax, true))
                .mapToInt(NavigableMap::size).sum();
    }

    public boolean isEmpty() {
        return backingTree.values().stream().allMatch(NavigableMap::isEmpty);
    }

    public boolean containsKey(Object key) {
        if (!(key instanceof TextLocation)) {
            return false;
        }
        return false;
    }

    public boolean containsValue(Object value) {
        return backingTree.values().stream().map(endMap -> endMap.subMap(endMin, true, endMax, true))
                .anyMatch(endMap -> endMap.containsValue(value));
    }

    public T get(Object key) {
        if (!(key instanceof TextLocation)) {
            return null;
        }

        TextLocation textLocation = (TextLocation) key;

        if (textLocation.getEnd() < endMin || textLocation.getEnd() > endMax) {
            return null;
        }

        NavigableMap<Integer, T> endMap = backingTree.get(textLocation.getBegin());
        if (endMap == null) {
            return null;
        }

        return endMap.get(textLocation.getEnd());
    }

    public T put(TextLocation textLocation, T object) {
        int begin = textLocation.getBegin();
        NavigableMap<Integer, T> endMap = backingTree.get(begin);
        if (endMap == null) {
            endMap = new TreeMap<>();
            backingTree.put(begin, endMap);
        }
        return endMap.put(textLocation.getEnd(), object);
    }

    public T remove(Object key) {
        if (!(key instanceof TextLocation)) {
            return null;
        }
        TextLocation textLocation = (TextLocation) key;
        NavigableMap<Integer, T> endMap = backingTree.get(textLocation.getBegin());
        if (endMap == null) {
            throw new IllegalArgumentException("Tree does not contain span");
        }
        return endMap.remove(textLocation.getEnd());
    }

    public void putAll(Map<? extends TextLocation, ? extends T> m) {
        m.entrySet().forEach(e -> {
            put(e.getKey(), e.getValue());
        });
    }

    @Override
    public OrderedSpanMap<T> toTheLeftOf(TextLocation textLocation) {
        NavigableMap<Integer, NavigableMap<Integer, T>> headMap = backingTree.headMap(textLocation.getBegin(), true);
        int newEndMax = Math.min(textLocation.getEnd(), endMax);
        return new OrderedSpanMap<>(headMap, endMin, newEndMax);
    }

    @Override
    public OrderedSpanMap<T> toTheRightOf(TextLocation textLocation) {
        NavigableMap<Integer, NavigableMap<Integer, T>> tailMap = backingTree.tailMap(textLocation.getEnd(), true);
        int newEndMin = Math.max(textLocation.getEnd(), endMin);
        return new OrderedSpanMap<>(tailMap, newEndMin, endMax);
    }

    public void clear() {
        backingTree.values().stream()
                .forEach(endMap -> endMap.subMap(endMin, true, endMax, true).clear());
    }

    @Override
    public Stream<T> valuesStream() {
        return backingTree.values().stream()
                .map(endMap -> endMap.subMap(endMin, true, endMax, true))
                .map(Map::values)
                .flatMap(Collection::stream);
    }

    @Override
    public Stream<T> descendingValuesStream() {
        return backingTree.descendingMap().values().stream()
                .map(endMap -> endMap.subMap(endMin, true, endMax, true).descendingMap())
                .map(Map::values)
                .flatMap(Collection::stream);
    }

    @Override
    public OrderedSpanMap<T> insideSpan(TextLocation textLocation) {
        int newEndMax = Math.min(endMax, textLocation.getEnd());
        return new OrderedSpanMap<>(backingTree.tailMap(textLocation.getBegin(), true), endMin, newEndMax);
    }

    @Override
    public OrderedSpanMap<T> containing(TextLocation textLocation) {
        int newEndMin = Math.max(endMin, textLocation.getEnd());
        return new OrderedSpanMap<>(backingTree.headMap(textLocation.getBegin(), true), newEndMin, endMax);
    }

    @Override
    public Stream<TextLocation> spansStream() {
        return backingTree.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream().map(e2 -> new Span(e.getKey(), e2.getKey())));
    }

    @Override
    public Stream<Pair<TextLocation, T>> pairStream() {
        return backingTree.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream().map(e2 -> new Pair<>(new Span(e.getKey(), e2.getKey()), e2.getValue())));
    }
}
