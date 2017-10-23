/*
 * Copyright (c) 2017 Regents of the University of Minnesota - All Rights Reserved
 * Unauthorized Copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package edu.umn.biomedicus.io

import edu.umn.biomedicus.annotations.ProcessorSetting
import edu.umn.biomedicus.common.StandardViews
import edu.umn.biomedicus.common.types.text.Sentence
import edu.umn.biomedicus.framework.DocumentProcessor
import edu.umn.biomedicus.framework.store.Document
import gate.Factory
import gate.Gate
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

class GateSentencesWriter @Inject internal constructor(
        @ProcessorSetting("outputDirectory") private val outputDirectory: Path
): DocumentProcessor {

    init {
        Gate.init()
    }


    override fun process(document: Document) {
        val documentId = document.documentId
        val systemView = StandardViews.getSystemView(document)

        val text = systemView.text
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

        val annotationSet = gateDocument.getAnnotations()

        for (sentenceLabel in systemView.getLabelIndex(Sentence::class.java)) {
            annotationSet.add(
                    sentenceLabel.begin.toLong(),
                    sentenceLabel.end.toLong(),
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
}