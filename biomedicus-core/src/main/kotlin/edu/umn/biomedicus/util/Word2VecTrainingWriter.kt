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

import edu.umn.biomedicus.annotations.ComponentSetting
import edu.umn.biomedicus.sentences.TextSegment
import edu.umn.biomedicus.tokenization.EmbeddingToken
import edu.umn.biomedicus.tokenization.TokenCandidate
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.DocumentsProcessor
import edu.umn.nlpengine.labelIndex
import java.io.File
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**
 * Pre-processes documents for Word2Vec or FastText by removing all punctuation and writing tokens
 * to a single file.
 */
class Word2VecTrainingWriter @Inject constructor(
    @ComponentSetting("outputFile.orig") outputFile: String
) : DocumentsProcessor {
    private val outputWriter = File(outputFile).bufferedWriter(StandardCharsets.UTF_8)

    override fun process(document: Document) {
        val segments = document.labelIndex<TextSegment>()
        val tokens = document.labelIndex<EmbeddingToken>()

        for (segment in segments) {
            for (token in tokens.inside(segment)) {
                if (token.text.isNotBlank()) {
                    outputWriter.append("${token.text} ")
                }
            }

            outputWriter.newLine()
        }
    }

    override fun done() {
        outputWriter.close()
    }
}
