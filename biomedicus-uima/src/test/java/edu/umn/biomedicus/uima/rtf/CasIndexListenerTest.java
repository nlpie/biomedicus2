/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.rtf;

import edu.umn.biomedicus.framework.store.Span;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.testng.annotations.Test;

/**
 * Unit test for {@link CasIndexListener}.
 */
public class CasIndexListenerTest {

  @Tested
  CasIndexListener casIndexListener;

  @Injectable
  CAS originalDocumentView;

  @Test
  public void testWroteToDestination(@Mocked Type type,
      @Mocked Feature feature,
      @Mocked AnnotationFS annotationFS)
      throws Exception {
    new Expectations() {{
      originalDocumentView.createAnnotation(type, 200, 201);
      result = annotationFS;
    }};

    casIndexListener.wroteToDestination("aDestination", 20, Span.create(200, 201));

    new Verifications() {{
      annotationFS.setIntValue(feature, 20);
      annotationFS.setStringValue(feature, "aDestination");
      originalDocumentView.addFsToIndexes(annotationFS);
    }};
  }
}