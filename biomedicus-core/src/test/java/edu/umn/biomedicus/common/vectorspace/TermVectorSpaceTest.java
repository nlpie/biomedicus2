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

import mockit.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Unit test for {@link TermVectorSpace}.
 */
public class TermVectorSpaceTest {
    @Tested TermVectorSpace termVectorSpace;

    @Injectable Map<String, Integer> termIndices;

    @Injectable List<String> terms;

    @Test
    public void testNoArgConstructor() throws Exception {
        TermVectorSpace termVectorSpace = new TermVectorSpace();

        @SuppressWarnings("unchecked")
        List<String> terms = Deencapsulation.getField(termVectorSpace, "terms");
        Assert.assertEquals(0, terms.size());
    }

    @Test
    public void testAddTerm() throws Exception {
        new Expectations() {{
            termIndices.containsKey("term"); result = false;
            termIndices.containsKey("term"); result = false;
            terms.size(); result = 100;
        }};

        termVectorSpace.addTerm("term");

        new FullVerificationsInOrder() {{
            terms.add("term");
            termIndices.put("term", 100);
        }};
    }

    @Test
    public void testAddExistingTerm() throws Exception {
        new Expectations() {{
            termIndices.containsKey("term"); result = true;
        }};

        termVectorSpace.addTerm("term");

        new FullVerificationsInOrder() {{
            terms.size(); times = 0;
            terms.add("term"); times = 0;
        }};
    }

    @Test
    public void testTermAddedAtSameTime() throws Exception {
        new Expectations() {{
            termIndices.containsKey("term"); returns(false, true);
        }};

        termVectorSpace.addTerm("term");

        new Verifications() {{
            terms.size(); times = 0;
            terms.add("term"); times = 0;
        }};
    }

    @Test
    public void testGetIndex() throws Exception {
        new Expectations() {{
            termIndices.get("aTerm"); result = 42;
        }};

        Assert.assertEquals(42, termVectorSpace.getIndex("aTerm"));
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void testGetIndexNull() throws Exception {
        new Expectations() {{
            termIndices.get("aTerm"); result = null;
        }};

        termVectorSpace.getIndex("aTerm");

        Assert.fail();
    }

    @Test
    public void testGetTerm() throws Exception {
        new Expectations() {{
            terms.get(4); result = "aTerm";
        }};

        Assert.assertEquals("aTerm", termVectorSpace.getTerm(4));
    }
}