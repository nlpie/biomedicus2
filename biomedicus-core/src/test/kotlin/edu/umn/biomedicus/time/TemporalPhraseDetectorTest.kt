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
import edu.umn.nlpengine.StandardArtifact
import edu.umn.nlpengine.addTo
import edu.umn.nlpengine.labelIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class TemporalPhraseDetectorTest {
    private val labelAliases = LabelAliases().apply {
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

    private val searchExprFactory = SearchExprFactory(labelAliases)

    private val pattern = TemporalPhrasePattern(searchExprFactory)

    private val detector = TemporalPhraseDetector(pattern)

    @Test
    fun testSinceTheAgeOf() {
        val document = StandardArtifact("1")
                .addDocument("doc", "since the age of 33")

        Sentence(0, 19).addTo(document)

        ParseToken(0, 5, "since", true).addTo(document)
        ParseToken(6, 9, "the", true).addTo(document)
        ParseToken(10, 13, "age", true).addTo(document)
        ParseToken(14, 16, "of", true).addTo(document)
        ParseToken(17, 19, "33", false).addTo(document)

        PosTag(0, 5, PartOfSpeech.IN).addTo(document)

        Number(17, 19, "33", "1", NumberType.CARDINAL)
                .addTo(document)

        detector.process(document)

        val temporals = document.labelIndex<TemporalPhrase>().asList()
        assertEquals(temporals.size, 1)
        assertEquals(temporals[0].startIndex, 0)
        assertEquals(temporals[0].endIndex, 19)
    }

    @Test
    fun testATimeUnit() {
        val document = StandardArtifact("1").addDocument("doc", "a week")

        document.labeler(Sentence::class.java).add(Sentence(0, 6))

        ParseToken(0, 1, "a", true).addTo(document)
        ParseToken(2, 6, "week", false).addTo(document)

        TimeUnit(2, 6).addTo(document)

        detector.process(document)

        val temporals = document.labelIndex(TemporalPhrase::class.java).asList()
        assertEquals(temporals.size, 1)
        assertEquals(temporals[0].startIndex, 0)
        assertEquals(temporals[0].endIndex, 6)
    }

    @Test
    fun testNumberYearsAgo() {
        val document = StandardArtifact("1").addDocument("doc", "30 years ago")

        Sentence(0, 12).addTo(document)
        ParseToken(0, 2, "30", true).addTo(document)
        ParseToken(3, 8, "years", true).addTo(document)
        ParseToken(9, 12, "ago", true).addTo(document)

        Quantifier(0, 2, true).addTo(document)

        TimeUnit(3, 8).addTo(document)

        detector.process(document)

        val temporals = document.labelIndex<TemporalPhrase>().asList()
        assertEquals(temporals.size, 1)
        assertEquals(temporals[0].startIndex, 0)
        assertEquals(temporals[0].endIndex, 12)
    }
}
