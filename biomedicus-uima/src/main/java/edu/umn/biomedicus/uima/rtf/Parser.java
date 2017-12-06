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

import edu.umn.nlpengine.Document;
import edu.umn.biomedicus.rtf.exc.RtfReaderException;
import edu.umn.biomedicus.rtf.reader.ReaderRtfSource;
import edu.umn.biomedicus.rtf.reader.RtfSource;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;
import javax.annotation.Nullable;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Annotator which parses rtf documents.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class Parser extends CasAnnotator_ImplBase {

  /**
   * UIMA Parameter for the original document view name.
   */
  public static final String PARAM_ORIGINAL_DOCUMENT_VIEW_NAME = "originalDocumentViewName";
  /**
   * UIMA Parameter for the target view name
   */
  public static final String PARAM_TARGET_VIEW_NAME = "targetViewName";
  /**
   * UIMA Parameter for the rtf properties descriptor classpath reference.
   */
  public static final String PARAM_RTF_PROPERTIES_DESC = "rtfPropertiesDesc";
  /**
   * UIMA parameter for the control keywords descriptor classpath reference.
   */
  public static final String PARAM_RTF_CONTROL_KEYWORDS_DESC = "rtfControlKeywordsDesc";
  /**
   * UIMA parameter for the cas mappings descriptor classpath reference.
   */
  public static final String PARAM_RTF_CAS_MAPPINGS_DESC = "rtfCasMappingsDesc";
  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);
  /**
   * The Rtf parser.
   */
  @Nullable
  private CasRtfParser casRtfParser;

  /**
   * Original document view name.
   */
  @Nullable
  private String originalDocumentViewName;

  /**
   * Target view name.
   */
  @Nullable
  private String targetViewName;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    LOGGER.info("initializing rtf parser");

    String rtfPropertiesDesc = (String) aContext.getConfigParameterValue(PARAM_RTF_PROPERTIES_DESC);

    String rtfControlKeywordsDesc = (String) aContext
        .getConfigParameterValue(PARAM_RTF_CONTROL_KEYWORDS_DESC);

    String rtfCasMappingsDesc = (String) aContext
        .getConfigParameterValue(PARAM_RTF_CAS_MAPPINGS_DESC);

    casRtfParser = CasRtfParser
        .createByLoading(rtfPropertiesDesc, rtfControlKeywordsDesc, rtfCasMappingsDesc);

    originalDocumentViewName = (String) aContext
        .getConfigParameterValue(PARAM_ORIGINAL_DOCUMENT_VIEW_NAME);

    targetViewName = (String) aContext.getConfigParameterValue(PARAM_TARGET_VIEW_NAME);
  }

  @Override
  public void process(CAS aCAS) throws AnalysisEngineProcessException {
    Objects.requireNonNull(casRtfParser);
    Objects.requireNonNull(originalDocumentViewName);
    Objects.requireNonNull(targetViewName);

    LOGGER.debug("Parsing an rtf document from {} into CAS", originalDocumentViewName);

    CAS originalDocument = aCAS.getView(originalDocumentViewName);

    String documentText = originalDocument.getDocumentText();

    CAS targetView;
    boolean isRtf;
    if (documentText.indexOf("{\\rtf1") == 0) {
      StringReader reader = new StringReader(documentText);
      RtfSource rtfSource = new ReaderRtfSource(reader);

      try {
        casRtfParser.parseFile(aCAS, rtfSource);
      } catch (IOException | RtfReaderException e) {
        throw new AnalysisEngineProcessException(e);
      }

      isRtf = true;
    } else {
      targetView = aCAS.createView(targetViewName);
      targetView.setDocumentText(documentText);
      isRtf = false;
    }

    Document document = UimaAdapters.getDocument(aCAS, null);
    document.getMetadata().put("isRtf", Boolean.toString(isRtf));
  }
}
