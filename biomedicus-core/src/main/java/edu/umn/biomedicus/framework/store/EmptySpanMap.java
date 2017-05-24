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

package edu.umn.biomedicus.framework.store;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 */
public class EmptySpanMap<T> implements SpansMap<T> {

  @Override
  public Optional<Label<T>> getLabel(TextLocation textLocation) {
    return Optional.empty();
  }

  @Override
  public Optional<T> get(TextLocation textLocation) {
    return Optional.empty();
  }

  @Override
  public SpansMap<T> toTheLeftOf(int index) {
    return this;
  }

  @Override
  public SpansMap<T> toTheRightOf(int index) {
    return this;
  }

  @Override
  public SpansMap<T> insideSpan(TextLocation textLocation) {
    return this;
  }

  @Override
  public SpansMap<T> containing(TextLocation textLocation) {
    return this;
  }

  @Override
  public SpansMap<T> ascendingBegin() {
    return this;
  }

  @Override
  public SpansMap<T> descendingBegin() {
    return this;
  }

  @Override
  public SpansMap<T> ascendingEnd() {
    return this;
  }

  @Override
  public SpansMap<T> descendingEnd() {
    return this;
  }

  @Override
  public Set<Span> spans() {
    return Collections.emptySet();
  }

  @Override
  public Collection<T> values() {
    return Collections.emptyList();
  }

  @Override
  public Set<Label<T>> entries() {
    return Collections.emptySet();
  }

  @Override
  public boolean containsLabel(Object o) {
    return false;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public List<Label<T>> asList() {
    return Collections.emptyList();
  }

  @Override
  public List<Span> spansAsList() {
    return Collections.emptyList();
  }

  @Override
  public List<T> valuesAsList() {
    return Collections.emptyList();
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Optional<Label<T>> first() {
    return Optional.empty();
  }
}
