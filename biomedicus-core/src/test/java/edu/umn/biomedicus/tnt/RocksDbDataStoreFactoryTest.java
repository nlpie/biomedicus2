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

package edu.umn.biomedicus.tnt;

import static org.testng.Assert.*;

import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Test;

public class RocksDbDataStoreFactoryTest {

  @Test
  public void testWordPosBytes() throws Exception {
    String word = "aWord";
    PartOfSpeech pos = PartOfSpeech.NN;

    byte[] bytes = RocksDbDataStoreFactory.getPosWordBytes(pos, word);
    Pair<PartOfSpeech, String> pair = RocksDbDataStoreFactory.getPosWordFromBytes(bytes);
    assertEquals(pair.getFirst(), pos);
    assertEquals(pair.getSecond(), word);
  }

  @Test
  public void testPosListBytes() throws Exception {
    List<PartOfSpeech> partOfSpeeches = Arrays.asList(PartOfSpeech.NN, PartOfSpeech.NNP,
        PartOfSpeech.NNS);

    byte[] there = RocksDbDataStoreFactory.getPartsOfSpeechBytes(partOfSpeeches);
    List<PartOfSpeech> andBack = RocksDbDataStoreFactory.getPartsOfSpeechFromBytes(there);
    assertEquals(partOfSpeeches, andBack);
  }
}