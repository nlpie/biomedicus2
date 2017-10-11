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

package edu.umn.biomedicus.common.dictionary;

import static org.testng.Assert.*;

import edu.umn.biomedicus.common.dictionary.StringsBag.Builder;
import org.testng.annotations.Test;

public class StringsBagTest {

  @Test
  public void testGetBytes() throws Exception {
    Builder builder = new Builder();
    builder.addTerm(new StringIdentifier(5));
    builder.addTerm(new StringIdentifier(5));
    builder.addTerm(new StringIdentifier(6));
    builder.addTerm(new StringIdentifier(10));

    StringsBag bag = builder.build();
    byte[] bytes = bag.getBytes();

    StringsBag bag2 = new StringsBag(bytes);

    assertEquals(bag2.uniqueTerms(), 3);
    assertEquals(bag2.size(), 4);
    assertEquals(bag2.countOf(new StringIdentifier(5)), 2);
    assertEquals(bag2.countOf(new StringIdentifier(6)), 1);
    assertEquals(bag2.countOf(new StringIdentifier(10)), 1);
  }
}