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

import edu.umn.biomedicus.annotations.ProcessorSetting
import edu.umn.biomedicus.tokenization.TokenCandidate
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.DocumentOperation
import edu.umn.nlpengine.labelIndex
import java.io.File
import javax.inject.Inject

class SentenceDeepLearningWriter @Inject constructor(
        @ProcessorSetting("outputDirectory") val outputDirectory: String
) : DocumentOperation {
    override fun process(document: Document) {
        val artifactID = document.artifactID

        val nameWithoutExtension = File(artifactID).nameWithoutExtension

        val text = document.text

        val sentences = document.labelIndex<Sentence>()
        val tokens = document.labelIndex<TokenCandidate>()

        File(outputDirectory).resolve("$nameWithoutExtension.txt")
                .writeText(text)

        val sentenceIt = sentences.iterator()
        var sentence = if (sentenceIt.hasNext()) sentenceIt.next() else null

        for ((count, textSegment) in document.labelIndex<TextSegment>().withIndex()) {
            File(outputDirectory)
                    .resolve("$nameWithoutExtension-$count.labels")
                    .bufferedWriter()
                    .use {
                        for (token in tokens inside textSegment) {
                            val prevEnd
                                    = tokens.backwardFrom(token).first()?.endIndex ?: 0
                            val nextBegin
                                    = tokens.forwardFrom(token).first()?.startIndex ?: text.length

                            if (sentence?.contains(token)?.not() == true) {
                                sentence = if (sentenceIt.hasNext()) sentenceIt.next() else null
                            }
                            if (sentence != null) {
                                val type = when {
                                    sentence?.sentenceClass == Sentence.unknown -> 'O'
                                    token == sentence?.let { tokens.inside(it).first() } -> 'B'
                                    else -> 'I'
                                }

                                it.appendln("$prevEnd ${token.startIndex} ${token.endIndex} $nextBegin $type ${token.coveredText(text)}")
                            }
                        }
                    }

        }


    }
}
