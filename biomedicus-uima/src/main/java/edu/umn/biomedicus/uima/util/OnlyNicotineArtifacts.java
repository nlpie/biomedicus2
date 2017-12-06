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

package edu.umn.biomedicus.uima.util;

import edu.umn.biomedicus.common.TextIdentifiers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;

public class OnlyNicotineArtifacts extends CasAnnotator_ImplBase {

  private static final List<String> GOOD_TYPES = Arrays.asList(
      "NicotineAmount",
      "NicotineFrequency",
      "NicotineTemporal",
      "NicotineType",
      "NicotineStatus",
      "NicotineMethod"
  );

  @Override
  public void process(CAS aCAS) throws AnalysisEngineProcessException {
    CAS view = aCAS.getView(TextIdentifiers.SYSTEM);

    AnnotationIndex<AnnotationFS> annotationIndex = view.getAnnotationIndex();

    ArrayList<AnnotationFS> toRemove = new ArrayList<>(annotationIndex.size());
    for (AnnotationFS annotationFS : annotationIndex) {
      if (!GOOD_TYPES.contains(annotationFS.getType().getShortName())) {
        toRemove.add(annotationFS);
      }
    }

    toRemove.forEach(view::removeFsFromIndexes);
  }
}
