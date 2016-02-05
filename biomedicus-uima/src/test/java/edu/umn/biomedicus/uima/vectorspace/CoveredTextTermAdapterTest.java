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

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Unit test for {@link CoveredTextTermAdapter}.
 */
public class CoveredTextTermAdapterTest {
    @Tested
    CoveredTextTermAdapter coveredTextTermAdapter;

    @Injectable(value = "typeName")
    String annotationTypeName;

    @Test
    public void testTerms(@Mocked JCas cas,
                          @Mocked Predicate<AnnotationFS> exclusionTest,
                          @Mocked TypeSystem typeSystem,
                          @Mocked Type type,
                          @Mocked Annotation annotationFS) throws Exception {
        Spliterator<Annotation> annotations = Arrays.asList(annotationFS, annotationFS, annotationFS).spliterator();

        new Expectations() {{
            cas.getTypeSystem(); result = typeSystem;
            typeSystem.getType("typeName"); result = type;
            cas.getAnnotationIndex(type).spliterator(); result = annotations;
            exclusionTest.test(annotationFS); returns(true, false, true);
            annotationFS.getCoveredText(); returns("a", "b");
        }};

        List<String> collect = coveredTextTermAdapter.terms(cas, exclusionTest).collect(Collectors.toList());
        Assert.assertEquals(2, collect.size());
        Assert.assertTrue(collect.contains("a"));
        Assert.assertTrue(collect.contains("b"));
    }
}