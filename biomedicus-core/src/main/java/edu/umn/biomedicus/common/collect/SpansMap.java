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

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.types.text.TextLocation;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface SpansMap<T> {
    Optional<T> get(TextLocation textLocation);

    SpansMap<T> toTheLeftOf(int index);

    SpansMap<T> toTheRightOf(int index);

    default SpansMap<T> toIncluding(TextLocation textLocation) {
        return toTheLeftOf(textLocation.getEnd());
    }

    default SpansMap<T> fromIncluding(TextLocation textLocation) {
        return toTheRightOf(textLocation.getBegin());
    }

    default SpansMap<T> toTheLeftOf(TextLocation textLocation) {
        return toTheLeftOf(textLocation.getBegin());
    }

    default SpansMap<T> toTheRightOf(TextLocation textLocation) {
        return toTheRightOf(textLocation.getEnd());
    }

    SpansMap<T> insideSpan(TextLocation textLocation);

    SpansMap<T> containing(TextLocation textLocation);

    SpansMap<T> ascendingBegin();

    SpansMap<T> descendingBegin();

    SpansMap<T> ascendingEnd();

    SpansMap<T> descendingEnd();

    Collection<T> values();

    Set<Label<T>> entries();

    boolean containsLabel(Label label);
}
