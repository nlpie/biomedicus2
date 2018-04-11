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

import edu.umn.biomedicus.annotations.ProcessorScoped
import edu.umn.biomedicus.annotations.ProcessorSetting
import edu.umn.biomedicus.sentences
import edu.umn.nlpengine.Artifact
import edu.umn.nlpengine.ArtifactProcessor
import java.io.File
import javax.inject.Inject

/**
 * Evaluates sentence segmentation performance using simple accuracy counting the number of
 * sentences from the gold standard document that are present in the evaluation document and are not
 * present in the evaluation document.
 */
class SentencesEvaluator @Inject internal constructor(
        @ProcessorSetting("evaluatedDocument") private val evaluatedDocumentName: String,
        @ProcessorSetting("goldDocument") private val goldDocumentName: String,
        private val sentencesEvaluationWriter: SentencesEvaluationWriter
) : ArtifactProcessor {

    override fun process(artifact: Artifact) {
        val goldDocument = artifact.documents[goldDocumentName]
                ?: error("No gold document: $goldDocumentName")
        val evaluatedDocument = artifact.documents[evaluatedDocumentName]
                ?: error("No evaluated document: $evaluatedDocumentName")

        var hits = 0
        var misses = 0

        val evaluatedSentences = evaluatedDocument.sentences()
        for (sentence in goldDocument.sentences()) {
            if (sentence.sentenceClass == 0) continue
            if (evaluatedSentences.contains(sentence)) {
                hits++
            } else {
                misses++
            }
        }

        sentencesEvaluationWriter.write("$hits $misses\n")
    }
}

@ProcessorScoped
internal class SentencesEvaluationWriter(private val outputFile: File) {
    @Inject internal constructor(
            @ProcessorSetting("outputFile") outputFile: String
    ) : this(File(outputFile))

    private val lock = Object()

    fun write(text: String) {
        synchronized(lock) {
            outputFile.appendText(text)
        }
    }
}
