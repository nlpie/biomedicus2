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
import edu.umn.biomedicus.io.TextFilesArtifactSource
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.DocumentProcessor
import edu.umn.nlpengine.Span
import java.io.File
import javax.inject.Inject

/**
 * Reads in sentences from the brat ann files and adds them as sentences.
 */
class BratSentencesReader internal @Inject constructor(
        @ProcessorSetting("labelUnsure") private val labelUnsure: Boolean
): DocumentProcessor {
    override fun process(document: Document) {
        val sourceFile = File(document.metadata[TextFilesArtifactSource.SOURCE_PATH]
                ?: error("Document does not have source path"))

        val annFile = sourceFile.resolveSibling(sourceFile.nameWithoutExtension + ".ann")

        val labeler = document.labeler<Sentence>()

        annFile.useLines {
            it.map { it.split("\t") }.map { it[1] }.forEach sentenceLoop@{
                val space = it.indexOf(' ')
                val type = it.substring(0, space)
                val segments = it.substring(space + 1).split(";")
                        .map {
                            val indexes = it.split(" ").map { it.toInt() }
                            Span(indexes[0], indexes[1])
                        }
                val startIndex = segments.map { it.startIndex }.min() ?: error("No min start index")
                val endIndex = segments.map { it.endIndex }.max() ?: error("No max end index")

                if (type == "Unsure" && !labelUnsure) {
                    return@sentenceLoop
                }

                labeler.add(Sentence(startIndex, endIndex, if (type == "Sentence") 1 else 0))
            }
        }
    }
}