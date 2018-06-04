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

@LabelMetadata(versionId = "2_0", distinct = true)
data class Section(
        override val startIndex: Int,
        override val endIndex: Int,
        val kind: String?
) : Label() {
    constructor(textRange: TextRange, kind: String?) : this(textRange.startIndex, textRange.endIndex, kind)
}

@LabelMetadata(versionId = "2_0", distinct = true)
data class SectionHeader(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

@LabelMetadata(versionId = "2_0", distinct = true)
data class SectionContent(override val startIndex: Int, override val endIndex: Int) : Label()

class SectionContentLabeler : DocumentOperation {
    override fun process(document: Document) {
        val sectionHeaders = document.labelIndex<SectionHeader>()
        val sentences = document.labelIndex<Sentence>()

        val sectionLabeler = document.labeler<Section>()
        val contentLabeler = document.labeler<SectionContent>()

        var prev: SectionHeader? = null
        for (sectionHeader in sectionHeaders) {
            if (prev != null) {
                val sectionSentences = sentences.inside(prev.endIndex, sectionHeader.startIndex)
                createSectionFromSentences(sectionSentences, prev, contentLabeler, sectionLabeler)
            }
            prev = sectionHeader
        }

        if (prev != null) {
            val endSentences = sentences.inside(prev.endIndex, document.endIndex)
            createSectionFromSentences(endSentences, prev, contentLabeler, sectionLabeler)
        }
    }

    private fun createSectionFromSentences(
            sectionSentences: LabelIndex<Sentence>,
            prev: SectionHeader,
            contentLabeler: Labeler<SectionContent>,
            sectionLabeler: Labeler<Section>) {
        if (!sectionSentences.isEmpty()) {
            val first = sectionSentences.first()
                    ?: throw IllegalStateException("Non-empty without first")

            val last = sectionSentences.last()
                    ?: throw IllegalStateException("Non-empty without last")

            SectionContent(first.startIndex, last.endIndex).addTo(contentLabeler)

            Section(prev.startIndex, last.endIndex, null).addTo(sectionLabeler)
        }
    }

}