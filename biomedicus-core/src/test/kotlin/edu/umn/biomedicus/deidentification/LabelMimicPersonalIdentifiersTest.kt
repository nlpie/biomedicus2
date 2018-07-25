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

package edu.umn.biomedicus.deidentification

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.Labeler
import kotlin.test.Test
import kotlin.test.assertEquals

class LabelMimicPersonalIdentifiersTest {


    @Test
    internal fun `labels de-identifiers`() {
        val identifiers = mock<MimicIdentifiers> {
            on(it.findPersonalIdentifiers("meh")).doReturn(
                sequenceOf(
                    PersonalIdentifier(0, 3), PersonalIdentifier(4, 8), PersonalIdentifier(20, 28)
                )
            )
        }

        val labeler = mock<Labeler<PersonalIdentifier>>()

        val doc = mock<Document> {
            on(it.text).doReturn("meh")
            on(it.labeler(PersonalIdentifier::class.java)).doReturn(labeler)
        }

        val tested = LabelMimicPersonalIdentifiers(identifiers)
        tested.run(doc)

        verify(labeler).add(PersonalIdentifier(0, 3))
        verify(labeler).add(PersonalIdentifier(4, 8))
        verify(labeler).add(PersonalIdentifier(20, 28))
    }

    @Test
    internal fun `finds de-identifiers`() {

        val tested = RegexMimicIdentifiers()

        val list = tested
            .findPersonalIdentifiers("Attending: [** First Name 4 **]")
            .toList()

        assertEquals(
            listOf(PersonalIdentifier(11, 31)),
            list
        )
    }
}
