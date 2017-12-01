/*
 * Copyright (c) 2017 Regents of the University of Minnesota - All Rights Reserved
 * Unauthorized Copying of this file, via any medium is strictly prohibited
 * Proprietary and Confidential
 */

package edu.umn.nlpengine

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
 */
interface Label {
    /**
     * The index of the first character in the the label
     */
    val startIndex: Int

    /**
     * The index immediately following the last character in the label
     */
    val endIndex: Int

    /**
     * Takes an index relative to this label's basis (0) and makes it relative to this label's
     * [startIndex] as a basis.
     */
    fun relativize(index: Int): Int = index - startIndex

    /**
     * Takes an index relative to this label's [startIndex] and normalizes it to the basis (0) of
     * this label
     */
    fun normalize(index: Int): Int = startIndex + index

    /**
     * Checks if the location in text of this label equals the location in text of the label [other]
     */
    fun locationEquals(other: Label): Boolean =
            startIndex == other.startIndex && endIndex == other.endIndex

    /**
     * Checks if the label [other] is contained by this label
     */
    fun contains(other: Label): Boolean =
            startIndex <= other.startIndex && endIndex >= other.endIndex

    fun isContainedBy(startIndex: Int, endIndex: Int): Boolean =
            this.startIndex >= startIndex && this.endIndex <= endIndex

    /**
     * Check if the label [other] contains this label
     */
    fun isContainedBy(other: Label): Boolean =
            startIndex >= other.startIndex && endIndex <= other.endIndex

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
    fun compareLocation(label: Label): Int {
        val compare = startIndex.compareTo(label.startIndex)
        if (compare != 0) return compare
        return endIndex.compareTo(label.endIndex)
    }

    fun compareStart(label: Label): Int {
        return startIndex.compareTo(label.startIndex)
    }

    fun toSpan(): Span = Span(startIndex, endIndex)

    fun relativize(label: Label): Span =
            Span(label.startIndex - startIndex, label.endIndex - startIndex)

    fun derelativize(label: Label): Span =
            Span(label.startIndex + startIndex, label.endIndex + startIndex)
}

abstract class AbstractLabel(
        override val startIndex: Int,
        override val endIndex: Int
) : Label {
    constructor(label: Label) : this(label.startIndex, label.endIndex)
}

/**
 * A location in text without any other associated data.
 */
data class Span(
        override val startIndex: Int,
        override val endIndex: Int
) : Label, Comparable<Label> {
    /**
     * Convience constructor that copies the location of another label
     */
    constructor(label: Label) : this(label.startIndex, label.endIndex)

    /**
     * Compares by comparing locations
     *
     * @see compareLocation
     */
    override fun compareTo(other: Label): Int {
        return compareLocation(other)
    }

    companion object Factory {
        @JvmStatic
        fun create(startIndex: Int, endIndex: Int): Span = Span(startIndex, endIndex)
    }
}


/**
 * A generalizable label that holds an object [value] as its data
 */
data class ReferenceLabel<out T>(
        override val startIndex: Int,
        override val endIndex: Int,
        val value: T
) : Label {
    constructor(label: Label, value: T) : this(label.startIndex, label.endIndex, value)
}

/**
 * A generalizable label that holds two objects [first] and [second] as its data
 */
data class PairLabel<out T, out U>(
        override val startIndex: Int,
        override val endIndex: Int,
        val first: T,
        val second: U
) : Label {
    constructor(
            label: Label,
            first: T,
            second: U
    ): this(label.startIndex, label.endIndex, first, second)
}

/**
 * A collection of labels ordered by their location in text.
 */
interface LabelIndex<out T : Label> : Collection<T> {
    /**
     * The collection of labels that contain the text specified by [startIndex] and [endIndex]
     */
    fun containing(startIndex: Int, endIndex: Int): LabelIndex<T>

    /**
     * The collection of labels that contain the text covered by [label]
     */
    fun containing(label: Label): LabelIndex<T> = containing(label.startIndex, label.endIndex)

    /**
     * The collection of labels inside the span of text from [startIndex] to [endIndex]
     */
    fun insideSpan(startIndex: Int, endIndex: Int): LabelIndex<T>

    /**
     * The collection of labels inside [label]
     */
    fun insideSpan(label: Label): LabelIndex<T> = insideSpan(label.startIndex, label.endIndex)

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
     * [Label.endIndex] is less than or equal to [index]
     */
    fun toTheLeftOf(index: Int): LabelIndex<T>

    /**
     * All the labels in this index that are before [label], i.e. where their [Label.endIndex] are
     * less than or equal to [label]'s [Label.startIndex]
     */
    fun toTheLeftOf(label: Label): LabelIndex<T> = toTheLeftOf(label.startIndex)

    /**
     * All the labels in this index that are after [index], i.e. where their [Label.startIndex] is
     * greater than or equal to [index]
     */
    fun toTheRightOf(index: Int): LabelIndex<T>

    /**
     * All the labels in this index that are after [label], i.e. where their [Label.startIndex] are
     * greater than or equal to [label]'s [Label.endIndex]
     */
    fun toTheRightOf(label: Label): LabelIndex<T> = toTheRightOf(label.endIndex)

    /**
     * A label index that goes through labels working forward from [index]
     *
     * @see toTheRightOf
     */
    fun forwardFrom(index: Int): LabelIndex<T> = toTheLeftOf(index).descendingStartIndex()

    /**
     * A label index that goes through labels working forward from [label]
     *
     * @see toTheRightOf
     */
    fun forwardFrom(label: Label): LabelIndex<T> = forwardFrom(label.startIndex)

    /**
     * A label index that goes through labels working backwards from [index]
     *
     * @see toTheLeftOf
     */
    fun backwardFrom(index: Int): LabelIndex<T> = toTheRightOf(index)

    /**
     * A label index that goes through labels working backwards from [label]
     *
     * @see toTheLeftOf
     */
    fun backwardFrom(label: Label): LabelIndex<T> = backwardFrom(label.endIndex)

    /**
     * Returns the first label in this label index or null if it is empty
     */
    fun first(): T?

    /**
     * Returns a collection of all the labels with the same location as [label].
     */
    fun atLocation(label: Label): Collection<T>

    /**
     * Alias for [atLocation] taking a start index and end index
     */
    fun atLocation(startIndex: Int, endIndex: Int): Collection<T> =
            atLocation(Span(startIndex, endIndex))

    fun firstAtLocation(label: Label): T? = atLocation(label).firstOrNull()

    fun firstAtLocation(startIndex: Int, endIndex: Int): T? =
            atLocation(startIndex, endIndex).firstOrNull()

    /**
     * Returns all the labels in this index as a list.
     */
    fun asList(): List<T>

    /**
     * Returns true if this contains a label with the same location as [label]
     */
    fun containsSpan(label: Label): Boolean

    /**
     * Alias for [containsSpan] taking creating a new [Span] form [startIndex] and [endIndex]
     */
    fun containsSpan(
            startIndex: Int,
            endIndex: Int
    ): Boolean = containsSpan(Span(startIndex, endIndex))
}

abstract class AbstractLabelIndex<out T : Label> : LabelIndex<T>

interface Labeler<in T : Label> {
    fun add(label: T)

    fun labelAll(elements: Iterable<T>) = elements.forEach { this.add(it) }
}
