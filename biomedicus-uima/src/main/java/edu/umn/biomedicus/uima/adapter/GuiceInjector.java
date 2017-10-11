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

package edu.umn.biomedicus.uima.adapter;

import com.google.inject.Injector;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.AggregatorRunner;
import edu.umn.biomedicus.framework.Application;
import edu.umn.biomedicus.framework.DocumentProcessorRunner;
import edu.umn.biomedicus.framework.DocumentSourceRunner;
import edu.umn.biomedicus.framework.LifecycleManager;
import java.util.concurrent.Semaphore;
import org.apache.uima.resource.Resource_ImplBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice injector resource implementation.
 *
 * @author Ben Knoll
 * @since 1.4
 */
public final class GuiceInjector extends Resource_ImplBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(GuiceInjector.class);

  private Injector injector;
  private LifecycleManager lifecycleManager;

  private final Semaphore semaphore = new Semaphore(Integer.MAX_VALUE);

  public GuiceInjector() {
    LOGGER.info("Initializing Guice Injector Resource");
    try {
      Application application = UimaBootstrapper.create();
      injector = application.getInjector();
      lifecycleManager = injector.getInstance(LifecycleManager.class);
    } catch (BiomedicusException e) {
      throw new IllegalStateException(e);
    }
  }

  public Injector attach() {
    if (!semaphore.tryAcquire(1)) {
      throw new IllegalStateException("Somehow attached Integer.MAX_VALUE things to Guice resource");
    }
    return injector;
  }

  public void detach() throws BiomedicusException {
    semaphore.release(1);
    if (semaphore.tryAcquire(Integer.MAX_VALUE)) {
      injector = null;
      lifecycleManager.triggerShutdown();
    }
  }

  public DocumentProcessorRunner createDocumentProcessorRunner() {
    return DocumentProcessorRunner.create(injector);
  }

  public DocumentSourceRunner createDocumentSourceRunner() {
    return DocumentSourceRunner.create(injector);
  }

  public AggregatorRunner createAggregatorRunner() {
    return AggregatorRunner.create(injector);
  }
}


