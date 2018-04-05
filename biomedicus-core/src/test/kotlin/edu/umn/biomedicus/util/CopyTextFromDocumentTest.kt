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

package edu.umn.biomedicus.util
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import edu.umn.nlpengine.Artifact
import edu.umn.nlpengine.Document
import kotlin.test.Test


class CopyTextFromDocumentTest {
    @Test
    fun `copies text`() {
        val document = mock<Document>()
        whenever(document.text) doReturn "This is some text"

        val artifact = mock<Artifact>()
        whenever(artifact.documents) doReturn mapOf(Pair("source", document))

        CopyTextFromDocument("source", "target")
                .process(artifact)

        verify(artifact).addDocument("target", "This is some text")
    }
}