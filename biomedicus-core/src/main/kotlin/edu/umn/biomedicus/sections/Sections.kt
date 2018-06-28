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

package edu.umn.biomedicus.sections

import edu.umn.biomedicus.sentences.Sentence
import edu.umn.nlpengine.*

class SectionsModule : SystemModule() {
    override fun setup() {
        addLabelClass<Section>()
        addLabelClass<SectionHeader>()
        addLabelClass<SectionContent>()
    }
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class Section(
        override val startIndex: Int,
        override val endIndex: Int,
        val kind: String?
) : Label() {
    constructor(textRange: TextRange, kind: String?) : this(textRange.startIndex, textRange.endIndex, kind)
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class SectionHeader(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class SectionContent(override val startIndex: Int, override val endIndex: Int) : Label()

class SectionContentLabeler : DocumentsProcessor {
    override fun process(document: Document) {
        val sentences = document.labelIndex<Sentence>()

        val sectionLabeler = document.labeler<Section>()
        val contentLabeler = document.labeler<SectionContent>()

        val create = { sectionStart: Int, contentStart: Int, end: Int ->
            val sectionSentences = sentences.inside(contentStart, end)
            if (!sectionSentences.isEmpty()) {
                val first = sectionSentences.first()
                        ?: throw IllegalStateException("Non-empty without first")

                val last = sectionSentences.last()
                        ?: throw IllegalStateException("Non-empty without last")

                SectionContent(first.startIndex, last.endIndex).addTo(contentLabeler)

                Section(sectionStart, last.endIndex, null).addTo(sectionLabeler)
            }
        }

        document.labelIndex<SectionHeader>().fold(null) { prev: SectionHeader?, current ->
            if (prev != null) {
                create(prev.startIndex, prev.endIndex, current.startIndex)
            }
            current
        }?.let { create(it.startIndex, it.endIndex, document.endIndex) }
    }
}
