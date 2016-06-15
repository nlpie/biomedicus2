/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.copying;

import mockit.*;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.testng.annotations.Test;

/**
 * Test class for {@link ViewCopier}.
 *
 * @author Ben Knoll
 */
public class ViewCopierTest {
    @Tested ViewCopier viewCopier;

    @Mocked FSIterator<FeatureStructure> fsIterator;

    @Mocked FeatureStructure featureStructure;

    @Mocked FeatureStructureCopyingQueue featureStructureCopyingQueue;

    @Injectable CAS oldCas;
    @Injectable CAS newCas;

    @Injectable JCas oldView;
    @Injectable JCas newView;

    @Test
    public void testMigrate() throws Exception {
        new Expectations() {{
            onInstance(oldView).getDocumentText(); result = "docText";
            fsIterator.hasNext(); result = new boolean[]{true, true, true, false};
            fsIterator.next(); result = featureStructure; times = 3;

            onInstance(newView).getCas(); result = newCas;
            onInstance(oldView).getCas(); result = oldCas;
            new FeatureStructureCopyingQueue(onInstance(oldCas), onInstance(newCas)); result = featureStructureCopyingQueue;
        }};

        viewCopier.migrate(oldView, newView);

        new Verifications() {{
            onInstance(newView).setDocumentText("docText");

            featureStructureCopyingQueue.enqueue(featureStructure); times = 3;
            featureStructureCopyingQueue.run();
        }};
    }
}