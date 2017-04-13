/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

import java.util.Collection;
import java.util.Optional;

/**
 * Results from {@link Searcher}.
 *
 *
 */
public interface Search {
    /**
     * Returns the named label if it matched against anything.
     * @param name
     * @return
     */
    Optional<Label<?>> getLabel(String name);

    Optional<Span> getSpan(String name);

    boolean foundMatch();

    boolean findNext();

    Optional<Span> getSpan();

    Collection<String> getGroups();
}
