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

/**
 * A collection of labels ordered by their location in text. By default sorts by ascending
 * [TextRange.startIndex] and then ascending [TextRange.endIndex].
 *
 * @param T the type of label that this label index contains.
 */
interface LabelIndex<T : Label> : Collection<T> {
    val labelClass: Class<T>

    /**
     * The collection of labels that contain the text specified by [startIndex] and [endIndex]
     */
    fun containing(startIndex: Int, endIndex: Int): LabelIndex<T>

    /**
     * The collection of labels that contain the text covered by [textRange]
     */
    fun containing(textRange: TextRange): LabelIndex<T> =
            containing(textRange.startIndex, textRange.endIndex)

    /**
     * The collection of labels inside the span of text from [startIndex] to [endIndex]
     */
    fun insideSpan(startIndex: Int, endIndex: Int): LabelIndex<T>

    /**
     * The collection of labels inside [textRange]
     */
    fun insideSpan(textRange: TextRange): LabelIndex<T> =
            insideSpan(textRange.startIndex, textRange.endIndex)

    /**
     * A label index of all labels that begin inside the span of [startIndex] until [endIndex].
     */
    fun beginsInside(startIndex: Int, endIndex: Int): LabelIndex<T>

    /**
     * A label index of all labels that begin inside [textRange].
     */
    fun beginsInside(textRange: TextRange): LabelIndex<T> =
            beginsInside(textRange.startIndex, textRange.endIndex)

    /**
     * The labels in this label index sorted according to ascending span
     */
    fun ascending(): LabelIndex<T> = ascendingStartIndex().ascendingEndIndex()

    /**
     * The labels in this label index sorted according to descending span
     */
    fun descending(): LabelIndex<T> = descendingStartIndex().descendingEndIndex()

    /**
     * The labels in this label index sorted according to ascending start index and any existing
     * sort order for end index
     */
    fun ascendingStartIndex(): LabelIndex<T>

    /**
     * The labels in this label index sorted according to descending start index and any existing
     * sort order for start index
     */
    fun descendingStartIndex(): LabelIndex<T>

    /**
     * The labels in this label index sorted according to any existing sort order for start index
     * and ascending end index
     */
    fun ascendingEndIndex(): LabelIndex<T>

    /**
     * The labels in this label index sorted according to any existing sort order for start index
     * and descending end index
     */
    fun descendingEndIndex(): LabelIndex<T>

    /**
     * All the labels in this label index that are before [index], i.e. where their
     * [TextRange.endIndex] is less than or equal to [index]
     */
    fun toTheLeftOf(index: Int): LabelIndex<T>

    /**
     * All the labels in this index that are before [textRange], i.e. where their
     * [TextRange.endIndex] are less than or equal to [textRange]'s [TextRange.startIndex]
     */
    fun toTheLeftOf(textRange: TextRange): LabelIndex<T> = toTheLeftOf(textRange.startIndex)

    /**
     * All the labels in this index that are after [index], i.e. where their [TextRange.startIndex]
     * is greater than or equal to [index]
     */
    fun toTheRightOf(index: Int): LabelIndex<T>

    /**
     * All the labels in this index that are after [textRange], i.e. where their
     * [TextRange.startIndex] are greater than or equal to [textRange]'s [TextRange.endIndex]
     */
    fun toTheRightOf(textRange: TextRange): LabelIndex<T> = toTheRightOf(textRange.endIndex)

    /**
     * A label index that goes through labels working forward from [index]
     *
     * @see toTheRightOf
     */
    fun forwardFrom(index: Int): LabelIndex<T> =
            toTheRightOf(index).ascendingStartIndex().ascendingEndIndex()

    /**
     * A label index that goes through labels working forward from [textRange]
     *
     * @see toTheRightOf
     */
    fun forwardFrom(textRange: TextRange): LabelIndex<T> = forwardFrom(textRange.endIndex)

    /**
     * A label index that goes through labels working backwards from [index]
     *
     * @see toTheLeftOf
     */
    fun backwardFrom(index: Int): LabelIndex<T> =
            toTheLeftOf(index).descendingStartIndex().descendingEndIndex()

    /**
     * A label index that goes through labels working backwards from [textRange]
     *
     * @see toTheLeftOf
     */
    fun backwardFrom(textRange: TextRange): LabelIndex<T> = backwardFrom(textRange.startIndex)

    /**
     * Returns the first label in this label index or null if it is empty.
     */
    fun first(): T?

    /**
     * Returns the last label in this label index or null if it is empty.
     */
    fun last(): T?

    /**
     * Returns a collection of all the labels with the same location as [textRange].
     */
    fun atLocation(textRange: TextRange): Collection<T>

    /**
     * Alias for [atLocation] taking a start index and end index
     */
    fun atLocation(startIndex: Int, endIndex: Int): Collection<T> =
            atLocation(Span(startIndex, endIndex))

    /**
     * Returns the first element of this index that has the same bounds as [textRange] or null if
     * there is no such object
     */
    fun firstAtLocation(textRange: TextRange): T? = atLocation(textRange).firstOrNull()

    /**
     * Returns the first element of this index that has the same bounds specified by [startIndex]
     * and [endIndex] or null if there is no such object
     */
    fun firstAtLocation(startIndex: Int, endIndex: Int): T? =
            atLocation(startIndex, endIndex).firstOrNull()

    /**
     * Returns all the labels in this index as a list.
     */
    fun asList(): List<T>

    /**
     * Returns true if this contains a label with the same location as [textRange]
     */
    fun containsSpan(textRange: TextRange): Boolean

    /**
     * Alias for [containsSpan] taking creating a new [Span] form [startIndex] and [endIndex]
     */
    fun containsSpan(
            startIndex: Int,
            endIndex: Int
    ): Boolean = containsSpan(Span(startIndex, endIndex))
}

/**
 * Used to implement [LabelIndex] in Java since default method implementations are not pulled from
 * interfaces in Java.
 */
abstract class AbstractLabelIndex<T : Label> : LabelIndex<T>

/**
 * Used to add labels to the document label indices.
 *
 * @param T the type of label that can be added using this labeler
 */
@Suppress("AddVarianceModifier") // implementations require invariance
interface Labeler<T : Label> {
    /**
     * Adds [label] to the document
     */
    fun add(label: T)

    /**
     * Adds all the labels in the [Iterable] [elements] to the document.
     */
    fun addAll(elements: Iterable<T>) = elements.forEach { this.add(it) }
}

/**
 * An empty label index.
 */
fun <T : Label> emptyLabelIndex(clazz: Class<T>): LabelIndex<T> = object : LabelIndex<T> {
    override val labelClass: Class<T>
        get() = clazz

    override val size: Int
        get() = 0

    override fun containing(startIndex: Int, endIndex: Int) = this

    override fun insideSpan(startIndex: Int, endIndex: Int) = this

    override fun beginsInside(startIndex: Int, endIndex: Int) = this

    override fun ascendingStartIndex() = this

    override fun descendingStartIndex() = this

    override fun ascendingEndIndex() = this

    override fun descendingEndIndex() = this

    override fun toTheLeftOf(index: Int) = this

    override fun toTheRightOf(index: Int) = this

    override fun first(): T? = null

    override fun last(): T? = null

    override fun atLocation(textRange: TextRange): Collection<T> = emptyList()

    override fun asList(): List<T> = emptyList()

    override fun containsSpan(textRange: TextRange) = false

    override fun contains(element: T) = false

    override fun containsAll(elements: Collection<T>) = elements.isEmpty()

    override fun isEmpty() = true

    override fun iterator(): Iterator<T> = emptyList<T>().iterator()
}
