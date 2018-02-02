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

package edu.umn.biomedicus.time

import edu.umn.biomedicus.common.TextIdentifiers
import edu.umn.biomedicus.framework.LabelAliases
import edu.umn.biomedicus.framework.SearchExprFactory
import edu.umn.biomedicus.measures.Number
import edu.umn.biomedicus.numbers.NumberType
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.StandardDocument
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class DateDetectorTest {
    val labelAliases = LabelAliases().apply {
        addAlias("Sentence", Sentence::class.java)
        addAlias("ParseToken", ParseToken::class.java)
        addAlias("Number", Number::class.java)
        addAlias("DayOfWeek", DayOfWeek::class.java)
        addAlias("Month", Month::class.java)
        addAlias("YearNumber", YearNumber::class.java)
    }

    val searchExprFactory = SearchExprFactory(labelAliases)

    val datePattern = DatePattern(searchExprFactory)

    val detector = DateDetector(datePattern)

    @Test
    fun testMonthDay() {
        val document = StandardDocument("doc")

        val text = document.attachText(TextIdentifiers.SYSTEM, "May 5")

        text.labeler(Sentence::class).add(Sentence(0, 5))

        val tokenLabeler = text.labeler(ParseToken::class)
        tokenLabeler.add(ParseToken(0, 3, "May", true))
        tokenLabeler.add(ParseToken(4, 5, "5", false))


        text.labeler(Month::class).add(Month(0, 3))
        text.labeler(Number::class).add(Number(4, 5, "5",
                "1", NumberType.CARDINAL))

        detector.process(document)

        val list = text.labelIndex(TextDate::class).asList()
        assertEquals(list.size, 1)
        assertEquals(list[0].startIndex, 0)
        assertEquals(list[0].endIndex, 5)
    }

    @Test
    fun testWeekdayMonthDay() {
        val document = StandardDocument("doc")

        val text = document.attachText(TextIdentifiers.SYSTEM, "Friday May 5")

        text.labeler(Sentence::class).add(Sentence(0, 12))

        val tokenLabeler = text.labeler(ParseToken::class)
        tokenLabeler.add(ParseToken(0, 6, "Friday", true))
        tokenLabeler.add(ParseToken(7, 10, "May", true))
        tokenLabeler.add(ParseToken(11, 12, "5", false))

        text.labeler(DayOfWeek::class).add(DayOfWeek(0, 6))
        text.labeler(Month::class).add(Month(7, 10))
        text.labeler(Number::class).add(Number(11, 12, "5",
                "1", NumberType.CARDINAL))

        detector.process(document)

        val list = text.labelIndex(TextDate::class).asList()
        assertEquals(list.size, 1)
        assertEquals(list[0].startIndex, 0)
        assertEquals(list[0].endIndex, 12)
    }

    @Test
    fun testWeekdayMonthDayYear() {

    }

    @Test
    fun testShort() {
        val document = StandardDocument("doc")

        val text = document.attachText(TextIdentifiers.SYSTEM, "5/5")

        text.labeler(Sentence::class).add(Sentence(0, 3))

        val tokenLabeler = text.labeler(ParseToken::class)
        tokenLabeler.add(ParseToken(0, 1, "5", false))
        tokenLabeler.add(ParseToken(1, 2, "/", false))
        tokenLabeler.add(ParseToken(2, 3, "5", false))

        detector.process(document)

        val list = text.labelIndex(TextDate::class).asList()
        assertEquals(list.size, 1)
        assertEquals(list[0].startIndex, 0)
        assertEquals(list[0].endIndex, 3)
    }
}