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

import edu.umn.biomedicus.common.types.text.TextLocation;
import edu.umn.biomedicus.common.tuples.Pair;

import java.util.stream.Stream;

public interface SpansMap<T> {
    SpansMap<T> toTheLeftOf(TextLocation textLocation);

    SpansMap<T> toTheRightOf(TextLocation textLocation);

    SpansMap<T> insideSpan(TextLocation textLocation);

    SpansMap<T> containing(TextLocation textLocation);

    Stream<TextLocation> spansStream();

    Stream<T> ascendingStartDecreasingSizeValuesStream();

    Stream<T> descendingStartDecreasingSizeValuesStream();

    Stream<T> ascendingStartIncreasingSizeValueStream();

    Stream<T> descendingStartIncreasingSizeValuesStream();

    Stream<Pair<TextLocation, T>> pairStream();
}
