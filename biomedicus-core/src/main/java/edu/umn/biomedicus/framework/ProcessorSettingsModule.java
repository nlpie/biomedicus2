/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import java.util.Collection;

/**
 * Module used for creating child injectors
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
final class ProcessorSettingsModule extends AbstractModule {

  private final Collection<Key<?>> processorSettings;

  ProcessorSettingsModule(Collection<Key<?>> processorSettings) {
    this.processorSettings = processorSettings;
  }

  @Override
  protected void configure() {
    processorSettings.forEach(this::bindToScope);
  }

  private <T> void bindToScope(Key<T> key) {
    bind(key).toProvider(BiomedicusScopes.providedViaSeeding())
        .in(BiomedicusScopes.PROCESSOR_SCOPE);
  }
}
