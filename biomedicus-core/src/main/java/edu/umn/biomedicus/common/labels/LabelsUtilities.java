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

/**
 * A collection of utilities for dealing with the {@link LabelIndex} type.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
public final class LabelsUtilities {
    private LabelsUtilities() {
        throw new UnsupportedOperationException("Instantiation of utility class.");
    }

    /**
     * Changes the type bound to that of a superclass of the current bound. This works because labelIndex collections are
     * immutable and thus the bounded type is covariant.
     *
     * @param labelIndex the labels object to be given a new bound.
     * @param <T> the super-type type bound parameter
     * @param <U> the sub-type type bound parameter
     * @return the same labelIndex object, except bounded by the super-type.
     */
    @SuppressWarnings("unchecked")
    public static <T, U extends T> LabelIndex<T> cast(LabelIndex<U> labelIndex) {
        return (LabelIndex<T>) labelIndex;
    }

    /**
     * Changes the type bound to that of a superclass of the current bound. This works because labels objects are
     * immutable and thus the bounded type is covariant.
     *
     * @param label the label object to be given a new bound.
     * @param <T> the super-type type bound parameter
     * @param <U> the sub-type type bound parameter
     * @return the same label object, except bounded by the super-type.
     */
    @SuppressWarnings("unchecked")
    public static <T, U extends T> Label<T> cast(Label<U> label) {
        return (Label<T>) label;
    }
}
