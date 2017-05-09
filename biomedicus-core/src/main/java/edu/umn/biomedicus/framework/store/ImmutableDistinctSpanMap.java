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

/**
 *
 */
public class ImmutableDistinctSpanMap<E> implements SpansMap<E> {
    private final int[] begins;
    private final Node[] nodes;

    ImmutableDistinctSpanMap(int[] begins, Node[] nodes) {
        this.begins = begins;
        this.nodes = nodes;
    }

    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<E> get(TextLocation textLocation) {
        int result = Arrays.binarySearch(begins, textLocation.getBegin());
        if (result >= 0 && textLocation.getEnd() == nodes[result].end) {
            return Optional.of((E) nodes[result].value);
        }
        return Optional.empty();
    }

    @Override
    public SpansMap<E> toTheLeftOf(int index) {
        int i = lower(index);
        return new AscendingView<>(this, 0, i);
    }

    @Override
    public SpansMap<E> toTheRightOf(int index) {
        int i = higher(index);
        return new AscendingView<>(this, i, nodes.length - 1);
    }

    @Override
    public SpansMap<E> insideSpan(TextLocation textLocation) {
        int begin = higher(textLocation.getBegin());
        int end = lower(textLocation.getEnd());
        return new AscendingView<>(this, begin, end);
    }

    @Override
    public SpansMap<E> containing(TextLocation textLocation) {
        int begin = lower(textLocation.getBegin());
        if (nodes[begin].end >= textLocation.getEnd()) {
            return new AscendingView<>(this, begin, begin);
        } else {
            return new AscendingView<>(this, 0, -1);
        }
    }

    @Override
    public SpansMap<E> ascendingBegin() {
        return this;
    }

    @Override
    public SpansMap<E> descendingBegin() {
        return new DescendingView<>(this, 0, nodes.length - 1);
    }

    @Override
    public SpansMap<E> ascendingEnd() {
        return this;
    }

    @Override
    public SpansMap<E> descendingEnd() {
        return this;
    }

    @Override
    public Set<Span> spans() {
        return new AbstractSet<Span>() {
            @Override
            public Iterator<Span> iterator() {
                return new Iterator<Span>() {
                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < nodes.length;
                    }

                    @Override
                    public Span next() {
                        return spanAtIndex(index++);
                    }
                };
            }

            @Override
            public int size() {
                return 0;
            }
        };
    }

    @Override
    public Collection<E> values() {
        return new AbstractCollection<E>() {
            @Override
            public Iterator<E> iterator() {
                return new Iterator<E>() {
                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < nodes.length;
                    }

                    @Override
                    public E next() {
                        return valueAtIndex(index++);
                    }
                };
            }

            @Override
            public int size() {
                return nodes.length;
            }
        };
    }

    @Override
    public Set<Label<E>> entries() {
        return new AbstractSet<Label<E>>() {
            @Override
            public Iterator<Label<E>> iterator() {
                return new Iterator<Label<E>>() {
                    int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < nodes.length;
                    }

                    @Override
                    public Label<E> next() {
                        return labelAtIndex(index++);
                    }
                };
            }

            @Override
            public int size() {
                return nodes.length;
            }

            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Label)) {
                    return false;
                }
                Label label = (Label) o;
                return containsLabel(label);
            }
        };
    }

    @Override
    public boolean containsLabel(Label label) {
        int i = Arrays.binarySearch(begins, label.getBegin());

        if (i < 0) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Node<E> node = (Node<E>) nodes[i];
        return node.end == label.end()
                && label.value().equals(node.value);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public List<Label<E>> asList() {
        return new LabelsListView<>(this);
    }

    @Override
    public List<Span> spansAsList() {
        return new SpansListView(this);
    }

    @Override
    public List<E> valuesAsList() {
        return new ValueListView<>(this);
    }

    @Override
    public int size() {
        return begins.length;
    }

    int lower(int index) {
        int i = Arrays.binarySearch(begins, index);
        if (i < 0) {
            i = -(i - 1);
            while (i >= 0) {
                if (nodes[i].end <= index) {
                    break;
                }
                i--;
            }
        } else {
            i = i - 1;
        }
        return i;
    }

    int higher(int index) {
        int i = Arrays.binarySearch(begins, index);
        if (i < 0) {
            i = -(i - 1);
        }
        return i;
    }

    int indexOfSpan(Object o) {
        if (o instanceof Span) {
            Span span = (Span) o;
            int result = Arrays.binarySearch(begins, span.getBegin());
            if (result >= 0 && nodes[result].end == span.getEnd()) {
                return result;
            }
        }
        return -1;
    }

    int indexOfLabel(Object o) {
        if (o instanceof Label) {
            Label label = (Label) o;
            int result = Arrays.binarySearch(begins, label.getBegin());
            if (result >= 0) {
                Node node = nodes[result];
                if (node.end == label.getEnd()
                        && node.value.equals(label.getValue())) {
                    return result;
                }
            }
        }
        return -1;
    }

    Span spanAtIndex(int index) {
        return new Span(begins[index], nodes[index].end);
    }

    @SuppressWarnings("unchecked")
    E valueAtIndex(int index) {
        return (E) nodes[index].value;
    }

    @SuppressWarnings("unchecked")
    Label<E> labelAtIndex(int index) {
        Node node = nodes[index];
        return new Label<>(new Span(begins[index], node.end), (E) node.value);
    }

    public static class Builder<E> {
        private Map<Integer, Node<E>> map = new TreeMap<>();

        public Builder<E> add(Span span, E element) {
            return add(span.getBegin(), span.getEnd(), element);
        }

        public Builder<E> add(Label<E> label) {
            return add(label.getBegin(), label.getEnd(), label.getValue());
        }

        public Builder<E> add(int begin, int end, E element) {
            map.put(begin, new Node<>(end, element));
            return this;
        }

        public Builder<E> addAll(Iterable<Label<E>> all) {
            for (Label<E> label : all) {
                add(label);
            }
            return this;
        }

        public ImmutableDistinctSpanMap<E> build() {
            int size = map.size();
            int[] begins = new int[size];
            Node[] nodes = new Node[size];
            int i = 0;
            for (Map.Entry<Integer, Node<E>> entry : map.entrySet()) {
                begins[i++] = entry.getKey();

            }
            return new ImmutableDistinctSpanMap<>(begins, nodes);
        }
    }

    static class Node<E> {
        final int end;
        final E value;

        Node(int end, E value) {
            this.end = end;
            this.value = value;
        }
    }

    static abstract class View<E> implements SpansMap<E> {
        final ImmutableDistinctSpanMap<E> backingMap;
        final int left;
        final int right;

        View(ImmutableDistinctSpanMap<E> backingMap, int left, int right) {
            this.backingMap = backingMap;
            this.left = left;
            this.right = right;
        }

        abstract View<E> update(int left, int right);

        @SuppressWarnings("unchecked")
        @Override
        public Optional<E> get(TextLocation textLocation) {
            int i = Arrays.binarySearch(backingMap.begins,
                    textLocation.getBegin());
            if (i < left || i > right) {
                return Optional.empty();
            }
            return Optional.of((E) backingMap.nodes[i].value);
        }

        @Override
        public SpansMap<E> toTheLeftOf(int index) {
            int i = backingMap.lower(index);
            if (i < right) {
                return update(left, i);
            }
            return this;
        }

        @Override
        public SpansMap<E> toTheRightOf(int index) {
            int i = backingMap.higher(index);
            if (i > left) {
                return update(i, right);
            }
            return this;
        }

        @Override
        public SpansMap<E> insideSpan(TextLocation textLocation) {
            int begin = backingMap.higher(textLocation.getBegin());
            int end = backingMap.lower(textLocation.getEnd());
            if (begin > left) {
                if (end < right) {
                    return update(begin, end);
                } else {
                    return update(begin, right);
                }
            } else if (end < right) {
                return update(left, end);
            }
            return this;
        }

        @Override
        public SpansMap<E> containing(TextLocation textLocation) {
            int begin = backingMap.lower(textLocation.getBegin());
            if (backingMap.nodes[begin].end >= textLocation.getEnd()) {
                return update(begin, begin);
            } else {
                return update(0, -1);
            }
        }

        @Override
        public boolean containsLabel(Label label) {
            int i = backingMap.indexOfLabel(label);
            return i != -1 && i >= left && i <= right;
        }
    }

    static class AscendingView<E> extends View<E> {
        AscendingView(ImmutableDistinctSpanMap<E> backingMap,
                      int left,
                      int right) {
            super(backingMap, left, right);
        }

        @Override
        View<E> update(int left, int right) {
            return new AscendingView<>(backingMap, left, right);
        }

        @Override
        public SpansMap<E> ascendingBegin() {
            return this;
        }

        @Override
        public SpansMap<E> descendingBegin() {
            return new DescendingView<>(backingMap, left, right);
        }

        @Override
        public SpansMap<E> ascendingEnd() {
            return this;
        }

        @Override
        public SpansMap<E> descendingEnd() {
            return this;
        }

        @Override
        public Set<Span> spans() {
            return null;
        }

        @Override
        public Collection<E> values() {
            return new AbstractCollection<E>() {
                @Override
                public Iterator<E> iterator() {
                    return new Iterator<E>() {
                        int i = left;

                        @Override
                        public boolean hasNext() {
                            return i <= right;
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public E next() {
                            return (E) backingMap.nodes[i++].value;
                        }
                    };
                }

                @Override
                public int size() {
                    return right - left + 1;
                }
            };
        }

        @Override
        public Set<Label<E>> entries() {
            return new AbstractSet<Label<E>>() {
                @Override
                public Iterator<Label<E>> iterator() {
                    return new Iterator<Label<E>>() {
                        int i = left;

                        @Override
                        public boolean hasNext() {
                            return i <= right;
                        }

                        @Override
                        public Label<E> next() {
                            return backingMap.labelAtIndex(i++);
                        }
                    };
                }

                @Override
                public int size() {
                    return right - left + 1;
                }

                @Override
                public boolean contains(Object o) {
                    if (!(o instanceof Label)) return false;
                    Label label = (Label) o;
                    return containsLabel(label);
                }
            };
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

        @Override
        public int size() {
            return right - left + 1;
        }

        private int offsetIndex(int index) {
            return left + index;
        }

        Span spanAtIndex(int index) {
            return backingMap.spanAtIndex(offsetIndex(index));
        }

        E valueAtIndex(int index) {
            return backingMap.valueAtIndex(offsetIndex(index));
        }

        Label<E> labelAtIndex(int index) {
            return backingMap.labelAtIndex(offsetIndex(index));
        }

        private int check(int index) {
            if (index < left || index > right) {
                return  -1;
            }
            return index;
        }

        int indexOfSpan(Object o) {
            return check(backingMap.indexOfSpan(o));
        }

        int indexOfLabel(Object o) {
            return check(backingMap.indexOfLabel(o));
        }

        static class AscendingSpansListView extends AbstractList<Span>
                implements RandomAccess {
            private final AscendingView<?> backingView;

            AscendingSpansListView(AscendingView<?> backingView) {
                this.backingView = backingView;
            }

            @Override
            public Span get(int index) {
                return backingView.spanAtIndex(index);
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean contains(Object o) {
                return indexOf(o) != -1;
            }

            @Override
            public int indexOf(Object o) {
                int i = backingMap.indexOfSpan(o);
                if (i >= left && i <= right) {
                    return i;
                }
                return -1;
            }

            @Override
            public int lastIndexOf(Object o) {
                return indexOf(o);
            }
        }
    }

    static class DescendingView<E> extends View<E> {
        DescendingView(ImmutableDistinctSpanMap<E> backingMap,
                       int left,
                       int right) {
            super(backingMap, left, right);
        }

        @Override
        View<E> update(int left, int right) {
            return new DescendingView<>(backingMap, left, right);
        }

        @Override
        public SpansMap<E> ascendingBegin() {
            return new AscendingView<>(backingMap, left, right);
        }

        @Override
        public SpansMap<E> descendingBegin() {
            return this;
        }

        @Override
        public SpansMap<E> ascendingEnd() {
            return this;
        }

        @Override
        public SpansMap<E> descendingEnd() {
            return this;
        }

        @Override
        public Set<Span> spans() {
            return new AbstractSet<Span>() {
                @Override
                public Iterator<Span> iterator() {
                    return new Iterator<Span>() {
                        int i = right;

                        @Override
                        public boolean hasNext() {
                            return i >= left;
                        }

                        @Override
                        public Span next() {
                            return new Span(backingMap.begins[i],
                                    backingMap.nodes[i--].end);
                        }
                    };
                }

                @Override
                public int size() {
                    return right - left + 1;
                }
            };
        }

        @Override
        public Collection<E> values() {
            return new AbstractCollection<E>() {
                @Override
                public Iterator<E> iterator() {
                    return new Iterator<E>() {
                        int i = right;

                        @Override
                        public boolean hasNext() {
                            return i >= left;
                        }

                        @SuppressWarnings("unchecked")
                        @Override
                        public E next() {
                            return (E) backingMap.nodes[i--].value;
                        }
                    };
                }

                @Override
                public int size() {
                    return right - left + 1;
                }
            };
        }

        @Override
        public Set<Label<E>> entries() {
            return new AbstractSet<Label<E>>() {
                @Override
                public Iterator<Label<E>> iterator() {
                    return new Iterator<Label<E>>() {
                        int i = right;

                        @Override
                        public boolean hasNext() {
                            return i >= left;
                        }

                        @Override
                        public Label<E> next() {
                            return backingMap.labelAtIndex(i--);
                        }
                    };
                }

                @Override
                public int size() {
                    return right - left + 1;
                }

                @Override
                public boolean contains(Object o) {
                    if (!(o instanceof Label)) return false;
                    Label label = (Label) o;
                    return containsLabel(label);
                }
            };
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

        @Override
        public int size() {
            return right - left + 1;
        }
    }

    static class SpansListView extends AbstractList<Span>
            implements RandomAccess {
        private final ImmutableDistinctSpanMap<?> backingMap;

        SpansListView(ImmutableDistinctSpanMap<?> backingMap) {
            this.backingMap = backingMap;
        }

        @Override
        public Span get(int index) {
            return backingMap.spanAtIndex(index);
        }

        @Override
        public int size() {
            return backingMap.size();
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) != -1;
        }

        @Override
        public int indexOf(Object o) {
            return backingMap.indexOfSpan(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return indexOf(o);
        }
    }

    static class ValueListView<E> extends AbstractList<E>
            implements RandomAccess {
        private final ImmutableDistinctSpanMap<E> backingMap;

        ValueListView(ImmutableDistinctSpanMap<E> backingMap) {
            this.backingMap = backingMap;
        }

        @Override
        public E get(int index) {
            return backingMap.valueAtIndex(index);
        }

        @Override
        public int size() {
            return backingMap.size();
        }
    }

    static class LabelsListView<E> extends AbstractList<Label<E>>
            implements RandomAccess {
        private final ImmutableDistinctSpanMap<E> backingMap;

        LabelsListView(ImmutableDistinctSpanMap<E> backingMap) {
            this.backingMap = backingMap;
        }

        @Override
        public Label<E> get(int index) {
            return backingMap.labelAtIndex(index);
        }

        @Override
        public int size() {
            return backingMap.size();
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) != -1;
        }

        @Override
        public int indexOf(Object o) {
            return backingMap.indexOfLabel(o);
        }


        @Override
        public int lastIndexOf(Object o) {
            return indexOf(o);
        }
    }
}
