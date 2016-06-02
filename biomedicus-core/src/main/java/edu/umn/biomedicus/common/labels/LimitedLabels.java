package edu.umn.biomedicus.common.labels;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

class LimitedLabels<T> extends AbstractLabels<T> {
    private final Labels<T> labels;

    private final int limit;

    LimitedLabels(Labels<T> labels, int limit) {
        this.labels = labels;
        this.limit = limit;
    }

    @Override
    public Labels<T> limit(int max) {
        if (max >= limit) {
            return this;
        }
        return new LimitedLabels<>(labels, max);
    }

    @Override
    public Iterator<Label<T>> iterator() {
        Iterator<Label<T>> iterator = labels.iterator();
        return new Iterator<Label<T>>() {
            private int count = 0;
            @Override
            public boolean hasNext() {
                return count < limit && iterator.hasNext();
            }

            @Override
            public Label<T> next() {
                if (count >= limit) {
                    throw new NoSuchElementException();
                }
                count += 1;
                return iterator.next();
            }
        };
    }

    @Override
    public Stream<Label<T>> stream() {
        return labels.stream().limit(limit);
    }
}
