package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.text.SpanLike;
import edu.umn.biomedicus.common.tuples.Pair;

import java.util.*;
import java.util.function.Predicate;

/**
 * Abstract class for a labels implementation. Provides sensible defaults for methods using an adapter pattern.
 *
 * @param <T>
 */
public abstract class AbstractLabels<T> implements Labels<T> {
    @Override
    public Labels<T> insideSpan(SpanLike spanLike) {
        return new TreeLabels<>(new FilteredLabels<>(this, spanLike::contains));
    }

    @Override
    public Labels<T> leftwardsFrom(SpanLike span) {
        return new TreeLabels<>(this).leftwardsFrom(span);
    }

    @Override
    public Labels<T> rightwardsFrom(SpanLike span) {
        return new TreeLabels<>(this).leftwardsFrom(span);
    }

    @Override
    public Labels<T> reverse() {
        return new TreeLabels<>(this).reverse();
    }

    @Override
    public Labels<T> limit(int max) {
        return new LimitedLabels<>(this, max);
    }

    @Override
    public Labels<T> filter(Predicate<Label<T>> predicate) {
        return new FilteredLabels<>(this, predicate);
    }
}
