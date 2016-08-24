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

import edu.umn.biomedicus.common.types.text.TextLocation;

import java.util.function.Predicate;

/**
 * Abstract class for a labels implementation. Provides sensible defaults for methods using an adapter pattern.
 *
 * @param <T>
 */
public abstract class AbstractLabels<T> implements Labels<T> {
    @Override
    public Labels<T> containing(TextLocation textLocation) {
        return new StandardLabels<>(this).containing(textLocation);
    }

    @Override
    public Labels<T> insideSpan(TextLocation textLocation) {
        return new StandardLabels<>(new FilteredLabels<>(this, textLocation::contains));
    }

    @Override
    public Labels<T> leftwardsFrom(TextLocation span) {
        return new StandardLabels<>(this).leftwardsFrom(span);
    }

    @Override
    public Labels<T> rightwardsFrom(TextLocation span) {
        return new StandardLabels<>(this).leftwardsFrom(span);
    }

    @Override
    public Labels<T> reverse() {
        return new StandardLabels<>(this).reverse();
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
