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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.BiomedicusScopes.Context;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Used for performing work that runs in a processing scope.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class ScopedWork {

  protected final Injector injector;

  protected final SettingsTransformer settingsTransformer;

  protected final Map<String, Object> globalSettings;

  @Nullable
  protected Injector settingsInjector;

  @Nullable
  protected Context processorContext;

  @Nullable
  protected Map<String, Object> processorSettings;

  ScopedWork(
      Injector injector,
      @Named("globalSettings") Map<String, Object> globalSettings,
      SettingsTransformer settingsTransformer
  ) {
    this.injector = injector;
    this.globalSettings = globalSettings;
    this.settingsTransformer = settingsTransformer;
  }

  public void initialize(
      @Nullable Map<String, Object> processorSettings,
      @Nullable Map<Key<?>, Object> processorScopedObjects
  ) throws BiomedicusException {
    this.processorSettings = processorSettings;

    settingsTransformer.setAnnotationFunction(ProcessorSettingImpl::new);

    if (processorSettings != null) {
      settingsTransformer.addAll(processorSettings);
    }
    settingsTransformer.addAll(globalSettings);

    Map<Key<?>, Object> settingsSeededObjects = settingsTransformer
        .getSettings();
    settingsInjector = injector.createChildInjector(
        new ProcessorSettingsModule(settingsSeededObjects.keySet()));

    Map<Key<?>, Object> processorScopeMap = new HashMap<>();
    processorScopeMap.putAll(settingsSeededObjects);
    if (processorScopedObjects != null) {
      processorScopeMap.putAll(processorScopedObjects);
    }

    processorContext = BiomedicusScopes
        .createProcessorContext(processorScopeMap);
  }

  public void require(String className) throws BiomedicusException {
    try {
      require(Class.forName(className));
    } catch (ClassNotFoundException e) {
      throw new BiomedicusException(e);
    }
  }

  public void require(Class<?> aClass) throws BiomedicusException {
    checkNotNull(processorContext,
        "Processor context is null, initialize not ran");

    checkNotNull(settingsInjector,
        "Settings injector is null, initialize not ran");

    try {
      processorContext.call(() -> {
        Provider<?> provider = settingsInjector.getProvider(aClass);
        if (provider instanceof EagerLoadable) {
          ((EagerLoadable) provider).eagerLoad();
          return null;
        }
        Object o = provider.get();
        if (o instanceof EagerLoadable) {
          ((EagerLoadable) o).eagerLoad();
        }
        return null;
      });
    } catch (Exception e) {
      throw new BiomedicusException(e);
    }
  }
}
