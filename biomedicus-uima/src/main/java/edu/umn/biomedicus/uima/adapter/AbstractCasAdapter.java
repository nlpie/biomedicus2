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

package edu.umn.biomedicus.uima.adapter;

import com.google.common.base.Preconditions;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.uima.labels.LabelAdapters;
import java.util.Collection;
import java.util.HashMap;
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

public abstract class AbstractCasAdapter extends CasAnnotator_ImplBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCasAdapter.class);

  @Nullable
  private LabelAdapters labelAdapters;

  @Nullable
  private GuiceInjector guiceInjector;

  private Map<String, Object> settingsMap;

  private Collection<String> requiredClassNames;

  protected GuiceInjector getGuiceInjector() {
    return Preconditions.checkNotNull(guiceInjector);
  }

  protected Map<String, Object> getSettingsMap() {
    return Preconditions.checkNotNull(settingsMap);
  }

  protected Collection<String> getRequiredClassNames() {
    return Preconditions.checkNotNull(requiredClassNames);
  }

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    try {
      guiceInjector = (GuiceInjector) aContext.getResourceObject("guiceInjector");

      labelAdapters = guiceInjector.attach().getInstance(LabelAdapters.class);
    } catch (ResourceAccessException e) {
      throw new ResourceInitializationException(e);
    }

    settingsMap = new HashMap<>();
    for (String parameterName : aContext.getConfigParameterNames()) {
      if (parameterName != null) {
        settingsMap.put(parameterName, aContext.getConfigParameterValue(parameterName));
      }
    }
  }

  @Override
  public final void process(CAS aCAS) throws AnalysisEngineProcessException {
    CASArtifact casArtifact = new CASArtifact(labelAdapters, aCAS);
    try {
      process(casArtifact);
    } catch (BiomedicusException e) {
      LOGGER.error("error while processing document: " + casArtifact.getArtifactID());
      throw new AnalysisEngineProcessException(e);
    }
  }

  protected abstract void process(CASArtifact casArtifact) throws BiomedicusException;

  @Override
  public void destroy() {
    try {
      guiceInjector.detach();
    } catch (BiomedicusException e) {
      LOGGER.error("Failed to detach from guice injector", e);
    }
  }
}
