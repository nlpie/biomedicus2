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

import com.google.inject.Inject
import com.google.inject.Singleton
import edu.umn.biomedicus.annotations.Setting
import edu.umn.biomedicus.common.SequenceDetector
import edu.umn.biomedicus.common.TextIdentifiers
import edu.umn.biomedicus.common.TextIdentifiers.getSystemLabeledText
import edu.umn.biomedicus.framework.DocumentProcessor
import edu.umn.biomedicus.framework.SearchExpr
import edu.umn.biomedicus.framework.SearchExprFactory
import edu.umn.biomedicus.sections.SectionTitle
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.biomedicus.tokenization.Token
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.TextRange

data class SocialHistorySectionHeader(
        override val startIndex: Int,
        override val endIndex: Int
) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

val headers = SequenceDetector(
        listOf("risk", "factor"),
        listOf("present", "illness"),
        listOf("cardiovascular", "risk", "analysis"),
        listOf("history"),
        listOf("complaint"),
        listOf("habits"),
        listOf("subjectiv"),
        listOf("allerg"),
        listOf("tobacco")
) { a: String, b: Token -> b.text.startsWith(a, true) }

val headersExact = SequenceDetector(
        listOf("ESRD"),
        listOf("SH"),
        listOf("SHX")
) { a: String, b: Token -> b.text.contentEquals(a) }

class SocialHistorySectionHeaderDetector : DocumentProcessor {
    override fun process(document: Document) {
        val view = getSystemLabeledText(document)

        val sectionHeaders = view.labelIndex(SectionTitle::class.java)
        val tokens = view.labelIndex(ParseToken::class.java)

        val labeler = view.labeler(SocialHistorySectionHeader::class.java)

        for (sectionHeader in sectionHeaders) {
            val headerTokens = tokens.insideSpan(sectionHeader).asList()
            if (headers.matches(headerTokens) != null || headersExact.matches(headerTokens) != null)
                labeler.add(SocialHistorySectionHeader(sectionHeader))
        }
    }
}

data class UsageFrequencyPhrase(
        override val startIndex: Int,
        override val endIndex: Int
) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}


@Singleton
class UsageFrequencyPhrases @Inject constructor(
        @Setting("sh.usageFrequencyPhrasesPath") path: String
) {
    val detector = SequenceDetector.loadFromFile(path) { a, b: Token ->
        b.text.compareTo(a, true) == 0
    }
}

class UsageFrequencyPhraseDetector @Inject constructor(
        usageFrequencyPhrases: UsageFrequencyPhrases
) : DocumentProcessor {
    val detector = usageFrequencyPhrases.detector

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val sentences = text.labelIndex(Sentence::class)
        val tokens = text.labelIndex(ParseToken::class)

        val labeler = text.labeler(UsageFrequencyPhrase::class)

        val nicotineCandidates = text.labelIndex(NicotineCandidate::class)
        val alcoholCandidates = text.labelIndex(AlcoholCandidate::class)
        val drugCandidates = text.labelIndex(DrugCandidate::class)

        sentences
                .filter {
                    nicotineCandidates.containsSpan(it) || alcoholCandidates.containsSpan(it)
                            || drugCandidates.containsSpan(it)
                }
                .map { tokens.insideSpan(it).asList() }
                .forEach { sentenceTokens ->
                    detector.detectAll(sentenceTokens).forEach {
                        labeler.add(UsageFrequencyPhrase(sentenceTokens[it.first].startIndex,
                                sentenceTokens[it.last].endIndex))
                    }
                }
    }
}

data class UsageFrequency(override val startIndex: Int, override val endIndex: Int) : TextRange

@Singleton
data class UsageFrequencyPattern(val searchExpr: SearchExpr) {
    @Inject constructor(searchExprFactory: SearchExprFactory) : this(
            searchExprFactory.parse(
                    """
[?Quantifier] ParseToken<getText="times"|i"x"> ((ParseToken<getText="a"|"per"|"/"> ->)? TimeUnit | TimeFrequencyUnit) |
([?Quantifier] -> TimeUnit)? ParseToken<getText="per"|"/"> -> TimeUnit |
[?ParseToken<getText="per"|"every">] (-> Quantifier)? -> TimeUnit |
[?ParseToken<getText="a">] -> TimeUnit |
[?UsageFrequencyPhrase] |
[?TimeFrequencyUnit]
                        """
            )
    )
}

data class UsageFrequencyDetector(val expr: SearchExpr) : DocumentProcessor {
    @Inject constructor(usageFrequencyPattern: UsageFrequencyPattern)
            : this(usageFrequencyPattern.searchExpr)

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val sentences = text.labelIndex<Sentence>()

        val nicotineCandidates = text.labelIndex<NicotineCandidate>()
        val alcoholCandidates = text.labelIndex<AlcoholCandidate>()
        val drugCandidates = text.labelIndex<DrugCandidate>()

        val searcher = expr.createSearcher(text)
        val labeler = text.labeler<UsageFrequency>()

        sentences
                .filter {
                    nicotineCandidates.containsSpan(it) || alcoholCandidates.containsSpan(it)
                            || drugCandidates.containsSpan(it)
                }
                .forEach {
                    while (searcher.search(it)) {
                        labeler.add(UsageFrequency(searcher.begin, searcher.end))
                    }
                }
    }
}

data class UsageStatus(override val startIndex: Int, override val endIndex: Int) : TextRange

@Singleton
data class UsageStatusPhrases(val detector: SequenceDetector<String, ParseToken>) {
    @Inject constructor(@Setting("sh.statusPhrasesPath") path: String)
            : this(SequenceDetector.loadFromFile(path) { string, token: ParseToken ->
        token.text.compareTo(string, true) == 0
    })
}

data class UsageStatusDetector(
        private val detector: SequenceDetector<String, ParseToken>
) : DocumentProcessor {
    @Inject constructor(phrases: UsageStatusPhrases) : this(phrases.detector)

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val sentences = text.labelIndex<Sentence>()
        val tokens = text.labelIndex<ParseToken>()

        val nicotineCandidates = text.labelIndex<NicotineCandidate>()
        val alcoholCandidates = text.labelIndex<AlcoholCandidate>()
        val drugCandidates = text.labelIndex<DrugCandidate>()

        val labeler = text.labeler<UsageStatus>()

        sentences
                .filter {
                    nicotineCandidates.containsSpan(it) || alcoholCandidates.containsSpan(it)
                            || drugCandidates.containsSpan(it)
                }
                .map { tokens.insideSpan(it).asList() }
                .forEach { sentenceTokens ->
                    detector.detectAll(sentenceTokens).forEach {
                        labeler.add(UsageStatus(sentenceTokens[it.first].startIndex,
                                sentenceTokens[it.last].endIndex))
                    }
                }
    }
}

data class GenericMethodPhrase(override val startIndex: Int, override val endIndex: Int) : TextRange

@Singleton
data class GenericMethodPhrases(val detector: SequenceDetector<String, ParseToken>) {
    @Inject constructor(@Setting("sh.genericMethodPhrasesPath") path: String)
            : this(SequenceDetector.loadFromFile(path) { string, token: ParseToken ->
        token.text.compareTo(string, true) == 0
    })
}

data class GenericMethodPhraseDetector(
        val detector: SequenceDetector<String, ParseToken>
) : DocumentProcessor {
    @Inject constructor(phrases: GenericMethodPhrases) : this(phrases.detector)

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val sentences = text.labelIndex<Sentence>()
        val tokens = text.labelIndex<ParseToken>()

        val nicotineCandidates = text.labelIndex<NicotineCandidate>()
        val alcoholCandidates = text.labelIndex<AlcoholCandidate>()
        val drugCandidates = text.labelIndex<DrugCandidate>()

        val labeler = text.labeler<GenericMethodPhrase>()

        sentences
                .filter {
                    nicotineCandidates.containsSpan(it) || alcoholCandidates.containsSpan(it)
                            || drugCandidates.containsSpan(it)
                }
                .map { tokens.insideSpan(it).asList() }
                .forEach { sentenceTokens ->
                    detector.detectAll(sentenceTokens).forEach {
                        labeler.add(GenericMethodPhrase(sentenceTokens[it.first].startIndex,
                                sentenceTokens[it.last].endIndex))
                    }
                }
    }
}