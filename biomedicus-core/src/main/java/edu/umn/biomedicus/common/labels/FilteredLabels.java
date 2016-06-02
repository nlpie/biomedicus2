package edu.umn.biomedicus.common.labels;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilteredLabels<T> extends AbstractLabels<T> {

    private final Labels<T> labels;
    private final Predicate<Label<T>> predicate;

    public FilteredLabels(Labels<T> labels, Predicate<Label<T>> predicate) {
        this.labels = labels;
        this.predicate = predicate;
    }

    @Override
    public Iterator<Label<T>> iterator() {
        Iterator<Label<T>> iterator = labels.iterator();
        return new Iterator<Label<T>>() {
            @Nullable private Label<T> current;

            {
                forward();
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
                Label<T> label = current;
                forward();
                return label;
            }

            private void forward() {
                while (iterator.hasNext()) {
                    Label<T> next = iterator.next();
                    if (predicate.test(next)) {
                        current = next;
                        return;
                    }
                }
                current = null;
            }
        };
    }

    @Override
    public Stream<Label<T>> stream() {
        return labels.stream().filter(predicate);
    }
}
