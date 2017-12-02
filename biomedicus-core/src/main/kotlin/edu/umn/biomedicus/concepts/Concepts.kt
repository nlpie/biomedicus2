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

package edu.umn.biomedicus.concepts

import edu.umn.nlpengine.Label

interface Concept {
    val identifier: String

    val source: String

    val type: String

    val confidence: Double
}

data class DictionaryConcept(
        override val startIndex: Int,
        override val endIndex: Int,
        override val identifier: String,
        override val source: String,
        override val type: String,
        override val confidence: Double
) : Label, Concept {
    constructor(label: Label, identifier: String, source: String, type: String, confidence: Double):
            this(label.startIndex, label.endIndex, identifier, source, type, confidence)
}

data class DictionaryTerm(
        override val startIndex: Int,
        override val endIndex: Int,
        val concepts: List<DictionaryConcept>
) : Label {
    constructor(label: Label, concepts: List<DictionaryConcept>) :
            this(label.startIndex, label.endIndex, concepts)
}
