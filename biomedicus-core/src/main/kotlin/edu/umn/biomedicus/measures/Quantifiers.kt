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

package edu.umn.biomedicus.measures

import edu.umn.biomedicus.annotations.Setting
import edu.umn.biomedicus.common.SequenceDetector
import edu.umn.biomedicus.framework.TagEx
import edu.umn.biomedicus.framework.TagExFactory
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.biomedicus.tokenization.Token
import edu.umn.nlpengine.*
import javax.inject.Inject

/**
 * A type of indefinite quantifier
 */
enum class IndefiniteQuantifierType {
    /**
     * Indefinite quantifier on the left, or lesser side of the number
     */
    LEFT,
    /**
     * Indefinite quantifier on the right, or greater side of the number
     */
    RIGHT,
    /**
     * Indefinite quantifier spanning around both sides of a number
     */
    LOCAL
}

/**
 * A part of a greater indefinite quantifier, the word or words that indicate that the quantifier is
 * indefinite
 *
 * @property startIndex the start of cue phrase
 * @property endIndex the end of the cue phrase
 * @property indefiniteQuantifierType
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class IndefiniteQuantifierCue(
    override val startIndex: Int,
    override val endIndex: Int,
    val indefiniteQuantifierType: String
) : Label() {
    constructor(
        textRange: TextRange,
        type: String
    ) : this(textRange.startIndex, textRange.endIndex, type)

    constructor(
        startIndex: Int,
        endIndex: Int,
        indefiniteQuantifierType: IndefiniteQuantifierType
    ) : this(startIndex, endIndex, indefiniteQuantifierType.toString())
}

/**
 * A standalone value which doesn't have a definite amount, like "a few" or "some."
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class FuzzyValue(override val startIndex: Int, override val endIndex: Int) : Label()

/**
 * A word that functions as a quantifier without modifying a unit. "never", "significant", "few",
 * "heavy".
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class StandaloneQuantifier(
    override val startIndex: Int,
    override val endIndex: Int
) : Label()

/**
 * An amount of something. Modifies a unit
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class Quantifier(
    override val startIndex: Int,
    override val endIndex: Int,
    val isExact: Boolean
) : Label() {

    constructor(range: TextRange, isExact: Boolean) : this(
        range.startIndex,
        range.endIndex,
        isExact
    )
}

private val test: (String, Token) -> Boolean = { word, token: Token ->
    token.text.equals(word, true)
}

/**
 * Detects instances of [IndefiniteQuantifierCue], which are of 3 types defined in
 * [IndefiniteQuantifierType] and also detects [FuzzyValue].
 */
class IndefiniteQuantifierDetector(
    private val left: SequenceDetector<String, Token>,
    private val right: SequenceDetector<String, Token>,
    private val local: SequenceDetector<String, Token>,
    private val fuzzy: SequenceDetector<String, Token>
) : DocumentsProcessor {
    @Inject internal constructor(
        @Setting("measures.indefiniteQuantifiers.leftPath") leftPath: String,
        @Setting("measures.indefiniteQuantifiers.rightPath") rightPath: String,
        @Setting("measures.indefiniteQuantifiers.localPath") localPath: String,
        @Setting("measures.indefiniteQuantifiers.fuzzyPath") fuzzyPath: String
    ) : this(
        SequenceDetector.loadFromFile(leftPath, test),
        SequenceDetector.loadFromFile(rightPath, test),
        SequenceDetector.loadFromFile(localPath, test),
        SequenceDetector.loadFromFile(fuzzyPath, test)
    )

    override fun process(document: Document) {
        val sentences = document.labelIndex<Sentence>()
        val tokens = document.labelIndex<ParseToken>()

        val cueLabeler = document.labeler<IndefiniteQuantifierCue>()
        val fuzzyLabeler = document.labeler<FuzzyValue>()

        for (sentence in sentences) {
            val sentenceTokens = tokens.inside(sentence).asList()

            left.detectAll(sentenceTokens).forEach {
                cueLabeler.add(
                    IndefiniteQuantifierCue(
                        sentenceTokens[it.first].startIndex,
                        sentenceTokens[it.last].endIndex, IndefiniteQuantifierType.LEFT.name
                    )
                )
            }

            right.detectAll(sentenceTokens).forEach {
                cueLabeler.add(
                    IndefiniteQuantifierCue(
                        sentenceTokens[it.first].startIndex,
                        sentenceTokens[it.last].endIndex, IndefiniteQuantifierType.RIGHT.name
                    )
                )
            }

            local.detectAll(sentenceTokens).forEach {
                cueLabeler.add(
                    IndefiniteQuantifierCue(
                        sentenceTokens[it.first].startIndex,
                        sentenceTokens[it.last].endIndex, IndefiniteQuantifierType.LOCAL.name
                    )
                )
            }

            fuzzy.detectAll(sentenceTokens).forEach {
                fuzzyLabeler.add(
                    FuzzyValue(
                        sentenceTokens[it.first].startIndex,
                        sentenceTokens[it.last].endIndex
                    )
                )
            }
        }
    }
}

/**
 * Detects instances of [StandaloneQuantifier] labels.
 */
class StandaloneQuantifierDetector(
    val detector: SequenceDetector<String, Token>
) : DocumentsProcessor {
    @Inject constructor(
        @Setting("measures.standaloneQuantifiersPath") path: String
    ) : this(SequenceDetector.loadFromFile(path, test))

    override fun process(document: Document) {
        val sentences = document.labelIndex<Sentence>()
        val tokens = document.labelIndex<ParseToken>()

        val labeler = document.labeler<StandaloneQuantifier>()

        sentences
            .map { tokens.inside(it).asList() }
            .forEach { sentenceTokens ->
                detector.detectAll(sentenceTokens).forEach {
                    labeler.add(
                        StandaloneQuantifier(
                            sentenceTokens[it.first].startIndex,
                            sentenceTokens[it.last].endIndex
                        )
                    )
                }
            }
    }
}

/**
 * Detects [Quantifier] instances in text.
 */
class QuantifierDetector(private val expr: TagEx) : DocumentsProcessor {
    @Inject constructor(factory: TagExFactory) : this(
        factory.parse(
            """([?indef:IndefiniteQuantifierCue] ->)?
                                   ([?NumberRange] | [?Number] | [?fuzz:FuzzyValue]
                                     | [?PosTag<getPartOfSpeech=eDT> ParseToken<getText="a">])"""
        )
    )

    override fun process(document: Document) {
        val labeler = document.labeler<Quantifier>()

        for (result in expr.findAll(document)) {
            val isExact = !result.namedLabels.containsLabel("indef")
                    && !result.namedLabels.containsLabel("fuzz")

            labeler.add(Quantifier(result, isExact))
        }
    }
}

