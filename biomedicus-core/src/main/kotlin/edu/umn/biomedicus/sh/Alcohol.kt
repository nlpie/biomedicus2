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
import edu.umn.biomedicus.common.detectAllSpans
import edu.umn.biomedicus.dependencies
import edu.umn.biomedicus.framework.TagEx
import edu.umn.biomedicus.framework.TagExFactory
import edu.umn.biomedicus.parsing.findHead
import edu.umn.biomedicus.time.TemporalPhrase
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.biomedicus.tokenization.Token
import edu.umn.biomedicus.tokens
import edu.umn.nlpengine.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A verb indicating dependants are related to alcohol usage.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class AlcoholRelevant(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The unit of measurement of an alcohol usage measurement, used in [AlcoholAmount] detection.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class AlcoholUnit(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The measurement / amount of an alcohol usage. E.g. 4-8 beers
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class AlcoholAmount(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The frequency that an alcohol usage occurs. E.g. "daily", "weekly"
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class AlcoholFrequency(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The time period alcohol usage occurs or occurred in. Phrases like "for thirty years", "at night"
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class AlcoholTemporal(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The type of alcohol, e.g. beer, liquor.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class AlcoholType(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * A word that indicates whether alcohol usage is ongoing or has ceased.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class AlcoholStatus(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The method
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class AlcoholMethod(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * Detects [AlcoholRelevant] labels from [AlcoholCue] labels in text.
 */
class AlcoholRelevantLabeler : DocumentOperation {
    override fun process(document: Document) {
        val relevants = document.findRelevantAncestors(document.labelIndex<AlcoholCue>())
                .map { AlcoholRelevant(it) }
        document.labelAll(relevants)
    }
}

internal fun Document.isAlcoholDep(textRange: TextRange): Boolean {
    val insideSpan = dependencies().inside(textRange)
    val alcoholRelevants = labelIndex<AlcoholRelevant>()
    val drugRelevants = labelIndex<DrugRelevant>()
    val nicotineRelevants = labelIndex<NicotineRelevant>()
    val phraseRoot = findHead(insideSpan)
    phraseRoot.selfAndParentIterator().forEach {
        if (alcoholRelevants.containsSpan(it)) return true
        if (drugRelevants.containsSpan(it) || nicotineRelevants.containsSpan(it)) return false
    }
    return false
}

/**
 * The model for alcohol amount unit detection.
 *
 * @property detector a sequence detector that finds alcohol amount units
 */
@Singleton
class AlcoholAmountUnits(
        val detector: SequenceDetector<String, Token>
) {
    @Inject internal constructor(
            @Setting("sh.alcohol.amountUnitsPath") path: String
    ) : this(SequenceDetector.loadFromFile(path, tokenStartsWith))
}

/**
 * Labels [AlcoholUnit] by checking if any phrases match the list of amount units using a sequence
 * detector, and verifies using a dependencies sanity check.
 *
 * @property detector the sequence detector for alcohol amount units
 */
class AlcoholUnitDetector(
        private val detector: SequenceDetector<String, Token>
) : DocumentOperation {
    @Inject internal constructor(amountUnits: AlcoholAmountUnits) : this(amountUnits.detector)

    override fun process(document: Document) {
        val tokens = document.labelIndex<ParseToken>()

        val labeler = document.labeler<AlcoholUnit>()

        document.labelIndex<AlcoholCandidate>()
                .map { tokens.inside(it).asList() }
                .flatMap { detector.detectAllSpans(it) }
                .filter { document.isAlcoholDep(it) }
                .forEach { labeler.add(AlcoholUnit(it)) }
    }
}

/**
 * The TagEx search expression for alcohol amounts.
 *
 * @property expr the alcohol amount TagEx expression.
 */
@Singleton
class AlcoholAmountExpr(val expr: TagEx) {
    @Inject internal constructor(
            factory: TagExFactory,
            @Setting("sh.alcohol.amountExprPath") path: String
    ) : this(factory.parse(File(path).readText()))
}

/**
 * Detects and labels instances of [AlcoholAmount] in text using the alcohol amount TagEx pattern.
 *
 * @property expr the alcohol amount TagEx expression.
 */
class AlcoholAmountDetector(private val expr: TagEx) : DocumentOperation {
    @Inject internal constructor(amountExpr: AlcoholAmountExpr) : this(amountExpr.expr)

    override fun process(document: Document) {
        val labeler = document.labeler<AlcoholAmount>()

        document.labelIndex<AlcoholCandidate>()
                .flatMap { expr.findAll(document, it).asIterable() }
                .filter { document.isAlcoholDep(it) }
                .map { AlcoholAmount(it) }
                .forEach { labeler.add(it) }
    }
}

/**
 * Detects and labels [AlcoholFrequency] instances in text using the general [UsageFrequency]
 * label and resolving alcohol related dependencies.
 */
class AlcoholFrequencyDetector : DocumentOperation {
    override fun process(document: Document) {
        val amounts = document.labelIndex<AlcoholAmount>()

        val usageFrequencies = document.labelIndex<UsageFrequency>()
        val labeler = document.labeler<AlcoholFrequency>()

        document.labelIndex<AlcoholCandidate>()
                .flatMap { usageFrequencies.inside(it) }
                .filter { amounts.containing(it).isEmpty() }
                .filter { document.isAlcoholDep(it) }
                .map { AlcoholFrequency(it) }
                .forEach { labeler.add(it) }
    }
}

/**
 * Detects and labels [AlcoholTemporal] instances in text using the general [TemporalPhrase] and
 * checks for whether there are overlaps with any amounts, frequencies, and whether it is an
 * alcohol related parsing dependency.
 */
class AlcoholTemporalDetector : DocumentOperation {
    override fun process(document: Document) {
        val frequencies = document.labelIndex<AlcoholFrequency>()
        val amounts = document.labelIndex<AlcoholAmount>()

        val temporalPhrases = document.labelIndex<TemporalPhrase>()
        val temporalLabeler = document.labeler<AlcoholTemporal>()

        document.labelIndex<AlcoholCandidate>()
                .flatMap { temporalPhrases.inside(it) }
                .filter { amounts.containing(it).isEmpty() }
                .filter { frequencies.containing(it).isEmpty() }
                .filter { document.isAlcoholDep(it) }
                .forEach { temporalLabeler.add(AlcoholTemporal(it)) }
    }
}

/**
 * The model for alcohol types.
 */
@Singleton
class AlcoholTypes(val detector: SequenceDetector<String, Token>) {
    @Inject internal constructor(
            @Setting("sh.alcohol.typesPath") path: String
    ) : this(SequenceDetector.loadFromFile(path, tokenTextEquals))
}

/**
 * Detects and labels [AlcoholType] using the alcohol type phrases and filters for alcohol related
 * linguistic dependency.
 */
class AlcoholTypeDetector(
        private val detector: SequenceDetector<String, Token>
) : DocumentOperation {
    @Inject internal constructor(alcoholTypes: AlcoholTypes) : this(alcoholTypes.detector)

    override fun process(document: Document) {
        val tokens = document.tokens()
        val labeler = document.labeler<AlcoholType>()
        document.labelIndex<AlcoholCandidate>()
                .map { tokens.inside(it).asList() }
                .flatMap { detector.detectAllSpans(it) }
                .filter { document.isAlcoholDep(it) }
                .forEach { labeler.add(AlcoholType(it)) }
    }
}

/**
 * Phrases that indicate the current usage status for alcohol.
 */
@Singleton
class AlcoholStatusPhrases(val detector: SequenceDetector<String, Token>) {
    @Inject internal constructor(
            @Setting("sh.alcohol.statusPhrasesPath") path: String
    ) : this(SequenceDetector.loadFromFile(path, tokenTextEquals))
}

/**
 * Detects and labels [AlcoholStatus] in text using [AlcoholStatusPhrases] and pre-labeled
 * [UsageStatus] instances. Filters on alcohol related linguistic dependency.
 */
class AlcoholStatusDetector(
        private val detector: SequenceDetector<String, Token>
) : DocumentOperation {
    @Inject internal constructor(phrases: AlcoholStatusPhrases) : this(phrases.detector)

    override fun process(document: Document) {
        val tokens = document.tokens()
        val usageStatuses = document.labelIndex<UsageStatus>()
        val labeler = document.labeler<AlcoholStatus>()

        document.labelIndex<AlcoholCandidate>()
                .onEach {
                    usageStatuses.inside(it)
                            .filter { document.isAlcoholDep(it) }
                            .forEach { labeler.add(AlcoholStatus(it)) }
                }
                .map { tokens.inside(it).asList() }
                .flatMap { detector.detectAllSpans(it) }
                .filter { document.isAlcoholDep(it) }
                .forEach { labeler.add(AlcoholStatus(it)) }
    }
}

/**
 * Phrases that indicate the method of alcohol usage.
 */
@Singleton
class AlcoholMethodPhrases(val detector: SequenceDetector<String, Token>) {
    @Inject internal constructor(
            @Setting("sh.alcohol.methodPhrasesPath") path: String
    ) : this(SequenceDetector.loadFromFile(path, tokenTextEquals))
}

/**
 * Detects and labels instances of [AlcoholMethod] using the [GenericMethodPhrase] labels and the
 * alcohol method phrases.
 */
class AlcoholMethodDetector(
        private val detector: SequenceDetector<String, Token>
) : DocumentOperation {
    @Inject internal constructor(phrases: AlcoholMethodPhrases) : this(phrases.detector)

    override fun process(document: Document) {
        val tokens = document.tokens()
        val genericMethods = document.labelIndex<GenericMethodPhrase>()
        val labeler = document.labeler<AlcoholMethod>()

        document.labelIndex<AlcoholCandidate>()
                .onEach {
                    genericMethods.inside(it)
                            .filter { document.isAlcoholDep(it) }
                            .forEach { labeler.add(AlcoholMethod(it)) }
                }
                .map { tokens.inside(it).asList() }
                .flatMap { detector.detectAllSpans(it) }
                .filter { document.isAlcoholDep(it) }
                .forEach { labeler.add(AlcoholMethod(it)) }
    }
}
