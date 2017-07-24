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

import com.google.inject.Provider;
import edu.umn.biomedicus.exc.BiomedicusException;
import javax.annotation.Nullable;

/**
 * Loads data into, and functions as a provider for, a singleton object.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public abstract class DataLoader<T> implements Provider<T>, EagerLoadable {

  private transient final Object lock = new Object();
  private transient volatile boolean loaded = false;
  @Nullable
  private transient volatile T instance;

  @Override
  public void eagerLoad() throws BiomedicusException {
    if (!loaded) {
      load();
    }
  }

  @Override
  public T get() {
    if (!loaded) {
      try {
        load();
      } catch (BiomedicusException e) {
        throw new IllegalStateException(e);
      }
    }
    return instance;
  }

  private void load() throws BiomedicusException {
    synchronized (lock) {
      if (!loaded) {
        instance = loadModel();
        if (instance == null) {
          throw new IllegalStateException("Loader returned null");
        }
        loaded = true;
      }
    }
  }

  /**
   * To be implemented by subclasses, performs the initialization of the
   * object.
   *
   * @return full initialized object.
   * @throws BiomedicusException if there is an issue loading the object
   */
  protected abstract T loadModel() throws BiomedicusException;
}
