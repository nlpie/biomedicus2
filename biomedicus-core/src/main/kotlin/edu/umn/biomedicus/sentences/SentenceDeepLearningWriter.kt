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
import edu.umn.nlpengine.DocumentProcessor
import edu.umn.nlpengine.labelIndex
import java.io.File
import javax.inject.Inject

class SentenceDeepLearningWriter @Inject constructor(
        @ProcessorSetting("outputDirectory") val outputDirectory: String
) : DocumentProcessor {
    override fun process(document: Document) {
        val artifactID = document.artifactID

        val nameWithoutExtension = File(artifactID).nameWithoutExtension

        val text = document.text
        File(outputDirectory).resolve("$nameWithoutExtension.txt").writeText(text)

        val tokens = document.labelIndex<TokenCandidate>()

        File(outputDirectory)
                .resolve("$nameWithoutExtension.labels")
                .bufferedWriter()
                .use {

                    for (sentence in document.labelIndex<Sentence>()) {
                        if (sentence.sentenceClass == Sentence.unknown) {
                            for ((startIndex, endIndex) in tokens inside sentence)
                                it.appendln("$startIndex $endIndex O ${text.subSequence(startIndex, endIndex)}")
                        } else {
                            val tokenIt = tokens.inside(sentence).iterator()
                            if (tokenIt.hasNext()) {
                                val (startIndex, endIndex) = tokenIt.next()
                                it.appendln("$startIndex $endIndex B ${text.subSequence(startIndex, endIndex)}")
                            }
                            while (tokenIt.hasNext()) {
                                val (startIndex, endIndex) = tokenIt.next()
                                it.appendln("$startIndex $endIndex I ${text.subSequence(startIndex, endIndex)}")
                            }
                        }
                    }
                }
    }
}
