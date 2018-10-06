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
import edu.umn.biomedicus.tokenization.EmbeddingToken
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.DocumentTask
import edu.umn.nlpengine.labelIndex
import java.io.File
import javax.inject.Inject

class SentenceDeepLearningWriter @Inject constructor(
    @ComponentSetting("outputDirectory.asPath") val outputDirectory: String
) : DocumentTask {
    override fun run(document: Document) {
        val artifactID = document.artifactID

        val nameWithoutExtension = File(artifactID).nameWithoutExtension

        val text = document.text

        if (text.isBlank()) return

        File(outputDirectory).resolve("$nameWithoutExtension.txt")
            .writeText(text)

        val segments = document.labelIndex<TextSegment>().iterator()
        val sentences = document.labelIndex<Sentence>().iterator()
        val tokens = document.labelIndex<EmbeddingToken>().iterator()


        File(outputDirectory)
            .resolve("$nameWithoutExtension.labels")
            .bufferedWriter()
            .use { writer ->
                if (!segments.hasNext() || !sentences.hasNext()) {
                    return
                }

                var segment = segments.next()
                var segmentIndex = 0
                var sentence = sentences.next()

                var emptySegment = true
                var firstToken = true
                tokens@ while (tokens.hasNext()) {
                    val token = tokens.next()
                    while (!segment.contains(token)) {
                        if (!segments.hasNext()) {
                            break@tokens
                        }
                        if (!emptySegment) {
                            segmentIndex++
                        }
                        segment = segments.next()
                        emptySegment = true
                    }

                    while (!sentence.contains(token)) {
                        if (!sentences.hasNext()) {
                            break@tokens
                        }
                        sentence = sentences.next()
                        firstToken = true
                    }

                    val type = when {
                        sentence.sentenceClass == Sentence.unknown -> 'O'
                        firstToken -> 'B'
                        else -> 'I'
                    }

                    val isIdentifier = if ("IDENTIFIER" == token.text) 1 else 0
                    val coveredText = token.coveredText(text)
                    val startIndex = token.startIndex
                    val endIndex = token.endIndex
                    writer.appendln(
                        "$segmentIndex $startIndex $endIndex $type $isIdentifier $coveredText"
                    )

                    firstToken = false
                    emptySegment = false
                }
            }
    }
}
