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
import java.util.concurrent.ConcurrentMap;

/**
 * Unit test for {@link TermVectorSpaceManager}.
 */
public class TermVectorSpaceManagerTest {
    @Tested TermVectorSpaceManager termVectorSpaceManager;

    @Injectable ConcurrentMap<String, TermVectorSpace> termVectorSpaces;

    @Test
    public void testGetExisting(@Mocked TermVectorSpace termVectorSpace) throws Exception {

        Deencapsulation.setField(TermVectorSpaceManager.class, "INSTANCE", termVectorSpaceManager);

        new Expectations() {{
            termVectorSpaces.get("terms"); result = termVectorSpace;
        }};

        TermVectorSpace terms = TermVectorSpaceManager.getTermVectorSpace("terms");

        Assert.assertEquals(termVectorSpace, terms);

        new Verifications() {{
            new TermVectorSpace(); times = 0;
            termVectorSpaces.putIfAbsent(anyString, withAny(termVectorSpace)); times = 0;
        }};
    }

    @Test
    public void testCreateAtSameTime(@Mocked TermVectorSpace termVectorSpace) throws Exception {

        Deencapsulation.setField(TermVectorSpaceManager.class, "INSTANCE", termVectorSpaceManager);

        new Expectations() {{
            termVectorSpaces.get("terms"); result = null;
            termVectorSpaces.putIfAbsent("terms", withAny(termVectorSpace));
        }};

        TermVectorSpace terms = TermVectorSpaceManager.getTermVectorSpace("terms");

        new Verifications() {{
            List<TermVectorSpace> termVectorSpaces = withCapture(new TermVectorSpace());
            Assert.assertNotEquals(termVectorSpaces.get(0), terms, "should return the existing rather than the created");
        }};
    }

    @Test
    public void testGetCreated(@Mocked TermVectorSpace termVectorSpace) throws Exception {
        Deencapsulation.setField(TermVectorSpaceManager.class, "INSTANCE", termVectorSpaceManager);

        new Expectations() {{
            termVectorSpaces.get("terms"); result = null;
            termVectorSpaces.putIfAbsent("terms", withAny(termVectorSpace)); result = null;
        }};

        TermVectorSpace terms = TermVectorSpaceManager.getTermVectorSpace("terms");

        new Verifications() {{
            List<TermVectorSpace> termVectorSpaces = withCapture(new TermVectorSpace());
            Assert.assertEquals(termVectorSpaces.get(0), terms, "should return the created");
        }};

    }
}