package edu.umn.biomedicus.common.tuples;

import java.util.function.BiConsumer;

public class Pair<T, U> {
    private final T first;

    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public T first() {
        return first;
    }

    public U second() {
        return second;
    }

    public Pair<U, T> swap() {
        return new Pair<>(second, first);
    }

    public void call(BiConsumer<T, U> function) {
        function.accept(first, second);
    }

}
