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
import java.nio.ByteBuffer

class ConceptModule : SystemModule() {
    override fun setup() {
        addLabelClass<UmlsConcept>()
        addLabelClass<DictionaryTerm>()
    }
}

/**
 * A discrete concept / idea in some kind of taxonomy.
 */
interface Concept {
    /**
     * A unique identifier for the concept.
     */
    val identifier: String

    /**
     * The source dictionary / taxonomy.
     */
    val source: String

    /**
     * A semantic type or grouping identifier for the concept.
     */
    val semanticType: String

    /**
     * A relative confidence value of how likely the concept represents an actual concept.
     */
    val confidence: Double
}

/**
 * A dictionary concept - a standardized code for the idea the text represents.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = false)
data class UmlsConcept(
        override val startIndex: Int,
        override val endIndex: Int,
        val sui: String,
        val cui: String,
        val tui: String,
        override val source: String,
        override val confidence: Double
) : Label(), Concept {
    constructor(
            textRange: TextRange,
            sui: String,
            cui: String,
            tui: String,
            source: String,
            confidence: Double
    ) : this(textRange.startIndex, textRange.endIndex, sui, cui, tui, source, confidence)

    override val identifier: String
        get() = cui

    override val semanticType: String
        get() = tui
}

/**
 * A dictionary term - a span of text that has one or more dictionary concepts associated with it.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = false)
data class DictionaryTerm(
        override val startIndex: Int,
        override val endIndex: Int
) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * Representative form of concept for storage.
 * @property sui the concept's string unique identifier
 * @property cui the concept's
 */
data class ConceptRow(
        val sui: SUI,
        val cui: CUI,
        val tui: TUI,
        val source: Int
) {
    /**
     * Serializes the ConceptRow to bytes.
     */
    val bytes: ByteArray get() {
        return ByteBuffer.allocate(16 )
                .putInt(sui.identifier())
                .putInt(cui.identifier())
                .putInt(tui.identifier())
                .putInt(source)
                .array()
    }

    companion object Factory {
        @JvmField
        val NUM_BYTES = 16

        @JvmStatic
        fun next(buffer: ByteBuffer): ConceptRow {
            return ConceptRow(
                    sui = SUI(buffer.int),
                    cui = CUI(buffer.int),
                    tui = TUI(buffer.int),
                    source = buffer.int
            )
        }
    }
}
