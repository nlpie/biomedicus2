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

package edu.umn.biomedicus.concepts

import edu.umn.nlpengine.Label
import edu.umn.nlpengine.LabelMetadata
import edu.umn.nlpengine.SystemModule
import edu.umn.nlpengine.TextRange

class ConceptModule : SystemModule() {
    override fun setup() {
        addLabelClass<DictionaryConcept>()
        addLabelClass<DictionaryTerm>()
    }
}

interface Concept {
    val identifier: String

    val source: String

    val semanticType: String

    val confidence: Double
}

/**
 * A dictionary concept - a standardized code for the idea the text represents.
 */
@LabelMetadata(versionId = "2_0", distinct = false)
data class DictionaryConcept(
        override val startIndex: Int,
        override val endIndex: Int,
        override val identifier: String,
        override val source: String,
        override val semanticType: String,
        override val confidence: Double
) : Label(), Concept {
    constructor(
            textRange: TextRange,
            identifier: String,
            source: String,
            type: String,
            confidence: Double
    ): this(textRange.startIndex, textRange.endIndex, identifier, source, type, confidence)
}

/**
 * A dictionary term - a span of text that has one or more dictionary concepts associated with it.
 */
@LabelMetadata(versionId = "2_0", distinct = false)
data class DictionaryTerm(
        override val startIndex: Int,
        override val endIndex: Int
) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}
