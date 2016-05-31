package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.SpanLike;

import java.util.*;
import java.util.function.Predicate;

public class TreeLabels<T> implements Labels<T> {
    private final NavigableMap<SpanLike, Collection<Label<T>>> tree;

    private final Predicate<Label<T>> filter;

    private TreeLabels(NavigableMap<SpanLike, Collection<Label<T>>> tree, Predicate<Label<T>> filter) {
        this.tree = tree;
        this.filter = filter;
    }

    public TreeLabels(Iterable<Label<T>> labels) {
        tree = new TreeMap<>();
        filter = (label) -> true;
        for (Label<T> label : labels) {
            tree.compute(Span.spanning(label.getBegin(), label.getEnd()), (l, collection) -> {
                if (collection == null) {
                    collection = new ArrayList<>();
                }
                collection.add(label);
                return collection;
            });
        }
    }

    @Override
    public Labels<T> insideSpan(SpanLike spanLike) {
        return new TreeLabels<>(tree.subMap(beginBegin(spanLike), true, endEnd(spanLike), true), filter);
    }

    @Override
    public Labels<T> withSpan(SpanLike spanLike) {
        return new CollectionLabels<>(tree.get(Span.spanning(spanLike.getBegin(), spanLike.getEnd())), filter);
    }

    @Override
    public Labels<T> filter(Predicate<Label<T>> predicate) {
        return new TreeLabels<>(tree, filter.and(predicate));
    }

    @Override
    public Iterator<Label<T>> iterator() {
        return tree.values().stream().flatMap(Collection::stream).filter(filter).iterator();
    }

    private SpanLike beginBegin(SpanLike spanLike) {
        int begin = spanLike.getBegin();
        return Span.spanning(begin, begin);
    }

    private SpanLike endEnd(SpanLike spanLike) {
        int end = spanLike.getEnd();
        return Span.spanning(end, end);
    }
}
