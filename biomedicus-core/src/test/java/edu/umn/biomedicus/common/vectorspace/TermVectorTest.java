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

package edu.umn.biomedicus.common.vectorspace;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link TermVector}.
 */
public class TermVectorTest {

    Map<Integer, Integer> termCounts;
    TermVector termVector;

    @Mocked TermVectorSpace termVectorSpace;

    @BeforeMethod
    public void setUp() throws Exception {
        termCounts = new HashMap<>();

        termVector = new TermVector(termVectorSpace, termCounts);
    }

    @Test
    public void testOneArgConstructor() throws Exception {
        TermVector termVector = new TermVector(termVectorSpace);

        @SuppressWarnings("unchecked")
        Map<Integer, Integer> termCounts = Deencapsulation.getField(termVector, "termCounts");
        Assert.assertEquals(0, termCounts.size());
    }

    @Test
    public void testIncrementTerm() throws Exception {
        termCounts.put(256, 3);

        new Expectations() {{
            termVectorSpace.getIndex("term"); result = 256;
        }};

        termVector.incrementTerm("term");

        @SuppressWarnings("unchecked")
        int count = termCounts.get(256);
        Assert.assertEquals(4, count);
    }

    @Test
    public void testInitializeTerm() throws Exception {
        new Expectations() {{
            termVectorSpace.getIndex("term"); result = 256;
        }};

        termVector.incrementTerm("term");

        @SuppressWarnings("unchecked")
        int count = termCounts.get(256);
        Assert.assertEquals(1, count);
    }

    @Test
    public void testCountOfIndex() throws Exception {
        termCounts.put(256, 3);

        Assert.assertEquals(3, termVector.countOfIndex(256));
    }

    @Test
    public void testCountOfTerm() throws Exception {
        termCounts.put(256, 3);

        new Expectations() {{
            termVectorSpace.getIndex("term"); result = 256;
        }};

        Assert.assertEquals(3, termVector.countOfTerm("term"));
    }
}