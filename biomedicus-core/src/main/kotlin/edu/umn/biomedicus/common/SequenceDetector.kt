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

package edu.umn.biomedicus.common

import java.io.File
import java.util.*

/**
 * Detects arbitrary sub-sequences of items in longer sequences of items using a test.
 *
 * @param T the type of items in the sub-sequence definitions
 * @param U the type of items to be tested
 * @property sequences the list of sub-sequence lists
 * @property test the test to run determining if items of type [T] match type [U]
 */
class SequenceDetector<T, U>(
        vararg val sequences: List<T>,
        val test: (T, U) -> Boolean
) {
    /**
     * Creates a matcher object against the sequences for this detector
     */
    fun createMatcher(): SequenceMatcher<T, U> = SequenceMatcher(this)

    /**
     * Tests if the list contains any elements of the subsequences
     */
    fun matches(sequence: List<U>): IntRange? = createMatcher().matches(sequence)

    /**
     * Detects all of the sub-sequences in this collection that match in the sequence.
     */
    fun detectAll(sequence: List<U>): Collection<IntRange> = createMatcher().detectAll(sequence)

    companion object Factory {
        /**
         * Loads the sub-sequences from a file, one sub-sequence per line, elements separated by
         * spaces.
         */
        fun <U> loadFromFile(
                filename: String,
                test: (String, U) -> Boolean
        ): SequenceDetector<String, U> {
            return File(filename).useLines { SequenceDetector(
                        sequences = *it.filter { it.isNotEmpty() }
                                .map { it.split(" ") }
                                .toList()
                                .toTypedArray(),
                        test = test)
            }
        }
    }
}


/**
 * A single instance of a sequence detector test. Not designed for multithreading.
 */
class SequenceMatcher<T, U> internal constructor(val detector: SequenceDetector<T, U>) {

    private val inProgresses: MutableList<InProgress> = LinkedList()

    /**
     * Tests if the sequence contains any of the matching sub-sequences.
     */
    fun matches(sequence: List<U>): IntRange? {
        val it = sequence.listIterator()
        while (it.hasNext()) {
            val len = isCompletedBy(it.next())
            if (len != null) {
                val lastIndex = it.previousIndex()
                return IntRange(lastIndex - len + 1, lastIndex)
            }
        }
        return null
    }

    /**
     * Used to send elements of a sequence, in order, to test if any sub-sequence is contained in
     * those elements.
     */
    fun isCompletedBy(element: U): Int? {
        val it = inProgresses.iterator()
        while (it.hasNext()) {
            val inProgress = it.next()
            if (detector.test(inProgress.sequence[inProgress.tokenIndex + 1], element)) {
                val size = inProgress.sequence.size
                if (inProgress.tokenIndex + 2 == size) return size
                else inProgress.tokenIndex += 1
            } else it.remove()
        }

        return detector.sequences
                .filter { detector.test(it[0], element) }
                .map {
                    if (it.size == 1) 1 else {
                        inProgresses.add(InProgress(it))
                        0
                    }
                }.firstOrNull { it != 0 }
    }

    /**
     * Detects all of the sequences in this collection in the specified sequence
     */
    fun detectAll(sequence: List<U>): Collection<IntRange> {
        val iter = sequence.listIterator()
        val ranges = ArrayList<IntRange>()
        while (iter.hasNext()) {
            val elem = iter.next()
            val index = iter.previousIndex()
            ranges.addAll(getCompleted(elem).map { IntRange(index - it + 1, index) })
        }
        return ranges
    }

    /**
     * Gets the completed sub-sequence lengths that are matches after the specified [element].
     */
    fun getCompleted(element: U): Collection<Int> {
        val it = inProgresses.iterator()
        val completed = ArrayList<Int>()
        while (it.hasNext()) {
            val inProgress = it.next()
            if (detector.test(inProgress.sequence[inProgress.tokenIndex + 1], element)) {
                val size = inProgress.sequence.size
                if (inProgress.tokenIndex + 2 == size) completed.add(size)
                else inProgress.tokenIndex += 1
            } else it.remove()
        }

        detector.sequences
                .filter { detector.test(it[0], element) }
                .map {
                    if (it.size == 1) completed.add(1) else {
                        inProgresses.add(InProgress(it))
                    }
                }

        return completed
    }

    /**
     * Resets the state of this matcher, if it needs to be re-used for some reason.
     */
    fun reset() {
        inProgresses.clear()
    }

    private inner class InProgress(
            val sequence: List<T>,
            var tokenIndex: Int = 0
    )
}
