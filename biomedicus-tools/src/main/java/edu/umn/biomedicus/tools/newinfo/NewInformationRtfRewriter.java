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

import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.tools.rtfrewriting.RegionTaggerBuilder;
import edu.umn.biomedicus.tools.rtfrewriting.SpecialTableMarker;
import edu.umn.biomedicus.tools.rtfrewriting.SpecialTableNotFoundException;
import edu.umn.biomedicus.tools.rtfrewriting.SymbolIndexedDocument;
import edu.umn.biomedicus.type.ClinicalNoteAnnotation;
import edu.umn.biomedicus.type.NewInformationAnnotation;
import edu.umn.biomedicus.uima.Views;
import edu.umn.biomedicus.uima.files.DirectoryOutputStreamFactory;
import edu.umn.biomedicus.uima.files.FileNameProvider;
import edu.umn.biomedicus.uima.files.FileNameProviders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Writes the new information annotations into the original RTF document.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class NewInformationRtfRewriter extends JCasAnnotator_ImplBase {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * UIMA parameter for output directory.
     */
    public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";

    /**
     * The uima parameter for the begin sequences map.
     */
    public static final String PARAM_BEGIN_MAP = "beginMap";

    /**
     * The uima parameter for the end sequences map.
     */
    public static final String PARAM_END_MAP = "endMap";

    /**
     * The uima parameter for the color table marker.
     */
    public static final String PARAM_COLOR_TABLE_MARKER = "colorTableMarker";

    /**
     * The uima parameter for the stylesheet marker.
     */
    public static final String PARAM_STYLESHEET_MARKER = "stylesheetMarker";

    /**
     * Characters to tag region begin by new information kind.
     */
    @Nullable
    private Map<String, String> beginSequences;

    /**
     * Characters to tag region end by new information kind.
     */
    @Nullable
    private Map<String, String> endSequences;

    /**
     * Marker to use for the color table.
     */
    @Nullable
    private String colorTableMarker;

    /**
     * Marker to use for the stylesheet.
     */
    @Nullable
    private String stylesheetMarker;

    /**
     * Provides the output stream to the rewritten file.
     */
    @Nullable
    private DirectoryOutputStreamFactory writerFactory = null;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        LOGGER.info("Initializing new information RTF rewriter.");

        String outputDirectory = (String) aContext.getConfigParameterValue(PARAM_OUTPUT_DIRECTORY);

        try {
            writerFactory = new DirectoryOutputStreamFactory(Paths.get(outputDirectory));
        } catch (BiomedicusException e) {
            throw new ResourceInitializationException(e);
        }

        String[] beginMapArray = (String[]) aContext.getConfigParameterValue(PARAM_BEGIN_MAP);

        beginSequences = new HashMap<>();
        int beginMapLength = beginMapArray.length;
        for (int i = 0; i < beginMapLength; i+=2) {
            beginSequences.put(beginMapArray[i], beginMapArray[i + 1]);
        }

        String[] endMapArray = (String[]) aContext.getConfigParameterValue(PARAM_END_MAP);

        endSequences = new HashMap<>();
        int endMapLength = endMapArray.length;
        for (int i = 0; i < endMapLength; i+=2) {
            endSequences.put(endMapArray[i], endMapArray[i + 1]);
        }

        colorTableMarker = (String) aContext.getConfigParameterValue(PARAM_COLOR_TABLE_MARKER);

        stylesheetMarker = (String) aContext.getConfigParameterValue(PARAM_STYLESHEET_MARKER);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        assert writerFactory != null;
        assert beginSequences != null;
        assert endSequences != null;
        assert colorTableMarker != null;
        assert stylesheetMarker != null;

        JCas systemView;
        JCas originalDocumentView;
        try {
            systemView = jCas.getView(Views.SYSTEM_VIEW);
            originalDocumentView = jCas.getView(Views.ORIGINAL_DOCUMENT_VIEW);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
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


        Path path;
        try {
            FileNameProvider fileNameProvider = FileNameProviders.fromSystemView(systemView, ".rtf");
            path = writerFactory.getPath(fileNameProvider);
        } catch (BiomedicusException e) {
            throw new AnalysisEngineProcessException(e);
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Writing rewritten RTF document to location: {}", path.toString());
        }

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE)) {
            bufferedWriter.write(document);
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
