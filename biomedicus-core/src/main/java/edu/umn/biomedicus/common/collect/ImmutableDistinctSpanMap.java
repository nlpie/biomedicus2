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

package edu.umn.biomedicus.common.collect;

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TextLocation;

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

                    @SuppressWarnings("unchecked")
                    @Override
                    public E next() {
                        return (E) nodes[index++].value;
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

                    @SuppressWarnings("unchecked")
                    @Override
                    public Label<E> next() {
                        Node<E> node = nodes[index];
                        return new Label<>(new Span(begins[index++], node.end),
                                node.value);
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

    @SuppressWarnings("unchecked")
    Label<E> exportLabel(int index) {
        Node<E> node = nodes[index];
        return new Label<>(begins[index], node.end, node.value);
    }

    @SuppressWarnings("unchecked")
    int indexOf(Label label) {
        int begin = label.getBegin();
        int i = Arrays.binarySearch(begins, begin);
        if (i < 0) {
            return -1;
        }
        Node<E> node = nodes[i];
        if (label.getEnd() == node.end && label.getValue().equals(node.value)) {
            return i;
        } else {
            return -1;
        }
    }


    public static <E> Builder<E> builder() {
        return new Builder<>();
    }

    public static class Builder<E> {
        private Map<Integer, Node<E>> map = new HashMap<>();

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
            int i = backingMap.indexOf(label);
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
                            return backingMap.exportLabel(i++);
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
                            return backingMap.exportLabel(i--);
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
    }
}
