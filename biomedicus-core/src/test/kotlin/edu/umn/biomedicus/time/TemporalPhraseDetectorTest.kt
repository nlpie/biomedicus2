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
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech
import edu.umn.biomedicus.framework.LabelAliases
import edu.umn.biomedicus.framework.SearchExprFactory
import edu.umn.biomedicus.measures.Number
import edu.umn.biomedicus.measures.Quantifier
import edu.umn.biomedicus.measures.TimeUnit
import edu.umn.biomedicus.numbers.NumberType
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.tagging.PosTag
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.StandardDocument
import edu.umn.nlpengine.addTo
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

class TemporalPhraseDetectorTest {
    val labelAliases = LabelAliases().apply {
        addAlias("Sentence", Sentence::class.java)
        addAlias("ParseToken", ParseToken::class.java)
        addAlias("PosTag", PosTag::class.java)
        addAlias("Quantifier", Quantifier::class.java)
        addAlias("TimeUnit", TimeUnit::class.java)
        addAlias("DayOfWeek", DayOfWeek::class.java)
        addAlias("TextDate", TextDate::class.java)
        addAlias("TextTime", TextTime::class.java)
        addAlias("TimeOfDayWord", TimeOfDayWord::class.java)
        addAlias("SeasonWord", SeasonWord::class.java)
        addAlias("YearRange", YearRange::class.java)
        addAlias("Number", Number::class.java)
    }

    val searchExprFactory = SearchExprFactory(labelAliases)

    val pattern = TemporalPhrasePattern(searchExprFactory)

    val detector = TemporalPhraseDetector(pattern)

    @Test
    fun testSinceTheAgeOf() {
        val doc = StandardDocument("Doc")

        val text = doc.attachText(TextIdentifiers.SYSTEM, "since the age of 33")

        text.labeler(Sentence::class).add(Sentence(0, 19))

        val tokenLabeler = text.labeler(ParseToken::class)
        tokenLabeler.add(ParseToken(0, 5, "since", true))
        tokenLabeler.add(ParseToken(6, 9, "the", true))
        tokenLabeler.add(ParseToken(10, 13, "age", true))
        tokenLabeler.add(ParseToken(14, 16, "of", true))
        tokenLabeler.add(ParseToken(17, 19, "33", false))

        text.labeler(PosTag::class).add(PosTag(0, 5, PartOfSpeech.IN))

        text.labeler(Number::class).add(Number(17, 19, "33", "1", NumberType.CARDINAL))

        detector.process(doc)

        val temporals = text.labelIndex(TemporalPhrase::class).asList()
        assertEquals(temporals.size, 1)
        assertEquals(temporals[0].startIndex, 0)
        assertEquals(temporals[0].endIndex, 19)
    }

    @Test
    fun testATimeUnit() {
        val doc = StandardDocument("Doc")

        val text = doc.attachText(TextIdentifiers.SYSTEM, "a week")

        text.labeler(Sentence::class.java).add(Sentence(0, 6))

        val tokenLabeler = text.labeler(ParseToken::class.java)
        tokenLabeler.add(ParseToken(0, 1, "a", true))
        tokenLabeler.add(ParseToken(2, 6, "week", false))

        text.labeler(TimeUnit::class.java).add(TimeUnit(2, 6))

        detector.process(doc)

        val temporals = text.labelIndex(TemporalPhrase::class.java).asList()
        assertEquals(temporals.size, 1)
        assertEquals(temporals[0].startIndex, 0)
        assertEquals(temporals[0].endIndex, 6)
    }

    @Test
    fun testNumberYearsAgo() {
        val doc = StandardDocument("doc")

        val text = doc.attachText(TextIdentifiers.SYSTEM, "30 years ago")

        Sentence(0, 12).addTo(text)
        ParseToken(0, 2, "30", true).addTo(text)
        ParseToken(3, 8, "years", true).addTo(text)
        ParseToken(9, 12, "ago", true).addTo(text)

        Quantifier(0, 2, true).addTo(text)

        TimeUnit(3, 8).addTo(text)

        detector.process(doc)

        val temporals = text.labelIndex<TemporalPhrase>().asList()
        assertEquals(temporals.size, 1)
        assertEquals(temporals[0].startIndex, 0)
        assertEquals(temporals[0].endIndex, 12)
    }
}