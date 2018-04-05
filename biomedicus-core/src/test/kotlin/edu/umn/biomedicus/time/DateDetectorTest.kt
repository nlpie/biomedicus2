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

import edu.umn.biomedicus.framework.LabelAliases
import edu.umn.biomedicus.framework.SearchExprFactory
import edu.umn.biomedicus.measures.Number
import edu.umn.biomedicus.numbers.NumberType
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.StandardArtifact
import edu.umn.nlpengine.addTo
import kotlin.test.*

class DateDetectorTest {
    private val labelAliases = LabelAliases().apply {
        addAlias("Sentence", Sentence::class.java)
        addAlias("ParseToken", ParseToken::class.java)
        addAlias("Number", Number::class.java)
        addAlias("DayOfWeek", DayOfWeek::class.java)
        addAlias("Month", Month::class.java)
        addAlias("YearNumber", YearNumber::class.java)
    }

    private val searchExprFactory = SearchExprFactory(labelAliases)

    private val datePattern = DatePattern(searchExprFactory)

    private val detector = DateDetector(datePattern)

    @Test
    fun testMonthDay() {
        val document = StandardArtifact("1").addDocument("doc", "May 5")

        Sentence(0, 5).addTo(document)
        ParseToken(0, 3, "May", true).addTo(document)
        ParseToken(4, 5, "5", false).addTo(document)

        Month(0, 3).addTo(document)

        Number(4, 5, "5", "1", NumberType.CARDINAL)
                .addTo(document)

        detector.process(document)

        val list = document.labelIndex<TextDate>().asList()
        assertEquals(list.size, 1)
        assertEquals(list[0].startIndex, 0)
        assertEquals(list[0].endIndex, 5)
    }

    @Test
    fun testWeekdayMonthDay() {
        val document = StandardArtifact("1").addDocument("doc", "Friday May 5")

        Sentence(0, 12).addTo(document)
        ParseToken(0, 6, "Friday", true).addTo(document)
        ParseToken(7, 10, "May", true).addTo(document)
        ParseToken(11, 12, "5", false).addTo(document)

        DayOfWeek(0, 6).addTo(document)
        Month(7, 10).addTo(document)
        Number(11, 12, "5", "1", NumberType.CARDINAL)
                .addTo(document)

        detector.process(document)

        val list = document.labelIndex<TextDate>().asList()
        assertEquals(list.size, 1)
        assertEquals(list[0].startIndex, 0)
        assertEquals(list[0].endIndex, 12)
    }

    @Test
    fun testWeekdayMonthDayYear() {

    }

    @Test
    fun testShort() {
        val document = StandardArtifact("1").addDocument("doc", "5/5")

        Sentence(0, 3).addTo(document)

        ParseToken(0, 1, "5", false).addTo(document)
        ParseToken(1, 2, "/", false).addTo(document)
        ParseToken(2, 3, "5", false).addTo(document)

        detector.process(document)

        val list = document.labelIndex<TextDate>().asList()
        assertEquals(list.size, 1)
        assertEquals(list[0].startIndex, 0)
        assertEquals(list[0].endIndex, 3)
    }
}