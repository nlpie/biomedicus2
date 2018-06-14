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


package edu.umn.biomedicus.concepts

import java.nio.ByteBuffer
import kotlin.test.*

class ConceptRowTest {
    @Test
    fun `test byte serialization`() {
        val first = ConceptRow(SUI(1), CUI(2), TUI(3), 7).bytes
        val second = ConceptRow(SUI(4), CUI(5), TUI(6), 8).bytes

        val array = ByteBuffer.allocate(first.size + second.size).put(first).put(second).array()

        val buffer = ByteBuffer.wrap(array)

        val firstO = ConceptRow.next(buffer)
        val secondO = ConceptRow.next(buffer)

        assertEquals(firstO.sui.identifier(), 1)
        assertEquals(firstO.cui.identifier(), 2)
        assertEquals(firstO.tui.identifier(), 3)
        assertEquals(firstO.source, 7)
        assertEquals(secondO.sui.identifier(), 4)
        assertEquals(secondO.cui.identifier(), 5)
        assertEquals(secondO.tui.identifier(), 6)
        assertEquals(secondO.source, 8)
    }
}