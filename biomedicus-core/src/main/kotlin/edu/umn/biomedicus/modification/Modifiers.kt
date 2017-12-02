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

package edu.umn.biomedicus.modification

import edu.umn.nlpengine.Label
import edu.umn.nlpengine.Span

interface DictionaryTermModifier : Label {
    val cueTerms: List<Span>
}

data class Historical(
        override val startIndex: Int,
        override val endIndex: Int,
        override val cueTerms: List<Span>
) : DictionaryTermModifier {
    constructor(label: Label, cueTerms: List<Span>):
            this(label.startIndex, label.endIndex, cueTerms)
}

data class Negated(
        override val startIndex: Int,
        override val endIndex: Int,
        override val cueTerms: List<Span>
) : DictionaryTermModifier {
    constructor(label: Label, cueTerms: List<Span>):
            this(label.startIndex, label.endIndex, cueTerms)
}

data class Probable(
        override val startIndex: Int,
        override val endIndex: Int,
        override val cueTerms: List<Span>
) : DictionaryTermModifier {
    constructor(label: Label, cueTerms: List<Span>):
            this(label.startIndex, label.endIndex, cueTerms)
}