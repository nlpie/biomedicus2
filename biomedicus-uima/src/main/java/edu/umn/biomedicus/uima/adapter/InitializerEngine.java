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

import java.util.Map;
import org.apache.uima.analysis_engine.impl.AnalysisEngineImplBase;
import org.apache.uima.analysis_engine.metadata.AnalysisEngineMetaData;
import org.apache.uima.resource.ResourceCreationSpecifier;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.resource.metadata.ResourceMetaData;
import org.apache.uima.resource.metadata.TypeSystemDescription;

public class InitializerEngine extends AnalysisEngineImplBase {

  @Override
  public boolean initialize(ResourceSpecifier aSpecifier, Map<String, Object> aAdditionalParams)
      throws ResourceInitializationException {
    if (!(aSpecifier instanceof ResourceCreationSpecifier)) {
      throw new ResourceInitializationException();
    }

    ResourceMetaData metaData = ((ResourceCreationSpecifier) aSpecifier).getMetaData();

    if (!(metaData instanceof AnalysisEngineMetaData)) {
      throw new ResourceInitializationException();
    }

    AnalysisEngineMetaData aeMetaData = (AnalysisEngineMetaData) metaData;

    TypeSystemDescription typeSystem = aeMetaData.getTypeSystem();

    return super.initialize(aSpecifier, aAdditionalParams);
  }
}
