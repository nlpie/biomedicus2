/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.tools.rtfrewriting;

import edu.umn.biomedicus.uima.common.Views;
import edu.umn.biomedicus.uima.files.FileNameProviders;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.Type;
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
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.IntStream;

/**
 * Inserts placeholders for annotations into RTF documents.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class AnnotationInsertingWriter extends JCasAnnotator_ImplBase {
    /**
     * The annotation types to insert into the the rtf document.
     */
    @Nullable
    private String[] annotationTypes = null;

    /**
     * The output directory.
     */
    @Nullable
    private Path outputDir;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        annotationTypes = (String[]) aContext.getConfigParameterValue("annotationTypes");

        outputDir = Paths.get((String) aContext.getConfigParameterValue("outputDirectory"));
        try {
            Files.createDirectories(outputDir);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        JCas originalDocumentView;
        try {
            originalDocumentView = aJCas.getView(Views.ORIGINAL_DOCUMENT_VIEW);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
        SymbolIndexedDocument symbolIndexedDocument = SymbolIndexedDocument.fromView(originalDocumentView);

        JCas view;
        try {
            view = aJCas.getView(Views.SYSTEM_VIEW);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        TreeSet<Integer> covered = new TreeSet<>();
        for (String annotationType : Objects.requireNonNull(annotationTypes)) {
            Type type = view.getTypeSystem().getType(annotationType);

            AnnotationIndex<Annotation> annotationIndex = view.getAnnotationIndex(type);

            for (Annotation annotation : annotationIndex) {
                IntStream.rangeClosed(annotation.getBegin(), annotation.getEnd()).forEach(covered::add);
            }
        }

        Iterator<Integer> iterator = covered.iterator();
        int next = iterator.next();
        int last = -1;
        while (iterator.hasNext()) {
            int first = next;
            while (iterator.hasNext()) {
                last = next;
                next = iterator.next();
                if (next - last > 1) {
                    break;
                }
            }
            RegionTaggerBuilder.create()
                    .withBeginTag("\\u2222221B ")
                    .withEndTag("\\u2222221E ")
                    .withSymbolIndexedDocument(symbolIndexedDocument)
                    .withDestinationName(Views.SYSTEM_VIEW)
                    .withBegin(first)
                    .withEnd(last)
                    .createRegionTagger()
                    .tagRegion();
        }

        String rewrittenDocument = symbolIndexedDocument.getDocument();

        String fileName = FileNameProviders.fromSystemView(view, ".rtf");
        Path file = outputDir.resolve(fileName);

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(file, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
            bufferedWriter.write(rewrittenDocument);
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
