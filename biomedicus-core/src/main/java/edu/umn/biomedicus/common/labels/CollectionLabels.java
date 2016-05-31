package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.text.SpanLike;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class CollectionLabels<T> implements Labels<T> {
    private final Collection<Label<T>> collection;

    private final Predicate<Label<T>> filter;

    public CollectionLabels(Collection<Label<T>> collection, Predicate<Label<T>> filter) {
        this.collection = collection;
        this.filter = filter;
    }

    public CollectionLabels(Collection<Label<T>> collection) {
        this(collection, (label) -> true);
    }

    @Override
    public Labels<T> insideSpan(SpanLike spanLike) {
        return new CollectionLabels<>(collection, filter.and(spanLike::contains));
    }

    @Override
    public Labels<T> withSpan(SpanLike spanLike) {
        return new CollectionLabels<>(collection, filter.and(spanLike::spanEquals));
    }

    @Override
    public Labels<T> filter(Predicate<Label<T>> predicate) {
        return new CollectionLabels<>(collection, filter.and(predicate));
    }

    @Override
    public Iterator<Label<T>> iterator() {
        return collection.iterator();
    }
}
