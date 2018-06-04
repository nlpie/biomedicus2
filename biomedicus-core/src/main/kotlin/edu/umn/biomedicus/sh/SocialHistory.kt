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
import edu.umn.biomedicus.exc.BiomedicusException
import edu.umn.biomedicus.family.Relative
import edu.umn.biomedicus.framework.SearchExpr
import edu.umn.biomedicus.framework.SearchExprFactory
import edu.umn.biomedicus.parsing.Dependency
import edu.umn.biomedicus.parsing.UDRelation
import edu.umn.biomedicus.parsing.findHead
import edu.umn.biomedicus.sections.Section
import edu.umn.biomedicus.sections.SectionContent
import edu.umn.biomedicus.sections.SectionHeader
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.biomedicus.tokenization.Token
import edu.umn.nlpengine.*
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

class SocialHistoryModule : SystemModule() {
    override fun setup() {
        addLabelClass<AlcoholCandidate>()
        addLabelClass<AlcoholCue>()
        addLabelClass<DrugCandidate>()
        addLabelClass<DrugCue>()
        addLabelClass<NicotineCandidate>()
        addLabelClass<NicotineCue>()

        addLabelClass<AlcoholRelevant>()
        addLabelClass<AlcoholUnit>()
        addLabelClass<AlcoholAmount>()
        addLabelClass<AlcoholFrequency>()
        addLabelClass<AlcoholTemporal>()
        addLabelClass<AlcoholType>()
        addLabelClass<AlcoholStatus>()
        addLabelClass<AlcoholMethod>()

        addLabelClass<DrugRelevant>()
        addLabelClass<DrugUnit>()
        addLabelClass<DrugAmount>()
        addLabelClass<DrugFrequency>()
        addLabelClass<DrugTemporal>()
        addLabelClass<DrugType>()
        addLabelClass<DrugStatus>()
        addLabelClass<DrugMethod>()

        addLabelClass<NicotineRelevant>()
        addLabelClass<NicotineUnit>()
        addLabelClass<NicotineAmount>()
        addLabelClass<NicotineFrequency>()
        addLabelClass<NicotineTemporal>()
        addLabelClass<NicotineType>()
        addLabelClass<NicotineStatus>()
        addLabelClass<NicotineMethod>()

        addLabelClass<UsageFrequencyPhrase>()
        addLabelClass<UsageFrequency>()
        addLabelClass<UsageStatus>()
        addLabelClass<GenericMethodPhrase>()
    }

}

/**
 * A social history candidate for alcohol usage.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class AlcoholCandidate(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * A word that indicates a sentence is an alcohol social history candidate.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class AlcoholCue(override val startIndex: Int, override val endIndex: Int) : Label()

/**
 * A social history candidate for drug usage.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class DrugCandidate(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * A word that indicates a sentence is a drug social history candidate.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class DrugCue(override val startIndex: Int, override val endIndex: Int) : Label()

/**
 * A social history candidate for nicotine usage.
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
 * A general known usage frequency phrase that could potentially apply to any social history usage
 * type.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class UsageFrequencyPhrase(
        override val startIndex: Int,
        override val endIndex: Int
) : Label()

/**
 * A general usage frequency that could potentially apply to any social history usage type.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class UsageFrequency(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * A general usage status that could potentially apply to any social history type.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class UsageStatus(override val startIndex: Int, override val endIndex: Int) : Label()

/**
 * A general method phrase that could potentially apply to any social history method.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class GenericMethodPhrase(override val startIndex: Int, override val endIndex: Int) : Label()

internal val headers = SequenceDetector(
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

internal val headersExact = SequenceDetector(
        listOf("ESRD"),
        listOf("SH"),
        listOf("SHX")
) { a: String, b: Token -> b.text.contentEquals(a) }

internal val tokenStartsWith: (String, Token) -> Boolean = { a, b: Token ->
    b.text.startsWith(a, true)
}

internal val tokenTextEquals: (String, Token) -> Boolean = { a, b: Token ->
    b.text.compareTo(a, true) == 0
}

/**
 * The models for the candidate detectors and general social history tasks.
 */
@Singleton
class CandidateDetectionRules @Inject constructor(
        @Setting("sh.alcohol.candidateCuesPath") alcoholCuePath: String,
        @Setting("sh.alcohol.cueIgnorePath") alcoholIgnorePath: String,
        @Setting("sh.alcohol.nonalcoholicDrinksPath") nonalcoholicDrinksPath: String,
        @Setting("sh.drugs.candidateCuesPath") drugCuePath: String,
        @Setting("sh.nicotine.candidateCuesPath") nicotineCuePath: String
) {
    val alcoholCueDetector: SequenceDetector<String, Token>

    val nonalcoholicDrinksDetector: SequenceDetector<String, Token>

    val alcoholIgnoreDetector: SequenceDetector<String, Token>

    val drugCueDetector: SequenceDetector<String, Token>

    val nicotineCueDetector: SequenceDetector<String, Token>

    init {
        log.info("Loading social history candidate detection rule set.")

        alcoholCueDetector = SequenceDetector.loadFromFile(alcoholCuePath, tokenStartsWith)
        alcoholIgnoreDetector = SequenceDetector.loadFromFile(alcoholIgnorePath, tokenStartsWith)
        nonalcoholicDrinksDetector = SequenceDetector.loadFromFile(nonalcoholicDrinksPath, tokenStartsWith)

        drugCueDetector = SequenceDetector.loadFromFile(drugCuePath, tokenStartsWith)

        nicotineCueDetector = SequenceDetector.loadFromFile(nicotineCuePath, tokenStartsWith)
    }

    companion object {
        private val log = LoggerFactory.getLogger(CandidateDetectionRules::class.java)
    }
}

/**
 * Detects candidate sentences which may have usage for alcohol, drugs, and nicotine.
 */
class SocialHistoryCandidateDetector(
        private val alcoholDetector: SequenceDetector<String, Token>,
        private val nonalcoholicDetector: SequenceDetector<String, Token>,
        private val alcoholIgnoreDetector: SequenceDetector<String, Token>,
        private val drugDetector: SequenceDetector<String, Token>,
        private val nicotineDetector: SequenceDetector<String, Token>
) : DocumentOperation {

    @Inject constructor(
            candidateDetectionRules: CandidateDetectionRules
    ) : this(
            alcoholDetector = candidateDetectionRules.alcoholCueDetector,
            nonalcoholicDetector = candidateDetectionRules.nonalcoholicDrinksDetector,
            alcoholIgnoreDetector = candidateDetectionRules.alcoholIgnoreDetector,
            drugDetector = candidateDetectionRules.drugCueDetector,
            nicotineDetector = candidateDetectionRules.nicotineCueDetector
    )

    override fun process(document: Document) {
        val sections = document.labelIndex<Section>()
        val sectionContents = document.labelIndex<SectionContent>()
        val sentences = document.labelIndex<Sentence>()
        val tokens = document.labelIndex<ParseToken>()
        val relatives = document.labelIndex<Relative>()

        val alcoholCandidateLabeler = document.labeler<AlcoholCandidate>()
        val alcoholCueLabeler = document.labeler<AlcoholCue>()

        val drugCandidateLabeler = document.labeler<DrugCandidate>()
        val drugCueLabeler = document.labeler<DrugCue>()

        val nicotineCandidateLabeler = document.labeler<NicotineCandidate>()
        val nicotineCueLabeler = document.labeler<NicotineCue>()

        document.labelIndex<SectionHeader>()
                .asSequence()
                .filter {
                    val headerTokens = tokens.inside(it).asList()
                    headers.matches(headerTokens) != null ||
                            headersExact.matches(headerTokens) != null
                }
                .forEach {
                    val section = sections.containing(it).first() ?: return@forEach

                    val contents = sectionContents.inside(section).first()
                            ?: throw BiomedicusException("No contents for section: $section")

                    sentences.inside(contents)
                            .filter { document.text[it.endIndex - 1] != ':' }
                            .filter { relatives.inside(it).isEmpty() }
                            .forEach { sentence ->
                                val sentenceTokens = tokens.inside(sentence).asList()

                                alcoholDetector.detectAll(sentenceTokens)
                                        .takeIf { it.isNotEmpty() }
                                        ?.let { alcoholMatches ->
                                            val nonalcoholicMatches =
                                                    nonalcoholicDetector.detectAll(sentenceTokens)
                                            if (nonalcoholicMatches.isNotEmpty()) return@let

                                            val alcoholIgnores =
                                                    alcoholIgnoreDetector.detectAll(sentenceTokens)
                                            if (alcoholIgnores.isNotEmpty()) return@let

                                            alcoholCandidateLabeler.add(AlcoholCandidate(sentence))
                                            for (match in alcoholMatches) {
                                                alcoholCueLabeler.add(
                                                        AlcoholCue(
                                                                sentenceTokens[match.first].startIndex,
                                                                sentenceTokens[match.last].endIndex
                                                        )
                                                )
                                            }
                                        }

                                val drugMatches = drugDetector.detectAll(sentenceTokens)
                                if (drugMatches.isNotEmpty()) {
                                    drugCandidateLabeler.add(DrugCandidate(sentence))
                                    for (match in drugMatches) {
                                        drugCueLabeler.add(
                                                DrugCue(
                                                        sentenceTokens[match.first].startIndex,
                                                        sentenceTokens[match.last].endIndex
                                                )
                                        )
                                    }
                                }

                                val matches = nicotineDetector.detectAll(sentenceTokens)
                                if (matches.isNotEmpty()) {
                                    nicotineCandidateLabeler.add(NicotineCandidate(sentence))
                                    for (match in matches) {
                                        nicotineCueLabeler.add(
                                                NicotineCue(
                                                        sentenceTokens[match.first].startIndex,
                                                        sentenceTokens[match.last].endIndex
                                                )
                                        )
                                    }
                                }
                            }
                }
    }
}

/**
 * Generic usage frequency phrases model.
 */
@Singleton
class UsageFrequencyPhrases @Inject constructor(
        @Setting("sh.usageFrequencyPhrasesPath") path: String
) {
    val detector = SequenceDetector.loadFromFile(path) { a, b: Token ->
        b.text.compareTo(a, true) == 0
    }
}

/**
 * Detects certain concrete usage frequency phrases from a dictionary of known phrases to be used in
 * the labeling of generic usage frequencies.
 */
class UsageFrequencyPhraseDetector @Inject constructor(
        usageFrequencyPhrases: UsageFrequencyPhrases
) : DocumentOperation {
    val detector = usageFrequencyPhrases.detector

    override fun process(document: Document) {
        val sentences = document.labelIndex<Sentence>()
        val tokens = document.labelIndex<ParseToken>()

        val labeler = document.labeler<UsageFrequencyPhrase>()

        val nicotineCandidates = document.labelIndex<NicotineCandidate>()
        val alcoholCandidates = document.labelIndex<AlcoholCandidate>()
        val drugCandidates = document.labelIndex<DrugCandidate>()

        sentences
                .filter {
                    nicotineCandidates.containsSpan(it) || alcoholCandidates.containsSpan(it)
                            || drugCandidates.containsSpan(it)
                }
                .map { tokens.inside(it).asList() }
                .forEach { sentenceTokens ->
                    detector.detectAll(sentenceTokens).forEach {
                        labeler.add(UsageFrequencyPhrase(sentenceTokens[it.first].startIndex,
                                sentenceTokens[it.last].endIndex))
                    }
                }
    }
}

/**
 * The usage frequency tagex pattern.
 */
@Singleton
data class UsageFrequencyPattern(val searchExpr: SearchExpr) {
    @Inject constructor(searchExprFactory: SearchExprFactory) : this(
            searchExprFactory.parse(
                    """
[?Quantifier] ParseToken<getText="times"|i"x"> ((ParseToken<getText="a"|"per"|"/"> ->)? TimeUnit | -> TimeFrequencyUnit) |
([?Quantifier] -> TimeUnit ->)? [?ParseToken<getText="per"|"/">] -> TimeUnit |
[?ParseToken<getText="per"|"every">] (-> Quantifier)? -> TimeUnit |
[?ParseToken<getText="a">] -> TimeUnit |
[?UsageFrequencyPhrase] |
[?TimeFrequencyUnit]
                        """
            )
    )
}

/**
 * Detects [UsageFrequency], generic usage frequency phrases that could apply to any social history
 * type.
 */
class UsageFrequencyDetector(private val expr: SearchExpr) : DocumentOperation {
    @Inject internal constructor(
            usageFrequencyPattern: UsageFrequencyPattern
    ) : this(usageFrequencyPattern.searchExpr)

    override fun process(document: Document) {
        val sentences = document.labelIndex<Sentence>()

        val nicotineCandidates = document.labelIndex<NicotineCandidate>()
        val alcoholCandidates = document.labelIndex<AlcoholCandidate>()
        val drugCandidates = document.labelIndex<DrugCandidate>()

        val searcher = expr.createSearcher(document)
        val labeler = document.labeler<UsageFrequency>()

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

/**
 * The model for generic usage status phrases.
 */
@Singleton
class UsageStatusPhrases(val detector: SequenceDetector<String, Token>) {
    @Inject constructor(
            @Setting("sh.statusPhrasesPath") path: String
    ) : this(SequenceDetector.loadFromFile(path, tokenTextEquals))
}

/**
 * Detects [UsageStatus], usage status phrases that could apply to any social history usage type.
 */
class UsageStatusDetector(
        private val detector: SequenceDetector<String, Token>
) : DocumentOperation {
    @Inject internal constructor(phrases: UsageStatusPhrases) : this(phrases.detector)

    override fun process(document: Document) {
        val sentences = document.labelIndex<Sentence>()
        val tokens = document.labelIndex<ParseToken>()

        val nicotineCandidates = document.labelIndex<NicotineCandidate>()
        val alcoholCandidates = document.labelIndex<AlcoholCandidate>()
        val drugCandidates = document.labelIndex<DrugCandidate>()

        val labeler = document.labeler<UsageStatus>()

        sentences
                .filter {
                    nicotineCandidates.containsSpan(it) || alcoholCandidates.containsSpan(it)
                            || drugCandidates.containsSpan(it)
                }
                .map { tokens.inside(it).asList() }
                .forEach { sentenceTokens ->
                    detector.detectAll(sentenceTokens).forEach {
                        labeler.add(UsageStatus(sentenceTokens[it.first].startIndex,
                                sentenceTokens[it.last].endIndex))
                    }
                }
    }
}

/**
 * The model for generic method phrases.
 */
@Singleton
class GenericMethodPhrases(val detector: SequenceDetector<String, Token>) {
    @Inject internal constructor(
            @Setting("sh.genericMethodPhrasesPath") path: String
    ) : this(SequenceDetector.loadFromFile(path, tokenTextEquals))
}

/**
 * Detects method phrases that could apply to any social history usage type.
 */
class GenericMethodPhraseDetector(
        val detector: SequenceDetector<String, Token>
) : DocumentOperation {
    @Inject internal constructor(phrases: GenericMethodPhrases) : this(phrases.detector)

    override fun process(document: Document) {
        val sentences = document.labelIndex<Sentence>()
        val tokens = document.labelIndex<ParseToken>()

        val nicotineCandidates = document.labelIndex<NicotineCandidate>()
        val alcoholCandidates = document.labelIndex<AlcoholCandidate>()
        val drugCandidates = document.labelIndex<DrugCandidate>()

        val labeler = document.labeler<GenericMethodPhrase>()

        sentences
                .filter {
                    nicotineCandidates.containsSpan(it) || alcoholCandidates.containsSpan(it)
                            || drugCandidates.containsSpan(it)
                }
                .map { tokens.inside(it).asList() }
                .forEach { sentenceTokens ->
                    detector.detectAll(sentenceTokens).forEach {
                        labeler.add(GenericMethodPhrase(sentenceTokens[it.first].startIndex,
                                sentenceTokens[it.last].endIndex))
                    }
                }
    }
}

internal fun Document.findRelevantAncestors(labels: LabelIndex<*>): Collection<Dependency> {
    val relevants = HashSet<Dependency>()

    val dependencies = dependencies()

    labels
            .asSequence()
            .map {
                val cueDependencies = dependencies.inside(it)
                cueDependencies.mapTo(relevants) { it }
                findHead(cueDependencies)
            }
            .forEach { dependency ->
                for (dep in dependency.selfAndParentIterator()) {
                    relevants.add(dep)
                    if (dep.dep.partOfSpeech.isVerb || dep.relation == UDRelation.ROOT) {
                        break
                    }
                }
            }

    return relevants
}