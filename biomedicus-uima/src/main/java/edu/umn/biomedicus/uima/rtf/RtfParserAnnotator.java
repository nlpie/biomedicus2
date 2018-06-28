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

import edu.umn.biomedicus.rtf.exc.RtfReaderException;
import edu.umn.biomedicus.rtf.reader.ReaderRtfSource;
import edu.umn.biomedicus.rtf.reader.RtfParser;
import edu.umn.biomedicus.rtf.reader.RtfSource;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import edu.umn.nlpengine.Artifact;
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
public class RtfParserAnnotator extends CasAnnotator_ImplBase {

  /**
   * UIMA Parameter for the original document view name.
   */
  public static final String PARAM_ORIGINAL_DOCUMENT_VIEW_NAME = "rtfDocumentName";
  /**
   * UIMA Parameter for the target view name
   */
  public static final String PARAM_TARGET_VIEW_NAME = "documentName";
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
  private static final Logger LOGGER = LoggerFactory.getLogger(RtfParserAnnotator.class);
  /**
   * The Rtf parser.
   */
  @Nullable
  private RtfParserFactory rtfParserFactory;

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

    rtfParserFactory = RtfParserFactory
        .createByLoading(rtfPropertiesDesc, rtfControlKeywordsDesc, rtfCasMappingsDesc);

    originalDocumentViewName = (String) aContext
        .getConfigParameterValue(PARAM_ORIGINAL_DOCUMENT_VIEW_NAME);

    targetViewName = (String) aContext.getConfigParameterValue(PARAM_TARGET_VIEW_NAME);
  }

  @Override
  public void process(CAS aCAS) throws AnalysisEngineProcessException {
    Objects.requireNonNull(rtfParserFactory);
    Objects.requireNonNull(originalDocumentViewName);
    Objects.requireNonNull(targetViewName);

    LOGGER.trace("Parsing an rtf document from {} into CAS", originalDocumentViewName);

    CAS originalDocument = aCAS.getView(originalDocumentViewName);

    String documentText = originalDocument.getDocumentText();

    Artifact artifact = UimaAdapters.getArtifact(aCAS, null);

    CAS targetView;
    boolean isRtf;
    boolean parsed = false;
    if (documentText.indexOf("{\\rtf1") == 0) {
      StringReader reader = new StringReader(documentText);
      RtfSource rtfSource = new ReaderRtfSource(reader);

      RtfParser parser;
      try {
        parser = rtfParserFactory.createParser(aCAS, rtfSource);
      } catch (RtfReaderException e) {
        LOGGER.error("Failed to initialize rtf parser.");
        throw new AnalysisEngineProcessException(e);
      }
      try {
        parser.parseFile();
        parsed = true;
      } catch (RtfReaderException e) {
        LOGGER.warn("Irrecoverable error during parsing: " + artifact.getArtifactID(), e);
      }
      if (!parser.finish()) {
        LOGGER.warn("Document with unclosed Rtf group(s): " + artifact.getArtifactID());
        parsed = false;
      }

      isRtf = true;
    } else {
      targetView = aCAS.createView(targetViewName);
      targetView.setDocumentText(documentText);
      isRtf = false;
    }
    if (!parsed) {
      LOGGER.warn("Failed to completely parse document from rtf: " + artifact.getArtifactID());
    }
    artifact.getMetadata().put("isRtfComplete", Boolean.toString(parsed));

    artifact.getMetadata().put("isRtf", Boolean.toString(isRtf));
  }
}
