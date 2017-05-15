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

import javax.annotation.Nullable;
import java.util.*;

public class OrderedSpanMap<T> implements SpansMap<T> {
    private final NavigableMap<Integer, NavigableMap<Integer, T>> backingTree;
    private final int endMin;
    private final int endMax;
    private final boolean beginDescending;
    private final boolean endDescending;

    private transient int size;

    public OrderedSpanMap() {
        backingTree = new TreeMap<>();
        size = 0;
        endMin = 0;
        endMax = Integer.MAX_VALUE;
        beginDescending = false;
        endDescending = false;
    }

    private OrderedSpanMap(NavigableMap<Integer, NavigableMap<Integer, T>> backingTree,
                           int endMin,
                           int endMax,
                           boolean beginDescending,
                           boolean endDescending) {
        this.backingTree = backingTree;
        this.endMin = endMin;
        this.endMax = endMax;
        this.beginDescending = beginDescending;
        this.endDescending = endDescending;
    }

    public int size() {
        return (size != -1) ? size
                : (
                        size = backingTree.values().stream()
                                .map(endMap -> endMap
                                        .subMap(endMin, true, endMax, true))
                                .mapToInt(Map::size)
                                .sum()
                );
    }

    @Override
    public Optional<Label<T>> first() {
        Map.Entry<Integer, NavigableMap<Integer, T>> e
                = backingTree.firstEntry();
        if (e != null) {
            Map.Entry<Integer, T> endEntry = e.getValue().firstEntry();
            return Optional.of(new Label<>(e.getKey(), endEntry.getKey(),
                    endEntry.getValue()));
        }
        return Optional.empty();
    }

    public boolean isEmpty() {
        return size() != 0;
    }

    @Override
    public List<Label<T>> asList() {
        return new ArrayList<>(entries());
    }

    @Override
    public List<Span> spansAsList() {
        return new ArrayList<>(spans());
    }

    @Override
    public List<T> valuesAsList() {
        return new ArrayList<>(values());
    }

    public boolean containsKey(Object key) {
        if (!(key instanceof TextLocation)) {
            return false;
        }
        return false;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean containsValue(Object value) {
        return values().contains(value);
    }

    public T put(TextLocation textLocation, T object) {
        size = -1;
        return backingTree
                .computeIfAbsent(textLocation.getBegin(), k -> new TreeMap<>())
                .put(textLocation.getEnd(), object);
    }

    @Nullable
    public T remove(Object key) {
        size = -1;
        if (!(key instanceof TextLocation)) {
            return null;
        }
        TextLocation textLocation = (TextLocation) key;
        NavigableMap<Integer, T> endMap = backingTree
                .get(textLocation.getBegin());
        if (endMap == null) {
            throw new IllegalArgumentException("Tree does not contain span");
        }
        return endMap.remove(textLocation.getEnd());
    }

    public void putAll(Map<? extends TextLocation, ? extends T> m) {
        m.forEach(this::put);
    }

    @Override
    public Optional<T> get(TextLocation textLocation) {
        if (textLocation.getEnd() < endMin || textLocation.getEnd() > endMax) {
            return Optional.empty();
        }

        NavigableMap<Integer, T> endMap = backingTree
                .get(textLocation.getBegin());
        if (endMap == null) {
            return Optional.empty();
        }

        T t = endMap.get(textLocation.getEnd());
        if (t == null) {
            return Optional.empty();
        }
        return Optional.of(t);
    }

    @Override
    public SpansMap<T> toTheLeftOf(int index) {
        NavigableMap<Integer, NavigableMap<Integer, T>> headMap = backingTree
                .headMap(index, false);
        int newEndMax = Math.min(index, endMax);
        return new OrderedSpanMap<>(headMap, endMin, newEndMax, beginDescending,
                endDescending);
    }

    @Override
    public SpansMap<T> toTheRightOf(int index) {
        NavigableMap<Integer, NavigableMap<Integer, T>> tailMap = backingTree
                .tailMap(index, true);
        int newEndMin = Math.max(index, endMin);
        return new OrderedSpanMap<>(tailMap, newEndMin, endMax, beginDescending,
                endDescending);
    }

    public void clear() {
        backingTree.values().forEach(
                endMap -> endMap.subMap(endMin, true, endMax, true).clear());
    }

    @Override
    public OrderedSpanMap<T> insideSpan(TextLocation textLocation) {
        int newEndMax = Math.min(endMax, textLocation.getEnd());
        return new OrderedSpanMap<>(
                backingTree.tailMap(textLocation.getBegin(), true), endMin,
                newEndMax, beginDescending, endDescending);
    }

    @Override
    public OrderedSpanMap<T> containing(TextLocation textLocation) {
        int newEndMin = Math.max(endMin, textLocation.getEnd());
        return new OrderedSpanMap<>(
                backingTree.headMap(textLocation.getBegin(), true), newEndMin,
                endMax, beginDescending, endDescending);
    }

    @Override
    public SpansMap<T> ascendingBegin() {
        return new OrderedSpanMap<>(backingTree, endMin, endMax,
                false, endDescending);
    }

    @Override
    public SpansMap<T> descendingBegin() {
        return new OrderedSpanMap<>(backingTree, endMin, endMax,
                true, endDescending);
    }

    @Override
    public SpansMap<T> ascendingEnd() {
        return new OrderedSpanMap<>(backingTree, endMin, endMax,
                beginDescending, false);
    }

    @Override
    public SpansMap<T> descendingEnd() {
        return new OrderedSpanMap<>(backingTree, endMin, endMax,
                beginDescending, true);
    }

    @Override
    public Set<Span> spans() {
        return new AbstractSet<Span>() {
            @Override
            public Iterator<Span> iterator() {
                return new Iterator<Span>() {
                    EntryIterator<?> ei = entryIt();

                    @Override
                    public boolean hasNext() {
                        return ei.hasNext();
                    }

                    @Override
                    public Span next() {
                        return ei.next().toSpan();
                    }
                };
            }

            @Override
            public int size() {
                return OrderedSpanMap.this.size();
            }
        };
    }

    @Override
    public Collection<T> values() {
        return new AbstractCollection<T>() {
            @Override
            public Iterator<T> iterator() {
                return new ValueIt<>(entryIt());
            }

            @Override
            public int size() {
                return OrderedSpanMap.this.size();
            }
        };
    }

    @Override
    public Set<Label<T>> entries() {
        return new AbstractSet<Label<T>>() {
            @Override
            public Iterator<Label<T>> iterator() {
                return entryIt();
            }

            @Override
            public int size() {
                return OrderedSpanMap.this.size();
            }
        };
    }

    @Override
    public boolean containsLabel(Label label) {
        return false;
    }

    EntryIterator<T> entryIt() {
        return new EntryIterator<>(backingTree, beginDescending, endDescending,
                endMin, endMax);
    }

    static class ValueIt<T> implements Iterator<T> {
        private final EntryIterator<T> ei;

        ValueIt(EntryIterator<T> ei) {
            this.ei = ei;
        }


        @Override
        public boolean hasNext() {
            return ei.hasNext();
        }

        @Override
        public T next() {
            return ei.next().value();
        }
    }

    static class EntryIterator<T> implements Iterator<Label<T>> {
        private final Iterator<Map.Entry<Integer, NavigableMap<Integer, T>>>
                beginIterator;
        private final boolean endDescending;
        private final int endMin;
        private final int endMax;

        @Nullable private Iterator<Map.Entry<Integer, T>> endIterator;

        private int begin = 0;

        @Nullable private Label<T> next;

        EntryIterator(
                NavigableMap<Integer, NavigableMap<Integer, T>> backingTree,
                boolean beginDescending,
                boolean endDescending,
                int endMin,
                int endMax
        ) {
            backingTree = beginDescending ? backingTree.descendingMap()
                    : backingTree;
            beginIterator = backingTree.entrySet().iterator();
            this.endDescending = endDescending;
            this.endMin = endMin;
            this.endMax = endMax;
            advance();
        }

        private void advance() {
            next = null;
            while (endIterator == null || !endIterator.hasNext()) {
                if (!beginIterator.hasNext()) {
                    return;
                }
                Map.Entry<Integer, NavigableMap<Integer, T>> nextBegin
                        = beginIterator.next();
                begin = nextBegin.getKey();
                NavigableMap<Integer, T> nextEnd = nextBegin.getValue();
                if (endDescending) {
                    nextEnd = nextEnd.descendingMap();
                }
                SortedMap<Integer, T> subMap = nextEnd.subMap(endMin, endMax);
                endIterator = subMap.entrySet().iterator();
            }
            Map.Entry<Integer, T> nextEnd = endIterator.next();
            Integer end = nextEnd.getKey();
            Span span = new Span(begin, end);
            T value = nextEnd.getValue();
            next = new Label<>(span, value);
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Label<T> next() {
            Label<T> next = this.next;
            if (next == null) {
                throw new NoSuchElementException();
            }
            advance();
            return next;
        }
    }
}
