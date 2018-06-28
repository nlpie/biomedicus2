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

import edu.umn.biomedicus.common.utilities.Patterns
import edu.umn.biomedicus.structure.Paragraph
import edu.umn.nlpengine.*

/**
 * Segments text according to rules
 */
val regex = Regex("\n\n")

class SegmentText : DocumentTask {
    override fun run(document: Document) {
        val breaks = HashSet<Int>()

        val text = document.text

        regex.findAll(text).forEach { breaks.add(it.range.endInclusive + 1) }

        document.labelIndex<Paragraph>().forEach { breaks.add(it.startIndex) }

        val labeler = document.labeler<TextSegment>()

        var prev = 0
        for (segmentBreak in breaks.sorted()) {
            if (segmentBreak == prev) continue

            val span = Span(prev, segmentBreak)

            if (Patterns.NON_WHITESPACE.matcher(span.coveredText(text)).find()) {
                labeler.add(TextSegment(span))
            }

            prev = segmentBreak
        }

        if (prev != text.length) {
            val span = Span(prev, text.length)

            if (Patterns.NON_WHITESPACE.matcher(span.coveredText(text)).find()) {
                labeler.add(TextSegment(span))
            }
        }
    }
}
