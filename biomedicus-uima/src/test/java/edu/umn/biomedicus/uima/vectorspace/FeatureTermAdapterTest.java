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

import edu.umn.biomedicus.uima.CasHelper;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for {@link FeatureTermAdapter}.
 */
public class FeatureTermAdapterTest {
    @Tested FeatureTermAdapter featureTermAdapter;

    @Injectable(value = "typeName") String fsTypeName;

    @Injectable(value = "feature") String featureBaseName;

    @Test
    public void testTerms(@Mocked JCas cas,
                          @Mocked Predicate<AnnotationFS> exclusionTest,
                          @Mocked CasHelper casHelper,
                          @Mocked Type type,
                          @Mocked Feature feature,
                          @Mocked FeatureStructure featureStructure,
                          @Mocked AnnotationFS annotationFS) throws Exception {
        List<FeatureStructure> featureStructures = Arrays.asList(featureStructure, annotationFS, annotationFS);

        new Expectations() {{
            new CasHelper(cas); result = casHelper;
            casHelper.getType("typeName"); result = type;
            type.getFeatureByBaseName("feature"); result = feature;
            casHelper.featureStructuresOfType(type); result = featureStructures.stream();
            exclusionTest.test(annotationFS); returns(true, false); times = 2;
            featureStructure.getStringValue(feature); result = "a";
            annotationFS.getStringValue(feature); result = "b";
        }};

        Stream<String> termsStream = featureTermAdapter.terms(cas, exclusionTest);
        List<String> terms = termsStream.collect(Collectors.toList());
        assertEquals(terms.size(), 2);
        assertEquals(terms.get(0), "a");
        assertEquals(terms.get(1), "b");
    }
}