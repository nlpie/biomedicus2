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

/**
 *
 */
public class ImmutableSpanMap<E> implements SpansMap<E> {
    private final int[] begins;
    private final int[] ends;
    private final int[] maxEnds;
    private final Object[] values;

    @Nullable private transient Set<Label<E>> entriesView;

    @Nullable private transient Collection<E> valuesView;

    ImmutableSpanMap(int[] begins, int[] ends, int[] maxEnds, E[] values) {
        this.begins = begins;
        this.ends = ends;
        this.maxEnds = maxEnds;
        this.values = values;
    }

    public ImmutableSpanMap(OrderedSpanMap<E> orderedSpanMap) {
        int size = orderedSpanMap.size();
        this.begins = new int[size];
        this.ends = new int[size];
        this.maxEnds = new int[size];
        this.values = new Object[size];

        int i = 0;
        for (Label<E> label : orderedSpanMap.entries()) {
            begins[i] = label.getBegin();
            ends[i] = label.getEnd();
            values[i] = label.getValue();
        }

        int left = 0, right = size - 1;
        int center = ((left + right) >>> 1);
        int depth = 0;
        while (center > left) {
            right = center - 1;
            center = ((left + right) >>> 1);
            depth++;
        }

        while (true) {
            if (left == right) {

            }
        }
    }

    @SuppressWarnings("unchecked")
    Label<E> exportLabel(int index) {
        return new Label<>(new Span(begins[index], ends[index]),
                (E) values[index]);
    }

    int insertionIndex(int begin, int end) {
        int lo = 0, hi = begins.length - 1;

        while (lo <= hi) {
            int mid = (hi + lo) >>> 1;
            int midBegin = begins[mid];
            if (midBegin < begin) {
                lo = mid + 1;
            } else if (midBegin > begin) {
                hi = mid - 1;
            } else {
                int midEnd = ends[mid];
                if (midEnd < end) {
                    lo = mid + 1;
                } else if (midEnd > end) {
                    hi = mid - 1;
                } else {
                    return mid;
                }
            }
        }
        return -(lo + 1);
    }

    int terminatingSearch(int begin, int end) {
        int lo = 0, hi = begins.length - 1;

        while (lo <= hi) {
            int mid = (hi + lo) >>> 1;
            if (end > maxEnds[mid]) {
                return -1;
            }

            int midBegin = begins[mid];
            if (midBegin < begin) {
                lo = mid + 1;
            } else if (midBegin > begin) {
                hi = mid - 1;
            } else {
                int midEnd = ends[mid];
                if (midEnd < end) {
                    lo = mid + 1;
                } else if (midEnd > end) {
                    hi = mid - 1;
                } else {
                    return mid;
                }
            }
        }
        return -1;
    }

    int ceilingIndex(int begin, int end) {
        int lo = 0, hi = begins.length - 1;

        while (lo <= hi) {
            int mid = (hi + lo) >>> 1;
            int midBegin = begins[mid];
            if (midBegin < begin) {
                lo = mid + 1;
            } else if (midBegin > begin) {
                hi = mid - 1;
            } else {
                int midEnd = ends[mid];
                if (midEnd < end) {
                    lo = mid + 1;
                } else if (midEnd > end) {
                    hi = mid - 1;
                } else {
                    return mid;
                }
            }
        }
        return lo;
    }

    int lowerIndex(int begin, int end) {
        int lo = 0, hi = begins.length - 1;

        while (lo <= hi) {
            int mid = (hi + lo) >>> 1;
            int midBegin = begins[mid];
            if (midBegin < begin) {
                lo = mid + 1;
            } else if (midBegin > begin) {
                hi = mid - 1;
            } else {
                int midEnd = ends[mid];
                if (midEnd < end) {
                    lo = mid + 1;
                } else if (midEnd > end) {
                    hi = mid - 1;
                } else {
                    return mid + 1;
                }
            }
        }
        return lo;
    }

    int floorIndex(int begin, int end) {
        int lo = 0, hi = begins.length - 1;

        while (lo <= hi) {
            int mid = (hi + lo) >>> 1;
            int midBegin = begins[mid];
            if (midBegin < begin) {
                lo = mid + 1;
            } else if (midBegin > begin) {
                hi = mid - 1;
            } else {
                int midEnd = ends[mid];
                if (midEnd < end) {
                    lo = mid + 1;
                } else if (midEnd > end) {
                    hi = mid - 1;
                } else {
                    return mid;
                }
            }
        }
        return lo > 0 ? lo - 1 : -1;
    }

    int terminatingSearch(TextLocation textLocation) {
        return terminatingSearch(textLocation.getBegin(),
                textLocation.getEnd());
    }

    @SuppressWarnings("unchecked")
    @Nullable
    E getInternal(TextLocation textLocation) {
        int index = terminatingSearch(textLocation);
        if (index < 0) {
            return null;
        }
        return (E) values[index];
    }

    @Override
    public Optional<E> get(TextLocation textLocation) {
        return Optional.ofNullable(getInternal(textLocation));
    }

    @Override
    public SpansMap<E> toTheLeftOf(int index) {
        return new AscendingView<>(this, 0, index, 0, index);
    }

    @Override
    public SpansMap<E> toTheRightOf(int index) {
        return new AscendingView<>(this, index, Integer.MAX_VALUE, index,
                Integer.MAX_VALUE);
    }

    @Override
    public SpansMap<E> insideSpan(TextLocation textLocation) {
        int min = textLocation.getBegin();
        int max = textLocation.getEnd();
        return new AscendingView<>(this, min, max, min, max);
    }

    @Override
    public SpansMap<E> containing(TextLocation textLocation) {
        return new AscendingView<>(this, 0,
                textLocation.getBegin(), textLocation.getEnd(),
                Integer.MAX_VALUE);
    }

    @Override
    public SpansMap<E> ascendingBegin() {
        return this;
    }

    @Override
    public SpansMap<E> descendingBegin() {
        return new DescendingReversingView<>(this, 0, Integer.MAX_VALUE, 0,
                begins.length - 1);
    }

    @Override
    public SpansMap<E> ascendingEnd() {
        return this;
    }

    @Override
    public SpansMap<E> descendingEnd() {
        return new AscendingReversingView<>(this, 0, Integer.MAX_VALUE, 0,
                begins.length - 1);
    }

    @Override
    public Set<Span> spans() {
        return null;
    }

    @Override
    public Collection<E> values() {
        return valuesView != null ? valuesView : (valuesView = new Values());
    }

    @Override
    public Set<Label<E>> entries() {
        return entriesView != null ? entriesView
                : (entriesView = new Entries());
    }

    @Override
    public int size() {
        return begins.length;
    }

    boolean containsEntry(Object o) {
        if (!(o instanceof Label)) {
            return false;
        }
        Label label = (Label) o;
        E e = getInternal(label);
        return label.value().equals(e);
    }

    @Override
    public boolean containsLabel(Label label) {
        E e = getInternal(label);
        return label.value().equals(e);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public List<Label<E>> asList() {
        return null;
    }

    @Override
    public List<Span> spansAsList() {
        return null;
    }

    @Override
    public List<E> valuesAsList() {
        return null;
    }

    static abstract class View<E> implements SpansMap<E> {
        final ImmutableSpanMap<E> backingMap;
        final int minBegin;
        final int maxBegin;
        final int minEnd;
        final int maxEnd;
        final int left;
        final int right;

        transient int size = -1;

        View(ImmutableSpanMap<E> backingMap,
             int minBegin,
             int maxBegin,
             int minEnd, int maxEnd) {
            this.backingMap = backingMap;
            this.minBegin = minBegin;
            this.maxBegin = maxBegin;
            this.minEnd = minEnd;
            this.maxEnd = maxEnd;
            this.left = backingMap.ceilingIndex(minBegin, minEnd);
            this.right = backingMap.floorIndex(maxBegin, maxEnd);
        }

        boolean check(TextLocation textLocation) {
            int begin = textLocation.getBegin();
            return minBegin <= begin && begin <= maxBegin;
        }

        @Override
        public Optional<E> get(TextLocation textLocation) {
            if (check(textLocation)) {
                return backingMap.get(textLocation);
            }
            return Optional.empty();
        }

        @Override
        public boolean containsLabel(Label label) {
            int begin = label.getBegin();
            return begin >= minBegin && begin <= maxBegin
                    && backingMap.containsLabel(label);
        }

        int sizeInternal() {
            if (size == -1) {
                int size = 0;
                for (E ignored : values()) {
                    size++;
                }
                this.size = size;
            }
            return size;
        }

        abstract View<E> copy(int minBegin,
                              int maxBegin,
                              int minEnd,
                              int maxEnd);

        @Override
        public SpansMap<E> toTheLeftOf(int index) {
            if (index < maxBegin) {
                return copy(minBegin, index, minEnd, maxEnd);
            } else {
                return this;
            }
        }

        @Override
        public SpansMap<E> toTheRightOf(int index) {
            if (index > minBegin) {
                return copy(index, maxBegin, minEnd, maxEnd);
            } else {
                return this;
            }
        }

        @Override
        public SpansMap<E> insideSpan(TextLocation textLocation) {
            int min = textLocation.getBegin();
            int max = textLocation.getEnd();
            if (min > minBegin) {
                if (max < maxBegin) {
                    return copy(min, max, minEnd, maxEnd);
                } else {
                    return copy(min, maxBegin, minEnd, maxEnd);
                }
            } else {
                if (max < maxBegin) {
                    return copy(minBegin, max, minEnd, maxEnd);
                } else {
                    return this;
                }
            }
        }

        @Override
        public SpansMap<E> containing(TextLocation textLocation) {
            int max = textLocation.getBegin();
            int minEnd = textLocation.getEnd();
            View<E> result;
            if (max < maxBegin) {
                if (minEnd > this.minBegin) {
                    result = copy(minBegin, max, minEnd, maxEnd);
                } else {
                    result = copy(minBegin, max, this.minEnd, maxEnd);
                }
            } else {
                result = this;
            }
            return result;
        }

        @Override
        abstract public View<E> ascendingBegin();

        @Override
        abstract public View<E> descendingBegin();

        @Override
        abstract public View<E> ascendingEnd();

        @Override
        abstract public View<E> descendingEnd();

        abstract class ViewValues extends AbstractCollection<E> {
            @Override
            public int size() {
                return sizeInternal();
            }
        }

        abstract class ViewEntries extends AbstractSet<Label<E>> {
            @Override
            public int size() {
                return sizeInternal();
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Label)) {
                    return false;
                }
                Label label = (Label) o;
                int begin = label.getBegin();
                int end = label.getEnd();
                if (begin >= minBegin && begin <= maxBegin
                        && end >= minEnd && end <= maxEnd) {
                    Object value = label.value();
                    int i = backingMap.terminatingSearch(begin, end);
                    if (i >= 0) {
                        return backingMap.values[i].equals(value);
                    }
                }
                return false;
            }
        }
    }

    static class AscendingView<E> extends View<E> {
        AscendingView(ImmutableSpanMap<E> backingMap,
                      int minBegin,
                      int maxBegin,
                      int minEnd,
                      int maxEnd) {
            super(backingMap, minBegin, maxBegin, minEnd, maxEnd);
        }

        @Override
        View<E> copy(int minBegin, int maxBegin, int minEnd, int maxEnd) {
            return new AscendingView<>(backingMap, minBegin, maxBegin, minEnd,
                    maxEnd);
        }

        @Override
        public View<E> ascendingBegin() {
            return this;
        }

        @Override
        public View<E> descendingBegin() {
            return new DescendingReversingView<>(backingMap, minBegin,
                    maxBegin, minEnd, maxEnd);
        }

        @Override
        public View<E> ascendingEnd() {
            return this;
        }

        @Override
        public View<E> descendingEnd() {
            return new AscendingReversingView<>(backingMap, minBegin, maxBegin,
                    minEnd, maxEnd);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Collection<E> values() {
            return new ViewValues() {
                @Override
                public Iterator<E> iterator() {
                    return new AscendingIterator<E>() {
                        @Override
                        public E next() {
                            if (index > right) {
                                throw new NoSuchElementException();
                            }
                            return (E) backingMap.values[index++];
                        }
                    };
                }
            };
        }

        @Override
        public Set<Label<E>> entries() {
            return new ViewEntries() {
                @Override
                public Iterator<Label<E>> iterator() {
                    return new AscendingIterator<Label<E>>() {
                        @Override
                        public Label<E> next() {
                            if (index > right) {
                                throw new NoSuchElementException();
                            }
                            return backingMap.exportLabel(index++);
                        }
                    };
                }
            };
        }

        abstract class AscendingIterator<T> implements Iterator<T> {
            int index = left;

            @Override
            public boolean hasNext() {
                return index <= right;
            }
        }
    }

    static class AscendingReversingView<E> extends View<E> {

        AscendingReversingView(ImmutableSpanMap<E> backingMap,
                               int minBegin,
                               int maxBegin,
                               int minEnd,
                               int maxEnd) {
            super(backingMap, minBegin, maxBegin, minEnd, maxEnd);
        }

        @Override
        View<E> copy(int minBegin, int maxBegin, int minEnd, int maxEnd) {
            return new AscendingReversingView<>(backingMap, minBegin, maxBegin,
                    minEnd, maxEnd);
        }

        @Override
        public View<E> ascendingBegin() {
            return this;
        }

        @Override
        public View<E> descendingBegin() {
            return new DescendingView<>(backingMap, minBegin, maxBegin, minEnd,
                    maxEnd);
        }

        @Override
        public View<E> ascendingEnd() {
            return new AscendingView<>(backingMap, minBegin, maxBegin, minEnd,
                    maxEnd);
        }

        @Override
        public View<E> descendingEnd() {
            return this;
        }


        @Override
        public Collection<E> values() {
            return new ViewValues() {
                @Override
                public Iterator<E> iterator() {
                    return new AscendingReversingIterator<E>(left, right) {
                        @SuppressWarnings("unchecked")
                        @Override
                        public E next() {
                            E val = (E) backingMap.values[index];
                            advance();
                            return val;
                        }
                    };
                }
            };
        }

        @Override
        public Set<Label<E>> entries() {
            return new ViewEntries() {
                @Override
                public Iterator<Label<E>> iterator() {
                    return new AscendingReversingIterator<Label<E>>(left,
                            right) {
                        @Override
                        public Label<E> next() {
                            Label<E> label = backingMap.exportLabel(index);
                            advance();
                            return label;
                        }
                    };
                }
            };
        }

        abstract class AscendingReversingIterator<T> implements Iterator<T> {
            final int end;
            int index, reverseBound, mark;

            AscendingReversingIterator(int start, int end) {
                reverseBound = index = start - 1;
                mark = start;
                this.end = end;
                advance();
            }

            void advance() {
                if (index > end) {
                    return;
                }

                if (index == reverseBound) {
                    index = reverseBound = mark;
                    int val = backingMap.begins[index];
                    while (backingMap.begins[index] == val) {
                        index++;
                    }
                    mark = index;
                }
                index--;
            }

            @Override
            public boolean hasNext() {
                return index <= end;
            }
        }
    }

    static class DescendingView<E> extends View<E> {
        DescendingView(ImmutableSpanMap<E> backingMap,
                       int minBegin,
                       int maxBegin,
                       int minEnd,
                       int maxEnd) {
            super(backingMap, minBegin, maxBegin, minEnd, maxEnd);
        }

        @Override
        View<E> copy(int minBegin, int maxBegin, int minEnd, int maxEnd) {
            return new DescendingView<>(backingMap, minBegin, maxBegin, minEnd,
                    maxEnd);
        }

        @Override
        public View<E> ascendingBegin() {
            return new AscendingReversingView<>(backingMap, minBegin, maxBegin,
                    minEnd, maxEnd);
        }

        @Override
        public View<E> descendingBegin() {
            return this;
        }

        @Override
        public View<E> ascendingEnd() {
            return new DescendingReversingView<>(backingMap, minBegin,
                    maxBegin, minEnd, maxEnd);
        }

        @Override
        public View<E> descendingEnd() {
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Collection<E> values() {
            return new ViewValues() {
                @Override
                public Iterator<E> iterator() {
                    return new DescendingIterator<E>() {
                        @Override
                        public E next() {
                            return (E) backingMap.values[index++];
                        }
                    };
                }
            };
        }

        @Override
        public Set<Label<E>> entries() {
            return new ViewEntries() {
                @Override
                public Iterator<Label<E>> iterator() {
                    return new DescendingIterator<Label<E>>() {
                        @Override
                        public Label<E> next() {
                            return backingMap.exportLabel(index++);
                        }
                    };
                }
            };
        }

        abstract class DescendingIterator<T> implements Iterator<T> {
            int index = right;

            @Override
            public boolean hasNext() {
                return index >= left;
            }
        }
    }

    static class DescendingReversingView<E> extends View<E> {
        DescendingReversingView(ImmutableSpanMap<E> backingMap,
                                int minBegin,
                                int maxBegin,
                                int minEnd,
                                int maxEnd) {
            super(backingMap, minBegin, maxBegin, minEnd, maxEnd);
        }

        @Override
        public View<E> ascendingBegin() {
            return new AscendingView<>(backingMap, minBegin, maxBegin, minEnd,
                    maxEnd);
        }

        @Override
        public View<E> descendingBegin() {
            return this;
        }

        @Override
        public View<E> ascendingEnd() {
            return this;
        }

        @Override
        public View<E> descendingEnd() {
            return new AscendingView<>(backingMap, minBegin, maxBegin, minEnd,
                    maxEnd);
        }


        @Override
        public Collection<E> values() {
            return new ViewValues() {
                @Override
                public Iterator<E> iterator() {
                    return new DescendingReversingIterator<E>(right, left) {
                        @SuppressWarnings("unchecked")
                        @Override
                        E retrieve(int index) {
                            return (E) backingMap.values[index];
                        }
                    };
                }
            };
        }

        @Override
        public Set<Label<E>> entries() {
            return new ViewEntries() {
                @Override
                public Iterator<Label<E>> iterator() {
                    return new DescendingReversingIterator<Label<E>>(right,
                            left) {
                        @Override
                        Label<E> retrieve(int index) {
                            return backingMap.exportLabel(index);
                        }
                    };
                }
            };
        }

        @Override
        View<E> copy(int minBegin, int maxBegin, int minEnd, int maxEnd) {
            return new DescendingReversingView<>(backingMap, minBegin,
                    maxBegin, minEnd, maxEnd);
        }

        abstract class DescendingReversingIterator<T> implements Iterator<T> {
            final int end;
            int index, reverseBound, mark;

            DescendingReversingIterator(int start, int end) {
                reverseBound = index = start + 1;
                mark = start;
                this.end = end;
                advanceInner();
            }

            void advanceOuter() {
                int end;
                do {
                    if (!advanceInner()) {
                        break;
                    }
                    end = backingMap.ends[index];
                } while (end < minEnd || end > maxEnd);
            }

            boolean advanceInner() {
                if (index < end) {
                    return false;
                }

                if (index == reverseBound) {
                    index = reverseBound = mark;
                    int val = backingMap.begins[index];
                    while (backingMap.begins[index] == val) {
                        index--;
                    }
                    mark = index;
                }
                index++;
                return true;
            }

            @Override
            public T next() {
                if (index < end) {
                    throw new NoSuchElementException();
                }

                T val = retrieve(index);
                advanceOuter();
                return val;
            }

            abstract T retrieve(int index);

            @Override
            public boolean hasNext() {
                return index >= end;
            }
        }
    }

    class Values extends AbstractCollection<E> implements RandomAccess {
        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                int index = 0;

                @Override
                public boolean hasNext() {
                    return index < begins.length;
                }

                @SuppressWarnings("unchecked")
                @Override
                public E next() {
                    return (E) values[index++];
                }
            };
        }

        @Override
        public int size() {
            return begins.length;
        }
    }

    class Entries extends AbstractSet<Label<E>> {
        @Override
        public boolean contains(Object o) {
            return containsEntry(o);
        }

        @Override
        public Iterator<Label<E>> iterator() {
            return new Iterator<Label<E>>() {
                int index = 0;

                @Override
                public boolean hasNext() {
                    return index < begins.length;
                }

                @SuppressWarnings("unchecked")
                @Override
                public Label<E> next() {
                    Label<E> label = new Label<>(new Span(begins[index],
                            ends[index]), (E) values[index]);
                    index++;
                    return label;
                }
            };
        }

        @Override
        public int size() {
            return begins.length;
        }
    }
}
