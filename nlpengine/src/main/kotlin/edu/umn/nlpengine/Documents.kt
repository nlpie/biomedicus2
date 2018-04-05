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

/**
 * Metadata associated with an [Artifact]. All [Document] objects on the artifact share the same
 * metadata.
 */
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

/**
 * A single processing artifact, containing a number of [documents] of text and their associated
 * labels.
 */
interface Artifact : Metadata {
    /**
     * The [Document] objects that are attached to this artifact.
     */
    val documents: Map<String, Document>

    /**
     * Creates a new [Document] attached to this artifact keyed with the given [name] and containing
     * [text]
     */
    fun addDocument(name: String, text: String): Document

    /**
     * Copies all documents from [other] to this artifact
     */
    fun copyDocuments(other: Artifact) {
        other.documents.values.forEach(this::copyDocument)
    }

    /**
     * Copies [document] to this artifact.
     */
    fun copyDocument(document: Document) {
        val newDocument = addDocument(document.name, document.text)
        newDocument.copyIndices(document)
    }

    /**
     * Copies [document] to this artifact with the specified name.
     */
    fun copyDocument(document: Document, name: String) {
        val newDocument = addDocument(name, document.text)
        newDocument.copyIndices(newDocument)
    }
}

/**
 * An abstract subclass of the [Artifact] interface, used for implementing Artifact in java.
 */
abstract class AbstractArtifact : Artifact

/**
 * A text document and its associated label indices. Implements [TextRange] as the range of the
 * document. Implements [Metadata] with the artifact metadata.
 *
 * @property name The name of the document, a key that identifies documents of a specific type on
 * the artifact
 * @property text The text of the document
 * @constructor Creates a Document with no labels added.
 */
interface Document : TextRange, Metadata {
    val name: String

    val text: String

    override val startIndex get() = 0

    override val endIndex get() = text.length

    /**
     * Returns the label index for the specific label class.
     *
     * @param labelClass the labelable class instance
     * @param <T> the type of the labelable class
     * @return label index for the labelable type
     */
    fun <T : Label> labelIndex(labelClass: Class<T>): LabelIndex<T>

    /**
     * Returns a labeler for the specific label class.
     *
     * @param labelClass the labelable class instance
     * @param <T> the type of the labelable class
     * @return labeler for the labelable type
     */
    fun <T : Label> labeler(labelClass: Class<T>): Labeler<T>

    /**
     * Returns all the label indices in this document.
     */
    fun labelIndexes(): Collection<LabelIndex<*>>

    /**
     * Copies [labelIndex] to this document.
     */
    fun <T : Label> copyIndex(labelIndex: LabelIndex<T>) {
        labeler(labelIndex.labelClass).addAll(labelIndex)
    }

    /**
     * Copies all the label indices from [document] to this document.
     */
    fun copyIndices(document: Document) {
        document.labelIndexes().forEach {
            copyIndex(it)
        }
    }
}

abstract class AbstractDocument(
        override val name: String,
        override val text: String
) : Document

/**
 * Returns the label index of the class specified in the type parameter [T]
 */
inline fun <reified T : Label> Document.labelIndex(): LabelIndex<T> = labelIndex(T::class.java)

/**
 * Returns the labeler for the class specified in the type parameter [T]
 */
inline fun <reified T : Label> Document.labeler(): Labeler<T> = labeler(T::class.java)

/**
 * Adds everything in the collection [labels] to this document.
 */
inline fun <reified T: Label> Document.labelAll(labels: Iterable<T>) = labeler(T::class.java).addAll(labels)

/**
 * Adds the label to the [document] by first retrieving the appropriate labeler.
 */
inline fun <reified T : Label> T.addTo(document: Document) {
    document.labeler(T::class.java).add(this)
}

/**
 * Adds the label to the [labeler].
 */
fun <T : Label> T.addTo(labeler: Labeler<T>): T {
    return apply { labeler.add(this) }
}

/**
 * A standard implementation of [Artifact].
 */
data class StandardArtifact(
        override val artifactID: String
) : Artifact {
    override val metadata = HashMap<String, String>()

    private val _documents = ArrayList<Document>()
    override val documents get() = _documents.associateBy { it.name }

    override fun addDocument(name: String, text: String): Document {
        if (_documents.any { it.name == name }) error("Existing document with name: $name")
        return StandardDocument(name, text, this).also { _documents.add(it) }
    }
}

internal data class StandardDocument(
        override val name: String,
        override val text: String,
        private val artifact: StandardArtifact
) : Document, Metadata by artifact {
    private val indices = ArrayList<StandardLabeler<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Label> labelIndex(labelClass: Class<T>): LabelIndex<T> {
        return indices.firstOrNull { labelClass == it.labelClass }
                ?.index as? LabelIndex<T> ?: emptyLabelIndex(labelClass)
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

    override fun labelIndexes(): Collection<LabelIndex<*>> {
        return indices.map { it.index }
    }
}

/**
 * A standard implementation of [Labeler] which handles the creation of [LabelIndex] instances.
 *
 * @property labelClass The [Class] of a [Label] subclass that this will label.
 * @param document The parent document that this labeler occurs on.
 * @constructor Creates a labeler which can be used to add labels to a document.
 */
class StandardLabeler<T : Label>(
        val labelClass: Class<T>,
        private val document: Document?
) : Labeler<T> {
    /**
     * Creates a labeler which can be used to store labels without a backing document.
     *
     * @param labelClass The value to initialize the [StandardLabeler.labelClass] property to.
     */
    constructor(labelClass: Class<T>) : this(labelClass, null)

    private var unsorted: ArrayList<T>? = ArrayList()

    /**
     * Upon first access creates the label index by turning everything added to this labeler into a
     * new [LabelIndex]. After this labels can no longer be added to the labeler.
     *
     * @return Either returns a new label index or the existing label index created.
     */
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
        label.document = document
        label.labelId = unsorted.size
        unsorted.add(label)
    }
}
