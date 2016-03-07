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
import edu.umn.biomedicus.uima.CasHelper;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Excludes indices based on the value of a feature.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class FeatureValueExclusionFilter implements ExclusionFilter {
    /**
     * The type of the feature structure
     */
    private final String annotationTypeName;

    /**
     * The base name of the feature
     */
    private final String featureBaseName;

    /**
     * The value of the feature.
     */
    private final String featureValue;

    /**
     * True will exclude all matching, false will exclude not matching
     */
    private final boolean excludeMatching;

    /**
     * Creates an exclusion filter which filters depending on either matching or not matching filter values.
     *
     * @param annotationTypeName the annotation's UIMA type name.
     * @param featureBaseName the feature base name to use.
     * @param featureValue the feature value to filter on.
     * @param excludeMatching whether to exclude matching or exclude not matching.
     */
    public FeatureValueExclusionFilter(String annotationTypeName,
                                       String featureBaseName,
                                       String featureValue,
                                       boolean excludeMatching) {
        this.annotationTypeName = annotationTypeName;
        this.featureBaseName = featureBaseName;
        this.featureValue = featureValue;
        this.excludeMatching = excludeMatching;
    }

    @Override
    public Stream<Span> excludedSpans(JCas cas) {
        CasHelper casHelper = new CasHelper(cas);
        Type annotationType = casHelper.getType(annotationTypeName);
        Feature feature = annotationType.getFeatureByBaseName(featureBaseName);

        return StreamSupport.stream(cas.getAnnotationIndex(annotationType).spliterator(), false)
                .filter(annotation -> {
                    String featureValueAsString = annotation.getFeatureValueAsString(feature);
                    boolean matches = featureValue.equals(featureValueAsString);
                    return excludeMatching ? matches : !matches;
                })
                .map(annotation -> Spans.spanning(annotation.getBegin(), annotation.getEnd()));
    }
}
