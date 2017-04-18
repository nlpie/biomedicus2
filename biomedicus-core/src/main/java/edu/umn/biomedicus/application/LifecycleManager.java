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

package edu.umn.biomedicus.application;

import com.google.inject.Singleton;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Service responsible for calling all of the lifecycle managed objects and
 * telling them to shut down. Lifecycle manageds should be injected with this
 * service and add themselves using the register method.
 *
 * @since 1.6.0
 */
@Singleton
public class LifecycleManager {
    private final Collection<LifecycleManaged> lifecycleManageds
            = new ArrayList<>();

    public void register(LifecycleManaged lifecycleManaged) {
        lifecycleManageds.add(lifecycleManaged);
    }

    public void triggerShutdown() throws BiomedicusException {
        for (LifecycleManaged lifecycleManaged : lifecycleManageds) {
            lifecycleManaged.doShutdown();
        }
    }
}
