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

package edu.umn.biomedicus.io

import edu.umn.biomedicus.annotations.ProcessorSetting
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.DocumentProcessor
import gate.Factory
import gate.Gate
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

class GateSentencesWriter @Inject internal constructor(
        @ProcessorSetting("outputDirectory") private val outputDirectory: Path
) : DocumentProcessor {
    override fun process(document: Document) {
        val documentId = document.artifactID

        val text = document.text
        val textPath = outputDirectory.resolve("txt").resolve("" + documentId[0])
                .resolve(documentId + ".txt")
        Files.createDirectories(textPath.parent)
        textPath.toFile().writeText(text, StandardCharsets.UTF_8)

        val params = Factory.newFeatureMap()
        params.put("sourceUrl", textPath.toUri().toURL())
        params.put("mimeType", "text/plain")

        val gateDocument = Factory.createResource(
                "gate.corpora.DocumentImpl",
                params
        ) as gate.Document

        val annotationSet = gateDocument.annotations

        for (sentenceLabel in document.labelIndex(Sentence::class.java)) {
            annotationSet.add(
                    sentenceLabel.startIndex.toLong(),
                    sentenceLabel.endIndex.toLong(),
                    "Sentence",
                    Factory.newFeatureMap()
            )
        }

        val xmlPath = outputDirectory.resolve("xml")
                .resolve("${documentId[0]}")
                .resolve(documentId + ".xml")


        Files.createDirectories(xmlPath.parent)

        xmlPath.toFile().writeText(gateDocument.toXml(), StandardCharsets.UTF_8)
    }

    companion object {
        init {
            Gate.init()
        }
    }
}