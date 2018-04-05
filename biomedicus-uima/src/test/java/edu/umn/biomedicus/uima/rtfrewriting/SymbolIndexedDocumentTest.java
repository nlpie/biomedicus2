/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.rtfrewriting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SymbolIndexedDocument}.
 */
public class SymbolIndexedDocumentTest {

  @Tested
  SymbolIndexedDocument symbolIndexedDocument;

  @Injectable
  List<SymbolLocation> symbolLocations;

  @Injectable
  Map<String, Map<Integer, Integer>> destinationMap;

  @Injectable
  String document = "／人◕ ‿‿ ◕人＼";

  @Mocked
  SymbolLocation symbolLocation;


  @Test
  public void testGetOriginalDocumentIndex() {
    new Expectations() {{
      symbolLocation.getIndex();
      result = 4;
      symbolLocations.get(anyInt);
      returns(symbolLocation, symbolLocation, symbolLocation, symbolLocation);
      symbolLocation.getOffset();
      returns(4, 4, 4, 4, 4);
      symbolLocation.getLength();
      returns(3, 3, 3, 3);
    }};

    int originalDocumentIndex = symbolIndexedDocument.getOriginalDocumentIndex(symbolLocation);

    assertEquals(originalDocumentIndex, 32);
  }
}
