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
import edu.umn.biomedicus.exc.BiomedicusException
import edu.umn.biomedicus.framework.DocumentProcessor
import edu.umn.nlpengine.Document
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

/**
 * Writes the contents of a view to a directory.
 */
class PlainTextWriter @Inject constructor(
        @ProcessorSetting("viewName") val viewName: String,
        @ProcessorSetting("outputDirectory") val outputDirectory: Path,
        @ProcessorSetting("charset") val charsetName: String
) : DocumentProcessor {
    override fun process(document: Document) {
        val text = document.labeledTexts[viewName]
                ?: throw BiomedicusException("No labeledText with key: " + viewName)

        outputDirectory.resolve("${document.documentId}.txt")
                .also { Files.createDirectories(it.parent) }
                .toFile().writeText(text.text, Charset.forName(charsetName))

    }
}