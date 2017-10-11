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

import static org.testng.Assert.*;

import edu.umn.biomedicus.exc.BiomedicusException;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.testng.annotations.Test;

public class LifecycleManagerTest {

  @Tested
  LifecycleManager lifecycleManager;

  @Mocked
  LifecycleManaged lifecycleManaged;

  /**
   * All registered managed objects should be shutdown even if previous objects threw exceptions.
   */
  @Test
  public void testShutdownWithExceptions() throws Exception {
    new Expectations() {{
      lifecycleManaged.doShutdown(); times = 3; result = new BiomedicusException();
    }};

    lifecycleManager.register(lifecycleManaged);
    lifecycleManager.register(lifecycleManaged);
    lifecycleManager.register(lifecycleManaged);

    assertThrows(() -> lifecycleManager.triggerShutdown());
  }
}