package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.SpanLike;

import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TreeLabels<T> extends AbstractLabels<T> {
    private final NavigableMap<Span, Label<T>> tree;

    private final Predicate<Label<T>> filter;

    private TreeLabels(NavigableMap<Span, Label<T>> tree, Predicate<Label<T>> filter) {
        this.tree = tree;
        this.filter = filter;
    }

    public TreeLabels(Iterable<Label<T>> labels) {
        tree = new TreeMap<>();
        filter = (label) -> true;
        for (Label<T> label : labels) {
            Span span = label.toSpan();
            if (tree.containsKey(span)) {
                throw new IllegalArgumentException("Duplicate span: " + span);
            }
            tree.put(span, label);
        }
    }

    @Override
    public Labels<T> insideSpan(SpanLike spanLike) {
        return new TreeLabels<>(tree.subMap(beginBegin(spanLike), true, endEnd(spanLike), true), filter);
    }

    @Override
    public Optional<Label<T>> withSpan(SpanLike spanLike) {
        return Optional.ofNullable(tree.get(spanLike.toSpan()));
    }

    @Override
    public Labels<T> leftwardsFrom(SpanLike span) {
        NavigableMap<Span, Label<T>> headMap = tree.headMap(beginBegin(span), true);
        NavigableMap<Span, Label<T>> descending = headMap.descendingMap();
        return new TreeLabels<>(descending, filter);
    }

    @Override
    public Labels<T> rightwardsFrom(SpanLike span) {
        NavigableMap<Span, Label<T>> tailMap = tree.tailMap(endEnd(span), true);
        return new TreeLabels<>(tailMap, filter);
    }

    public Labels<T> reverse() {
        return new TreeLabels<>(tree.descendingMap(), filter);
    }

    @Override
    public Labels<T> filter(Predicate<Label<T>> predicate) {
        return new TreeLabels<>(tree, filter.and(predicate));
    }

    @Override
    public Iterator<Label<T>> iterator() {
        return tree.values().stream().filter(filter).iterator();
    }

    private Span beginBegin(SpanLike spanLike) {
        int begin = spanLike.getBegin();
        return Span.spanning(begin, begin);
    }

    private Span endEnd(SpanLike spanLike) {
        int end = spanLike.getEnd();
        return Span.spanning(end, end);
    }

    @Override
    public Stream<Label<T>> stream() {
        return tree.values().stream().filter(filter);
    }
}
