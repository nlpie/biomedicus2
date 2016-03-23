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

package edu.umn.biomedicus.uima.rtf;

import edu.umn.biomedicus.common.simple.Spans;
import edu.umn.biomedicus.rtfuima.type.ViewIndex;
import mockit.*;
import org.apache.uima.jcas.JCas;
import org.testng.annotations.Test;

/**
 * Unit test for {@link CasIndexListener}.
 */
public class CasIndexListenerTest {
    @Tested CasIndexListener casIndexListener;

    @Injectable JCas originalDocumentView;

    @Test
    public void testWroteToDestination(@Mocked ViewIndex viewIndex) throws Exception {
        new Expectations() {{
            new ViewIndex(originalDocumentView, 200, 201); result = viewIndex;
        }};

        casIndexListener.wroteToDestination("aDestination", 20, Spans.spanning(200, 201));

        new Verifications() {{
            viewIndex.setDestinationIndex(20);
            viewIndex.setDestinationName("aDestination");
            viewIndex.addToIndexes();
        }};
    }
}