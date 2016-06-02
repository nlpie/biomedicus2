package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.text.SpanLike;
import edu.umn.biomedicus.common.tuples.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.*;

/**
 * A collection of {@link Label} objects. By default, a typed injectable of labels should map to the collection of all
 * such labels for a document. Labels are unique per span, meaning that for each unique span at most one instance of T
 * will be labeled.
 *
 * @param <T> the type that is labeled
 * @since 1.5.0
 */
public interface Labels<T> extends Iterable<Label<T>> {
    /**
     * Returns a collection of these labels only inside the span parameter. All labels in the returned objects will have
     * a begin greater than or equal to the argument's begin and an end less than or equal to the arguments end.
     *
     * @param spanLike the boundaries.
     * @return Labels object filtered down so that all labels meet the requirement
     */
    Labels<T> insideSpan(SpanLike spanLike);

    /**
     * The collection of labels where the begin and end are less than or equal to the begin of the span argument.
     * Iterator order is such that we start with the first to the immediate left of the span, and continue onwards.
     *
     * @param span span to work leftwards from
     * @return labels leftwards from the specified span.
     */
    Labels<T> leftwardsFrom(SpanLike span);

    /**
     * The collection of labels where the begin and end are greater than or equal to the end of the span argument.
     *
     * @param span span to work rightwards from
     * @return labels rightwards after the specified span.
     */
    Labels<T> rightwardsFrom(SpanLike span);

    /**
     * Reverses the iteration order of this collection of labels. By default labels iterate in order from left to right.
     *
     * @return
     */
    Labels<T> reverse();

    /**
     * @param max
     * @return
     */
    Labels<T> limit(int max);

    /**
     * Applies the predicate, return a collection of the labels that match that predicate.
     *
     * @param predicate
     * @return
     */
    Labels<T> filter(Predicate<Label<T>> predicate);

    default Optional<Label<T>> withSpan(SpanLike spanLike) {
        Iterator<Label<T>> it = insideSpan(spanLike).filter(spanLike::spanEquals).iterator();
        if (it.hasNext()) {
            return Optional.of(it.next());
        }
        return Optional.empty();
    }

    default Iterator<List<Label<T>>> slidingWindowsOfSize(int size) {
        return new Iterator<List<Label<T>>>() {
            private final Iterator<Label<T>> iterator = iterator();
            private final LinkedList<Label<T>> window = new LinkedList<>();
            private boolean done = false;

            {
                for (int i = 0; i < size; i++) {
                    if (iterator.hasNext()) {
                        window.addLast(iterator.next());
                    }
                }
            }

            @Override
            public boolean hasNext() {
                return !done;
            }

            @Override
            public List<Label<T>> next() {
                if (done) {
                    throw new NoSuchElementException();
                }
                ArrayList<Label<T>> labels = new ArrayList<>(window);
                window.removeFirst();
                if (iterator.hasNext()) {
                    window.addLast(iterator.next());
                } else {
                    done = true;
                }
                return labels;
            }
        };
    }


    /**
     * @return
     */
    default List<Label<T>> all() {
        return stream().collect(Collectors.toList());
    }

    default Stream<Label<T>> stream() {
        Iterator<Label<T>> iterator = iterator();
        Spliterator<Label<T>> spliterator = Spliterators.spliteratorUnknownSize(iterator,
                ORDERED | DISTINCT | IMMUTABLE | NONNULL | SORTED);
        return StreamSupport.stream(spliterator, false);
    }
}
