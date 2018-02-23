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
import edu.umn.biomedicus.tagging.PosTag
import edu.umn.nlpengine.Label
import edu.umn.nlpengine.LabelMetadata
import edu.umn.nlpengine.SystemModule
import edu.umn.nlpengine.TextRange

class TokenizationModule : SystemModule() {
    override fun setup() {
        addLabelClass<TermToken>()
        addLabelClass<ParseToken>()
        addLabelClass<TokenCandidate>()
        addLabelClass<WordIndex>()
    }

}

interface Token : TextRange {
    val text: String
    val hasSpaceAfter: Boolean
}

@LabelMetadata(versionId = "2_0", distinct = true)
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

@LabelMetadata(versionId = "2_0", distinct = true)
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
        get() = document?.labelIndex<PosTag>()?.firstAtLocation(this)?.partOfSpeech
                ?: throw IllegalStateException("ParseToken not added to document or doesn't have a part of speech yet: $this")
}

@LabelMetadata(versionId = "2_0", distinct = true)
data class TokenCandidate(
        override val startIndex: Int,
        override val endIndex: Int,
        val isLast: Boolean
) : Label() {
    constructor(textRange: TextRange, isLast: Boolean) : this(textRange.startIndex, textRange.endIndex, isLast)
}

@LabelMetadata(versionId = "2_0", distinct = true)
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