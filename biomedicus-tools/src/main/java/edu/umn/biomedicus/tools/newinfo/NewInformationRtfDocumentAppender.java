/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.tools.newinfo;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.tools.rtfrewriting.RegionTaggerBuilder;
import edu.umn.biomedicus.tools.rtfrewriting.SpecialTableMarker;
import edu.umn.biomedicus.tools.rtfrewriting.SpecialTableNotFoundException;
import edu.umn.biomedicus.tools.rtfrewriting.SymbolIndexedDocument;
import edu.umn.biomedicus.type.ClinicalNoteAnnotation;
import edu.umn.biomedicus.type.NewInformationAnnotation;
import edu.umn.biomedicus.uima.Views;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Writes the new information annotations into the original RTF document.
 *
 */
@ProvidedBy(NewInformationRtfDocumentAppenderLoader.class)
public class NewInformationRtfDocumentAppender  {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Characters to tag region begin by new information kind.
     */
    private final Map<String, String> beginSequences;

    /**
     * Characters to tag region end by new information kind.
     */
    private final Map<String, String> endSequences;

    /**
     * Marker to use for the color table.
     */
    private final String colorTableMarker;

    /**
     * Marker to use for the stylesheet.
     */
    private final String stylesheetMarker;

    public NewInformationRtfDocumentAppender(Map<String, String> beginSequences,
                                             Map<String, String> endSequences,
                                             String colorTableMarker,
                                             String stylesheetMarker) {
        this.beginSequences = beginSequences;
        this.endSequences = endSequences;
        this.colorTableMarker = colorTableMarker;
        this.stylesheetMarker = stylesheetMarker;
    }

    /**
     * 
     * @param jCas CAS object containing the original RTF in the ORIGINAL_DOCUMENT_VIEW and new information annotations in the SYSTEM_VIEW
     * @return String holding the RTF document appended with new information markers
     * @throws AnalysisEngineProcessException
     */
    public String modifyRtf(JCas jCas) throws BiomedicusException {
        JCas systemView;
        JCas originalDocumentView;
        try {
            systemView = jCas.getView(Views.SYSTEM_VIEW);
            originalDocumentView = jCas.getView(Views.ORIGINAL_DOCUMENT_VIEW);
        } catch (CASException e) {
            throw new BiomedicusException(e);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Rewriting a new information tagged rtf document");
            ClinicalNoteAnnotation clinicalNoteAnnotation = (ClinicalNoteAnnotation) originalDocumentView
                    .getAnnotationIndex(ClinicalNoteAnnotation.type)
                    .iterator()
                    .next();
            LOGGER.debug("Rewriting document: {}", clinicalNoteAnnotation.getDocumentId());
        }

        SymbolIndexedDocument symbolIndexedDocument = SymbolIndexedDocument.fromView(originalDocumentView);

        AnnotationIndex<Annotation> newInfosIndex = systemView.getAnnotationIndex(NewInformationAnnotation.type);

        boolean any = false;

        for (Annotation annotation : newInfosIndex) {
            @SuppressWarnings("unchecked")
            NewInformationAnnotation newInformationAnnotation = (NewInformationAnnotation) annotation;

            String kind = newInformationAnnotation.getKind();
            if (kind == null) {
                kind = "New";
            }
            LOGGER.debug("Tagging region with kind: {}", kind);
            String beginTag = beginSequences.get(kind);
            if (beginTag == null) {
                LOGGER.error("Begin tag was not found for kind: {}", kind);
                throw new IllegalStateException("Begin tag was not found for kind: " + kind);
            }
            String endTag = endSequences.get(kind);
            if (endTag == null) {
                LOGGER.error("End tag was not found for kind: {}", kind);
                throw new IllegalStateException("End tag was not found for kind: " + kind);
            }
            RegionTaggerBuilder.create()
                    .withDestinationName("Rtf")
                    .withBegin(newInformationAnnotation.getBegin())
                    .withEnd(newInformationAnnotation.getEnd())
                    .withBeginTag(beginTag)
                    .withEndTag(endTag)
                    .withSymbolIndexedDocument(symbolIndexedDocument)
                    .createRegionTagger()
                    .tagRegion();

            any = true;
        }

        if (!any && LOGGER.isWarnEnabled()) {
            ClinicalNoteAnnotation clinicalNoteAnnotation = (ClinicalNoteAnnotation) originalDocumentView
                    .getAnnotationIndex(ClinicalNoteAnnotation.type)
                    .iterator()
                    .next();
            LOGGER.warn("No new information annotations in document: {}", clinicalNoteAnnotation.getDocumentId());
        }

        String document = symbolIndexedDocument.getDocument();

        SpecialTableMarker specialTableMarker = new SpecialTableMarker(document, colorTableMarker, "\\colortbl");

        try {
            document = specialTableMarker.insertInTable();
        } catch (SpecialTableNotFoundException e) {
            LOGGER.warn("Color table was not found in the document.");
        }

        SpecialTableMarker stylesheetTableMarker = new SpecialTableMarker(document, stylesheetMarker, "\\stylesheet");

        try {
            document = stylesheetTableMarker.insertInTable();
        } catch (SpecialTableNotFoundException e) {
            LOGGER.warn("Stylesheet was not found in the document.");
        }
        
        return document;
    }
}
