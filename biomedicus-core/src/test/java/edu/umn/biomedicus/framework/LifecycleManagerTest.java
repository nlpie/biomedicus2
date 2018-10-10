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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import edu.umn.biomedicus.exc.BiomedicusException;
import org.junit.jupiter.api.Test;

class LifecycleManagerTest {


  /**
   * All registered managed objects should be shutdown even if previous objects threw exceptions.
   */
  @Test
  void testShutdownWithExceptions() throws Exception {
    LifecycleManager lifecycleManager = new LifecycleManager();

    LifecycleManaged lifecycleManaged = mock(LifecycleManaged.class);
    doThrow(new BiomedicusException(), new BiomedicusException(), new BiomedicusException())
        .when(lifecycleManaged).doShutdown();

    lifecycleManager.register(lifecycleManaged);
    lifecycleManager.register(lifecycleManaged);
    lifecycleManager.register(lifecycleManaged);

    assertThrows(BiomedicusException.class, () -> lifecycleManager.triggerShutdown());
  }
}
