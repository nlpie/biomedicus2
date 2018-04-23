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
 * @property startIndex The index of the first character
 * @property endIndex The index after any characters included in this label
 * @property length A computed property how long the text range is, number of characters covered by
 * this label.
 */
interface TextRange : Comparable<TextRange> {
    val startIndex: Int

    val endIndex: Int

    val length: Int
        get() = endIndex - startIndex

    /**
     * A function which returns the length of this text range.
     */
    fun length(): Int = length

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
     * Gets the sub sequence of covered text from [charSequence]
     */
    fun coveredText(charSequence: CharSequence): CharSequence =
            charSequence.subSequence(startIndex, endIndex)

    /**
     * Gets a substring of the covered string from [string]
     */
    fun coveredString(string: String): String = string.substring(startIndex, endIndex)

    /**
     * Returns a new [Span] which trims all the whitespace from this [TextRange].
     */
    fun trim(charSequence: CharSequence): Span {
        requireBounds(charSequence)
        val newEnd = (endIndex - 1).let {
            var index = it
            while (index > startIndex && Character.isWhitespace(charSequence[index])) index--
            index + 1
        }
        val newStart = startIndex.let {
            var index = it
            while (index < newEnd && Character.isWhitespace(charSequence[index])) index++
            index
        }
        return Span(newStart, newEnd)
    }

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

    /**
     * Creates a new span by shifting the [TextRange] by [value] characters.
     */
    fun offset(value: Int): Span = Span(startIndex + value, endIndex + value)

    /**
     * Creates a new span by shifting this text range's indexes by [startIndex] of [textRange]
     * characters. If this object is relative to [textRange] has the effect of normalizing it to the
     * same 0-basis.
     */
    fun offsetRightByStartIndex(textRange: TextRange): Span =
            Span(startIndex + textRange.startIndex, endIndex + textRange.startIndex)

    /**
     * Performs a sanity check to make sure the indexes make sense, that [startIndex] is greater
     * than or equal to 0, and that [endIndex] is greater than or equal to [startIndex].
     *
     * @throws IllegalStateException if it fails one of the conditions for index validity
     */
    fun checkIndexes() {
        check(startIndex >= 0) { "startIndex: $startIndex less than 0" }
        check(endIndex >= startIndex) {
            "endIndex: $endIndex less than startIndex: $startIndex"
        }
    }

    /**
     * Converts to an [IntRange]
     */
    fun toIntRange(): IntRange = IntRange(startIndex, endIndex - 1)

    /**
     * Used to check that the text sequence has a valid [startIndex] and [endIndex]
     */
    fun requireBounds(charSequence: CharSequence) {
        require(startIndex <= charSequence.length) {
            "startIndex: $startIndex outside char sequence"
        }
        require(endIndex <= charSequence.length) {
            "endIndex: $endIndex outside charSequence"
        }
    }

    override operator fun compareTo(other: TextRange): Int {
        return compareLocation(other)
    }
}

/**
 * A labeled element in text.
 *
 * ### Implementation specification
 * Labels should be immutable after construction, [equals] should be consistent with
 * [locationEquals] (returning true iff [locationEquals] returns true), [Comparable] should be
 * consistent with [compareLocation] (returning 0 iff compareLocation returns 0, and having
 * ordering first determined by [compareLocation] then by any other factors). Kotlin data classes
 * are an excellent choice for implementations of label.
 */
abstract class Label : TextRange {
    /**
     * The document that this document is labeled on, or null if this label has not been added to a
     * document.
     */
    var document: Document? = null

    /**
     * Uniquely identifies this label against all other labels on the document of the same type,
     * or null if this label has not been added to a document.
     */
    var labelId: Int? = null

    /**
     * The text that the element covers, only available once the element has been added to a
     * document.
     */
    val coveredText: CharSequence? get() = document?.let { coveredText(it.text) }
}

/**
 * Used to implement [TextRange] in Java.
 */
abstract class AbstractTextRange : TextRange

/**
 * A location in text without any other associated data.
 */
data class Span(
        override val startIndex: Int,
        override val endIndex: Int
) : TextRange, Comparable<TextRange> {
    /**
     * Convenience constructor that copies the location of another label
     */
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)

    constructor(matcher: Matcher) : this(matcher.start(), matcher.end())

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
