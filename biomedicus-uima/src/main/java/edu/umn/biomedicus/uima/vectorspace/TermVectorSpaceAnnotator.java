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

import edu.umn.biomedicus.common.simple.Spans;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.vectorspace.TermVector;
import edu.umn.biomedicus.common.vectorspace.TermVectorSpace;
import edu.umn.biomedicus.common.vectorspace.TermVectorSpaceManager;
import edu.umn.biomedicus.type.TermVectorEntryFS;
import edu.umn.biomedicus.type.TermVectorFS;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * UIMA annotator for running a term adapter and term consumer for the Vector space.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class TermVectorSpaceAnnotator extends JCasAnnotator_ImplBase {
    /**
     * UIMA parameter for the Term Exclusion strings.
     */
    public static final String PARAM_EXCLUSION_FILTER_STRINGS = "exclusionFilterStrings";

    /**
     * UIMA parameter for the Term Adapter Factory class.
     */
    public static final String PARAM_TERM_ADAPTER_FACTORY_CLASS = "termAdapterFactoryClass";

    /**
     * UIMA parameter for the Term Adapter Factory params.
     */
    public static final String PARAM_TERM_ADAPTER_PARAMS = "termAdapterParams";

    /**
     * UIMA parameter for the vector space identifier.
     */
    public static final String PARAM_VECTOR_SPACE_ID = "vectorSpaceIdentifier";

    /**
     * UIMA parameter for the view to use. Non-mandatory.
     */
    public static final String PARAM_VIEW_NAME = "viewName";

    /**
     * Adapts uima cas to terms.
     */
    private TermAdapter termAdapter;

    /**
     * View name to use.
     */
    @Nullable
    private String viewName;

    /**
     * The exclusion filters.
     */
    private List<ExclusionFilter> exclusionFilters;


    private String vectorSpaceIdentifier;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        try {
            String termAdapterFactoryClassName
                    = (String) aContext.getConfigParameterValue(PARAM_TERM_ADAPTER_FACTORY_CLASS);

            String termAdapterParamString = (String) aContext.getConfigParameterValue(PARAM_TERM_ADAPTER_PARAMS);
            String[] termAdapterParams = termAdapterParamString.split(" ");
            termAdapter = Class.forName(termAdapterFactoryClassName)
                    .asSubclass(TermAdapterFactory.class).newInstance().create(termAdapterParams);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new ResourceInitializationException(e);
        }

        try {
            String[] exclusionFilterStrings
                    = (String[]) aContext.getConfigParameterValue(PARAM_EXCLUSION_FILTER_STRINGS);

            exclusionFilters = new ArrayList<>();
            for (String exclusionFilterString : exclusionFilterStrings) {
                int firstSpace = exclusionFilterString.indexOf(" ");
                firstSpace = firstSpace != -1 ? firstSpace : exclusionFilterString.length();
                String className = exclusionFilterString.substring(0, firstSpace);
                ExclusionFilterFactory exclusionFilterFactory = Class.forName(className)
                        .asSubclass(ExclusionFilterFactory.class).newInstance();

                String paramString = exclusionFilterString.substring(firstSpace + 1);
                String[] params = paramString.split(" ");
                ExclusionFilter exclusionFilter = exclusionFilterFactory.create(params);

                exclusionFilters.add(exclusionFilter);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new ResourceInitializationException(e);
        }

        vectorSpaceIdentifier = (String) aContext.getConfigParameterValue(PARAM_VECTOR_SPACE_ID);

        viewName = (String) aContext.getConfigParameterValue(PARAM_VIEW_NAME);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        JCas view;
        try {
            view = viewName != null ? aJCas.getView(viewName) : aJCas;
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        int[] excludedIndexes = exclusionFilters.stream()
                .flatMap(exclusionFilter -> exclusionFilter.excludedSpans(view))
                .flatMapToInt(Span::indices)
                .distinct()
                .sorted()
                .toArray();

        // should be kept if any character in annotation is not in excluded
        Predicate<AnnotationFS> exclusionFilter = (annotationFS) -> {
            Span span = Spans.spanning(annotationFS.getBegin(), annotationFS.getEnd());
            return !span.allIndicesAreIn(excludedIndexes);
        };

        List<String> terms = termAdapter.terms(view, exclusionFilter).collect(Collectors.toList());

        TermVectorSpace termVectorSpace = TermVectorSpaceManager.getTermVectorSpace(vectorSpaceIdentifier);
        TermVector termVector = new TermVector(termVectorSpace);
        for (String term : terms) {
            termVectorSpace.addTerm(term);
            termVector.incrementTerm(term);
        }

        int numberOfTerms = termVector.numberOfTerms();

        TermVectorFS termVectorFS = new TermVectorFS(view);
        termVectorFS.setIdentifier(vectorSpaceIdentifier);
        FSArray fsArray = new FSArray(view, numberOfTerms);
        int i = 0;
        for (Map.Entry<Integer, Integer> entry : termVector.entries()) {
            TermVectorEntryFS termVectorEntryFS = new TermVectorEntryFS(view);
            termVectorEntryFS.setIndex(entry.getKey());
            termVectorEntryFS.setCount(entry.getValue());
            termVectorEntryFS.addToIndexes();
            fsArray.set(i++, termVectorEntryFS);
        }
        termVectorFS.setTerms(fsArray);
        termVectorFS.addToIndexes();
    }
}
