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

package edu.umn.biomedicus.sh

import edu.umn.biomedicus.framework.LabelAliases
import edu.umn.biomedicus.framework.SearchExprFactory
import edu.umn.biomedicus.measures.Quantifier
import edu.umn.biomedicus.measures.TimeFrequencyUnit
import edu.umn.biomedicus.measures.TimeUnit
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.StandardArtifact
import edu.umn.nlpengine.addTo
import kotlin.test.*

class UsageFrequencyDetectorTest {
    private val labelAliases = LabelAliases()
            .apply {
                addAlias("Quantifier", Quantifier::class.java)
                addAlias("ParseToken", ParseToken::class.java)
                addAlias("TimeUnit", TimeUnit::class.java)
                addAlias("TimeFrequencyUnit", TimeFrequencyUnit::class.java)
                addAlias("UsageFrequencyPhrase", UsageFrequencyPhrase::class.java)
            }

    private val searchExprFactory = SearchExprFactory(labelAliases)

    private val usageFrequencyPattern = UsageFrequencyPattern(searchExprFactory)

    private val usageFrequencyDetector = UsageFrequencyDetector(usageFrequencyPattern)

    @Test
    fun testATimeUnit() {
        val document = StandardArtifact("a").addDocument("doc", "a day")

        Sentence(0, 5).addTo(document)
        NicotineCandidate(0, 5).addTo(document)

        ParseToken(0, 1, "a", true).addTo(document)
        ParseToken(2, 5, "day", true).addTo(document)

        TimeUnit(2, 5).addTo(document)

        usageFrequencyDetector.process(document)

        val freqs = document.labelIndex<UsageFrequency>().asList()
        assertEquals(freqs.size, 1)
        assertEquals(freqs[0].startIndex, 0)
        assertEquals(freqs[0].endIndex, 5)
    }
}