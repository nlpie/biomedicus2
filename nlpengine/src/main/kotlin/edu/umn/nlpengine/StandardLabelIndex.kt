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

inline fun <reified T : Label> StandardLabelIndex(vararg labels: T): StandardLabelIndex<T> {
    return StandardLabelIndex(T::class.java, *labels)
}

inline fun <reified T : Label> StandardLabelIndex(
        comparator: Comparator<T>,
        vararg labels: T
): StandardLabelIndex<T> {
    return StandardLabelIndex(T::class.java, comparator, *labels)
}

inline fun <reified T : Label> StandardLabelIndex(
        labels: Iterable<T>
): StandardLabelIndex<T> {
    return StandardLabelIndex(T::class.java, labels)
}

inline fun <reified T : Label> StandardLabelIndex(
        comparator: Comparator<T>,
        labels: Iterable<T>
): StandardLabelIndex<T> {
    return StandardLabelIndex(T::class.java, comparator, labels)
}

/**
 * A label index backed by a immutable sorted array of [TextRange] values.
 */
class StandardLabelIndex<T : Label> internal constructor(
        override val labelClass: Class<T>,
        private val values: List<T>
) : LabelIndex<T>, Collection<T> by values {

    constructor(
            labelClass: Class<T>,
            vararg labels: T
    ) : this(labelClass, labels.sortedWith(Comparator { o1, o2 ->
        o1.compareLocation(o2)
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
        o1.compareLocation(o2)
    }))

    constructor(
            labelClass: Class<T>,
            comparator: Comparator<T>,
            labels: Iterable<T>
    ) : this(labelClass, labels.sortedWith(comparator))

    companion object Factory {
        @JvmStatic
        fun <T : Label> create(labelClass: Class<T>, vararg labels: T): StandardLabelIndex<T> {
            return StandardLabelIndex(labelClass, *labels)
        }
    }

    override fun containing(startIndex: Int, endIndex: Int): LabelIndex<T> = AscendingView(
            maxBegin = startIndex,
            minEnd = endIndex
    )

    override fun insideSpan(startIndex: Int, endIndex: Int): LabelIndex<T> = AscendingView(
            minBegin = startIndex,
            maxBegin = endIndex - 1,
            minEnd = startIndex,
            maxEnd = endIndex
    )

    override fun beginsInside(startIndex: Int, endIndex: Int): LabelIndex<T> =
            AscendingView(minBegin = startIndex, maxBegin = endIndex - 1, minEnd = startIndex)

    override fun ascendingStartIndex(): LabelIndex<T> = this

    override fun descendingStartIndex(): LabelIndex<T> = DescendingReversingView()

    override fun ascendingEndIndex(): LabelIndex<T> = this

    override fun descendingEndIndex(): LabelIndex<T> = AscendingReversingView()

    override fun toTheLeftOf(index: Int): LabelIndex<T> =
            AscendingView(maxBegin = index, maxEnd = index)

    override fun toTheRightOf(index: Int): LabelIndex<T> =
            AscendingView(minBegin = index, minEnd = index)

    override fun first() = values.firstOrNull()

    override fun last() = values.lastOrNull()

    override fun atLocation(textRange: TextRange) = internalAtLocation(textRange)

    override fun contains(element: @UnsafeVariance T) = internalIndexOf(element) != -1

    override fun containsSpan(textRange: TextRange) = internalContainsLocation(textRange)

    override fun asList() = object : List<T> by values {
        override fun indexOf(element: @UnsafeVariance T) = internalIndexOf(element)

        override fun lastIndexOf(element: @UnsafeVariance T) = internalLastIndexOf(element)

        override fun contains(element: @UnsafeVariance T) = internalIndexOf(element) != -1

        override fun equals(other: Any?): Boolean {
            return values == other as? List<*>
        }

        override fun hashCode(): Int {
            return values.hashCode()
        }

        override fun toString(): String {
            return values.toString()
        }
    }

    internal fun internalAtLocation(
            textRange: TextRange,
            fromIndex: Int = 0,
            toIndex: Int = size
    ): Collection<T> {
        val index = values.binarySearch(textRange, Comparator { o1, o2 ->
            o1.compareLocation(o2)
        }, fromIndex, toIndex)

        if (index < 0) return emptyList()

        var left = index
        while (left > fromIndex && values[left - 1].locationEquals(textRange)) {
            left--
        }

        var right = index
        while (right < toIndex && values[right].locationEquals(textRange)) {
            right++
        }

        return values.subList(left, right)
    }

    internal fun internalIndexOf(
            element: @UnsafeVariance T,
            fromIndex: Int = 0,
            toIndex: Int = size
    ): Int {
        val result = values.binarySearch(element, Comparator { o1, o2 ->
            o1.compareLocation(o2)
        }, fromIndex, toIndex)

        if (result < 0) return -1

        val begin = values[result].startIndex
        var left = result
        var found: Int? = null
        while (left > fromIndex && begin == values[--left].startIndex) {
            if (values[left] == element) found = left
        }
        if (found != null) {
            return found
        }

        val end = values[result].endIndex
        var right = result
        while (right < toIndex && end == values[right].endIndex) {
            if (values[right] == element) return right
            right++
        }

        return -1
    }

    internal fun internalLastIndexOf(
            element: @UnsafeVariance T,
            fromIndex: Int = 0,
            toIndex: Int = size
    ): Int {
        val result = values.binarySearch(element, Comparator { o1, o2 ->
            o1.compareLocation(o2)
        }, fromIndex, toIndex)

        if (result < 0) return -1

        val end = values[result].endIndex
        var right = result
        var found: Int? = null
        while (right < toIndex && end == values[right].endIndex) {
            if (values[right] == element) found = right
            right++
        }
        if (found != null) {
            return found
        }

        val begin = values[result].startIndex
        var left = result
        while (left > fromIndex && begin == values[--left].startIndex) {
            if (values[left] == element) return left
        }

        return -1
    }

    internal fun internalContainsLocation(
            textRange: TextRange,
            fromIndex: Int = 0,
            toIndex: Int = size
    ) = 0 <= values.binarySearch(textRange, Comparator { o1, o2 ->
        o1.compareLocation(o2)
    }, fromIndex, toIndex)

    /**
     * Least index with a location greater than or equal to the provided location
     * or -1 if there is no such index
     */
    internal fun ceilingIndex(
            begin: Int,
            end: Int,
            fromIndex: Int = 0,
            toIndex: Int = size
    ) = ceilingIndex(Span(begin, end), fromIndex, toIndex)

    internal fun ceilingIndex(span: Span, fromIndex: Int = 0, toIndex: Int = values.size): Int {
        var index = values.binarySearch(span, Comparator { o1, o2 ->
            o1.compareLocation(o2)
        }, fromIndex, toIndex)

        if (index < 0) {
            val insert = -1 * (index + 1)

            return if (insert == toIndex) -1 else insert
        }

        while (index > fromIndex && values[index - 1].locationEquals(span)) {
            index--
        }

        return index
    }

    /**
     * Least index with a location greater than the provided location or the size
     */
    internal fun greaterIndex(
            begin: Int,
            end: Int,
            fromIndex: Int = 0,
            toIndex: Int = size
    ) = greaterIndex(Span(begin, end), fromIndex, toIndex)

    internal fun greaterIndex(span: Span, fromIndex: Int = 0, toIndex: Int = values.size): Int {
        val index = values.binarySearch(span, Comparator { o1, o2 ->
            o1.compareLocation(o2)
        }, fromIndex, toIndex)

        if (index < 0) {
            val insert = -1 * (index + 1)

            return insert
        }

        return index + 1
    }

    internal fun floorBeginAndEnd(
            begin: Int,
            end: Int,
            fromIndex: Int = 0,
            toIndex: Int = size
    ): Int {
        var index = floorIndex(begin, end, fromIndex, toIndex)

        while (index >= 0 && values[index].endIndex > end) {
            index--
        }
        return index
    }

    /**
     * Greatest index with a location less than or equal to the provided location or -1
     * if there is no such index
     */
    internal fun floorIndex(
            begin: Int,
            end: Int,
            fromIndex: Int = 0,
            toIndex: Int = size
    ) = floorIndex(Span(begin, end), fromIndex, toIndex)

    /**
     * Greatest index with a location less than or equal to the provided location or -1
     * if there is no such index
     */
    internal fun floorIndex(
            span: Span,
            fromIndex: Int = 0,
            toIndex: Int = values.size
    ): Int {
        var index = values.binarySearch(span, Comparator { o1, o2 ->
            o1.compareLocation(o2)
        }, fromIndex, toIndex)

        if (index < 0) {
            val insert = -1 * (index + 1)

            return if (insert == fromIndex) -1 else insert - 1
        }

        while (index < toIndex - 1 && values[index + 1].locationEquals(span)) {
            index++
        }

        return index
    }

    internal fun beginsEqual(firstIndex: Int, secondIndex: Int) =
            firstIndex !in 0 until size || secondIndex !in 0 until size ||
                    values[firstIndex].startIndex == values[secondIndex].startIndex

    internal abstract inner class View(
            val minBegin: Int,
            val maxBegin: Int,
            val minEnd: Int,
            val maxEnd: Int,
            left: Int,
            right: Int
    ) : LabelIndex<T> {
        override val labelClass get() = this@StandardLabelIndex.labelClass

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
        }

        abstract val firstIndex: Int

        abstract val lastIndex: Int

        abstract fun updateBounds(
                newMinBegin: Int = minBegin,
                newMaxBegin: Int = maxBegin,
                newMinEnd: Int = minEnd,
                newMaxEnd: Int = maxEnd
        ): View

        abstract fun updateEnds(left: Int, right: Int): View

        abstract fun nextIndex(index: Int): Int

        abstract fun prevIndex(index: Int): Int

        override val size: Int by lazy {
            var size = 0
            var i = firstIndex
            while (i != -1) {
                size++
                i = nextIndex(i)
            }
            size
        }

        override fun first(): T? {
            if (firstIndex in 0 until values.size && firstIndex <= right && firstIndex >= left) {
                return values[firstIndex]
            }
            return null
        }

        override fun last(): T? {
            if (lastIndex in 0 until values.size && lastIndex <= right && lastIndex >= left) {
                return values[lastIndex]
            }
            return null
        }

        override fun atLocation(textRange: TextRange): Collection<T> {
            if (!insideView(textRange)) return emptyList()
            return internalAtLocation(textRange, left, right + 1)
        }

        override fun isEmpty() = size == 0

        override fun contains(element: @UnsafeVariance T): Boolean {
            if (!insideView(element)) return false
            return internalIndexOf(element, left, right + 1) != -1
        }

        override fun containsAll(elements: Collection<@UnsafeVariance T>) =
                elements.all { contains(it) }

        override fun containsSpan(textRange: TextRange): Boolean {
            if (!insideView(textRange)) return false
            return internalContainsLocation(textRange, left, right + 1)
        }

        override fun toTheLeftOf(index: Int) =
                updateBounds(
                        newMaxBegin = minOf(index, maxBegin),
                        newMaxEnd = minOf(index, maxEnd)
                )

        override fun toTheRightOf(index: Int) =
                updateBounds(
                        newMinBegin = maxOf(index, minBegin),
                        newMinEnd = maxOf(index, minEnd)
                )

        override fun insideSpan(startIndex: Int, endIndex: Int) =
                updateBounds(
                        newMinBegin = maxOf(startIndex, minBegin),
                        newMaxBegin = minOf(endIndex - 1, maxBegin),
                        newMinEnd = maxOf(startIndex, minEnd),
                        newMaxEnd = minOf(endIndex, maxEnd)
                )

        override fun beginsInside(startIndex: Int, endIndex: Int) =
                updateBounds(
                        newMinBegin = maxOf(startIndex, minBegin),
                        newMaxBegin = minOf(endIndex - 1, maxBegin),
                        newMinEnd = maxOf(startIndex, minEnd)
                )

        override fun containing(startIndex: Int, endIndex: Int) =
                updateBounds(
                        newMaxBegin = minOf(startIndex, maxBegin),
                        newMinEnd = maxOf(endIndex, minEnd)
                )

        override fun asList(): List<T> = ViewList()

        override fun iterator() = ViewIterator(0)

        internal fun insideView(textRange: TextRange) =
                textRange.startIndex in minBegin..maxBegin && textRange.endIndex in minEnd..maxEnd

        internal fun endsInView(index: Int) =
                if (index == -1 || values[index].endIndex in minEnd..maxEnd) index else -1

        internal fun nextIndexAscending(index: Int): Int {
            var cursor = index
            while (cursor < right) {
                val result = endsInView(++cursor)
                if (result != -1) return result
            }
            return -1
        }

        internal fun nextIndexDescending(index: Int): Int {
            var cursor = index
            while (cursor > left) {
                val result = endsInView(--cursor)
                if (result != -1) return result
            }
            return -1
        }

        internal fun nextAscendingReversing(index: Int): Int {
            var tmp = index
            var atBeginning = false
            if (index == left) {
                atBeginning = true
            } else {
                tmp = nextIndexDescending(index)
            }

            if (atBeginning || !beginsEqual(tmp, index)) {
                tmp = nextIndexAscending(nextBreakAscending(index))
                if (tmp != -1) {
                    tmp = nextBreakAscending(tmp)
                }
            }

            return tmp
        }

        internal fun nextBreakAscending(index: Int): Int {
            var result: Int
            var tmp = index
            do {
                result = tmp
                tmp = nextIndexAscending(result)
                if (tmp == -1) {
                    break
                }
            } while (beginsEqual(tmp, result))
            return result
        }

        internal fun nextDescendingReversing(index: Int): Int {
            var tmp = index
            var atEnd = false
            if (index >= right) {
                atEnd = true
            } else {
                tmp = nextIndexAscending(index)
            }

            if (atEnd || !beginsEqual(tmp, index)) {
                tmp = nextIndexDescending(nextBreakDescending(index))
                if (tmp != -1) {
                    tmp = nextBreakDescending(tmp)
                }
            }
            return tmp
        }

        internal fun nextBreakDescending(index: Int): Int {
            var result: Int
            var tmp = index
            do {
                result = tmp
                tmp = nextIndexDescending(result)
                if (tmp == -1) {
                    break
                }
            } while (beginsEqual(tmp, result))
            return result
        }

        inner class ViewList : AbstractList<T>() {
            override val size: Int by lazy { this@View.size }

            override fun get(index: Int): T {
                var realIndex = firstIndex
                for (i in 0 until index) {
                    realIndex = nextIndex(realIndex)
                    if (realIndex == -1) {
                        throw IndexOutOfBoundsException("index: $index is not in bounds")
                    }
                }
                return values[realIndex]
            }

            override fun isEmpty() = size == 0

            override fun indexOf(element: @UnsafeVariance T): Int {
                val listIterator = listIterator()
                while (listIterator.hasNext()) {
                    if (listIterator.next() == element) return listIterator.previousIndex()
                }
                return -1
            }

            override fun lastIndexOf(element: @UnsafeVariance T): Int {
                val listIterator = listIterator(size)
                while (listIterator.hasPrevious()) {
                    if (listIterator.previous() == element) return listIterator.nextIndex()
                }
                return -1
            }

            override fun contains(element: @UnsafeVariance T) = this@View.contains(element)

            override fun containsAll(elements: Collection<@UnsafeVariance T>) =
                    elements.all { contains(it) }

            override fun listIterator() = ViewIterator(0)

            override fun listIterator(index: Int) = ViewIterator(index)

            override fun iterator(): Iterator<T> = ViewIterator(0)

            override fun subList(fromIndex: Int, toIndex: Int): List<T> {
                val viewIterator = ViewIterator(fromIndex)
                val globalFromIndex = viewIterator.index
                var globalToIndex: Int = globalFromIndex
                while (viewIterator.hasNext() && viewIterator.localIndex < toIndex) {
                    globalToIndex = viewIterator.index
                    viewIterator.next()
                }
                if (viewIterator.localIndex != toIndex) {
                    throw IndexOutOfBoundsException("toIndex: $toIndex is not in bounds")
                }

                return updateEnds(left = globalFromIndex, right = globalToIndex).asList()
            }

            override fun toString(): String {
                val sb = StringBuilder("[")

                val it = iterator()
                if (it.hasNext()) {
                    sb.append(it.next().toString())
                }
                while (it.hasNext()) {
                    sb.append(", ").append(it.next().toString())
                }

                return sb.append(']').toString()
            }
        }

        inner class ViewIterator(index: Int) : ListIterator<T> {
            var index = firstIndex
            var localIndex = 0

            init {
                while (localIndex < index) {
                    if (!hasNext()) {
                        throw IndexOutOfBoundsException("index: $index is not in bounds")
                    }
                    next()
                }
            }

            override fun hasNext() = localIndex < size

            override fun next(): T {
                if (!hasNext()) {
                    throw IndexOutOfBoundsException("index $localIndex is not in bounds")
                }

                val current = index
                index = nextIndex(index)
                if (index == -1) {
                    index = current + 1
                }
                localIndex++
                return values[current]
            }

            override fun nextIndex() = localIndex

            override fun hasPrevious() = localIndex > 0

            override fun previous(): T {
                if (!hasPrevious()) {
                    throw IndexOutOfBoundsException("index $localIndex is not in bounds")
                }

                index = prevIndex(index)
                localIndex--
                return values[index]
            }

            override fun previousIndex() = localIndex - 1
        }
    }

    internal inner class AscendingView(
            minBegin: Int = 0,
            maxBegin: Int = Int.MAX_VALUE,
            minEnd: Int = 0,
            maxEnd: Int = Int.MAX_VALUE,
            left: Int = ceilingIndex(minBegin, minEnd),
            right: Int = floorBeginAndEnd(maxBegin, maxEnd)
    ) : View(minBegin, maxBegin, minEnd, maxEnd, left, right) {
        override val firstIndex by lazy { nextIndex(left - 1) }
        override val lastIndex by lazy { prevIndex(right + 1) }

        override fun updateBounds(
                newMinBegin: Int,
                newMaxBegin: Int,
                newMinEnd: Int,
                newMaxEnd: Int
        ): View = AscendingView(
                minBegin = newMinBegin,
                maxBegin = newMaxBegin,
                minEnd = newMinEnd,
                maxEnd = newMaxEnd,
                left = maxOf(left, ceilingIndex(newMinBegin, newMinEnd)),
                right = minOf(right, floorBeginAndEnd(newMaxBegin, newMaxEnd))
        )

        override fun updateEnds(left: Int, right: Int): View {
            return AscendingView(
                    minBegin = minBegin,
                    maxBegin = maxBegin,
                    minEnd = minEnd,
                    maxEnd = maxEnd,
                    left = left,
                    right = right
            )
        }

        override fun nextIndex(index: Int) = nextIndexAscending(index)

        override fun prevIndex(index: Int) = nextIndexDescending(index)

        override fun ascendingStartIndex() = this

        override fun descendingStartIndex() = DescendingReversingView(
                minBegin = minBegin,
                maxBegin = maxBegin,
                minEnd = minEnd,
                maxEnd = maxEnd,
                left = left,
                right = right
        )

        override fun ascendingEndIndex() = this

        override fun descendingEndIndex() = AscendingReversingView(
                minBegin = minBegin,
                maxBegin = maxBegin,
                minEnd = minEnd,
                maxEnd = maxEnd,
                left = left,
                right = right
        )
    }

    internal inner class DescendingView(
            minBegin: Int = 0,
            maxBegin: Int = Int.MAX_VALUE,
            minEnd: Int = 0,
            maxEnd: Int = Int.MAX_VALUE,
            left: Int = ceilingIndex(minBegin, minEnd),
            right: Int = floorBeginAndEnd(maxBegin, maxEnd)
    ) : View(minBegin, maxBegin, minEnd, maxEnd, left, right) {
        override val firstIndex by lazy { nextIndex(right + 1) }
        override val lastIndex by lazy { prevIndex(left - 1) }

        override fun updateBounds(
                newMinBegin: Int,
                newMaxBegin: Int,
                newMinEnd: Int,
                newMaxEnd: Int
        ) = DescendingView(
                minBegin = newMinBegin,
                maxBegin = newMaxBegin,
                minEnd = newMinEnd,
                maxEnd = newMaxEnd,
                left = maxOf(left, ceilingIndex(newMinBegin, newMinEnd)),
                right = minOf(right, floorBeginAndEnd(newMaxBegin, newMaxEnd))
        )

        override fun updateEnds(left: Int, right: Int) = DescendingView(
                minBegin = minBegin,
                maxBegin = maxBegin,
                minEnd = minEnd,
                maxEnd = maxEnd,
                left = left,
                right = right
        )

        override fun nextIndex(index: Int) = nextIndexDescending(index)

        override fun prevIndex(index: Int) = nextIndexAscending(index)

        override fun ascendingStartIndex() = AscendingReversingView(
                minBegin = minBegin,
                maxBegin = maxBegin,
                minEnd = minEnd,
                maxEnd = maxEnd,
                left = left,
                right = right
        )

        override fun descendingStartIndex() = this

        override fun ascendingEndIndex() = DescendingReversingView(
                minBegin = minBegin,
                maxBegin = maxBegin,
                minEnd = minEnd,
                maxEnd = maxEnd,
                left = left,
                right = right
        )

        override fun descendingEndIndex() = this
    }

    internal inner class AscendingReversingView(
            minBegin: Int = 0,
            maxBegin: Int = Int.MAX_VALUE,
            minEnd: Int = 0,
            maxEnd: Int = Int.MAX_VALUE,
            left: Int = ceilingIndex(minBegin, minEnd),
            right: Int = floorBeginAndEnd(maxBegin, maxEnd)
    ) : View(minBegin, maxBegin, minEnd, maxEnd, left, right) {
        override val firstIndex by lazy { nextBreakAscending(left) }
        override val lastIndex by lazy { nextBreakDescending(right) }

        override fun updateBounds(
                newMinBegin: Int,
                newMaxBegin: Int,
                newMinEnd: Int,
                newMaxEnd: Int
        ): AscendingReversingView = AscendingReversingView(
                minBegin = newMinBegin,
                maxBegin = newMaxBegin,
                minEnd = newMinEnd,
                maxEnd = newMaxEnd,
                left = maxOf(left, ceilingIndex(newMinBegin, newMinEnd)),
                right = minOf(right, floorBeginAndEnd(newMaxBegin, newMaxEnd))
        )

        override fun updateEnds(left: Int, right: Int): View = AscendingReversingView(
                minBegin = minBegin,
                maxBegin = maxBegin,
                minEnd = minEnd,
                maxEnd = maxEnd,
                left = left,
                right = right
        )

        override fun nextIndex(index: Int) = nextAscendingReversing(index)

        override fun prevIndex(index: Int) = nextDescendingReversing(index)

        override fun ascendingStartIndex() = this

        override fun descendingStartIndex() = DescendingView(
                minBegin = minBegin,
                maxBegin = maxBegin,
                minEnd = minEnd,
                maxEnd = maxEnd,
                left = left,
                right = right
        )

        override fun ascendingEndIndex() = AscendingView(
                minBegin = minBegin,
                maxBegin = maxBegin,
                minEnd = minEnd,
                maxEnd = maxEnd,
                left = left,
                right = right
        )

        override fun descendingEndIndex() = this
    }

    internal inner class DescendingReversingView(
            minBegin: Int = 0,
            maxBegin: Int = Int.MAX_VALUE,
            minEnd: Int = 0,
            maxEnd: Int = Int.MAX_VALUE,
            left: Int = ceilingIndex(minBegin, minEnd),
            right: Int = floorBeginAndEnd(maxBegin, maxEnd)
    ) : View(minBegin, maxBegin, minEnd, maxEnd, left, right) {
        override val firstIndex by lazy { nextBreakDescending(right) }
        override val lastIndex by lazy { nextBreakAscending(left) }

        override fun updateBounds(
                newMinBegin: Int,
                newMaxBegin: Int,
                newMinEnd: Int,
                newMaxEnd: Int
        ) = DescendingReversingView(
                minBegin = newMinBegin,
                maxBegin = newMaxBegin,
                minEnd = newMinEnd,
                maxEnd = newMaxEnd,
                left = maxOf(left, ceilingIndex(newMinBegin, newMinEnd)),
                right = minOf(right, floorBeginAndEnd(newMaxBegin, newMaxEnd))
        )

        override fun updateEnds(left: Int, right: Int) = DescendingReversingView(
                minBegin = minBegin,
                maxBegin = maxBegin,
                minEnd = minEnd,
                maxEnd = maxEnd,
                left = left,
                right = right
        )

        override fun nextIndex(index: Int) = nextDescendingReversing(index)

        override fun prevIndex(index: Int) = nextAscendingReversing(index)

        override fun ascendingStartIndex() = AscendingView(
                minBegin = minBegin,
                maxBegin = maxBegin,
                minEnd = minEnd,
                maxEnd = maxEnd,
                left = left,
                right = right
        )

        override fun descendingStartIndex() = this

        override fun ascendingEndIndex() = this

        override fun descendingEndIndex() = DescendingView(
                minBegin = minBegin,
                maxBegin = maxBegin,
                minEnd = minEnd,
                maxEnd = maxEnd,
                left = left,
                right = right
        )
    }
}
