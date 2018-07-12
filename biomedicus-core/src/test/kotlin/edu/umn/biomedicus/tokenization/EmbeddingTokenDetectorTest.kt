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

package edu.umn.biomedicus.tokenization

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.Labeler
import kotlin.test.Test
import kotlin.test.assertEquals

class EmbeddingTokenDetectorTest {

    private val embeddingTokenDetector = EmbeddingTokenDetector()

    @Test
    internal fun `split and replace numbers`() {
        val embeddingTokens = embeddingTokenDetector.detect("0.123456789")
            .toList()

        assertEquals(
            listOf(
                EmbeddingToken(0, 1, "zero", true),
                EmbeddingToken(1, 2, " "),
                EmbeddingToken(2, 3, "one", true),
                EmbeddingToken(3, 4, "two", true),
                EmbeddingToken(4, 5, "three", true),
                EmbeddingToken(5, 6, "four", true),
                EmbeddingToken(6, 7, "five", true),
                EmbeddingToken(7, 8, "six", true),
                EmbeddingToken(8, 9, "seven", true),
                EmbeddingToken(9, 10, "eight", true),
                EmbeddingToken(10, 11, "nine", true)
            ), embeddingTokens
        )
    }

    @Test
    internal fun `split on whitespace`() {
        val embeddingTokens = embeddingTokenDetector.detect("hi there")
            .toList()

        assertEquals(
            listOf(
                EmbeddingToken(0, 2, "hi", true),
                EmbeddingToken(3, 8, "there", true)
            ),
            embeddingTokens
        )
    }

    @Test
    internal fun `lowercase words`() {
        val embeddingTokens = embeddingTokenDetector.detect("Blah")
            .toList()

        assertEquals(
            listOf(
                EmbeddingToken(0, 4, "blah", true)
            ),
            embeddingTokens
        )
    }

    @Test
    internal fun `replace punct with spaces`() {
        val embeddingTokens = embeddingTokenDetector.detect("can't\n won't")
            .toList()

        assertEquals(
            listOf(
                EmbeddingToken(0, 3, "can"),
                EmbeddingToken(3, 4, " "),
                EmbeddingToken(4, 5, "t"),
                EmbeddingToken(7, 10, "won"),
                EmbeddingToken(10, 11, " "),
                EmbeddingToken(11, 12, "t")
            ),
            embeddingTokens
        )
    }

    @Test
    internal fun `test document run`() {
        val labeler = mock<Labeler<EmbeddingToken>> {
            on(it.addAll(any())).then {
                it.getArgument(0)
            }

            on(it.addAll(any())).then { invo ->
                (invo.getArgument(0) as Iterable<EmbeddingToken>).forEach { tok -> it.add(tok) }
            }
        }

        val document = mock<Document> {
            on(it.text).thenReturn("Hey this is some text.")
            on(it.labeler(EmbeddingToken::class.java)).thenReturn(labeler)
        }

        embeddingTokenDetector.process(document)

        verify(labeler).add(EmbeddingToken(0, 3, "hey"))
        verify(labeler).add(EmbeddingToken(4, 8, "this"))
        verify(labeler).add(EmbeddingToken(9, 11, "is"))
        verify(labeler).add(EmbeddingToken(12, 16, "some"))
        verify(labeler).add(EmbeddingToken(17, 21, "text"))
        verify(labeler).add(EmbeddingToken(21, 22, " "))
    }


}
