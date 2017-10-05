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

import com.google.common.base.Preconditions;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentSourceRunner;
import edu.umn.biomedicus.uima.labels.LabelAdapters;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs {@link edu.umn.biomedicus.framework.DocumentSource} classes using the
 * {@link DocumentSourceRunner} class using the UIMA pipeline framework.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class DocumentSourceUimaAdapter extends CollectionReader_ImplBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentSourceUimaAdapter.class);

  private static final List<String> KNOWN_PARAMETERS = Collections.singletonList("eagerLoad");

  @Nullable
  private DocumentSourceRunner documentSourceRunner;

  @Nullable
  private LabelAdapters labelAdapters;

  private int total;

  private int completed;
  private GuiceInjector guiceInjector;

  @Override
  public void initialize() throws ResourceInitializationException {
    UimaContext uimaContext = getUimaContext();
    try {
      guiceInjector = (GuiceInjector) uimaContext
          .getResourceObject("guiceInjector");
      documentSourceRunner = guiceInjector.createDocumentSourceRunner();
      labelAdapters = guiceInjector.attach().getInstance(LabelAdapters.class);
    } catch (ResourceAccessException e) {
      throw new ResourceInitializationException(e);
    }

    try {
      String documentSourceClassName = (String) uimaContext
          .getConfigParameterValue("documentSource");
      documentSourceRunner.setDocumentSourceClassName(documentSourceClassName);
    } catch (ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
    }

    Map<String, Object> settingsMap = new HashMap<>();
    for (String parameterName : uimaContext.getConfigParameterNames()) {
      if (parameterName != null && !KNOWN_PARAMETERS.contains(parameterName)) {
        settingsMap.put(parameterName, uimaContext.getConfigParameterValue(parameterName));
      }
    }

    try {
      documentSourceRunner.initialize(settingsMap, Collections.emptyMap());
    } catch (BiomedicusException e) {
      throw new ResourceInitializationException(e);
    }

    String[] eagerLoad = (String[]) uimaContext.getConfigParameterValue("eagerLoad");
    if (eagerLoad != null) {
      for (String className : eagerLoad) {
        try {
          documentSourceRunner.require(className);
        } catch (BiomedicusException e) {
          throw new ResourceInitializationException(e);
        }
      }
    }

    try {
      total = (int) documentSourceRunner.estimateTotal();
      completed = 0;
    } catch (BiomedicusException e) {
      throw new ResourceInitializationException(e);
    }

  }

  @Override
  public void getNext(CAS aCAS) throws IOException, CollectionException {
    Preconditions.checkNotNull(documentSourceRunner);
    try {
      documentSourceRunner.populateNext((docId) -> new CASDocument(labelAdapters, aCAS, docId));
    } catch (BiomedicusException e) {
      throw new CollectionException(e);
    }
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    Preconditions.checkNotNull(documentSourceRunner);
    try {
      boolean hasNext = documentSourceRunner.hasNext();
      if (!hasNext) {
        try {
          guiceInjector.detach();
        } catch (BiomedicusException e) {
          LOGGER.error("Failed to detach from guice injector", e);
        }
      }
      return hasNext;
    } catch (BiomedicusException e) {
      throw new CollectionException(e);
    }
  }

  @Override
  public Progress[] getProgress() {
    return new Progress[]{new ProgressImpl(completed, total, "Documents", true)};
  }

  @Override
  public void close() throws IOException {

  }
}
