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

package edu.umn.nlpengine

import java.util.*
import kotlin.reflect.full.findAnnotation

interface Metadata {
    /**
     * Returns the unique artifact identifier. Generally sourced from the relative path to the
     * source file, a primary key/integer unique identifier, or an UUID. It should be a valid
     * relative file path in order for writers to properly function.
     *
     * @return string document identifier
     */
    val artifactID: String

    /**
     * Returns a map of all the metadata for an artifact.
     *
     * @return unmodifiable map of all the metadata for the artifact.
     */
    val metadata: MutableMap<String, String>
}

interface Artifact : Metadata {
    /**
     * [LabeledText] objects that are attached to this document.
     */
    val documents: Map<String, Document>

    /**
     * Creates a new [LabeledText] attached to this document
     */
    fun addDocument(name: String, text: String): Document

    fun copyDocuments(other: Artifact) {
        other.documents.values.forEach(this::copyDocument)
    }

    fun copyDocument(document: Document) {
        val newDocument = addDocument(document.name, document.text)
        newDocument.copyIndices(document)
    }
}

abstract class AbstractArtifact : Artifact

/**
 * A single processing artifact and all the data that it contains. Contains individual text views
 * containing the text associated with a document and all of the label indexes containing labeled
 * types.
 *
 * @since 1.6.0
 */
abstract class Document(
        val name: String,
        val text: String
) : TextRange, Metadata {
    override val startIndex get() = 0

    override val endIndex get() = text.length

    /**
     * Returns the label index for the specific label class.
     *
     * @param labelClass the labelable class instance
     * @param <T> the type of the labelable class
     * @return label index for the labelable type
     */
    abstract fun <T : Label> labelIndex(labelClass: Class<T>): LabelIndex<T>

    inline fun <reified T : Label> labelIndex(): LabelIndex<T> = labelIndex(T::class.java)

    /**
     * Returns a labeler for the specific label class.
     *
     * @param labelClass the labelable class instance
     * @param <T> the type of the labelable class
     * @return labeler for the labelable type
     */
    abstract fun <T : Label> labeler(labelClass: Class<T>): Labeler<T>

    inline fun <reified T : Label> labeler(): Labeler<T> = labeler(T::class.java)

    abstract fun labelIndexes(): Collection<LabelIndex<*>>

    fun <T : Label> copyIndex(labelIndex: LabelIndex<T>) {
        labeler(labelIndex.labelClass).addAll(labelIndex)
    }

    fun copyIndices(document: Document) {
        document.labelIndexes().forEach {
            copyIndex(it)
        }
    }
}

inline fun <reified T : Label> T.addTo(document: Document) {
    document.labeler(T::class.java).add(this)
}

fun <T : Label> T.addTo(labeler: Labeler<T>) {
    labeler.add(this)
}

class StandardArtifact(
        override val artifactID: String
) : Artifact {
    override val metadata = HashMap<String, String>()

    private val _documents = ArrayList<Document>()
    override val documents get() = _documents.associateBy { it.name }

    override fun addDocument(name: String, text: String): Document {
        return StandardDocument(name, text, this).also { _documents.add(it) }
    }
}

internal class StandardDocument(
        name: String,
        text: String,
        private val artifact: StandardArtifact
) : Document(name, text), Metadata by artifact {
    private val indices = ArrayList<StandardLabeler<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Label> labelIndex(labelClass: Class<T>): LabelIndex<T> {
        return indices.firstOrNull { labelClass == it.labelClass }
                ?.index as? LabelIndex<T> ?: StandardLabelIndex(labelClass)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Label> labeler(labelClass: Class<T>): Labeler<T> {
        synchronized(this) {
            var labeler = indices.firstOrNull { it.labelClass == labelClass }
                    ?.let { it as StandardLabeler<T> }

            if (labeler == null) {
                labeler = StandardLabeler(labelClass)

                indices.add(labeler)
            }
            return labeler
        }
    }

    private fun <T : Label> createLabelIndex(labelClass: Class<T>): LabelIndex<T> {
        return if (labelClass.kotlin.findAnnotation<LabelMetadata>()?.distinct
                        ?: throw IllegalStateException("Label without @LabelMetadata annotation")) {
            DistinctLabelIndex(labelClass)
        } else {
            StandardLabelIndex(labelClass)
        }
    }

    override fun labelIndexes(): Collection<LabelIndex<*>> {
        return indices.map { it.index }
    }
}

class StandardLabeler<T : Label>(
        val labelClass: Class<T>,
        private val document: Document?
) : Labeler<T> {
    constructor(labelClass: Class<T>) : this(labelClass, null)

    private var unsorted: ArrayList<T>? = ArrayList()

    val index: LabelIndex<T> by lazy {
        if (labelClass.kotlin.findAnnotation<LabelMetadata>()?.distinct
                        ?: throw IllegalStateException("Label without @LabelMetadata annotation")) {
            DistinctLabelIndex(labelClass, unsorted!!)
        } else {
            StandardLabelIndex(labelClass, unsorted!!)
        }.also { unsorted = null }
    }

    override fun add(label: T) {
        val unsorted = unsorted
                ?: throw IllegalStateException("Index has been accessed and finalized already")
        label.internalLabeledOnDocument = document
        label.internalLabelIdentifier = unsorted.size
        unsorted.add(label)
    }
}