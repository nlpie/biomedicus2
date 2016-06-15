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

import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.SpanLike;
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
        if (!(key instanceof SpanLike)) {
            return false;
        }
        return false;
    }

    public boolean containsValue(Object value) {
        return backingTree.values().stream().map(endMap -> endMap.subMap(endMin, true, endMax, true))
                .anyMatch(endMap -> endMap.containsValue(value));
    }

    public T get(Object key) {
        if (!(key instanceof SpanLike)) {
            return null;
        }

        SpanLike spanLike = (SpanLike) key;

        if (spanLike.getEnd() < endMin || spanLike.getEnd() > endMax) {
            return null;
        }

        NavigableMap<Integer, T> endMap = backingTree.get(spanLike.getBegin());
        if (endMap == null) {
            return null;
        }

        return endMap.get(spanLike.getEnd());
    }

    public T put(SpanLike spanLike, T object) {
        int begin = spanLike.getBegin();
        NavigableMap<Integer, T> endMap = backingTree.get(begin);
        if (endMap == null) {
            endMap = new TreeMap<>();
            backingTree.put(begin, endMap);
        }
        return endMap.put(spanLike.getEnd(), object);
    }

    public T remove(Object key) {
        if (!(key instanceof SpanLike)) {
            return null;
        }
        SpanLike spanLike = (SpanLike) key;
        NavigableMap<Integer, T> endMap = backingTree.get(spanLike.getBegin());
        if (endMap == null) {
            throw new IllegalArgumentException("Tree does not contain span");
        }
        return endMap.remove(spanLike.getEnd());
    }

    public void putAll(Map<? extends SpanLike, ? extends T> m) {
        m.entrySet().forEach(e -> {
            put(e.getKey(), e.getValue());
        });
    }

    @Override
    public OrderedSpanMap<T> toTheLeftOf(SpanLike spanLike) {
        NavigableMap<Integer, NavigableMap<Integer, T>> headMap = backingTree.headMap(spanLike.getBegin(), true);
        int newEndMax = Math.min(spanLike.getEnd(), endMax);
        return new OrderedSpanMap<>(headMap, endMin, newEndMax);
    }

    @Override
    public OrderedSpanMap<T> toTheRightOf(SpanLike spanLike) {
        NavigableMap<Integer, NavigableMap<Integer, T>> tailMap = backingTree.tailMap(spanLike.getEnd(), true);
        int newEndMin = Math.max(spanLike.getEnd(), endMin);
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
    public OrderedSpanMap<T> insideSpan(SpanLike spanLike) {
        int newEndMax = Math.min(endMax, spanLike.getEnd());
        return new OrderedSpanMap<>(backingTree.tailMap(spanLike.getBegin(), true), endMin, newEndMax);
    }

    @Override
    public OrderedSpanMap<T> containing(SpanLike spanLike) {
        int newEndMin = Math.max(endMin, spanLike.getEnd());
        return new OrderedSpanMap<>(backingTree.headMap(spanLike.getBegin(), true), newEndMin, endMax);
    }

    @Override
    public Stream<SpanLike> spansStream() {
        return backingTree.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream().map(e2 -> new Span(e.getKey(), e2.getKey())));
    }

    @Override
    public Stream<Pair<SpanLike, T>> pairStream() {
        return backingTree.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream().map(e2 -> new Pair<>(new Span(e.getKey(), e2.getKey()), e2.getValue())));
    }
}
