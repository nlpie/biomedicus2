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

package edu.umn.biomedicus.uima.common;

import mockit.*;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Unit test for {@link CasHelper}.
 */
public class CasHelperTest {
    @Tested CasHelper casHelper;

    @Injectable CAS cas;

    @Test
    public void testJCasConstructor(@Mocked JCas jCas) throws Exception {
        new Expectations() {{
            jCas.getCas(); result = cas;
        }};

        CasHelper casHelper = new CasHelper(jCas);

        @SuppressWarnings("unchecked")
        CAS cas = Deencapsulation.getField(casHelper, "cas");
        Assert.assertEquals(cas, this.cas);
    }

    @Test
    public void testFeatureStructuresOfType(@Mocked Type type,
                                            @Mocked FSIterator<FeatureStructure> fsIterator,
                                            @Mocked Spliterators spliterators,
                                            @Mocked Spliterator<FeatureStructure> spliterator,
                                            @Mocked StreamSupport streamSupport,
                                            @Mocked Stream<FeatureStructure> stream) throws Exception {
        new Expectations() {{
            cas.getIndexRepository().getAllIndexedFS(type); result = fsIterator; times = 1;
            Spliterators.spliteratorUnknownSize(fsIterator, anyInt); result = spliterator; times = 1;
            StreamSupport.stream(spliterator, anyBoolean); result = stream; times = 1;
        }};

        Stream<FeatureStructure> featureStructureStream = casHelper.featureStructuresOfType(type);
        Assert.assertEquals(featureStructureStream, stream);
    }

    @Test
    public void testFeatureStructuresOfTypeName(@Mocked Type type,
                                                @Mocked FSIterator<FeatureStructure> fsIterator,
                                                @Mocked Spliterators spliterators,
                                                @Mocked Spliterator<FeatureStructure> spliterator,
                                                @Mocked StreamSupport streamSupport,
                                                @Mocked Stream<FeatureStructure> stream) throws Exception {
        new Expectations() {{
            cas.getTypeSystem().getType("typeName"); result = type;
            cas.getIndexRepository().getAllIndexedFS(type); result = fsIterator; times = 1;
            Spliterators.spliteratorUnknownSize(fsIterator, anyInt); result = spliterator; times = 1;
            StreamSupport.stream(spliterator, anyBoolean); result = stream; times = 1;
        }};

        Stream<FeatureStructure> featureStructureStream = casHelper.featureStructuresOfType("typeName");
        Assert.assertEquals(featureStructureStream, stream);
    }

    @Test
    public void testGetType(@Mocked Type type) throws Exception {
        new Expectations() {{
            cas.getTypeSystem().getType("typeName"); result = type;
        }};

        Type typeReturn = casHelper.getType("typeName");
        Assert.assertEquals(typeReturn, type);
    }
}