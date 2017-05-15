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
import java.util.function.IntFunction;

/**
 *
 */
public class ImmutableSpanMap<E> implements SpansMap<E> {
    private final int[] begins;
    private final int[] ends;
    private final int[] maxEnds;
    private final Object[] values;

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
            i++;
        }


        Arrays.fill(maxEnds, -1);

        ArrayDeque<Integer> stack = new ArrayDeque<>();
        stack.push(0);
        stack.push(size - 1);

        while (true) {
            if (stack.isEmpty()) {
                break;
            }

            int right = stack.peek();
            int left = stack.peek();

            int center = ((left + right) >>> 1);

            int leftChild = ((left + center - 1) >>> 1);
            int rightChild = ((center + 1 + right) >>> 1);

            int max = ends[center];

            if (left < center) {
                int leftMax = maxEnds[leftChild];
                if (leftMax == -1) {
                    stack.push(left);
                    stack.push(center - 1);
                    continue;
                }

                max = Math.max(max, leftMax);
            }

            if (right > center) {
                int rightMax = maxEnds[rightChild];
                if (rightMax == -1) {
                    stack.push(center + 1);
                    stack.push(right);
                    continue;
                }

                max = Math.max(max, rightMax);
            }

            maxEnds[center] = max;
            stack.pop();
            stack.pop();
        }
    }

    @SuppressWarnings("unchecked")
    Label<E> labelForIndex(int index) {
        return new Label<>(
                new Span(begins[index], ends[index]),
                (E) values[index]
        );
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

    boolean indexEquals(int index, Label<E> label) {
        return index >= 0 && values[index].equals(label.getValue());
    }

    boolean beginsEqual(int firstIndex, int secondIndex) {
        return begins[firstIndex] == begins[secondIndex];
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
        List<Span> spans = spansAsList();
        return new AbstractSet<Span>() {
            @Override
            public Iterator<Span> iterator() {
                return spans.iterator();
            }

            @Override
            public int size() {
                return spans.size();
            }

            @Override
            public boolean contains(Object o) {
                return spans.contains(o);
            }
        };
    }

    @Override
    public Collection<E> values() {
        return valuesAsList();

    }

    @Override
    public Set<Label<E>> entries() {
        List<Label<E>> entries = asList();
        return new AbstractSet<Label<E>>() {
            @Override
            public Iterator<Label<E>> iterator() {
                return entries.iterator();
            }

            @Override
            public int size() {
                return entries.size();
            }

            @Override
            public boolean contains(Object o) {
                return entries.contains(o);
            }
        };
    }

    @Override
    public int size() {
        return begins.length;
    }

    @Override
    public Optional<Label<E>> first() {
        return isEmpty() ? Optional.empty() : Optional.of(labelForIndex(0));
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
        return size() == 0;
    }

    @Override
    public List<Label<E>> asList() {
        return new Entries();
    }

    @Override
    public List<Span> spansAsList() {
        return new Spans();
    }

    @Override
    public List<E> valuesAsList() {
        return new Values();
    }

    Span spanForIndex(int index) {
        return new Span(begins[index], ends[index]);
    }

    @SuppressWarnings("unchecked")
    E valueForIndex(int index) {
        return (E) values[index];
    }

    int indexOfSpan(Object o) {
        if (o instanceof Span) {
            Span span = (Span) o;
            int i = terminatingSearch(span);
            if (i >= 0) {
                return i;
            }
        }
        return -1;
    }

    int indexOfLabel(Object o) {
        if (o instanceof Label) {
            Label label = (Label) o;
            int i = terminatingSearch(label);
            if (i >= 0 && label.getValue().equals(values[i])) {
                return i;
            }
        }
        return -1;
    }

    int indexOfTextLocation(TextLocation textLocation) {
        int i = terminatingSearch(textLocation);
        if (i >= 0) {
            return i;
        }
        return -1;
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

        boolean inView(TextLocation textLocation) {
            int begin = textLocation.getBegin();
            int end = textLocation.getEnd();
            return minEnd <= end && end <= maxEnd
                    && minBegin <= begin && begin <= maxBegin;
        }

        boolean endsInView(TextLocation textLocation) {
            int end = textLocation.getEnd();
            return minEnd <= end && end <= maxEnd;
        }

        int endsInView(int index) {
            if (index != -1) {
                int end = backingMap.ends[index];
                if (minEnd <= end && end <= maxEnd) {
                    return index;
                }
            }
            return -1;
        }

        @Override
        public Optional<E> get(TextLocation textLocation) {
            if (inView(textLocation)) {
                return backingMap.get(textLocation);
            }
            return Optional.empty();
        }

        @Override
        public boolean containsLabel(Label label) {
            return inView(label) && backingMap.containsLabel(label);
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public int size() {
            return sizeInternal();
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

        abstract int nextNode(int index);

        abstract int prevNode(int index);

        int nextNodeAscending(int index) {
            while (index < right) {
                int tmp = endsInView(++index);
                if (tmp != -1) {
                    return tmp;
                }
            }
            return -1;
        }

        int nextNodeDescending(int index) {
            while (index > left) {
                int tmp = endsInView(--index);
                if (tmp != -1) {
                    return tmp;
                }
            }
            return -1;
        }

        int nextAscendingReversing(int index) {
            int tmp = index - 1;
            if (tmp >= left && backingMap.beginsEqual(tmp, index)) {
                return tmp;
            } else {
                tmp = index + 1;
                do {
                    if (index == right) {
                        return -1;
                    }
                    index = tmp;
                    tmp = index + 1;
                } while (backingMap.beginsEqual(index, tmp));
                while (backingMap.beginsEqual(index, tmp)) {
                    index = tmp;
                    if (index == right) {
                        break;
                    }
                    tmp = index + 1;
                }
            }
            return index;
        }

        int nextDescendingReversing(int index) {
            int tmp = index + 1;
            if (tmp <= right && backingMap.beginsEqual(tmp, index)) {
                return tmp;
            } else {
                tmp = index - 1;
                do {
                    if (index == left) {
                        return -1;
                    }
                    index = tmp;
                    tmp = index - 1;
                } while (backingMap.beginsEqual(index, tmp));
                while (backingMap.beginsEqual(index, tmp)) {
                    index = tmp;
                    if (index == left) {
                        break;
                    }
                    tmp = index - 1;
                }
            }
            return index;
        }

        abstract int firstIndex();

        @Override
        abstract public View<E> ascendingBegin();

        @Override
        abstract public View<E> descendingBegin();

        @Override
        abstract public View<E> ascendingEnd();

        @Override
        abstract public View<E> descendingEnd();

        @Override
        public Set<Span> spans() {
            List<Span> spans = spansAsList();
            return new AbstractSet<Span>() {
                @Override
                public Iterator<Span> iterator() {
                    return spans.iterator();
                }

                @Override
                public boolean contains(Object o) {
                    return spans.contains(o);
                }

                @Override
                public int size() {
                    return spans.size();
                }
            };
        }

        @Override
        public Collection<E> values() {
            List<E> values = valuesAsList();
            return new AbstractCollection<E>() {
                @Override
                public Iterator<E> iterator() {
                    return values.iterator();
                }

                @Override
                public int size() {
                    return values.size();
                }
            };
        }

        @Override
        public Set<Label<E>> entries() {
            List<Label<E>> entries = asList();
            return new AbstractSet<Label<E>>() {
                @Override
                public Iterator<Label<E>> iterator() {
                    return entries.iterator();
                }

                @Override
                public int size() {
                    return entries.size();
                }
            };
        }

        @Override
        public List<Label<E>> asList() {
            return new ViewEntries();
        }

        @Override
        public List<Span> spansAsList() {
            return new ViewSpans();
        }

        @Override
        public List<E> valuesAsList() {
            return new ViewValues();
        }

        @Override
        public Optional<Label<E>> first() {
            return isEmpty() ? Optional.empty() : Optional.of(backingMap.labelForIndex(firstIndex()));
        }

        abstract class ViewList<T> extends AbstractList<T> {
            @Override
            public final int size() {
                return sizeInternal();
            }

            @Override
            public final T get(int index) {
                int realIndex = firstIndex();
                for (int i = 0; i < index; i++) {
                    realIndex = nextNode(realIndex);
                    if (realIndex == -1) {
                        throw new IndexOutOfBoundsException();
                    }
                }
                return retrieve(realIndex);
            }

            @Override
            public ListIterator<T> listIterator(int index) {
                return new ViewIterator<>(this::retrieve, index);
            }

            abstract T retrieve(int index);
        }

        class ViewSpans extends ViewList<Span> {
            @Override
            Span retrieve(int index) {
                return backingMap.spanForIndex(index);
            }

            @Override
            public boolean contains(Object o) {
                return indexOf(o) != -1;
            }

            @Override
            public int indexOf(Object o) {
                return (backingMap.indexOfSpan(o));
            }

            @Override
            public int lastIndexOf(Object o) {
                return indexOf(o);
            }
        }

        class ViewValues extends ViewList<E> {
            @Override
            E retrieve(int index) {
                return backingMap.valueForIndex(index);
            }
        }

        class ViewEntries extends ViewList<Label<E>> {
            @Override
            Label<E> retrieve(int index) {
                return backingMap.labelForIndex(index);
            }

            @Override
            public boolean contains(Object o) {
                return indexOf(o) != -1;
            }

            @Override
            public int indexOf(Object o) {
                if (o instanceof TextLocation && inView((TextLocation) o)) {
                    return backingMap.indexOfLabel(o);
                }
                return -1;
            }

            @Override
            public int lastIndexOf(Object o) {
                return indexOf(o);
            }
        }

        class ViewIterator<T> implements ListIterator<T> {
            final IntFunction<T> retrieve;
            int index = firstIndex();
            boolean hasNext = index != -1;
            int localIndex = 0;

            public ViewIterator(IntFunction<T> retrieve, int index) {
                this.retrieve = retrieve;
                while (localIndex < index) {
                    if (!hasNext()) {
                        throw new IndexOutOfBoundsException();
                    }
                    next();
                }
            }

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public T next() {
                int cur = index;
                hasNext = (index = nextNode(index)) != -1;
                localIndex++;
                return retrieve.apply(cur);
            }

            @Override
            public boolean hasPrevious() {
                return localIndex > 0;
            }

            @Override
            public T previous() {
                hasNext = (index = prevNode(index)) != -1;
                localIndex--;
                return retrieve.apply(index);
            }

            @Override
            public int nextIndex() {
                return localIndex + 1;
            }

            @Override
            public int previousIndex() {
                return localIndex - 1;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void set(T t) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void add(T t) {
                throw new UnsupportedOperationException();
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
        int nextNode(int index) {
            return nextNodeAscending(index);
        }

        @Override
        int prevNode(int index) {
            return nextNodeDescending(index);
        }

        @Override
        int firstIndex() {
            return left <= right ? left : -1;
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
        int nextNode(int index) {
            return nextAscendingReversing(index);
        }

        @Override
        int prevNode(int index) {
            return nextDescendingReversing(index);
        }

        @Override
        int firstIndex() {
            if (left > right) {
                return -1;
            }

            int index = left;
            int antecedent = index + 1;
            while (backingMap.beginsEqual(index, antecedent)) {
                if (antecedent == right) {
                    return antecedent;
                }
                index = antecedent;
                antecedent = index + 1;
            }

            return index;
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
        int nextNode(int index) {
            return nextNodeDescending(index);
        }

        @Override
        int prevNode(int index) {
            return nextNodeAscending(index);
        }

        @Override
        int firstIndex() {
            return left <= right ? right : -1;
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
        View<E> copy(int minBegin, int maxBegin, int minEnd, int maxEnd) {
            return new DescendingReversingView<>(backingMap, minBegin,
                    maxBegin, minEnd, maxEnd);
        }

        @Override
        int nextNode(int index) {
            return nextDescendingReversing(index);
        }

        @Override
        int prevNode(int index) {
            return nextAscendingReversing(index);
        }

        @Override
        int firstIndex() {
            if (left > right) {
                return -1;
            }

            int index = right;
            int antecedent = index - 1;
            while (backingMap.beginsEqual(index, antecedent)) {
                if (antecedent == left) {
                    return antecedent;
                }
                index = antecedent;
                antecedent = index - 1;
            }

            return index;
        }
    }

    class Spans extends AbstractList<Span> implements RandomAccess {
        @Override
        public Span get(int index) {
            return spanForIndex(index);
        }

        @Override
        public int size() {
            return begins.length;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) != -1;
        }

        @Override
        public int indexOf(Object o) {
            return indexOfSpan(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return indexOf(o);
        }
    }

    class Values extends AbstractList<E> implements RandomAccess {
        @Override
        public E get(int index) {
            return valueForIndex(index);
        }

        @Override
        public int size() {
            return begins.length;
        }
    }

    class Entries extends AbstractList<Label<E>> implements RandomAccess {
        @Override
        public Label<E> get(int index) {
            return labelForIndex(index);
        }

        @Override
        public int size() {
            return begins.length;
        }

        @Override
        public boolean contains(Object o) {
            return containsEntry(o);
        }

        @Override
        public int indexOf(Object o) {
            return indexOfLabel(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return indexOfLabel(o);
        }
    }
}
