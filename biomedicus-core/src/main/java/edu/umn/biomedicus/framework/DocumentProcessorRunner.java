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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.nlpengine.Document;
import java.util.Map;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs {@link DocumentProcessor} classes on documents, handling the instantiation and calling of
 * the document processor.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public final class DocumentProcessorRunner extends ScopedWork {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentProcessorRunner.class);

  @Nullable
  private Class<? extends DocumentProcessor> documentProcessorClass;

  @Inject
  DocumentProcessorRunner(
      Injector injector,
      SettingsTransformer settingsTransformer,
      @Named("globalSettings") Map<String, Object> globalSettings
  ) {
    super(injector, globalSettings, settingsTransformer);
  }

  /**
   * Uses the supplied guice injector to create a new document processor runner.
   *
   * @param injector injector to use to create the document processor runner.
   * @return new instance of a document processor runner.
   */
  public static DocumentProcessorRunner create(Injector injector) {
    return injector.getInstance(DocumentProcessorRunner.class);
  }

  /**
   * Sets the document processor class to instantiate and run.
   *
   * @param documentProcessorClass class object for the type of document processor to run.
   */
  public void setDocumentProcessorClass(Class<? extends DocumentProcessor> documentProcessorClass) {
    this.documentProcessorClass = documentProcessorClass;
  }

  /**
   * Sets the document processor class to instantiate and run by name.
   *
   * @param documentProcessorClassName the name of the document processor class.
   * @throws ClassNotFoundException if the class with the specified name was not found.
   */
  public void setDocumentProcessorClassName(String documentProcessorClassName)
      throws ClassNotFoundException {
    this.documentProcessorClass = Class.forName(documentProcessorClassName)
        .asSubclass(DocumentProcessor.class);
  }

  /**
   * Instantiates a document processor and then runs it on the document.
   *
   * @param document the document to pass to the document processor
   * @throws BiomedicusException if the class there was any exception thrown while instantiating or
   * running the document processor.
   */
  public void processDocument(Document document) throws BiomedicusException {
    checkNotNull(processorContext, "Processor context is null, initialize not ran");

    checkNotNull(settingsInjector, "Settings injector is null, initialize not ran");

    checkNotNull(documentProcessorClass, "Processor class has not been set");

    try {
      processorContext.call(() -> {
        settingsInjector.getInstance(documentProcessorClass).process(document);
        return null;
      });
    } catch (Exception e) {
      LOGGER.error("Error during processing on document: " + document.getDocumentId());
      throw new BiomedicusException(e);
    }
  }
}
