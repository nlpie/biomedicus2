/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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
import edu.umn.nlpengine.Label

interface Token : Label {
    val text: String
    val hasSpaceAfter: Boolean
}

data class TermToken(
        override val startIndex: Int,
        override val endIndex: Int,
        override val text: String,
        override val hasSpaceAfter: Boolean
) : Token {
    constructor(
            label: Label,
            text: String,
            hasSpaceAfter: Boolean
    ) : this(label.startIndex, label.endIndex, text, hasSpaceAfter)
}

data class ParseToken(
        override val startIndex: Int,
        override val endIndex: Int,
        override val text: String,
        override val hasSpaceAfter: Boolean
) : Token {
    constructor(
            label: Label,
            text: String,
            hasSpaceAfter: Boolean
    ) : this(label.startIndex, label.endIndex, text, hasSpaceAfter)
}

data class TokenCandidate(
        override val startIndex: Int,
        override val endIndex: Int,
        val isLast: Boolean
) : Label {
    constructor(label: Label, isLast: Boolean) : this(label.startIndex, label.endIndex, isLast)
}

data class WordIndex(
        override val startIndex: Int,
        override val endIndex: Int,
        val stringIdentifier: StringIdentifier
) : Label {
    constructor(
            label: Label,
            stringIdentifier: StringIdentifier
    ) : this(label.startIndex, label.endIndex, stringIdentifier)
}