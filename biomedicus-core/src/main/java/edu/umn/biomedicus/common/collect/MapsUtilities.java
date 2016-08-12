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

package edu.umn.biomedicus.common.collect;

import java.util.function.BiFunction;

/**
 *
 */
public final class MapsUtilities {
    private MapsUtilities() {
        throw new UnsupportedOperationException();
    }

    public static <T> BiFunction<T, Integer, Integer> computeIncrementFunction() {
        return (key, value) -> {
            if (value == null) {
                value = 0;
            }
            return value + 1;
        };
    }

    public static final BiFunction<String, Integer, Integer> STRING_COMPUTE_INCREMENT = computeIncrementFunction();
}
