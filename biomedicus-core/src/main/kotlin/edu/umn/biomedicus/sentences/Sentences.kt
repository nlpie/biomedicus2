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

package edu.umn.biomedicus.sentences

import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.*

class SentencesModule : SystemModule() {
    override fun setup() {
        addLabelClass<Sentence>()
        addLabelClass<TextSegment>()
    }
}

/**
 * A unit of language of multiple words making up a complete thought.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class Sentence(
        override val startIndex: Int,
        override val endIndex: Int,
        val sentenceClass: Int
) : Label() {
    constructor(
            textRange: TextRange,
            sentenceClass: Int
    ) : this(textRange.startIndex, textRange.endIndex, sentenceClass)

    constructor(startIndex: Int, endIndex: Int): this(startIndex, endIndex, 1)

    constructor(textRange: TextRange): this(textRange, 1)

    /**
     * Retrieves a label index of all the [ParseToken] labels inside of this sentence.
     */
    fun tokens() : LabelIndex<ParseToken> {
        return document?.labelIndex<ParseToken>()?.inside(this)
                ?: throw IllegalStateException("This sentence has not been added to a document.")
    }

    companion object Classes {
        const val unknown = 0
        const val sentence = 1
    }
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class TextSegment(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}