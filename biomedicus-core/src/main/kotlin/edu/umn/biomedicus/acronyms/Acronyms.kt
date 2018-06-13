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

package edu.umn.biomedicus.acronyms

import edu.umn.biomedicus.tokenization.Token
import edu.umn.nlpengine.Label
import edu.umn.nlpengine.LabelMetadata
import edu.umn.nlpengine.SystemModule
import edu.umn.nlpengine.TextRange

class AcronymModule : SystemModule() {
    override fun setup() {
        addLabelClass<Acronym>()
        addLabelClass<OtherAcronymSense>()
    }
}

/**
 * An acronym or abbreviation in text.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class Acronym(
        override val startIndex: Int,
        override val endIndex: Int,
        override val text: String,
        override val hasSpaceAfter: Boolean,
        val score: Double
): Label(), Token {
    constructor(
            textRange: TextRange,
            text: String,
            hasSpaceAfter: Boolean,
            score: Double
    ): this(textRange.startIndex, textRange.endIndex, text, hasSpaceAfter, score)
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = false)
data class OtherAcronymSense(
        override val startIndex: Int,
        override val endIndex: Int,
        override val text: String,
        override val hasSpaceAfter: Boolean,
        val score: Double
): Label(), Token {
    constructor(
            textRange: TextRange,
            text: String,
            hasSpaceAfter: Boolean,
            score: Double
    ): this(textRange.startIndex, textRange.endIndex, text, hasSpaceAfter, score)
}

internal data class ScoredSense(val sense: String, val score: Double)