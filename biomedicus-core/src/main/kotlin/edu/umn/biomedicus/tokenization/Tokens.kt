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

package edu.umn.biomedicus.tokenization

import edu.umn.biomedicus.common.dictionary.StringIdentifier
import edu.umn.biomedicus.sentences
import edu.umn.biomedicus.tagging.PosTag
import edu.umn.nlpengine.*
import javax.inject.Inject
import javax.inject.Singleton

class TokenizationModule : SystemModule() {
    override fun setup() {
        addLabelClass<EmbeddingToken>()
        addLabelClass<TermToken>()
        addLabelClass<ParseToken>()
        addLabelClass<TokenCandidate>()
        addLabelClass<WordIndex>()
    }

}

/**
 * A generic token, extends [TextRange] has [text] containing the covered text and [hasSpaceAfter]
 * whether the token has space following it.
 */
interface Token : TextRange {
    val text: String
    val hasSpaceAfter: Boolean
}

@LabelMetadata(classpath = "biomedicus.v2_1", distinct = true)
data class EmbeddingToken(
    override val startIndex: Int,
    override val endIndex: Int,
    override val text: String,
    override val hasSpaceAfter: Boolean = true
) : Label(), Token {
    constructor(
        textRange: TextRange,
        text: String,
        hasSpaceAfter: Boolean = true
    ) : this(textRange.startIndex, textRange.endIndex, text, hasSpaceAfter)
}

/**
 * A token which represents a single word or semantic unit. Examples would be the parse tokens
 * "wo" "n't" being merged into "won't".
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class TermToken(
    override val startIndex: Int,
    override val endIndex: Int,
    override val text: String,
    override val hasSpaceAfter: Boolean
) : Label(), Token {
    constructor(
        textRange: TextRange,
        text: String,
        hasSpaceAfter: Boolean
    ) : this(textRange.startIndex, textRange.endIndex, text, hasSpaceAfter)
}

/**
 * A token roughly equivalent to a penn treebank token. Contains tokens to be labeled with part of
 * speech tags and
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class ParseToken(
    override val startIndex: Int,
    override val endIndex: Int,
    override val text: String,
    override val hasSpaceAfter: Boolean
) : Label(), Token {
    constructor(
        textRange: TextRange,
        text: String,
        hasSpaceAfter: Boolean
    ) : this(textRange.startIndex, textRange.endIndex, text, hasSpaceAfter)

    val partOfSpeech
        get() = document?.let {
            it.labelIndex<PosTag>().firstAtLocation(this)?.partOfSpeech
                    ?: error("No PosTag for parse token: $this")
        } ?: error("ParseToken not added to document: $this")
}

/**
 * An initial candidate for tokens. Uses a simple rule based penn-like tokenization without
 * splitting end-of-sentence periods into their own tokens like [ParseToken].
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class TokenCandidate(
    override val startIndex: Int,
    override val endIndex: Int
) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)

    fun hasSpaceAfter(text: String): Boolean {
        return text.length > endIndex && Character.isWhitespace(text[endIndex])
    }

    fun endsWithPeriod(text: String): Boolean {
        return length > 1 && text[endIndex - 1] == '.'
    }
}

/**
 * A universal word index (for a word vector space) which is parallel to [ParseToken] labels.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class WordIndex(
    override val startIndex: Int,
    override val endIndex: Int,
    val stringIdentifierIndex: Int
) : Label() {
    constructor(
        startIndex: Int,
        endIndex: Int,
        stringIdentifier: StringIdentifier
    ) : this(startIndex, endIndex, stringIdentifier.value())

    constructor(
        textRange: TextRange,
        stringIdentifier: StringIdentifier
    ) : this(textRange.startIndex, textRange.endIndex, stringIdentifier.value())

    val stringIdentifier: StringIdentifier get() = StringIdentifier(stringIdentifierIndex)
}

class EmbeddingTokenDetector : DocumentsProcessor {
    private val tokens = Regex("[\\p{L}]++|\\p{Nd}|[\\S&&[^\\p{Nd}\\p{L}]]")

    private val remove = Regex("[^\\p{L}\\p{Nd}]")

    override fun process(document: Document) {
        document.labelAll(detect(document.text).asIterable())
    }

    fun detect(text: String): Sequence<EmbeddingToken> {
        return tokens.findAll(text)
            .map {
                val replaced = remove.replace(it.value, " ")
                val tokenText = when (replaced) {
                    "0" -> "zero"
                    "1" -> "one"
                    "2" -> "two"
                    "3" -> "three"
                    "4" -> "four"
                    "5" -> "five"
                    "6" -> "six"
                    "7" -> "seven"
                    "8" -> "eight"
                    "9" -> "nine"
                    else -> replaced.toLowerCase()
                }
                EmbeddingToken(
                    it.range.start,
                    it.range.endInclusive + 1,
                    tokenText
                )
            }
    }
}

/**
 * Detects [TokenCandidate] labels using [Tokenizer].
 */
class DetectTokenCandidates : DocumentTask {
    override fun run(document: Document) {
        val labeler = document.labeler<TokenCandidate>()
        Tokenizer.tokenize(document.text)
            .forEach { labeler.add(TokenCandidate(it.startIndex, it.endIndex)) }
    }
}

/**
 * Turns [TokenCandidate] labels into [ParseToken] labels by using
 * [edu.umn.biomedicus.sentences.Sentence] labels to split trailing periods.
 */
class DetectParseTokens : DocumentTask {
    override fun run(document: Document) {
        val candidates = document.labelIndex<TokenCandidate>()
        val labeler = document.labeler<ParseToken>()
        val text = document.text
        document.sentences().forEach {
            val insideSentence = candidates.inside(it).asList()
            val lastOrNull = insideSentence.lastOrNull()
            insideSentence.forEach { candidate ->
                if (lastOrNull == candidate && candidate.endsWithPeriod(text)) {
                    val notPeriod = Span(candidate.startIndex, candidate.endIndex - 1)
                    val coveredText = notPeriod.coveredString(text)
                    labeler.add(ParseToken(notPeriod, coveredText, false))

                    labeler.add(
                        ParseToken(
                            candidate.endIndex - 1,
                            candidate.endIndex,
                            ".",
                            candidate.hasSpaceAfter(text)
                        )
                    )
                } else {
                    val coveredText = candidate.coveredString(text)
                    val hasSpaceAfter = candidate.hasSpaceAfter(text)
                    labeler.add(ParseToken(candidate, coveredText, hasSpaceAfter))
                }
            }
        }
    }
}
