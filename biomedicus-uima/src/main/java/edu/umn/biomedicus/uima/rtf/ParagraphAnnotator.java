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

package edu.umn.biomedicus.uima.rtf;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Annotates paragraphs in rtf text using annotations for the \par keyword.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class ParagraphAnnotator extends CasAnnotator_ImplBase {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ParagraphAnnotator.class);

  private String documentName;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    documentName = ((String) aContext.getConfigParameterValue("documentName"));
  }

  @Override
  public void process(CAS aCAS) {
    LOGGER.trace("Annotating rtf paragraphs.");
    CAS systemView = aCAS.getView(documentName);

    Type newParagraphType = systemView.getTypeSystem()
        .getType("edu.umn.biomedicus.rtfuima.type.NewParagraph");

    Type paragraphType = systemView.getTypeSystem()
        .getType("edu.umn.nlpengine.generated2_0.Paragraph");

    AnnotationIndex<AnnotationFS> newParagraphIndex = systemView
        .getAnnotationIndex(newParagraphType);
    int start = 0;

    for (AnnotationFS newParagraph : newParagraphIndex) {
      int end = newParagraph.getEnd();
      systemView.addFsToIndexes(
          systemView.createAnnotation(paragraphType, start, end));

      start = end;
    }
  }
}
