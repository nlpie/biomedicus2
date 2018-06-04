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

package edu.umn.biomedicus.util

import edu.umn.biomedicus.annotations.ProcessorSetting
import edu.umn.nlpengine.ArtifactProcessor
import edu.umn.nlpengine.Artifact
import java.io.File
import javax.inject.Inject


class UniqueCharactersProcessor @Inject internal constructor(
        @ProcessorSetting("outputFile") private val outputFile: String,
        @ProcessorSetting("documentName") private val documentName: String
) : ArtifactProcessor {
    private val chars = HashSet<Char>()

    override fun process(artifact: Artifact) {
        (artifact.documents[documentName] ?: error("No document with name $documentName"))
                .text.chars().forEach { chars.add(it.toChar()) }
    }

    override fun done() {
        val file = File(outputFile)
        file.parentFile.mkdirs()
        file.writeText(chars.joinToString("", postfix = "\n"))
    }
}
