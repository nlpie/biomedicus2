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

package edu.umn.biomedicus.tokenization

import edu.umn.biomedicus.annotations.ProcessorSetting
import edu.umn.biomedicus.sentences.TextSegment
import edu.umn.nlpengine.ArtifactProcessor
import edu.umn.nlpengine.Artifact
import edu.umn.nlpengine.labelIndex
import javax.inject.Inject

/**
 * Performs pre-processing on MIMIC tokens before writing them for FastText vector creation.
 */
class MimicFastTextPreprocessor @Inject constructor(
        @ProcessorSetting("documentName") private val documentName: String,
        @ProcessorSetting("outputFile") private val outputFile: String
) : ArtifactProcessor {

    override fun process(artifact: Artifact) {
        val document = artifact.documents[documentName]
                ?: error("No document with name $documentName")

        val tokens = document.labelIndex<ParseToken>()

        for (segment in document.labelIndex<TextSegment>()) {
            for (token in tokens.inside(segment)) {

            }
        }
    }

    override fun done() {

    }
}

val trailingPeriod = Regex("\\.$")
val lowercaseLetter = Regex("[^\\p{Lower}]")

fun doPreProcessText(text: String): String {
    return text.toLowerCase() // lowercase
            .replace("1", "one")
            .replace("2", "two")
            .replace("3", "three")
            .replace("4", "four")
            .replace("5", "five")
            .replace("6", "six")
            .replace("7", "seven")
            .replace("8", "eight")
            .replace("9", "nine")
            .replace("0", "zero")
            .replace(lowercaseLetter, "")
}