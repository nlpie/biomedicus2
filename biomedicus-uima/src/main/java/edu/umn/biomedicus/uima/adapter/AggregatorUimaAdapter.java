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

import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.AggregatorRunner;
import edu.umn.biomedicus.uima.labels.LabelAdapters;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for running {@link edu.umn.biomedicus.framework.Aggregator} classes.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class AggregatorUimaAdapter extends CasAnnotator_ImplBase {

  private static final List<String> KNOWN_PARAMETERS = Arrays
      .asList("viewName", "eagerLoad", "postProcessors");

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentProcessorUimaAdapter.class);

  @Nullable
  private AggregatorRunner aggregatorRunner;

  @Nullable
  private LabelAdapters labelAdapters;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {

      GuiceInjector guiceInjector = (GuiceInjector) aContext
          .getResourceObject("guiceInjector");

      aggregatorRunner = guiceInjector.createAggregatorRunner();
      labelAdapters = guiceInjector.getInjector().getInstance(LabelAdapters.class);
    } catch (ResourceAccessException e) {
      throw new ResourceInitializationException(e);
    }

    try {
      String aggClassName = (String) aContext.getConfigParameterValue("aggregator");
      aggregatorRunner.setAggregatorClassName(aggClassName);
    } catch (ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
    }

    Map<String, Object> settingsMap = new HashMap<>();
    for (String parameterName : aContext.getConfigParameterNames()) {
      if (parameterName != null && !KNOWN_PARAMETERS.contains(parameterName)) {
        settingsMap.put(parameterName, aContext.getConfigParameterValue(parameterName));
      }
    }

    try {
      aggregatorRunner.initialize(settingsMap, Collections.emptyMap());
    } catch (BiomedicusException e) {
      throw new ResourceInitializationException(e);
    }

    String[] eagerLoad = (String[]) aContext.getConfigParameterValue("eagerLoad");
    if (eagerLoad != null) {
      for (String className : eagerLoad) {
        try {
          aggregatorRunner.require(className);
        } catch (BiomedicusException e) {
          throw new ResourceInitializationException(e);
        }
      }
    }
  }

  @Override
  public void process(CAS cas) throws AnalysisEngineProcessException {
    if (aggregatorRunner == null) {
      throw new IllegalStateException("Document processor runner is null.");
    }

    try {
      CASDocument casDocument = new CASDocument(labelAdapters, cas);
      aggregatorRunner.processDocument(casDocument);
    } catch (BiomedicusException e) {
      LOGGER.error("error while processing document");
      throw new AnalysisEngineProcessException(e);
    }
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    assert aggregatorRunner != null;

    try {
      aggregatorRunner.doneProcessing();
    } catch (BiomedicusException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }
}
