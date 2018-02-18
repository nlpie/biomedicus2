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

import java.util.regex.Matcher

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LabelMetadata(val versionId: String, val distinct: Boolean = false)

/**
 * A location in text. Its indices consistent with [String.substring] and [CharSequence.subSequence]
 *
 * By default is sorted ascending start indexes then ascending end indexes.
 *
 * ### Implementation specification
 * Labels should be immutable after construction, [equals] should be consistent with
 * [locationEquals] (returning true iff [locationEquals] returns true), [Comparable] should be
 * consistent with [compareLocation] (returning 0 iff compareLocation returns 0, and having
 * ordering first determined by [compareLocation] then by any other factors). Kotlin data classes
 * are an excellent choice for implementations of label.
 *
 * @property startIndex the index of the first character
 * @property endIndex the index after any characters included in this label
 */
interface TextRange {
    val startIndex: Int

    val endIndex: Int

    /**
     * Checks if the location in text of this label equals the location in text of the label [other]
     */
    fun locationEquals(other: TextRange): Boolean =
            startIndex == other.startIndex && endIndex == other.endIndex

    /**
     * Checks if the label [other] is contained by this label
     */
    fun contains(other: TextRange): Boolean =
            startIndex <= other.startIndex && endIndex >= other.endIndex

    /**
     * The number of characters covered by this label
     */
    fun length(): Int = endIndex - startIndex

    /**
     * Gets the sub sequence of covered text from [charSequence]
     */
    fun coveredText(charSequence: CharSequence): CharSequence =
            charSequence.subSequence(startIndex, endIndex)

    /**
     * Gets a substring of the covered string from [string]
     */
    fun coveredString(string: String): String = string.substring(startIndex, endIndex)

    /**
     * Compares the location of two labels, first comparing [startIndex], then comparing [endIndex]
     */
    fun compareLocation(textRange: TextRange): Int {
        val compare = startIndex.compareTo(textRange.startIndex)
        if (compare != 0) return compare
        return endIndex.compareTo(textRange.endIndex)
    }

    /**
     * Compares the [startIndex] of two labels using natural ordering
     */
    fun compareStart(textRange: TextRange): Int {
        return startIndex.compareTo(textRange.startIndex)
    }

    /**
     * Transforms this label into a [Span] of just this label's [startIndex] and [endIndex].
     */
    fun toSpan(): Span = Span(startIndex, endIndex)

    fun relativize(startIndex: Int, endIndex: Int): Span =
            Span(startIndex - this.startIndex, endIndex - this.startIndex)

    /**
     * Takes the argument [textRange], which is relative to this label, and changes it to have the same
     * 0-basis as this label.
     */
    fun normalize(textRange: TextRange): Span =
            Span(textRange.startIndex + startIndex, textRange.endIndex + startIndex)

    fun normalize(startIndex: Int, endIndex: Int): Span =
            Span(startIndex + this.startIndex, endIndex + this.startIndex)

    fun offset(value: Int): Span = Span(startIndex + value, endIndex + value)

    fun offsetByStartIndex(textRange: TextRange): Span =
            Span(startIndex + textRange.startIndex, endIndex + textRange.startIndex)
}

abstract class Label : TextRange {
    var internalLabeledOnDocument: Document? = null
    var internalLabelIdentifier: Int? = null
}

/**
 * Used to implement [TextRange] in Java since default method implementations on Kotlin interfaces are
 * not pulled through
 */
abstract class AbstractTextRange(
        override val startIndex: Int,
        override val endIndex: Int
) : TextRange {
    /**
     * Constructor which copies the bounds of another label
     */
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * A location in text without any other associated data.
 */
data class Span(
        override val startIndex: Int,
        override val endIndex: Int
) : TextRange, Comparable<TextRange> {
    /**
     * Convience constructor that copies the location of another label
     */
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)

    constructor(matcher: Matcher): this(matcher.start(), matcher.end())

    /**
     * Compares by comparing locations
     *
     * @see compareLocation
     */
    override fun compareTo(other: TextRange): Int {
        return compareLocation(other)
    }

    companion object Factory {
        /**
         * Creates a new span with the given [startIndex] and [endIndex]
         */
        @JvmStatic
        fun create(startIndex: Int, endIndex: Int): Span = Span(startIndex, endIndex)
    }
}

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
    fun containing(textRange: TextRange): LabelIndex<T> = containing(textRange.startIndex, textRange.endIndex)

    /**
     * The collection of labels inside the span of text from [startIndex] to [endIndex]
     */
    fun insideSpan(startIndex: Int, endIndex: Int): LabelIndex<T>

    /**
     * The collection of labels inside [textRange]
     */
    fun insideSpan(textRange: TextRange): LabelIndex<T> = insideSpan(textRange.startIndex, textRange.endIndex)

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
     * All the labels in this index that are before [textRange], i.e. where their [TextRange.endIndex] are
     * less than or equal to [textRange]'s [TextRange.startIndex]
     */
    fun toTheLeftOf(textRange: TextRange): LabelIndex<T> = toTheLeftOf(textRange.startIndex)

    /**
     * All the labels in this index that are after [index], i.e. where their [TextRange.startIndex] is
     * greater than or equal to [index]
     */
    fun toTheRightOf(index: Int): LabelIndex<T>

    /**
     * All the labels in this index that are after [textRange], i.e. where their [TextRange.startIndex] are
     * greater than or equal to [textRange]'s [TextRange.endIndex]
     */
    fun toTheRightOf(textRange: TextRange): LabelIndex<T> = toTheRightOf(textRange.endIndex)

    /**
     * A label index that goes through labels working forward from [index]
     *
     * @see toTheRightOf
     */
    fun forwardFrom(index: Int): LabelIndex<T> = toTheLeftOf(index).descendingStartIndex()

    /**
     * A label index that goes through labels working forward from [textRange]
     *
     * @see toTheRightOf
     */
    fun forwardFrom(textRange: TextRange): LabelIndex<T> = forwardFrom(textRange.startIndex)

    /**
     * A label index that goes through labels working backwards from [index]
     *
     * @see toTheLeftOf
     */
    fun backwardFrom(index: Int): LabelIndex<T> = toTheRightOf(index)

    /**
     * A label index that goes through labels working backwards from [textRange]
     *
     * @see toTheLeftOf
     */
    fun backwardFrom(textRange: TextRange): LabelIndex<T> = backwardFrom(textRange.endIndex)

    /**
     * Returns the first label in this label index or null if it is empty
     */
    fun first(): T?

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
     * Returns the first element of this index that has the same bounds as [textRange] or null if there
     * is no such object
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
