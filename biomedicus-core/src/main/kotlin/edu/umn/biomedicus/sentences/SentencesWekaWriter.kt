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

import edu.umn.biomedicus.annotations.ComponentSetting
import edu.umn.biomedicus.tokenization.TokenCandidate
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.DocumentsProcessor
import edu.umn.nlpengine.labelIndex
import java.io.File
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class SentencesWekaWriter @Inject constructor(
    @ComponentSetting("outputFile") outputFile: String
) : DocumentsProcessor {
    private val outputWriter = File(outputFile).bufferedWriter(StandardCharsets.UTF_8)

    init {
        outputWriter.appendln("@relation Token")
            .appendln()
            .appendln("@attribute preword string")
            .appendln("@attribute word string")
            .appendln("@attribute postword string")
            .appendln("@attribute class {B,I,O}")
            .appendln()
            .appendln("@data")
    }

    override fun process(document: Document) {
        val artifactID = document.artifactID

        val nameWithoutExtension = File(artifactID).nameWithoutExtension

        val text = document.text

        val sentences = document.labelIndex<Sentence>()
        val tokens = document.labelIndex<TokenCandidate>()

        val sentenceIt = sentences.iterator()
        var sentence = if (sentenceIt.hasNext()) sentenceIt.next() else null

        for (textSegment in document.labelIndex<TextSegment>()) {
            for (token in tokens inside textSegment) {
                val prevEnd = tokens.backwardFrom(token).first()?.endIndex ?: 0
                val nextBegin = tokens.forwardFrom(token).first()?.startIndex ?: text.length

                if (sentence?.contains(token)?.not() == true) {
                    sentence = if (sentenceIt.hasNext()) sentenceIt.next() else null
                }
                if (sentence != null) {
                    val type = when {
                        sentence.sentenceClass == Sentence.unknown -> 'O'
                        token == sentence.let { tokens.inside(it).first() } -> 'B'
                        else -> 'I'
                    }

                    val pre = escape(text.substring(prevEnd, token.startIndex))
                    val word = escape(text.substring(token.startIndex, token.endIndex))
                    val post = escape(text.substring(token.endIndex, nextBegin))
                    outputWriter.appendln("$pre,$word,$post,$type")
                }
            }

        }
    }

    private fun escape(string: String): String {
        return "'${string
            .replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("'", "\\'")
        }'"
    }

    override fun done() {
        outputWriter.close()
    }
}
