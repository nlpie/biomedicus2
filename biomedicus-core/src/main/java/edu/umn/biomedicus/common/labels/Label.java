package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.SpanLike;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;

public final class Label<T> implements SpanLike {
    private final Span span;
    private final T value;

    public Label(Span span, T value) {
        this.span = Objects.requireNonNull(span, "Span must not be null");
        this.value = Objects.requireNonNull(value, "Value must not be null");
    }

    public T value() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public Class<T> labelClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public int getBegin() {
        return span.getBegin();
    }

    @Override
    public int getEnd() {
        return span.getEnd();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Label<?> label = (Label<?>) o;

        if (!span.equals(label.span)) return false;
        return value.equals(label.value);

    }

    @Override
    public int hashCode() {
        int result = span.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Label(" + span + ", " + value + ")";
    }
}
