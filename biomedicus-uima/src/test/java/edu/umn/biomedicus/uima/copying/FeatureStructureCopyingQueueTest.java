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

package edu.umn.biomedicus.uima.copying;

import mockit.*;
import org.apache.uima.cas.FeatureStructure;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Deque;
import java.util.Map;

/**
 * Test for {@link FeatureStructureCopyingQueue}.
 */
public class FeatureStructureCopyingQueueTest {
    @Tested FeatureStructureCopyingQueue featureStructureCopyingQueue;

    @Injectable FsCopiers fsCopiers;

    @Injectable FsConstructors fsConstructors;

    @Injectable Deque<FeatureStructure> fsQueue;

    @Injectable Map<FeatureStructure, FeatureStructure> fsMap;

    @Injectable FeatureStructure retFs;

    @Mocked FeatureStructure mockFs;

    @Test
    public void testEnqueueShouldReturnExisting(@Injectable FeatureStructure featureStructure) throws Exception {
        new Expectations() {{
            fsMap.containsKey(featureStructure); result = true;
            fsMap.get(featureStructure); result = retFs;
        }};

        Assert.assertEquals(retFs, featureStructureCopyingQueue.enqueue(featureStructure));

        new Verifications() {{
            fsConstructors.createNewInstanceOfSameType(featureStructure); times = 0;
        }};
    }

    @Test
    public void testEnqueueShouldCreateNew(@Injectable FeatureStructure featureStructure) throws Exception {
        new Expectations() {{
            fsMap.containsKey(featureStructure); result = false;
            fsConstructors.createNewInstanceOfSameType(featureStructure); result = retFs;
        }};

        Assert.assertEquals(retFs, featureStructureCopyingQueue.enqueue(featureStructure));

        new Verifications() {{
            fsQueue.add(featureStructure);
            fsMap.put(featureStructure, retFs);
        }};
    }

    @Test
    public void testRun() throws Exception {
        new Expectations() {{
            fsQueue.isEmpty(); result = new boolean[]{false, false, false, true};
            fsQueue.poll(); result = mockFs; times = 3;
            fsMap.get(mockFs); result = mockFs; times = 3;
        }};

        featureStructureCopyingQueue.run();

        new Verifications() {{
            fsCopiers.copy(mockFs, mockFs); times = 3;
        }};
    }
}