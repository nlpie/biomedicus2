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

package edu.umn.biomedicus.framework;

import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.Span;

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

    boolean found();

    boolean search();

    boolean search(int begin, int end);

    boolean search(Span span);

    boolean match();

    boolean match(int begin, int end);

    boolean match(Span span);

    Optional<Span> getSpan();

    Collection<String> getGroups();
}
