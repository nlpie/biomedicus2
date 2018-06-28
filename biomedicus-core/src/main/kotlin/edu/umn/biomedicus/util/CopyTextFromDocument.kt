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
import edu.umn.nlpengine.Artifact
import edu.umn.nlpengine.ArtifactTask
import javax.inject.Inject

/**
 * Copies the text from the documents named [sourceDocumentName] to new documents named
 * [targetDocumentName].
 *
 * @param sourceDocumentName The name of the document to take text from.
 * @param targetDocumentName The name of the document to create with text.
 */
class CopyTextFromDocument @Inject internal constructor(
        @ComponentSetting("sourceDocumentName") private val sourceDocumentName: String,
        @ComponentSetting("targetDocumentName") private val targetDocumentName: String
) : ArtifactTask {
    override fun run(artifact: Artifact) {
        val text = artifact.documents[sourceDocumentName]?.text
                ?: error("No existing document with name $sourceDocumentName")
        artifact.addDocument(targetDocumentName, text)
    }
}
