package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.text.SpanLike;

import java.util.*;
import java.util.function.Predicate;

public interface Labels<T> extends Iterable<Label<T>> {
    Labels<T> insideSpan(SpanLike spanLike);

    /**
     * Return all labels with the span equal to the
     * @param spanLike
     *
     * @return
     */
    Labels<T> withSpan(SpanLike spanLike);

    /**
     * Applies the predicate, return a collection of the labels that match that predicate.
     *
     * @param predicate
     * @return
     */
    default Labels<T> filter(Predicate<Label<T>> predicate) {
        return new LabelsUtilities.FilteredLabels<>(this, predicate);
    }

    default List<Label<T>> all() {
        List<Label<T>> labels = new ArrayList<>();
        for (Label<T> tLabel : this) {
            labels.add(tLabel);
        }
        return labels;
    }

    /**
     * Requires that only one instance of the label exists in this Labels collection and returns it.
     *
     * @return the one instance that exists in this labels
     * @throws IllegalStateException if there is more than one instance of the label
     * @throws NoSuchElementException if there is 0 instances in the label.
     */
    default Label<T> get() {
        Iterator<Label<T>> iterator = iterator();
        if (!iterator.hasNext()) {
            throw new NoSuchElementException();
        }
        Label<T> next = iterator.next();
        if (iterator.hasNext()) {
            throw new IllegalStateException("More than one label in this labels collection.");
        }
        return next;
    }

    /**
     * Requires that at most one instance of the label exists in this Labels collection and returns an optional of that
     * instance.
     *
     * @return optional, empty if there are no labels
     * @throws IllegalStateException if there is mroe than one instance of the label
     */
    default Optional<Label<T>> getOptionally() {
        Iterator<Label<T>> iterator = iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        Label<T> next = iterator.next();
        if (iterator.hasNext()) {
            throw new IllegalStateException("More than one label in this labels collection.");
        }
        return Optional.of(next);
    }
}
