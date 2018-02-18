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

package edu.umn.biomedicus.measures

import edu.umn.biomedicus.common.DocumentIdentifiers
import edu.umn.biomedicus.framework.LabelAliases
import edu.umn.biomedicus.framework.SearchExprFactory
import edu.umn.biomedicus.numbers.NumberType
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.StandardArtifact
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class NumberRangesLabelerTest {
    val labelAliases = LabelAliases().apply {
        addAlias("Number", Number::class.java)
        addAlias("ParseToken", ParseToken::class.java)
    }

    val searchExprFactory = SearchExprFactory(labelAliases)

    val pattern = NumberRangesPattern(searchExprFactory)

    val numberRanges = NumberRangesLabeler(pattern)

    @Test
    fun testBetween() {
        val artifact = StandardArtifact("doc")

        val document = artifact.addDocument(DocumentIdentifiers.DEFAULT, "between 30 and 40")

        val tokenLabeler = document.labeler<ParseToken>()
        tokenLabeler.add(ParseToken(0, 7, "between", true))
        tokenLabeler.add(ParseToken(8, 10, "30", true))
        tokenLabeler.add(ParseToken(11, 14, "and", true))
        tokenLabeler.add(ParseToken(15, 17, "40", false))

        val numberLabeler = document.labeler<Number>()
        numberLabeler.add(Number(8, 10, "30", "1", NumberType.CARDINAL))
        numberLabeler.add(Number(15, 17, "40", "1", NumberType.CARDINAL))

        numberRanges.process(document)

        val numberRanges = document.labelIndex<NumberRange>().asList()

        assertEquals(numberRanges.size, 1)
        assertEquals(numberRanges[0].startIndex, 0)
        assertEquals(numberRanges[0].endIndex, 17)
    }
}