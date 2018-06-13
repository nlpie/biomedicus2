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
 * A verb indicating dependants are related to drug usage.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class DrugRelevant(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The unit of measurement of an drug usage measurement, used in [DrugAmount] detection.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class DrugUnit(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The measurement / amount of an drug usage. E.g. 4-8 beers
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class DrugAmount(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The frequency that an drug usage occurs. E.g. "daily", "weekly"
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class DrugFrequency(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The time period drug usage occurs or occurred in. Phrases like "for thirty years", "at night"
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class DrugTemporal(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The type of drug, e.g. beer, liquor.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class DrugType(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * A word that indicates whether drug usage is ongoing or has ceased.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class DrugStatus(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The method
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class DrugMethod(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * Detects [DrugRelevant] labels from [DrugCue] labels in text.
 */
class DrugRelevantLabeler : DocumentOperation {
    override fun process(document: Document) {
        val relevants = document.findRelevantAncestors(document.labelIndex<DrugCue>())
                .map { DrugRelevant(it) }
        document.labelAll(relevants)
    }
}

internal fun Document.isDrugDep(textRange: TextRange): Boolean {
    val insideSpan = dependencies().inside(textRange)
    val alcoholRelevants = labelIndex<AlcoholRelevant>()
    val drugRelevants = labelIndex<DrugRelevant>()
    val nicotineRelevants = labelIndex<NicotineRelevant>()
    val phraseRoot = findHead(insideSpan)
    phraseRoot.selfAndParentIterator().forEach {
        if (drugRelevants.containsSpan(it)) return true
        if (alcoholRelevants.containsSpan(it) || nicotineRelevants.containsSpan(it)) return false
    }
    return false
}

/**
 * The model for drug amount unit detection.
 *
 * @property detector a sequence detector that finds drug amount units
 */
@Singleton
class DrugAmountUnits(
        val detector: SequenceDetector<String, Token>
) {
    @Inject internal constructor(
            @Setting("sh.drugs.amountUnitsPath") path: String
    ) : this(SequenceDetector.loadFromFile(path, tokenStartsWith))
}

/**
 * Labels [DrugUnit] by checking if any phrases match the list of amount units using a sequence
 * detector, and verifies using a dependencies sanity check.
 *
 * @property detector the sequence detector for drug amount units
 */
class DrugUnitDetector(
        private val detector: SequenceDetector<String, Token>
) : DocumentOperation {
    @Inject internal constructor(amountUnits: DrugAmountUnits) : this(amountUnits.detector)

    override fun process(document: Document) {
        val tokens = document.labelIndex<ParseToken>()

        val labeler = document.labeler<DrugUnit>()

        document.labelIndex<DrugCandidate>()
                .map { tokens.inside(it).asList() }
                .flatMap { detector.detectAllSpans(it) }
                .filter { document.isDrugDep(it) }
                .forEach { labeler.add(DrugUnit(it)) }
    }
}

/**
 * The TagEx search expression for drug amounts.
 *
 * @property expr the drug amount TagEx expression.
 */
@Singleton
class DrugAmountExpr(val expr: TagEx) {
    @Inject internal constructor(
            factory: TagExFactory,
            @Setting("sh.drugs.amountExprPath") path: String
    ) : this(factory.parse(File(path).readText()))
}

/**
 * Detects and labels instances of [DrugAmount] in text using the drug amount TagEx pattern.
 *
 * @property expr the drug amount TagEx expression.
 */
class DrugAmountDetector(private val expr: TagEx) : DocumentOperation {
    @Inject internal constructor(amountExpr: DrugAmountExpr) : this(amountExpr.expr)

    override fun process(document: Document) {
        val labeler = document.labeler<DrugAmount>()

        document.labelIndex<DrugCandidate>()
                .flatMap { expr.findAll(document, it).asIterable() }
                .filter { document.isDrugDep(it) }
                .map { DrugAmount(it) }
                .forEach { labeler.add(it) }
    }
}

/**
 * Detects and labels [DrugFrequency] instances in text using the general [UsageFrequency]
 * label and resolving drug related dependencies.
 */
class DrugFrequencyDetector : DocumentOperation {
    override fun process(document: Document) {
        val amounts = document.labelIndex<DrugAmount>()

        val usageFrequencies = document.labelIndex<UsageFrequency>()
        val labeler = document.labeler<DrugFrequency>()

        document.labelIndex<DrugCandidate>()
                .flatMap { usageFrequencies.inside(it) }
                .filter { amounts.containing(it).isEmpty() }
                .filter { document.isDrugDep(it) }
                .map { DrugFrequency(it) }
                .forEach { labeler.add(it) }
    }
}

/**
 * Detects and labels [DrugTemporal] instances in text using the general [TemporalPhrase] and
 * checks for whether there are overlaps with any amounts, frequencies, and whether it is an
 * drug related parsing dependency.
 */
class DrugTemporalDetector : DocumentOperation {
    override fun process(document: Document) {
        val frequencies = document.labelIndex<DrugFrequency>()
        val amounts = document.labelIndex<DrugAmount>()

        val temporalPhrases = document.labelIndex<TemporalPhrase>()
        val temporalLabeler = document.labeler<DrugTemporal>()

        document.labelIndex<DrugCandidate>()
                .flatMap { temporalPhrases.inside(it) }
                .filter { amounts.containing(it).isEmpty() }
                .filter { frequencies.containing(it).isEmpty() }
                .filter { document.isDrugDep(it) }
                .forEach { temporalLabeler.add(DrugTemporal(it)) }
    }
}

/**
 * The model for drug types.
 */
@Singleton
class DrugTypes(val detector: SequenceDetector<String, Token>) {
    @Inject internal constructor(
            @Setting("sh.drugs.typesPath") path: String
    ) : this(SequenceDetector.loadFromFile(path, tokenTextEquals))
}

/**
 * Detects and labels [DrugType] using the drug type phrases and filters for drug related
 * linguistic dependency.
 */
class DrugTypeDetector(
        private val detector: SequenceDetector<String, Token>
) : DocumentOperation {
    @Inject internal constructor(drugTypes: DrugTypes) : this(drugTypes.detector)

    override fun process(document: Document) {
        val tokens = document.tokens()
        val labeler = document.labeler<DrugType>()
        document.labelIndex<DrugCandidate>()
                .map { tokens.inside(it).asList() }
                .flatMap { detector.detectAllSpans(it) }
                .filter { document.isDrugDep(it) }
                .forEach { labeler.add(DrugType(it)) }
    }
}

/**
 * Detects and labels [DrugStatus] in text using pre-labeled [UsageStatus] instances. Filters on
 * drug related linguistic dependency.
 */
class DrugStatusDetector : DocumentOperation {
    override fun process(document: Document) {
        val usageStatuses = document.labelIndex<UsageStatus>()
        val labeler = document.labeler<DrugStatus>()

        document.labelIndex<DrugCandidate>()
                .flatMap { usageStatuses.inside(it) }
                .filter { document.isDrugDep(it) }
                .forEach { labeler.add(DrugStatus(it)) }
    }
}

/**
 * Phrases that indicate the method of drug usage.
 */
@Singleton
class DrugMethodPhrases(val detector: SequenceDetector<String, Token>) {
    @Inject internal constructor(
            @Setting("sh.drugs.methodPhrasesPath") path: String
    ) : this(SequenceDetector.loadFromFile(path, tokenTextEquals))
}

/**
 * Detects and labels instances of [DrugMethod] using the [GenericMethodPhrase] labels and the
 * drug method phrases.
 */
class DrugMethodDetector(
        private val detector: SequenceDetector<String, Token>
) : DocumentOperation {
    @Inject internal constructor(phrases: DrugMethodPhrases) : this(phrases.detector)

    override fun process(document: Document) {
        val tokens = document.tokens()
        val genericMethods = document.labelIndex<GenericMethodPhrase>()
        val labeler = document.labeler<DrugMethod>()

        document.labelIndex<DrugCandidate>()
                .onEach {
                    genericMethods.inside(it)
                            .filter { document.isDrugDep(it) }
                            .forEach { labeler.add(DrugMethod(it)) }
                }
                .map { tokens.inside(it).asList() }
                .flatMap { detector.detectAllSpans(it) }
                .filter { document.isDrugDep(it) }
                .forEach { labeler.add(DrugMethod(it)) }
    }
}
