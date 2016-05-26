package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.text.Span;

import java.lang.reflect.ParameterizedType;

public class Label<T> {
    private final Span span;

    private final T label;

    public Label(Span span, T label) {
        this.span = span;
        this.label = label;
    }

    public static <T> Label<T> create(Span span, T label) {
        return new Label<>(span, label);
    }

    public Span span() {
        return span;
    }

    public T label() {
        return label;
    }

    @SuppressWarnings("unchecked")
    public Class<T> labelClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Label<?> label1 = (Label<?>) o;

        if (!span.equals(label1.span)) return false;
        return label.equals(label1.label);

    }

    @Override
    public int hashCode() {
        int result = span.hashCode();
        result = 31 * result + label.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Label(" + span + ", " + label + ")";
    }
}
