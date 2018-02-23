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
import edu.umn.biomedicus.dependencies
import edu.umn.biomedicus.exc.BiomedicusException
import edu.umn.biomedicus.family.Relative
import edu.umn.biomedicus.framework.SearchExprFactory
import edu.umn.biomedicus.parsing.UDRelation
import edu.umn.biomedicus.parsing.findHead
import edu.umn.biomedicus.sections.Section
import edu.umn.biomedicus.sections.SectionContent
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.time.TemporalPhrase
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.biomedicus.tokenization.Token
import edu.umn.nlpengine.*
import java.util.*

/**
 * A social history candidate for smoking status.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class NicotineCandidate(
        override val startIndex: Int,
        override val endIndex: Int
) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * A word that indicates a sentence is a nicotine social history candidate.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class NicotineCue(
        override val startIndex: Int,
        override val endIndex: Int
) : Label()

/**
 * The verb that is a head for nicotine social history information.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class NicotineVerb(
        override val startIndex: Int,
        override val endIndex: Int
) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The unit of a nicotine usage measurement, used in [NicotineAmount] detection.
 * E.g. cigarettes, packs, tins.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class NicotineUnit(override val startIndex: Int, override val endIndex: Int) : Label()

/**
 * The quantity and unit of a nicotine usage measurement. E.g. 1 - 5 packs per day
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class NicotineAmount(override val startIndex: Int, override val endIndex: Int) : Label()

/**
 * How often nicotine is used. E.g. daily, infrequently
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class NicotineFrequency(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The time period nicotine usage occurs/occurred in or over. Includes phrases like
 * "for thirty years" or "nightly" or "weekend nights"
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class NicotineTemporal(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The type of nicotine, cigarettes, chewing tobacco, etc.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class NicotineType(override val startIndex: Int, override val endIndex: Int) : Label()

/**
 * A word that indicates whether usage is ongoing or has ceased.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class NicotineStatus(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The method how nicotine usage occurred. E.g. smoked, chewed, etc.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class NicotineMethod(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The model for nicotine cue words.
 */
@Singleton
class NicotineCues @Inject constructor(
        @Setting("sh.nicotine.candidateCuesPath") path: String
) {
    val detector = SequenceDetector.loadFromFile(path) { a, b: Token -> b.text.startsWith(a, true) }
}

/**
 * Detects nicotine use social history candidate sentences.
 *
 * @property cues the nicotine cues model
 */
class NicotineCandidateDetector @Inject constructor(
        val cues: NicotineCues
) : DocumentProcessor {
    override fun process(document: Document) {
        val shHeaders = document.labelIndex<SocialHistorySectionHeader>()

        val sections = document.labelIndex<Section>()
        val sectionContents = document.labelIndex<SectionContent>()

        val sentences = document.labelIndex<Sentence>()
        val tokens = document.labelIndex<ParseToken>()

        val relatives = document.labelIndex<Relative>()

        val labeler = document.labeler<NicotineCandidate>()
        val cueLabeler = document.labeler<NicotineCue>()

        for (header in shHeaders) {
            val section = sections.containing(header).first()
                    ?: continue
            val contents = sectionContents.insideSpan(section).first()
                    ?: throw BiomedicusException("No contents for section: $section")

            sentences.insideSpan(contents)
                    .filter { document.text[it.endIndex - 1] != ':' }
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

/**
 * Detects [NicotineVerb] labels from [NicotineCue] labels in text.
 */
class NicotineVerbLabeler : DocumentProcessor {
    override fun process(document: Document) {
        val cues = document.labelIndex<NicotineCue>()
        val dependencies = document.dependencies()

        val nicotineVerbs = HashSet<NicotineVerb>()
        for (cue in cues) {
            val dependency = findHead(dependencies.insideSpan(cue))

            dependency.selfAndParentIterator().asSequence().first {
                it.dep.partOfSpeech.isVerb || it.relation == UDRelation.ROOT
            }.let { nicotineVerbs.add(NicotineVerb(it)) }
        }
        document.labelAll(nicotineVerbs)
    }
}

/**
 * Detects if a phrase is a nicotine dependant phrase by seeing if it is, or has a [NicotineVerb]
 * ancestor
 */
internal fun Document.isNicotineDep(textRange: TextRange): Boolean {
    val insideSpan = dependencies().insideSpan(textRange)
    val verbs = labelIndex<NicotineVerb>()
    if (insideSpan.any { verbs.containsSpan(it) }) return true
    val phraseRoot = findHead(insideSpan)
    return phraseRoot.selfAndParentIterator().asSequence().any {
        verbs.containsSpan(it)
    }
}

/**
 * The model for nicotine amount units.
 */
@Singleton
class NicotineAmountUnits @Inject constructor(
        @Setting("sh.nicotine.amountUnitsPath") path: String
) {
    val detector = SequenceDetector.loadFromFile(path) { a, b: Token ->
        b.text.startsWith(a, true)
    }
}

/**
 * Detects and labels [NicotineUnit] instances.
 */
class NicotineUnitDetector @Inject constructor(
        amountUnits: NicotineAmountUnits
) : DocumentProcessor {

    private val detector = amountUnits.detector

    override fun process(document: Document) {
        val sentences = document.labelIndex<Sentence>()
        val tokens = document.labelIndex<ParseToken>()

        val candidates = document.labelIndex<NicotineCandidate>()

        val labeler = document.labeler<NicotineUnit>()

        candidates
                .map { sentences.insideSpan(it) }
                .forEach {
                    it.map { tokens.insideSpan(it).asList() }
                            .forEach { sentenceTokens ->
                                detector.detectAll(sentenceTokens).forEach {
                                    val unit = NicotineUnit(
                                            sentenceTokens[it.first].startIndex,
                                            sentenceTokens[it.last].endIndex
                                    )
                                    if (document.isNicotineDep(unit)) labeler.add(unit)
                                }
                            }
                }
    }
}

/**
 * The TagEx pattern for nicotine amounts.
 */
@Singleton
class NicotineAmountPattern @Inject constructor(
        searchExprFactory: SearchExprFactory
) {
    val expr = searchExprFactory.parse("(([?Quantifier] | [?ParseToken<getText=i\"half\">]) ParseToken<getText=\"of\">? ParseToken<getText=\"a\">? -> NicotineUnit | [?StandaloneQuantifier])")
}

/**
 * Detects and labels instances of [NicotineAmount] in text using the nicotine amount tagex pattern.
 */
class NicotineAmountDetector @Inject constructor(
        nicotineAmountPattern: NicotineAmountPattern
) : DocumentProcessor {
    private val expr = nicotineAmountPattern.expr

    override fun process(document: Document) {
        val searcher = expr.createSearcher(document)

        val candidates = document.labelIndex<NicotineCandidate>()

        val labeler = document.labeler<NicotineAmount>()

        for (candidate in candidates) {
            while (searcher.search(candidate)) {
                val amount = NicotineAmount(searcher.begin, searcher.end)
                if (document.isNicotineDep(amount)) labeler.add(amount)
            }
        }
    }
}

/**
 * Detects and labels [NicotineFrequency] instances in text using the general [UsageFrequency] label.
 */
class NicotineFrequencyDetector : DocumentProcessor {
    override fun process(document: Document) {
        val nicotineCandidates = document.labelIndex<NicotineCandidate>()

        val amounts = document.labelIndex<NicotineAmount>()

        val usageFrequencies = document.labelIndex<UsageFrequency>()
        val labeler = document.labeler<NicotineFrequency>()

        for (nicotineCandidate in nicotineCandidates) {
            usageFrequencies
                    .insideSpan(nicotineCandidate)
                    .asSequence()
                    .filter { amounts.containing(it).isEmpty() }
                    .filter { document.isNicotineDep(it) }
                    .map { NicotineFrequency(it) }
                    .forEach { labeler.add(it) }
        }
    }
}

/**
 * Detects and labels [NicotineTemporal] instances in text using the general [TemporalPhrase].
 */
class NicotineTemporalDetector : DocumentProcessor {
    override fun process(document: Document) {
        val nicotineCandidates = document.labelIndex<NicotineCandidate>()

        val frequencies = document.labelIndex<NicotineFrequency>()
        val amounts = document.labelIndex<NicotineAmount>()

        val temporalPhrases = document.labelIndex<TemporalPhrase>()
        val temporalLabeler = document.labeler<NicotineTemporal>()

        for (nicotineCandidate in nicotineCandidates) {
            temporalPhrases.insideSpan(nicotineCandidate)
                    .asSequence()
                    .filter { amounts.containing(it).isEmpty() }
                    .filter { frequencies.containing(it).isEmpty() }
                    .filter { document.isNicotineDep(it) }
                    .forEach { temporalLabeler.add(NicotineTemporal(it)) }
        }
    }
}

/**
 * The model for nicotine types.
 */
@Singleton
class NicotineTypes(val sequenceDetector: SequenceDetector<String, ParseToken>) {
    @Inject internal constructor(@Setting("sh.nicotine.typesPath") path: String)
            : this(SequenceDetector.loadFromFile(path) { t, u: ParseToken ->
        u.text.compareTo(t, true) == 0
    })
}

/**
 * Detects and labels [NicotineType] instances in text using the nicotine types model.
 */
class NicotineTypeDetector(
        private val sequenceDetector: SequenceDetector<String, ParseToken>
) : DocumentProcessor {
    @Inject internal constructor(nicotineTypes: NicotineTypes) : this(nicotineTypes.sequenceDetector)

    override fun process(document: Document) {
        val candidates = document.labelIndex<NicotineCandidate>()
        val tokens = document.labelIndex<ParseToken>()

        val labeler = document.labeler<NicotineType>()

        candidates.asSequence()
                .map { tokens.insideSpan(it).asList() }
                .forEach { candidateTokens ->
                    sequenceDetector.detectAll(candidateTokens)
                            .forEach {
                                val type = NicotineType(
                                        candidateTokens[it.first].startIndex,
                                        candidateTokens[it.last].endIndex
                                )
                                if (document.isNicotineDep(type)) labeler.add(type)
                            }
                }
    }
}

/**
 * Model for nicotine status phrases.
 */
@Singleton
class NicotineStatusPhrases(val detector: SequenceDetector<String, ParseToken>) {
    @Inject constructor(@Setting("sh.nicotine.statusPhrasesPath") path: String)
            : this(SequenceDetector.loadFromFile(path) { string, token: ParseToken ->
        token.text.compareTo(string, true) == 0
    })
}

/**
 * Detects nicotine status phrases in text using the nicotine status model and the general
 * [UsageStatusPhrases]
 */
class NicotineStatusDetector(
        private val detector: SequenceDetector<String, ParseToken>
) : DocumentProcessor {
    @Inject constructor(statusPhrases: NicotineStatusPhrases) : this(statusPhrases.detector)

    override fun process(document: Document) {
        val tokens = document.labelIndex<ParseToken>()

        val candidates = document.labelIndex<NicotineCandidate>()

        val usageStatuses = document.labelIndex<UsageStatus>()

        val labeler = document.labeler<NicotineStatus>()

        candidates.asSequence()
                .onEach {
                    usageStatuses.insideSpan(it).forEach {
                        labeler.add(NicotineStatus(it))
                    }
                }
                .map { tokens.insideSpan(it).asList() }
                .forEach { sentenceTokens ->
                    detector.detectAll(sentenceTokens).forEach {
                        val status = NicotineStatus(sentenceTokens[it.first].startIndex,
                                sentenceTokens[it.last].endIndex)
                        if (document.isNicotineDep(status)) labeler.add(status)
                    }
                }
    }
}

/**
 * Nicotine methods model.
 */
@Singleton
data class NicotineMethodPhrases(val detector: SequenceDetector<String, ParseToken>) {
    @Inject constructor(@Setting("sh.nicotine.methodPhrasesPath") path: String)
            : this(SequenceDetector.loadFromFile(path) { string, token: ParseToken ->
        token.text.compareTo(string, true) == 0
    })
}

/**
 * Detects and labels instances of [NicotineMethod] in text using the [GenericMethodPhrase]
 * instances and the nicotine methods model.
 */
data class NicotineMethodDetector(
        private val detector: SequenceDetector<String, ParseToken>
) : DocumentProcessor {
    @Inject constructor(phrases: NicotineMethodPhrases) : this(phrases.detector)

    override fun process(document: Document) {
        val candidates = document.labelIndex<NicotineCandidate>()
        val tokens = document.labelIndex<ParseToken>()

        val genericMethods = document.labelIndex<GenericMethodPhrase>()

        val labeler = document.labeler<NicotineMethod>()

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
                            .filter { document.isNicotineDep(it) }
                            .forEach {
                                labeler.add(it)
                            }
                }
    }
}
