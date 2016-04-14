package edu.umn.biomedicus.common;

import java.util.function.BiFunction;

/**
 *
 */
public final class MapsHelper {
    private MapsHelper() {
        throw new UnsupportedOperationException();
    }

    public static <T> BiFunction<T, Integer, Integer> getComputeIncrementFunction() {
        return (key, value) -> {
            if (value == null) {
                value = 0;
            }
            return value + 1;
        };
    }
}
