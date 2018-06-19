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

import edu.umn.biomedicus.annotations.Setting
import edu.umn.biomedicus.common.SequenceDetector
import edu.umn.biomedicus.dependencies
import edu.umn.biomedicus.framework.TagEx
import edu.umn.biomedicus.framework.TagExFactory
import edu.umn.biomedicus.parsing.findHead
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.time.TemporalPhrase
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.biomedicus.tokenization.Token
import edu.umn.nlpengine.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The verb that is a head for nicotine social history information.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class NicotineRelevant(
        override val startIndex: Int,
        override val endIndex: Int
) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The unit of a nicotine usage measurement, used in [NicotineAmount] detection.
 * E.g. cigarettes, packs, tins.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class NicotineUnit(override val startIndex: Int, override val endIndex: Int) : Label()

/**
 * The quantity and unit of a nicotine usage measurement. E.g. 1 - 5 packs
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class NicotineAmount(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * How often nicotine is used. E.g. daily, infrequently
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class NicotineFrequency(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The time period nicotine usage occurs/occurred in or over. Includes phrases like
 * "for thirty years" or "at night" or "weekend nights"
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class NicotineTemporal(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The type of nicotine, cigarettes, chewing tobacco, etc.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class NicotineType(override val startIndex: Int, override val endIndex: Int) : Label()

/**
 * A word that indicates whether usage is ongoing or has ceased.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class NicotineStatus(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The method how nicotine usage occurred. E.g. smoked, chewed, etc.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class NicotineMethod(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * Detects [NicotineRelevant] labels from [NicotineCue] labels in text.
 */
class NicotineRelevantLabeler : DocumentTask {
    override fun run(document: Document) {
        val relevants = document.findRelevantAncestors(document.labelIndex<NicotineCue>())
                .map { NicotineRelevant(it) }
        document.labelAll(relevants)
    }
}

/**
 * Detects if a phrase is a nicotine dependant phrase by seeing if it is, or has a
 * [NicotineRelevant] ancestor
 */
internal fun Document.isNicotineDep(textRange: TextRange): Boolean {
    val insideSpan = dependencies().inside(textRange)
    val nicotineRelevants = labelIndex<NicotineRelevant>()
    val alcoholRelevants = labelIndex<AlcoholRelevant>()
    val drugRelevants = labelIndex<DrugRelevant>()
    val phraseRoot = findHead(insideSpan)
    phraseRoot.selfAndParentIterator().forEach {
        if (nicotineRelevants.containsSpan(it)) return true
        if (alcoholRelevants.containsSpan(it) || drugRelevants.containsSpan(it)) return false
    }
    return false
}

/**
 * The model for nicotine amount units.
 */
@Singleton
class NicotineAmountUnits(
        val detector: SequenceDetector<String, Token>
) {
    @Inject internal constructor(
            @Setting("sh.nicotine.amountUnitsPath") path: String
    ) : this(SequenceDetector.loadFromFile(path) { a, b: Token ->
        b.text.startsWith(a, true)
    })
}

/**
 * Detects and labels [NicotineUnit] instances.
 */
class NicotineUnitDetector(
        private val detector: SequenceDetector<String, Token>
) : DocumentTask {
    @Inject internal constructor(amountUnits: NicotineAmountUnits) : this(amountUnits.detector)

    override fun run(document: Document) {
        val sentences = document.labelIndex<Sentence>()
        val tokens = document.labelIndex<ParseToken>()

        val candidates = document.labelIndex<NicotineCandidate>()

        val labeler = document.labeler<NicotineUnit>()

        candidates
                .map { sentences.inside(it) }
                .forEach {
                    it.map { tokens.inside(it).asList() }
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
 * The TagEx search expression for nicotine amounts.
 *
 * @property expr the nicotine amount TagEx search expression
 */
@Singleton
class NicotineAmountSearchExpr(val expr: TagEx) {
    @Inject internal constructor(
            searchExprFactory: TagExFactory,
            @Setting("sh.nicotine.amountExprPath") path: String
    ) : this(searchExprFactory.parse(File(path).readText()))
}

/**
 * Detects and labels instances of [NicotineAmount] in text using the nicotine amount TagEx pattern.
 *
 * @property expr the nicotine amount TagEx search expression
 */
class NicotineAmountDetector(private val expr: TagEx) : DocumentTask {
    @Inject internal constructor(
            nicotineAmountSearchExpr: NicotineAmountSearchExpr
    ) : this(nicotineAmountSearchExpr.expr)

    override fun run(document: Document) {
        val labeler = document.labeler<NicotineAmount>()

        document.labelIndex<NicotineCandidate>()
                .asSequence()
                .flatMap { expr.findAll(document, it) }
                .filter(document::isNicotineDep)
                .map(::NicotineAmount)
                .forEach(labeler::add)
    }
}

/**
 * Detects and labels [NicotineFrequency] instances in text using the general [UsageFrequency]
 * label.
 */
class NicotineFrequencyDetector : DocumentTask {
    override fun run(document: Document) {
        val nicotineCandidates = document.labelIndex<NicotineCandidate>()

        val amounts = document.labelIndex<NicotineAmount>()

        val usageFrequencies = document.labelIndex<UsageFrequency>()
        val labeler = document.labeler<NicotineFrequency>()

        for (nicotineCandidate in nicotineCandidates) {
            usageFrequencies
                    .inside(nicotineCandidate)
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
class NicotineTemporalDetector : DocumentTask {
    override fun run(document: Document) {
        val nicotineCandidates = document.labelIndex<NicotineCandidate>()

        val frequencies = document.labelIndex<NicotineFrequency>()
        val amounts = document.labelIndex<NicotineAmount>()

        val temporalPhrases = document.labelIndex<TemporalPhrase>()
        val temporalLabeler = document.labeler<NicotineTemporal>()

        for (nicotineCandidate in nicotineCandidates) {
            temporalPhrases.inside(nicotineCandidate)
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
class NicotineTypes(val detector: SequenceDetector<String, Token>) {
    @Inject internal constructor(
            @Setting("sh.nicotine.typesPath") path: String
    ) : this(SequenceDetector.loadFromFile(path, tokenTextEquals))
}

/**
 * Detects and labels [NicotineType] instances in text using the nicotine types model.
 */
class NicotineTypeDetector(
        private val detector: SequenceDetector<String, Token>
) : DocumentTask {
    @Inject internal constructor(nicotineTypes: NicotineTypes) : this(nicotineTypes.detector)

    override fun run(document: Document) {
        val candidates = document.labelIndex<NicotineCandidate>()
        val tokens = document.labelIndex<ParseToken>()

        val labeler = document.labeler<NicotineType>()

        candidates
                .map { tokens.inside(it).asList() }
                .forEach { candidateTokens ->
                    detector.detectAll(candidateTokens)
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
    @Inject internal constructor(
            @Setting("sh.nicotine.statusPhrasesPath") path: String
    ) : this(SequenceDetector.loadFromFile(path) { string, token: ParseToken ->
        token.text.compareTo(string, true) == 0
    })
}

/**
 * Detects nicotine status phrases in text using the nicotine status model and the general
 * [UsageStatusPhrases]
 */
class NicotineStatusDetector(
        private val detector: SequenceDetector<String, ParseToken>
) : DocumentTask {
    @Inject internal constructor(
            statusPhrases: NicotineStatusPhrases
    ) : this(statusPhrases.detector)

    override fun run(document: Document) {
        val tokens = document.labelIndex<ParseToken>()

        val usageStatuses = document.labelIndex<UsageStatus>()

        val labeler = document.labeler<NicotineStatus>()

        document.labelIndex<NicotineCandidate>()
                .onEach {
                    usageStatuses.inside(it)
                            .filter { document.isNicotineDep(it) }
                            .forEach {
                                labeler.add(NicotineStatus(it))
                            }
                }
                .map { tokens.inside(it).asList() }
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
class NicotineMethodPhrases(val detector: SequenceDetector<String, ParseToken>) {
    @Inject internal constructor(
            @Setting("sh.nicotine.methodPhrasesPath") path: String
    ) : this(SequenceDetector.loadFromFile(path) { string, token: ParseToken ->
        token.text.compareTo(string, true) == 0
    })
}

/**
 * Detects and labels instances of [NicotineMethod] in text using the [GenericMethodPhrase]
 * instances and the nicotine methods model.
 */
class NicotineMethodDetector(
        private val detector: SequenceDetector<String, ParseToken>
) : DocumentTask {
    @Inject internal constructor(phrases: NicotineMethodPhrases) : this(phrases.detector)

    override fun run(document: Document) {
        val candidates = document.labelIndex<NicotineCandidate>()
        val tokens = document.labelIndex<ParseToken>()

        val genericMethods = document.labelIndex<GenericMethodPhrase>()

        val labeler = document.labeler<NicotineMethod>()

        candidates
                .onEach {
                    genericMethods
                            .inside(it)
                            .filter { document.isNicotineDep(it) }
                            .forEach {
                                labeler.add(NicotineMethod(it))
                            }
                }
                .map { tokens.inside(it).asList() }
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
