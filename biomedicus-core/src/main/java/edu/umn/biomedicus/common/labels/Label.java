/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TextLocation;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class Label<T> implements TextLocation {
    private final Span span;
    private final T value;

    public Label(Span span, T value) {
        this.span = Objects.requireNonNull(span, "Span must not be null");
        this.value = Objects.requireNonNull(value, "Value must not be null");
    }

    public Label(int begin, int end, T value) {
        this.span = new Span(begin, end);
        this.value = Objects.requireNonNull(value, "Value must not be null");
    }

    public T getValue() {
        return value;
    }

    public T value() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public Class<T> labelClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void call(BiConsumer<Span, T> biConsumer) {
        biConsumer.accept(span, value);
    }

    public <U> Label<U> map(Function<T, U> function) {
        return new Label<>(span, function.apply(value));
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
    public Span toSpan() {
        return span;
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
