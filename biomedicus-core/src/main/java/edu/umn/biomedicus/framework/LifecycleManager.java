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

import com.google.inject.Singleton;
import edu.umn.biomedicus.exc.BiomedicusException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service responsible for calling all of the lifecycle managed objects and
 * telling them to shut down. Lifecycle managed objects will be automatically registered by the biomedicus guice module.
 *
 * @since 1.6.0
 */
@Singleton
public class LifecycleManager {
  private final Collection<LifecycleManaged> lifecycleManageds = new ArrayList<>();

  void register(LifecycleManaged lifecycleManaged) {
    lifecycleManageds.add(lifecycleManaged);
  }

  /**
   *
   *
   * @throws BiomedicusException if any of the services shutdown
   */
  public void triggerShutdown() throws BiomedicusException {
    List<BiomedicusException> exceptionList = new ArrayList<>();
    for (LifecycleManaged lifecycleManaged : lifecycleManageds) {
      try {
        lifecycleManaged.doShutdown();
      } catch (BiomedicusException e) {
        exceptionList.add(e);
      }
    }
    if (!exceptionList.isEmpty()) {
      BiomedicusException e = new BiomedicusException("Multiple exceptions ("
          + exceptionList.size() + ") while shutting down lifecycle resources");
      exceptionList.forEach(e::addSuppressed);
      throw e;
    }
  }
}
