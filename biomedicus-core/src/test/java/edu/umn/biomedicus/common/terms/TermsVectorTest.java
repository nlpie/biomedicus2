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

package edu.umn.biomedicus.common.terms;

import static org.testng.Assert.*;

import edu.umn.biomedicus.common.terms.TermsVector.Builder;
import org.testng.annotations.Test;

public class TermsVectorTest {

  @Test
  public void testBytes() throws Exception {
    Builder builder = new Builder();
    builder.addTerm(new IndexedTerm(5));
    builder.addTerm(new IndexedTerm(5));
    builder.addTerm(new IndexedTerm(6));
    builder.addTerm(new IndexedTerm(10));

    TermsVector vect = builder.build();
    byte[] bytes = vect.getBytes();

    TermsVector newVector = new TermsVector(bytes);

    assertEquals(newVector.length(), 4);
    assertEquals(newVector.get(0), new IndexedTerm(5));
    assertEquals(newVector.get(1), new IndexedTerm(5));
    assertEquals(newVector.get(2), new IndexedTerm(6));
    assertEquals(newVector.get(3), new IndexedTerm(10));
  }
}