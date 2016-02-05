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

package edu.umn.biomedicus.uima.vectorspace;

import edu.umn.biomedicus.common.vectorspace.TermVectorSpace;
import edu.umn.biomedicus.common.vectorspace.TermVectorSpaceManager;
import edu.umn.biomedicus.type.ClinicalNoteAnnotation;
import edu.umn.biomedicus.type.TermVectorEntryFS;
import edu.umn.biomedicus.type.TermVectorFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Writes the term vectors to Weka ARFF files.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class ArffTextTermVectorWriter extends JCasAnnotator_ImplBase {
    /**
     * Class logger
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * UIMA parameter for the feature of the class.
     */
    public static final String PARAM_CLASS_FEATURE_NAME = "classFeatureName";

    /**
     * UIMA parameter for the name of the output file.
     */
    public static final String PARAM_OUTPUT_FILE_NAME = "outputFileName";

    /**
     * UIMA parameter for the relation name.
     */
    public static final String PARAM_RELATION_NAME = "relationName";

    /**
     * UIMA parameter for the vector space name.
     */
    public static final String PARAM_VECTOR_SPACE_NAME = "vectorSpaceIdentifier";

    /**
     * UIMA parameter for the view name to use.
     */
    public static final String PARAM_VIEW_NAME = "viewName";

    /**
     * Stores the encountered classes.
     */
    private Set<String> classes = new HashSet<>();

    /**
     * The feature base name to retrieve the class of the document from on the clinical note annotation.
     */
    private String classFeatureName;

    /**
     * The output file to write to.
     */
    private Path outputFile;

    /**
     * Temporary output file
     */
    private Path tempFile;

    /**
     * The name/identifier of the vector space.
     */
    private String vectorSpaceIdentifier;

    /**
     * The view name to use.
     */
    @Nullable
    private String viewName;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        String outputFileName = (String) aContext.getConfigParameterValue(PARAM_OUTPUT_FILE_NAME);
        outputFile = Paths.get(outputFileName);
        String fileName = outputFile.getFileName().toString();
        tempFile = outputFile.resolveSibling(fileName + ".tmp");

        classFeatureName = (String) aContext.getConfigParameterValue(PARAM_CLASS_FEATURE_NAME);

        vectorSpaceIdentifier = (String) aContext.getConfigParameterValue(PARAM_VECTOR_SPACE_NAME);

        viewName = (String) aContext.getConfigParameterValue(PARAM_VIEW_NAME);

        if (Files.exists(tempFile)) {
            try {
                Files.delete(tempFile);
            } catch (IOException e) {
                throw new ResourceInitializationException(e);
            }
        }

        try {
            Files.createFile(tempFile);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        JCas view;

        try {
            view = viewName != null ? aJCas.getView(viewName) : aJCas;
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        try (
                BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardOpenOption.APPEND)
        ) {
            writer.write("{");

            AnnotationIndex<Annotation> clinicalNoteAnnotationIndex
                    = view.getAnnotationIndex(ClinicalNoteAnnotation.type);

            List<Annotation> clinicNoteAnnotations
                    = StreamSupport.stream(clinicalNoteAnnotationIndex.spliterator(), false)
                    .collect(Collectors.toList());

            if (clinicNoteAnnotations.size() != 1) {
                IllegalStateException cause
                        = new IllegalStateException("Document should have only one clinical note annotation");
                throw new AnalysisEngineProcessException(cause);
            }

            @SuppressWarnings("unchecked")
            ClinicalNoteAnnotation clinicalNoteAnnotation = (ClinicalNoteAnnotation) clinicNoteAnnotations.get(0);
            Feature classFeature = clinicalNoteAnnotation.getType().getFeatureByBaseName(classFeatureName);
            String docClass = clinicalNoteAnnotation.getStringValue(classFeature);
            LOGGER.debug("Setting class for document: {}", docClass);
            writer.write(0 + " " + docClass + ", ");
            classes.add(docClass);

            FSIterator<FeatureStructure> termVectorIterator = view.getIndexRepository()
                    .getAllIndexedFS(view.getCasType(TermVectorFS.type));
            while (termVectorIterator.hasNext()) {
                @SuppressWarnings("unchecked")
                TermVectorFS termVectorFS = (TermVectorFS) termVectorIterator.next();
                if (!vectorSpaceIdentifier.equals(termVectorFS.getIdentifier())) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                Iterator<TermVectorEntryFS> entriesIterator = Arrays.asList(termVectorFS.getTerms().toArray())
                        .stream()
                        .map((fs) -> (TermVectorEntryFS) fs)
                        .sorted((first, second) -> Integer.compare(first.getIndex(), second.getIndex()))
                        .iterator();

                while (entriesIterator.hasNext()) {
                    TermVectorEntryFS termVectorEntry = entriesIterator.next();
                    writer.write((termVectorEntry.getIndex() + 1) + " " + termVectorEntry.getCount());
                    if (entriesIterator.hasNext()) {
                        writer.write(", ");
                    }
                }
            }

            writer.write("}\n");
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        super.collectionProcessComplete();

        try (
                OutputStream outputStream = Files.newOutputStream(outputFile, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))
        ) {
            writer.write("@relation " + getContext().getConfigParameterValue(PARAM_RELATION_NAME) + "\n");
            writer.write("@attribute documentClass {");

            Iterator<String> iterator = classes.iterator();
            while (iterator.hasNext()) {
                writer.write(iterator.next() + (iterator.hasNext() ? ", " : "}\n"));
            }

            TermVectorSpace termVectorSpace = TermVectorSpaceManager.getTermVectorSpace(vectorSpaceIdentifier);
            List<String> terms = termVectorSpace.getTerms();
            for (String term : terms) {
                writer.write("@attribute \"" + term.replace("\\", "\\\\").replace("\"", "\\\"") + "\" NUMERIC\n");
            }
            writer.write("\n@data\n");
            writer.flush();

            Files.copy(tempFile, outputStream);
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }

        try {
            Files.delete(tempFile);
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
