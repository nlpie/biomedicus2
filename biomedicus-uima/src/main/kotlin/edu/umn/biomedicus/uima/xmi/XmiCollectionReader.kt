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

package edu.umn.biomedicus.uima.xmi

import edu.umn.biomedicus.uima.adapter.UimaAdapters
import org.apache.uima.cas.CAS
import org.apache.uima.cas.impl.XmiCasDeserializer
import org.apache.uima.collection.CollectionReader_ImplBase
import org.apache.uima.util.Progress
import org.apache.uima.util.ProgressImpl
import java.io.File

class XmiCollectionReader : CollectionReader_ImplBase() {

    var failOnUnknownType: Boolean = true

    var completed: Int = 0

    var total: Int = 0

    var iterator: Iterator<File>? = null

    var addDocumentId: Boolean = false

    override fun initialize() {
        uimaContext.getConfigParameterValue("failOnUnknownType")
                .let { it as? Boolean }
                ?.let { failOnUnknownType = it }

        val inputDirectory = uimaContext
                .getConfigParameterValue("inputDirectory") as? String
                ?: throw RuntimeException("Input directory not set")

        val recurseDepth = uimaContext.getConfigParameterValue("recurseDepth") as? Int ?: 1

        val extension = uimaContext
                .getConfigParameterValue("extension") as? String ?: "xmi"

        uimaContext.getConfigParameterValue("")
                .let { it as? Boolean }
                ?.let { addDocumentId = it }

        total = File(inputDirectory).walkTopDown().maxDepth(recurseDepth)
                .count { it.extension == extension }
        iterator = File(inputDirectory).walkTopDown().maxDepth(recurseDepth)
                .filter { it.extension == extension }
                .iterator()
    }

    override fun getProgress(): Array<Progress> {
        return arrayOf(ProgressImpl(completed, total, Progress.ENTITIES))
    }

    override fun hasNext(): Boolean {
        return iterator?.hasNext() ?: throw IllegalStateException("Initialize not run")
    }

    override fun close() {

    }

    override fun getNext(aCAS: CAS?) {
        (iterator ?: throw IllegalStateException("Initialize not run"))
                .next()
                .let { file ->
                    file.inputStream().use {
                        XmiCasDeserializer.deserialize(it, aCAS, !failOnUnknownType)

                        if (addDocumentId) {
                            UimaAdapters.createArtifact(
                                    aCAS,
                                    null,
                                    file.nameWithoutExtension
                            )
                        }
                    }
                }
    }
}