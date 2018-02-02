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
import edu.umn.biomedicus.exc.BiomedicusException
import edu.umn.biomedicus.family.Relative
import edu.umn.biomedicus.framework.DocumentProcessor
import edu.umn.biomedicus.framework.SearchExprFactory
import edu.umn.biomedicus.sections.Section
import edu.umn.biomedicus.sections.SectionContent
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.time.TemporalPhrase
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.biomedicus.tokenization.Token
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.TextRange

/**
 * A social history candidate for smoking status.
 */
data class NicotineCandidate(
        override val startIndex: Int,
        override val endIndex: Int
) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

data class NicotineCue(
        override val startIndex: Int,
        override val endIndex: Int
) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

@Singleton
class NicotineCues @Inject constructor(
        @Setting("sh.nicotine.candidateCuesPath") path: String
) {
    val detector = SequenceDetector.loadFromFile(path) { a, b: Token -> b.text.startsWith(a, true) }
}

/**
 * Detects nicotine use social history candidate sentences.
 */
class NicotineCandidateDetector @Inject constructor(
        val cues: NicotineCues
) : DocumentProcessor {
    override fun process(document: Document) {
        val systemView = TextIdentifiers.getSystemLabeledText(document)

        val shHeaders = systemView.labelIndex<SocialHistorySectionHeader>()

        val sections = systemView.labelIndex<Section>()
        val sectionContents = systemView.labelIndex<SectionContent>()

        val sentences = systemView.labelIndex<Sentence>()
        val tokens = systemView.labelIndex<ParseToken>()

        val relatives = systemView.labelIndex<Relative>()

        val labeler = systemView.labeler<NicotineCandidate>()
        val cueLabeler = systemView.labeler<NicotineCue>()

        for (header in shHeaders) {
            val section = sections.containing(header).first()
                    ?: throw BiomedicusException("No section for header: $header")
            val contents = sectionContents.insideSpan(section).first()
                    ?: throw BiomedicusException("No contents for section: $section")

            sentences.insideSpan(contents)
                    .filter { systemView.text[it.endIndex - 1] != ':' }
                    .filter { relatives.insideSpan(it).isEmpty() }
                    .forEach { sentence ->
                        val sentenceTokens = tokens.insideSpan(sentence).asList()
                        val matcher = cues.detector.createMatcher()
                        val matches = matcher.detectAll(sentenceTokens)
                        if (matches.isNotEmpty()) {
                            labeler.add(NicotineCandidate(sentence))
                            for (match in matches) {
                                cueLabeler.add(NicotineCue(sentenceTokens[match.first].startIndex,
                                        sentenceTokens[match.last].endIndex))
                            }
                        }
                    }
        }
    }
}

data class NicotineUnit(override val startIndex: Int, override val endIndex: Int) : TextRange

@Singleton
class NicotineAmountUnits @Inject constructor(
        @Setting("sh.nicotine.amountUnitsPath") path: String
) {
    val detector = SequenceDetector.loadFromFile(path) { a, b: Token ->
        b.text.startsWith(a, true)
    }
}

class NicotineUnitDetector @Inject constructor(
        amountUnits: NicotineAmountUnits
) : DocumentProcessor {

    val detector = amountUnits.detector

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val sentences = text.labelIndex<Sentence>()
        val tokens = text.labelIndex<ParseToken>()

        val candidates = text.labelIndex<NicotineCandidate>()

        val labeler = text.labeler<NicotineUnit>()

        candidates
                .map { sentences.insideSpan(it) }
                .forEach {
                    it.map { tokens.insideSpan(it).asList() }
                            .forEach { sentenceTokens ->
                                detector.detectAll(sentenceTokens).forEach {
                                    labeler.add(
                                            NicotineUnit(
                                                    sentenceTokens[it.first].startIndex,
                                                    sentenceTokens[it.last].endIndex
                                            ))
                                }
                            }
                }
    }
}

data class NicotineAmount(override val startIndex: Int, override val endIndex: Int) : TextRange

@Singleton
class NicotineAmountPattern @Inject constructor(
        searchExprFactory: SearchExprFactory
) {
    val expr = searchExprFactory.parse("(([?Quantifier] | [?ParseToken<getText=i\"half\">]) ParseToken<getText=\"of\">? ParseToken<getText=\"a\">? -> NicotineUnit | StandaloneQuantifier)")
}

class NicotineAmountDetector @Inject constructor(
        nicotineAmountPattern: NicotineAmountPattern
) : DocumentProcessor {
    private val expr = nicotineAmountPattern.expr

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val searcher = expr.createSearcher(text)

        val candidates = text.labelIndex<NicotineCandidate>()

        val labeler = text.labeler<NicotineAmount>()

        for (candidate in candidates) {
            while (searcher.search(candidate)) {
                labeler.add(NicotineAmount(searcher.begin, searcher.end))
            }
        }
    }
}

data class NicotineFrequency(override val startIndex: Int, override val endIndex: Int) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

class NicotineFrequencyDetector : DocumentProcessor {
    override fun process(document: Document) {
        val systemView = TextIdentifiers.getSystemLabeledText(document)

        val nicotineCandidates = systemView.labelIndex<NicotineCandidate>()

        val amounts = systemView.labelIndex<NicotineAmount>()

        val usageFrequencies = systemView.labelIndex<UsageFrequency>()
        val labeler = systemView.labeler<NicotineFrequency>()

        for (nicotineCandidate in nicotineCandidates) {
            usageFrequencies
                    .insideSpan(nicotineCandidate)
                    .filter { amounts.containing(it).isEmpty() }
                    .forEach { labeler.add(NicotineFrequency(it)) }
        }
    }
}

data class NicotineTemporal(override val startIndex: Int, override val endIndex: Int) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

class NicotineTemporalDetector : DocumentProcessor {
    override fun process(document: Document) {
        val systemView = TextIdentifiers.getSystemLabeledText(document)

        val nicotineCandidates = systemView.labelIndex<NicotineCandidate>()

        val frequencies = systemView.labelIndex<NicotineFrequency>()
        val amounts = systemView.labelIndex<NicotineAmount>()

        val temporalPhrases = systemView.labelIndex<TemporalPhrase>()
        val temporalLabeler = systemView.labeler<NicotineTemporal>()

        for (nicotineCandidate in nicotineCandidates) {
            temporalPhrases.insideSpan(nicotineCandidate)
                    .filter { amounts.containing(it).isEmpty() }
                    .filter { frequencies.containing(it).isEmpty() }
                    .forEach { temporalLabeler.add(NicotineTemporal(it)) }
        }
    }
}

data class NicotineType(override val startIndex: Int, override val endIndex: Int) : TextRange

@Singleton
data class NicotineTypes(val sequenceDetector: SequenceDetector<String, ParseToken>) {
    @Inject constructor(@Setting("sh.nicotine.typesPath") path: String)
            : this(SequenceDetector.loadFromFile(path) { t, u: ParseToken ->
        u.text.compareTo(t, true) == 0
    })
}

data class NicotineTypeDetector(
        private val sequenceDetector: SequenceDetector<String, ParseToken>
) : DocumentProcessor {
    @Inject constructor(nicotineTypes: NicotineTypes) : this(nicotineTypes.sequenceDetector)

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val candidates = text.labelIndex<NicotineCandidate>()
        val tokens = text.labelIndex<ParseToken>()

        val labeler = text.labeler<NicotineType>()

        candidates
                .map { tokens.insideSpan(it).asList() }
                .forEach { candidateTokens ->
                    sequenceDetector.detectAll(candidateTokens)
                            .forEach {
                                labeler.add(NicotineType(candidateTokens[it.first].startIndex,
                                        candidateTokens[it.last].endIndex))
                            }
                }
    }
}

data class NicotineStatus(override val startIndex: Int, override val endIndex: Int) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

@Singleton
data class NicotineStatusPhrases(val detector: SequenceDetector<String, ParseToken>) {
    @Inject constructor(@Setting("sh.nicotine.statusPhrasesPath") path: String)
            : this(SequenceDetector.loadFromFile(path) { string, token: ParseToken ->
        token.text.compareTo(string, true) == 0
    })
}

data class NicotineStatusDetector(
        private val detector: SequenceDetector<String, ParseToken>
) : DocumentProcessor {
    @Inject constructor(statusPhrases: NicotineStatusPhrases) : this(statusPhrases.detector)

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val tokens = text.labelIndex<ParseToken>()

        val candidates = text.labelIndex<NicotineCandidate>()

        val usageStatuses = text.labelIndex<UsageStatus>()

        val labeler = text.labeler<NicotineStatus>()

        candidates
                .onEach {
                    usageStatuses.insideSpan(it).forEach {
                        labeler.add(NicotineStatus(it))
                    }
                }
                .map { tokens.insideSpan(it).asList() }
                .forEach { sentenceTokens ->
                    detector.detectAll(sentenceTokens).forEach {
                        labeler.add(NicotineStatus(sentenceTokens[it.first].startIndex,
                                sentenceTokens[it.last].endIndex))
                    }
                }
    }
}

data class NicotineMethod(override val startIndex: Int, override val endIndex: Int) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

@Singleton
data class NicotineMethodPhrases(val detector: SequenceDetector<String, ParseToken>) {
    @Inject constructor(@Setting("sh.nicotine.methodPhrasesPath") path: String)
            : this(SequenceDetector.loadFromFile(path) { string, token: ParseToken ->
        token.text.compareTo(string, true) == 0
    })
}

data class NicotineMethodDetector(
        private val detector: SequenceDetector<String, ParseToken>
) : DocumentProcessor {
    @Inject constructor(phrases: NicotineMethodPhrases) : this(phrases.detector)

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val candidates = text.labelIndex<NicotineCandidate>()
        val tokens = text.labelIndex<ParseToken>()

        val genericMethods = text.labelIndex<GenericMethodPhrase>()

        val labeler = text.labeler<NicotineMethod>()

        candidates
                .onEach {
                    genericMethods
                            .insideSpan(it)
                            .forEach {
                                labeler.add(NicotineMethod(it))
                            }
                }
                .map { tokens.insideSpan(it).asList() }
                .forEach { sentenceTokens ->
                    detector.detectAll(sentenceTokens)
                            .map {
                                NicotineMethod(sentenceTokens[it.first].startIndex,
                                        sentenceTokens[it.last].endIndex)
                            }
                            .forEach {
                                labeler.add(it)
                            }
                }
    }
}