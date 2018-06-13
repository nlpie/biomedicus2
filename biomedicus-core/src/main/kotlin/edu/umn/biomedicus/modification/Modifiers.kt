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

package edu.umn.biomedicus.modification

import edu.umn.nlpengine.Label
import edu.umn.nlpengine.LabelMetadata
import edu.umn.nlpengine.SystemModule
import edu.umn.nlpengine.TextRange

class ModifiersModule : SystemModule() {
    override fun setup() {
        addLabelClass<ModificationCue>()
        addLabelClass<Historical>()
        addLabelClass<Negated>()
        addLabelClass<Probable>()
    }
}

interface DictionaryTermModifier : TextRange {
    val cueTerms: List<ModificationCue>
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class ModificationCue(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange): this(textRange.startIndex, textRange.endIndex)
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class Historical(
        override val startIndex: Int,
        override val endIndex: Int,
        override val cueTerms: List<ModificationCue>
) : Label(), DictionaryTermModifier {
    constructor(textRange: TextRange, cueTerms: List<ModificationCue>):
            this(textRange.startIndex, textRange.endIndex, cueTerms)
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class Negated(
        override val startIndex: Int,
        override val endIndex: Int,
        override val cueTerms: List<ModificationCue>
) : Label(), DictionaryTermModifier {
    constructor(textRange: TextRange, cueTerms: List<ModificationCue>):
            this(textRange.startIndex, textRange.endIndex, cueTerms)
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class Probable(
        override val startIndex: Int,
        override val endIndex: Int,
        override val cueTerms: List<ModificationCue>
) : Label(), DictionaryTermModifier {
    constructor(textRange: TextRange, cueTerms: List<ModificationCue>):
            this(textRange.startIndex, textRange.endIndex, cueTerms)
}