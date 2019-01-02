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

import edu.umn.biomedicus.annotations.ComponentSetting
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.DocumentTask
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

/**
 * Writes the contents of a view to a directory.
 */
class PlainTextWriter @Inject constructor(
        @ComponentSetting("outputDirectory.asPath") private val outputDirectory: Path,
        @ComponentSetting("charset") private val charsetName: String
) : DocumentTask {
    init {
        log.debug("Writing plain text files to directory: {}", outputDirectory)
    }

    override fun run(document: Document) {
        outputDirectory.resolve("${document.artifactID}.txt")
                .also { Files.createDirectories(it.parent) }
                .toFile().writeText(document.text, Charset.forName(charsetName))

    }

    companion object {
        private val log = LoggerFactory.getLogger(PlainTextWriter::class.java)
    }
}
