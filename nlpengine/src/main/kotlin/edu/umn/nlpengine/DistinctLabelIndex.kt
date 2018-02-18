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
import java.util.Collections.emptyList
import java.util.Collections.unmodifiableCollection

inline fun <reified T: Label> DistinctLabelIndex(vararg labels: T): DistinctLabelIndex<T> {
    return DistinctLabelIndex(T::class.java, *labels)
}

inline fun <reified T: Label> DistinctLabelIndex(
        comparator: Comparator<T>,
        vararg labels: T
) : DistinctLabelIndex<T> {
    return DistinctLabelIndex(T::class.java, comparator, *labels)
}

inline fun <reified T: Label> DistinctLabelIndex(
        labels: Iterable<T>
) : DistinctLabelIndex<T> {
    return DistinctLabelIndex(T::class.java, labels)
}

inline fun <reified T: Label> DistinctLabelIndex(
        comparator: Comparator<T>,
        labels: Iterable<T>
) : DistinctLabelIndex<T> {
    return DistinctLabelIndex(T::class.java, comparator, labels)
}

/**
 * A label index where the labels are distinct i.e. non-overlapping.
 */
class DistinctLabelIndex<T : Label> internal constructor(
        override val labelClass: Class<T>,
        private val values: List<T>
) : LabelIndex<T>, Collection<T> by unmodifiableCollection(values) {

    constructor(
            labelClass: Class<T>, 
            vararg labels: T
    ) : this(labelClass, labels.sortedWith(Comparator { o1, o2 ->
        o1.compareStart(o2)
    }))

    constructor(
            labelClass: Class<T>,
            comparator: Comparator<T>,
            vararg labels: T
    ) : this(labelClass, labels.sortedWith(comparator))

    constructor(
            labelClass: Class<T>,
            labels: Iterable<T>
    ) : this(labelClass, labels.sortedWith(Comparator { o1, o2 ->
        o1.compareStart(o2)
    }))

    constructor(
            labelClass: Class<T>,
            comparator: Comparator<T>, labels: Iterable<T>
    ) : this(labelClass, labels.sortedWith(comparator))

    override fun containing(startIndex: Int, endIndex: Int): LabelIndex<T> {
        val index = containingIndex(startIndex, endIndex)

        return AscendingView(left = index, right = index)
    }

    override fun insideSpan(startIndex: Int, endIndex: Int): LabelIndex<T> =
            AscendingView(minTextIndex = startIndex, maxTextIndex = endIndex)

    override fun ascendingStartIndex() = this

    override fun descendingStartIndex(): LabelIndex<T> = DescendingView()

    override fun ascendingEndIndex() = this

    override fun descendingEndIndex() = this

    override fun toTheLeftOf(index: Int): LabelIndex<T> = AscendingView(maxTextIndex = index)

    override fun toTheRightOf(index: Int): LabelIndex<T> = AscendingView(minTextIndex = index)

    override fun first() = if (values.isNotEmpty()) values[0] else null

    override fun atLocation(textRange: TextRange) = internalAtLocation(textRange)

    override fun contains(element: @UnsafeVariance T) = internalIndexOf(element) != -1

    override fun containsSpan(textRange: TextRange) = internalContainsLocation(textRange)

    override fun asList() = object : List<T> by Collections.unmodifiableList(values) {
        override fun indexOf(element: @UnsafeVariance T) = internalIndexOf(element)

        override fun lastIndexOf(element: @UnsafeVariance T) = internalIndexOf(element)

        override fun contains(element: @UnsafeVariance T) = internalIndexOf(element) != -1
    }

    internal fun containingIndex(
            startIndex: Int,
            endIndex: Int,
            fromIndex: Int = 0,
            toIndex: Int = size
    ): Int {
        if (values.isEmpty()) {
            return -1
        }

        var index = values.binarySearchBy(startIndex, fromIndex, toIndex) { it.startIndex }

        if (index < 0) {
            index = -1 * (index + 1) - 1
        }

        return if (index >= 0 && index < values.size && values[index].startIndex <= startIndex && values[index].endIndex >= endIndex) {
            index
        } else -1
    }

    internal fun internalAtLocation(
            textRange: TextRange,
            fromIndex: Int = 0,
            toIndex: Int = values.size
    ): Collection<T> {
        val index = values.binarySearch(textRange, Comparator { o1, o2 ->
            o1.compareStart(o2)
        }, fromIndex, toIndex)

        return if (index >= 0 && values[index].endIndex == textRange.endIndex) {
            listOf(values[index])
        } else emptyList()
    }

    internal fun internalIndexOf(
            element: @UnsafeVariance T,
            fromIndex: Int = 0,
            toIndex: Int = size
    ): Int {
        val index = values.binarySearch(element, Comparator { o1, o2 ->
            o1.compareStart(o2)
        }, fromIndex, toIndex)

        return if (index < 0 || values[index] != element) -1 else index
    }

    internal fun internalContainsLocation(
            textRange: TextRange,
            fromIndex: Int = 0,
            toIndex: Int = size
    ): Boolean {
        val index = values.binarySearch(textRange, Comparator { o1, o2 ->
            o1.compareStart(o2)
        }, fromIndex, toIndex)

        return index >= 0 && values[index].endIndex == textRange.endIndex
    }

    /**
     * Index of earliest label with a location after than the text index [index] or -1 if there is
     * no such index
     */
    internal fun higherIndex(
            index: Int,
            fromIndex: Int = 0,
            toIndex: Int = size
    ): Int {
        var i = values.binarySearchBy(index, fromIndex, toIndex) { it.startIndex }

        if (i < 0) {
            i = -1 * (i + 1)
            if (i == toIndex) {
                return -1
            }
        }
        return i
    }

    /**
     * Index of the last label with a location before than the text index [index] or -1 if there is
     * no such index
     */
    internal fun lowerIndex(
            index: Int,
            fromIndex: Int = 0,
            toIndex: Int = size
    ): Int {
        var i = values.binarySearchBy(index, fromIndex, toIndex) { it.endIndex }

        if (i < 0) {
            i = -1 * (i + 1)
            if (i <= fromIndex) {
                return -1
            }
            i--
        }

        return i
    }


    internal abstract inner class View(
            left: Int,
            right: Int
    ) : LabelIndex<T> {
        override val labelClass get() = this@DistinctLabelIndex.labelClass

        final override val size: Int

        val left: Int
        val right: Int

        init {
            if (left == -1 || right == -1 || right < left) {
                this.left = 0
                this.right = -1
            } else {
                this.left = left
                this.right = right
            }
            size = this.right - this.left + 1
        }

        abstract val firstIndex: Int

        abstract fun updateEnds(newLeft: Int, newRight: Int): LabelIndex<T>

        override fun isEmpty() = size == 0

        override fun atLocation(textRange: TextRange) = internalAtLocation(textRange, left, right + 1)

        override fun contains(element: @UnsafeVariance T) =
                internalIndexOf(element, left, right + 1) != -1

        override fun containsAll(elements: Collection<@UnsafeVariance T>) =
                elements.all { contains(it) }

        override fun containsSpan(textRange: TextRange) =
                internalContainsLocation(textRange, left, right + 1)

        override fun toTheLeftOf(index: Int) = updateBounds(maxTextIndex = index)

        override fun toTheRightOf(index: Int) = updateBounds(minTextIndex = index)

        override fun insideSpan(startIndex: Int, endIndex: Int) = updateBounds(
                minTextIndex = startIndex,
                maxTextIndex = endIndex
        )

        override fun first(): T? {
            if (firstIndex in 0 until values.size && firstIndex <= right) {
                return values[firstIndex]
            }
            return null
        }

        override fun containing(startIndex: Int, endIndex: Int): LabelIndex<T> {
            val index = containingIndex(startIndex, endIndex, left, right + 1)

            return if (index != -1) updateEnds(index, index) else updateEnds(0, -1)
        }

        internal fun inBounds(index: Int) = index in left..right

        internal fun updateBounds(
                minTextIndex: Int? = null,
                maxTextIndex: Int? = null
        ): LabelIndex<T> {
            val newLeft = if (minTextIndex != null) higherIndex(minTextIndex) else left
            val newRight = if (maxTextIndex != null) lowerIndex(maxTextIndex) else right

            return updateEnds(newLeft, newRight)
        }
    }

    internal inner class AscendingView(
            minTextIndex: Int = 0,
            maxTextIndex: Int = Int.MAX_VALUE,
            left: Int = higherIndex(minTextIndex),
            right: Int = lowerIndex(maxTextIndex)
    ) : View(left, right) {
        override val firstIndex = this.left

        override fun updateEnds(newLeft: Int, newRight: Int): LabelIndex<T> {
            if (newLeft == -1) {
                return AscendingView(0, -1)
            }
            return AscendingView(left = maxOf(left, newLeft), right = minOf(right, newRight))
        }


        override fun descendingEndIndex() = this

        override fun descendingStartIndex() = DescendingView(left = left, right = right)

        override fun ascendingStartIndex() = this

        override fun ascendingEndIndex() = this

        override fun iterator() = AscendingListIterator(0)

        override fun asList(): List<T> = object: List<T> {
            override val size = this@AscendingView.size

            override fun isEmpty() = size == 0

            override fun contains(element: @UnsafeVariance T) =
                    internalIndexOf(element, left, right + 1) != -1

            override fun containsAll(elements: Collection<@UnsafeVariance T>) =
                    elements.all { contains(it) }

            override fun lastIndexOf(element: @UnsafeVariance T) = indexOf(element)

            override fun iterator(): Iterator<T> = listIterator()

            override fun listIterator() = listIterator(0)

            override fun listIterator(index: Int) = AscendingListIterator(index)

            override fun get(index: Int): T {
                if (index !in 0 until size) {
                    throw IndexOutOfBoundsException("$index is not in bounds 0:$size")
                }
                return values[firstIndex + index]
            }

            override fun indexOf(element: @UnsafeVariance T): Int {
                val index = internalIndexOf(element, left, right + 1)
                if (index == -1) return -1

                return index - left
            }

            override fun subList(fromIndex: Int, toIndex: Int): List<T> {
                if (fromIndex !in 0..size || toIndex !in 0..size || toIndex < fromIndex) {
                    throw IllegalArgumentException("Invalid range: from=$fromIndex to=$toIndex")
                }

                return AscendingView(left = left + fromIndex, right = left + toIndex - 1).asList()
            }
        }

        internal inner class AscendingListIterator(index: Int) : ListIterator<T> {
            var index: Int = left + index

            val localIndex get() = index - left

            override fun hasNext() = index <= right

            override fun hasPrevious() = index > left

            override fun nextIndex() = localIndex

            override fun previousIndex() = localIndex - 1

            override fun next(): T {
                if (!hasNext()) {
                    throw NoSuchElementException()
                }

                return values[index++]
            }

            override fun previous(): T {
                if (!hasPrevious()) {
                    throw NoSuchElementException()
                }

                return values[--index]
            }
        }
    }


    internal inner class DescendingView(
            minTextIndex: Int = 0,
            maxTextIndex: Int = Int.MAX_VALUE,
            left: Int = higherIndex(minTextIndex),
            right: Int = lowerIndex(maxTextIndex)
    ) : View(left, right) {

        override val firstIndex = right

        override fun ascendingStartIndex() = AscendingView(left = left, right = right)

        override fun descendingStartIndex() = this

        override fun ascendingEndIndex() = this

        override fun descendingEndIndex() = this

        override fun updateEnds(newLeft: Int, newRight: Int): View {
            if (newLeft == -1) {
                return DescendingView(0, -1)
            }
            return DescendingView(left = maxOf(left, newLeft), right = minOf(right, newRight))
        }

        override fun iterator() = DescendingListIterator(0)

        override fun asList(): List<T> = object: List<T> {
            override val size = this@DescendingView.size

            override fun isEmpty() = size == 0

            override fun contains(element: @UnsafeVariance T) =
                    internalIndexOf(element, left, right + 1) != -1

            override fun containsAll(elements: Collection<@UnsafeVariance T>) =
                    elements.all { contains(it) }

            override fun lastIndexOf(element: @UnsafeVariance T) = indexOf(element)

            override fun iterator() = listIterator()

            override fun listIterator() = listIterator(0)

            override fun listIterator(index: Int) = DescendingListIterator(index)

            override fun get(index: Int): T {
                if (index !in 0 until size) {
                    throw IndexOutOfBoundsException("$index is not in bounds")
                }

                return values[right - index]
            }

            override fun indexOf(element: @UnsafeVariance T): Int {
                val index = internalIndexOf(element, left, right + 1)
                if (index == -1) return -1

                return right - index
            }

            override fun subList(fromIndex: Int, toIndex: Int): List<T> {
                if (fromIndex !in 0..size || toIndex !in 0..size || toIndex < fromIndex) {
                    throw IllegalArgumentException("Invalid range: from=$fromIndex to=$toIndex")
                }

                return DescendingView(left = right - toIndex + 1, right = right - fromIndex)
                        .asList()
            }
        }

        internal inner class DescendingListIterator(index: Int) : ListIterator<T> {
            var index: Int = right - index

            val localIndex get() = right - index

            override fun hasNext() = index >= left

            override fun hasPrevious() = index < right

            override fun nextIndex() = localIndex

            override fun previousIndex() = localIndex - 1

            override fun next(): T {
                if (!hasNext()) {
                    throw NoSuchElementException()
                }

                return values[index--]
            }

            override fun previous(): T {
                if (!hasPrevious()) {
                    throw NoSuchElementException()
                }

                return values[++index]
            }
        }
    }
}
