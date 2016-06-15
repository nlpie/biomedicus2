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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DistinctSpansMap<T> implements SpansMap<T> {
    private final NavigableMap<Integer, Pair<Integer, T>> backingMap;

    public DistinctSpansMap() {
        this(new TreeMap<>());
    }

    private DistinctSpansMap(NavigableMap<Integer, Pair<Integer, T>> backingMap) {
        this.backingMap = backingMap;
    }

    @Override
    public DistinctSpansMap<T> toTheLeftOf(SpanLike spanLike) {
        int begin = spanLike.getBegin();
        NavigableMap<Integer, Pair<Integer, T>> headMap = backingMap.headMap(begin, true);
        headMap = checkLastEntry(begin, headMap);
        return new DistinctSpansMap<>(headMap);
    }

    private NavigableMap<Integer, Pair<Integer, T>> checkLastEntry(int maxEnd,
                                                                   NavigableMap<Integer, Pair<Integer, T>> headMap) {
        if (headMap.size() < 2) {
            return headMap;
        }
        Map.Entry<Integer, Pair<Integer, T>> lastEntry = headMap.lastEntry();
        Pair<Integer, T> endPair = lastEntry.getValue();
        Integer end = endPair.first();
        if (end > maxEnd) {
            return headMap.headMap(lastEntry.getKey() - 1, true);
        } else {
            return headMap;
        }
    }

    @Override
    public DistinctSpansMap<T> toTheRightOf(SpanLike spanLike) {
        int end = spanLike.getEnd();
        return new DistinctSpansMap<>(backingMap.tailMap(end, true));
    }

    @Override
    public Stream<T> valuesStream() {
        return backingMap.values().stream().map(Pair::second);
    }

    @Override
    public Stream<T> descendingValuesStream() {
        return backingMap.descendingMap().values().stream().map(Pair::second);
    }

    @Override
    public DistinctSpansMap<T> insideSpan(SpanLike spanLike) {
        NavigableMap<Integer, Pair<Integer, T>> subMap = checkLastEntry(spanLike.getEnd(),
                backingMap.subMap(spanLike.getBegin(), true, spanLike.getEnd(), true));

        return new DistinctSpansMap<>(subMap);
    }

    public void clear() {
        backingMap.clear();
    }

    @Override
    public DistinctSpansMap<T> containing(SpanLike spanLike) {
        NavigableMap<Integer, Pair<Integer, T>> allSpansWithGreaterBegin = backingMap.headMap(spanLike.getBegin(), true);

        for (Map.Entry<Integer, Pair<Integer, T>> entry : allSpansWithGreaterBegin.entrySet()) {
            Integer begin = entry.getKey();
            Pair<Integer, T> pair = entry.getValue();
            Integer end = pair.first();
            T value = pair.second();

            if (end >= spanLike.getEnd()) {
                return new DistinctSpansMap<>(new TreeMap<>(Collections.singletonMap(begin, new Pair<>(end, value))));
            }
        }
        return new DistinctSpansMap<>(Collections.emptyNavigableMap());
    }

    @Override
    public Stream<SpanLike> spansStream() {
        return backingMap.entrySet().stream().map(e -> new Span(e.getKey(), e.getValue().first()));
    }

    @Override
    public Stream<Pair<SpanLike, T>> pairStream() {
        return backingMap.entrySet().stream()
                .map(e -> new Pair<>(new Span(e.getKey(), e.getValue().first()), e.getValue().second()));
    }

    public int size() {
        return backingMap.size();
    }

    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    public boolean containsKey(SpanLike spanLike) {
        Pair<Integer, T> pair = backingMap.get(spanLike.getBegin());
        return pair != null && pair.first().equals(spanLike.getEnd());
    }

    public boolean containsValue(T value) {
        return backingMap.values().stream().map(Pair::second).anyMatch(t -> t.equals(value));
    }

    public T get(SpanLike spanLike) {
        Pair<Integer, T> pair = backingMap.get(spanLike.getBegin());
        if (pair != null && pair.first().equals(spanLike.getBegin())) {
            return pair.second();
        }
        return null;
    }

    public T put(SpanLike key, T value) {
        int begin = key.getBegin();
        int end = key.getEnd();
        Map.Entry<Integer, Pair<Integer, T>> lowerEntry = backingMap.lowerEntry(end);
        if (backingMap.containsKey(begin) || (lowerEntry != null && lowerEntry.getValue().first() > begin)) {
            throw new IllegalStateException("Attempted to insert duplicate or overlapping span:");
        }
        backingMap.put(begin, new Pair<>(end, value));

        return null;
    }

    public T remove(SpanLike spanLike) {
        Pair<Integer, T> pair = backingMap.get(spanLike.getBegin());
        if (pair == null || !pair.first().equals(spanLike.getEnd())) {
            return null;
        }
        return backingMap.remove(spanLike.getBegin()).second();
    }

    public boolean overlaps(SpanLike spanLike) {
        Map.Entry<Integer, Pair<Integer, T>> floorEntry = backingMap.floorEntry(spanLike.getBegin());
        int end = floorEntry.getValue().first();
        return end > spanLike.getBegin();
    }
}
