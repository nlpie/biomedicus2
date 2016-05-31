package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.text.SpanLike;
import edu.umn.biomedicus.common.tuples.Pair;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

public final class LabelsUtilities {
    private LabelsUtilities() {
        throw new UnsupportedOperationException("Instantiation of utility class");
    }

    public static <T, U> Labels<Pair<T, U>> paired(Labels<T> firstLabels, Labels<U> secondLabels) {
        return new Labels<Pair<T, U>>() {
            @Override
            public Labels<Pair<T, U>> insideSpan(SpanLike spanLike) {
                return paired(firstLabels.insideSpan(spanLike), secondLabels.insideSpan(spanLike));
            }

            @Override
            public Labels<Pair<T, U>> withSpan(SpanLike spanLike) {
                return paired(firstLabels.withSpan(spanLike), secondLabels.withSpan(spanLike));
            }

            @Override
            public Labels<Pair<T, U>> filter(Predicate<Label<Pair<T, U>>> predicate) {
                return new FilteredLabels<>(this, predicate);
            }

            @Override
            public Iterator<Label<Pair<T, U>>> iterator() {
                return new Iterator<Label<Pair<T, U>>>() {
                    private final Iterator<Label<T>> firstIterator = firstLabels.iterator();
                    private final Iterator<Label<U>> secondIterator = secondLabels.iterator();
                    @Nullable private Label<Pair<T, U>> current = null;

                    {
                        advance();
                    }

                    @Override
                    public boolean hasNext() {
                        return current != null;
                    }

                    @Override
                    public Label<Pair<T, U>> next() {
                        Label<Pair<T, U>> current = this.current;
                        if (current == null) {
                            throw new NoSuchElementException();
                        }
                        advance();
                        return current;
                    }

                    private void advance() {
                        if (!firstIterator.hasNext()) {
                            current = null;
                            return;
                        }
                        Label<T> first = firstIterator.next();
                        Label<U> second = secondIterator.next();
                        if (!first.span().spanEquals(second.span())) {
                            throw new IllegalStateException("Spans on labels do not match");
                        }
                        current = new Label<>(first.span(), new Pair<>(first.value(), second.value()));
                    }
                };
            }
        };
    }

    public static <T, U> Labels<Pair<T, Optional<U>>> pairedSecondOptional(Labels<T> firstLabels,
                                                                           Labels<U> secondLabels) {
        return new Labels<Pair<T, Optional<U>>>() {
            @Override
            public Labels<Pair<T, Optional<U>>> insideSpan(SpanLike spanLike) {
                return pairedSecondOptional(firstLabels.insideSpan(spanLike), secondLabels.insideSpan(spanLike));
            }

            @Override
            public Labels<Pair<T, Optional<U>>> withSpan(SpanLike spanLike) {
                return pairedSecondOptional(firstLabels.withSpan(spanLike), secondLabels.withSpan(spanLike));
            }

            @Override
            public Labels<Pair<T, Optional<U>>> filter(Predicate<Label<Pair<T, Optional<U>>>> predicate) {
                return new FilteredLabels<>(this, predicate);
            }

            @Override
            public Iterator<Label<Pair<T, Optional<U>>>> iterator() {
                return new Iterator<Label<Pair<T, Optional<U>>>>() {
                    private final Iterator<Label<T>> firstIterator = firstLabels.iterator();
                    private final Iterator<Label<U>> secondIterator = secondLabels.iterator();
                    @Nullable private Label<Pair<T, Optional<U>>> current = null;

                    @Override
                    public boolean hasNext() {
                        return current != null;
                    }

                    @Override
                    public Label<Pair<T, Optional<U>>> next() {
                        Label<Pair<T, Optional<U>>> current = this.current;
                        if (current == null) {
                            throw new NoSuchElementException();
                        }
                        advance();
                        return current;
                    }

                private void advance() {
                    if (!firstIterator.hasNext()) {
                        current = null;
                        return;
                    }
                    Label<T> first = firstIterator.next();
                    Label<U> second = secondIterator.next();
                    while (second.compare(first) < 0) second = secondIterator.next();
                    Optional<U> secondOption = second.spanEquals(first) ? Optional.of(second.value()) : Optional.empty();
                    current = new Label<>(second.span(), new Pair<>(first.value(), secondOption));
                }
                };
            }
        };
    }

    static class FilteredLabels<T> implements Labels<T> {
        private final Labels<T> labels;
        private final Predicate<Label<T>> filter;

        FilteredLabels(Labels<T> labels, Predicate<Label<T>> filter) {
            this.labels = labels;
            this.filter = filter;
        }


        @Override
        public Labels<T> insideSpan(SpanLike spanLike) {
            return new FilteredLabels<>(this, spanLike::contains);
        }

        @Override
        public Labels<T> withSpan(SpanLike spanLike) {
            return new FilteredLabels<>(this, spanLike::spanEquals);
        }

        @Override
        public Labels<T> filter(Predicate<Label<T>> predicate) {
            return new FilteredLabels<>(this, predicate);
        }

        @Override
        public Iterator<Label<T>> iterator() {
            return new Iterator<Label<T>>() {
                private final Iterator<Label<T>> iterator = labels.iterator();
                @Nullable
                private Label<T> current = null;

                {
                    advance();
                }

                private void advance() {
                    while (iterator.hasNext()) {
                        Label<T> next = iterator.next();
                        if (filter.test(next)) {
                            current = next;
                            return;
                        }
                    }
                    current = null;
                }

                @Override
                public boolean hasNext() {
                    return current != null;
                }

                @Override
                public Label<T> next() {
                    if (current == null) {
                        throw new NoSuchElementException();
                    }
                    Label<T> label = this.current;
                    advance();
                    return label;
                }
            };
        }
    }
}
