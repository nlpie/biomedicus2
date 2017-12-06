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

package edu.umn.biomedicus.common.viterbi;

import static org.testng.AssertJUnit.assertEquals;

import java.util.NoSuchElementException;
import org.testng.annotations.Test;

/**
 * Test class for {@link HistoryChain}.
 */
public class HistoryChainTest {

  @Test
  public void testGetPayload() throws Exception {
    HistoryChain<String> historyChain = new HistoryChain<>(null, "payload");
    assertEquals("payload", historyChain.getState());
  }

  @Test
  public void testGetNonnullPayloadSkips() throws Exception {
    HistoryChain<String> historyChain = new HistoryChain<>(null, "payload").skip()
        .append("payload2");
    assertEquals("payload", historyChain.getNonnullPayload(1));
  }

  @Test
  public void testGetNonnullPayload() throws Exception {
    HistoryChain<String> historyChain = new HistoryChain<>(null, "payload1").append("payload2");
    assertEquals("payload1", historyChain.getNonnullPayload(1));
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void testGetNonnullPayloadExc() throws Exception {
    HistoryChain<String> historyChain = new HistoryChain<>(null, "payload1").append("payload2");
    assertEquals("payload1", historyChain.getNonnullPayload(2));
  }

  @Test
  public void testGetPrevious() throws Exception {
    HistoryChain<String> prev = new HistoryChain<>(null, "payload1");
    HistoryChain<String> historyChain = prev.append("payload2");
    assertEquals(prev, historyChain.getPrevious());
  }
}