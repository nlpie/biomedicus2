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

import java.io.Reader
import java.io.StringReader
import java.util.*
import kotlin.reflect.KClass

/**
 * A single processing artifact and all the data that it contains. Contains individual text views
 * containing the text associated with a document and all of the label indexes containing labeled
 * types.
 *
 * @since 1.6.0
 */
interface Document {

    /**
     * Returns the unique document identifier. Generally sourced from the relative path to the
     * source file, a primary key/integer unique identifier, or an UUID. It should be a valid
     * relative file path in order for writers to properly function.
     *
     * @return string document identifier
     */
    val documentId: String

    /**
     * Returns a map of all the metadata for this document.
     *
     * @return unmodifiable map of all the metadata for this document.
     */
    val metadata: MutableMap<String, String>

    /**
     * [LabeledText] objects that are attached to this document.
     */
    val labeledTexts: Map<String, LabeledText>

    /**
     * Creates a new [LabeledText] attached to this document
     */
    fun attachText(id: String, text: String): LabeledText
}

/**
 * A biomedicus basic unit for document text and its associated labels.
 *
 * @since 1.6.0
 */
abstract class LabeledText {

    /**
     * Returns a reader for the document text
     *
     * @return a java reader for the document text
     */
    abstract val reader: Reader

    /**
     * Gets the entire text of the document
     *
     * @return document text
     */
    abstract val text: String

    /**
     * Returns the [Span] of the entire document.
     *
     * @return the Span of the entire document.
     */
    abstract val documentSpan: Span

    /**
     * Returns the label index for the specific label class.
     *
     * @param labelClass the labelable class instance
     * @param <T> the type of the labelable class
     * @return label index for the labelable type
     */
    abstract fun <T : TextRange> labelIndex(labelClass: Class<T>): LabelIndex<T>

    fun <T : TextRange> labelIndex(clazz: KClass<T>): LabelIndex<T> = labelIndex(clazz.java)

    inline fun <reified T : TextRange> labelIndex() : LabelIndex<T> {
        return labelIndex(T::class)
    }

    /**
     * Returns a labeler for the specific label class.
     *
     * @param labelClass the labelable class instance
     * @param <T> the type of the labelable class
     * @return labeler for the labelable type
     */
    abstract fun <T : TextRange> labeler(labelClass: Class<T>): Labeler<T>

    fun <T : TextRange> labeler(clazz: KClass<T>): Labeler<T> = labeler(clazz.java)

    inline fun <reified T : TextRange> labeler() : Labeler<T> {
        return labeler(T::class)
    }

    /**
     * A builder for a new text view.
     */
    interface Builder {
        /**
         * Sets the text of the text view.
         *
         * @param text the text of the text view
         * @return this builder
         */
        fun withText(text: String): Builder

        /**
         * Sets the name of the text view.
         *
         * @param name the name identifier of the text view.
         * @return this builder
         */
        fun withName(name: String): Builder

        /**
         * Finalizes and builds the new text view.
         *
         * @return the finished text view.
         */
        fun build(): LabeledText
    }
}

inline fun <reified T : TextRange> T.addTo(labeledText: LabeledText) {
    labeledText.labeler(T::class).add(this)
}

class StandardDocument(
        override val documentId: String
): Document {
    override val metadata = HashMap<String, String>()

    override val labeledTexts = HashMap<String, StandardLabeledText>()

    override fun attachText(id: String, text: String): LabeledText {
        return StandardLabeledText(text).also { labeledTexts[id] = it }
    }
}

class StandardLabeledText(override val text: String) : LabeledText() {
    private val indices = HashMap<Class<*>, StandardLabeler<*>>()

    override val reader: Reader
        get() = StringReader(text)
    override val documentSpan: Span
        get() = Span(0, text.length)

    @Suppress("UNCHECKED_CAST")
    override fun <T : TextRange> labelIndex(labelClass: Class<T>): LabelIndex<T> {
        return indices[labelClass]?.index as? LabelIndex<T> ?: StandardLabelIndex()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : TextRange> labeler(labelClass: Class<T>): Labeler<T> {
        synchronized(this) {
            var labeler = indices[labelClass] as StandardLabeler<T>?

            if (labeler == null) {
                labeler = StandardLabeler()

                if (indices.put(labelClass, labeler) != null) {
                    throw IllegalStateException("type already been/being labeled: " + labelClass)
                }
            }
            return labeler
        }
    }
}

internal class StandardLabeler<T : TextRange> : Labeler<T> {
    private var unsorted: ArrayList<T>? = ArrayList()

    val index: LabelIndex<T> by lazy {
        StandardLabelIndex(unsorted!!).also { unsorted = null }
    }

    override fun add(label: T) {
        val unsorted = unsorted
                ?: throw IllegalStateException("Index has been accessed and finalized already")
        unsorted.add(label)
    }
}

