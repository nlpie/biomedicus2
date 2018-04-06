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
import kotlin.test.*

class StandardLabelIndexTest {

    data class TestLabel(override val startIndex: Int, override val endIndex: Int) : Label()

    val tested = StandardLabelIndex(
            TestLabel(0, 5),
            TestLabel(0, 7),
            TestLabel(2, 6),
            TestLabel(6, 8),
            TestLabel(6, 7),
            TestLabel(9, 10),
            TestLabel(9, 13),
            TestLabel(9, 13)
    )

    @Test
    fun testContaining() {
        val containing = tested.containing(TestLabel(2, 4))

        assertEquals(containing.size, 3)

        val it = containing.iterator()
        assertEquals(it.next(), TestLabel(0, 5))
        assertEquals(it.next(), TestLabel(0, 7))
        assertEquals(it.next(), TestLabel(2, 6))
        assertFalse(it.hasNext())
    }

    @Test
    fun testContainingEquals() {
        val containing = tested.containing(TestLabel(6, 8))

        assertEquals(containing.size, 1)

        val it = containing.iterator()
        assertEquals(it.next(), TestLabel(6, 8))
        assertFalse(it.hasNext())
    }

    @Test
    fun testContainingEmpty() {
        val containing = tested.containing(4, 10)

        assertEquals(containing.size, 0)

        val it = containing.iterator()
        assertFalse(it.hasNext())
    }

    @Test
    fun testInsideSpan() {
        val insideSpan = tested.insideSpan(1, 8)

        assertEquals(insideSpan.size, 3)

        val it = insideSpan.iterator()
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertFalse(it.hasNext())
    }

    @Test
    fun testInsideSpanBefore() {
        val insideSpan = tested.insideSpan(0, 3)

        assertEquals(insideSpan.size, 0)

        val it = insideSpan.iterator()
        assertFalse(it.hasNext())
    }

    @Test
    fun testInsideSpanAfter() {
        val insideSpan = tested.insideSpan(15, 20)

        assertEquals(insideSpan.size, 0)

        val it = insideSpan.iterator()
        assertFalse(it.hasNext())
    }

    @Test
    fun testBeginsInside() {
        val tested = StandardLabelIndex(
                TestLabel(0, 5),
                TestLabel(0, 7),
                TestLabel(2, 6),
                TestLabel(6, 8),
                TestLabel(6, 7),
                TestLabel(9, 10),
                TestLabel(9, 13),
                TestLabel(9, 13)
        )

        val beginsInside = tested.beginsInside(1, 9)

        assertEquals(
                expected = listOf(
                        TestLabel(2, 6),
                        TestLabel(6, 7),
                        TestLabel(6, 8)
                ),
                actual = beginsInside.asList()
        )
    }

    @Test
    fun testBeginsInsideWithOverlap() {
        val tested = StandardLabelIndex(
                TestLabel(0, 5),
                TestLabel(0, 7),
                TestLabel(2, 6),
                TestLabel(6, 8),
                TestLabel(6, 7),
                TestLabel(9, 10),
                TestLabel(9, 13),
                TestLabel(9, 13)
        )

        val beginsInside = tested.beginsInside(1, 10)

        assertEquals(
                expected = listOf(
                        TestLabel(2, 6),
                        TestLabel(6, 7),
                        TestLabel(6, 8),
                        TestLabel(9, 10),
                        TestLabel(9, 13),
                        TestLabel(9, 13)
                ),
                actual = beginsInside.asList()
        )
    }

    @Test
    fun testAscendingBegin() {
        val ascendingBegin = tested.ascendingStartIndex()

        assertTrue(ascendingBegin === tested)
    }

    @Test
    fun testDescendingBegin() {
        val descendingBegin = tested.descendingStartIndex()

        assertEquals(descendingBegin.size, 8)

        val it = descendingBegin.iterator()
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(0, 5))
        assertEquals(it.next(), TestLabel(0, 7))
        assertFalse(it.hasNext())
    }


    @Test
    fun testAscendingEnd() {
        val ascendingEnd = tested.ascendingEndIndex()

        assertTrue(ascendingEnd === tested)
    }

    @Test
    fun testDescendingEnd() {
        val descendingEnd = tested.descendingEndIndex()

        assertEquals(descendingEnd.size, 8)

        val it = descendingEnd.iterator()
        assertEquals(it.next(), TestLabel(0, 7))
        assertEquals(it.next(), TestLabel(0, 5))
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 10))
        assertFalse(it.hasNext())
    }

    @Test
    fun testToTheLeftOf() {
        val toTheLeftOf = tested.toTheLeftOf(8)

        assertEquals(toTheLeftOf.size, 5)

        val it = toTheLeftOf.iterator()
        assertEquals(it.next(), TestLabel(0, 5))
        assertEquals(it.next(), TestLabel(0, 7))
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertFalse(it.hasNext())
    }

    @Test
    fun testToTheRightOf() {
        val toTheRightOf = tested.toTheRightOf(2)

        assertEquals(toTheRightOf.size, 6)

        val it = toTheRightOf.iterator()
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertFalse(it.hasNext())
    }

    @Test
    fun testFirstEmpty() {
        val standardLabelIndex = StandardLabelIndex<TestLabel>()

        assertNull(standardLabelIndex.first())
    }

    @Test
    fun testFirst() {
        assertEquals(tested.first(), TestLabel(0, 5))
    }

    @Test
    fun testLast() {
        assertEquals(tested.last(), TestLabel(9, 13))
    }

    @Test
    fun testGetMultiple() {
        val get = tested.atLocation(TestLabel(9, 13))

        assertEquals(get.size, 2)

        val it = get.iterator()
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertFalse(it.hasNext())
    }

    @Test
    fun testGetOne() {
        val get = tested.atLocation(TestLabel(2, 6))

        assertEquals(get.size, 1)
        assertEquals(get.iterator().next(), TestLabel(2, 6))
    }

    @Test
    fun testGetNone() {
        val get = tested.atLocation(TestLabel(0, 30))

        assertEquals(get.size, 0)
    }

    @Test
    fun testContainsTrue() {
        assertTrue(tested.contains(TestLabel(2, 6)))
    }

    @Test
    fun testContainsFalse() {
        assertFalse(tested.contains(TestLabel(0, 30)))
    }

    @Test
    fun testContainsSpanTrue() {
        assertTrue(tested.containsSpan(TestLabel(2, 6)))
    }

    @Test
    fun testContainsSpanFalse() {
        assertFalse(tested.containsSpan(TestLabel(0, 30)))
    }

    @Test
    fun testAsList() {
        val asList = tested.asList()

        assertEquals(asList, Arrays.asList(
                TestLabel(0, 5),
                TestLabel(0, 7),
                TestLabel(2, 6),
                TestLabel(6, 7),
                TestLabel(6, 8),
                TestLabel(9, 10),
                TestLabel(9, 13),
                TestLabel(9, 13))
        )
    }

    @Test
    fun testAsListIndexOf() {
        val asList = tested.asList()

        assertEquals(asList.indexOf(TestLabel(9, 13)), 6)
    }

    @Test
    fun testAsListIndexOfNone() {
        val asList = tested.asList()

        assertEquals(asList.indexOf(TestLabel(0, 30)), -1)
    }

    @Test
    fun testAsListLastIndexOf() {
        val asList = tested.asList()

        assertEquals(asList.lastIndexOf(TestLabel(9, 13)), 7)
    }

    @Test
    fun testAsListLastIndexOfNone() {
        val asList = tested.asList()

        assertEquals(asList.indexOf(TestLabel(0, 30)), -1)
    }

    @Test
    fun testAsListContains() {
        val asList = tested.asList()

        assertTrue(asList.contains(TestLabel(2, 6)))
    }

    @Test
    fun testAsListContainsFalse() {
        val asList = tested.asList()

        assertFalse(asList.contains(TestLabel(0, 30)))
    }

    val ascending = tested.insideSpan(0, 13)
    val insideSpan = ascending.insideSpan(1, 13)
    val ascendingReversing = tested.descendingEndIndex()
    val descendingReversing = tested.descendingStartIndex()
    val descending = tested.descendingStartIndex().descendingEndIndex()

    @Test
    fun testAscendingViewSize() {
        assertEquals(ascending.size, 8)
    }

    @Test
    fun testDescendingViewSize() {
        assertEquals(descending.size, 8)
    }

    @Test
    fun testAscendingReversingViewSize() {
        assertEquals(ascendingReversing.size, 8)
    }

    @Test
    fun testDescendingReversingViewSize() {
        assertEquals(descendingReversing.size, 8)
    }

    @Test
    fun testAscendingFirst() {
        assertEquals(ascending.first(), TestLabel(0, 5))
    }

    @Test
    fun testAscendingLast() {
        assertEquals(ascending.last(), TestLabel(9, 13))
    }

    @Test
    fun testAscendingReversingFirst() {
        assertEquals(ascendingReversing.first(), TestLabel(0, 7))
    }

    @Test
    fun testAscendingReversingLast() {
        assertEquals(ascendingReversing.last(), TestLabel(9, 10))
    }

    @Test
    fun testDescendingReversingFirst() {
        assertEquals(descendingReversing.first(), TestLabel(9, 10))
    }

    @Test
    fun testDescendingReversingLast() {
        assertEquals(descendingReversing.last(), TestLabel(0, 7))
    }

    @Test
    fun testDescendingFirst() {
        assertEquals(descending.first(), TestLabel(9, 13))
    }

    @Test
    fun testDescendingLast() {
        assertEquals(descending.last(), TestLabel(0, 5))
    }

    @Test
    fun testViewGet() {
        val get = descending.atLocation(TestLabel(9, 13))
        assertEquals(get.size, 2)

        val it = get.iterator()
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertFalse(it.hasNext())
    }

    @Test
    fun testViewGetNotInsideView() {
        val nothing = tested.insideSpan(0, 0)

        val get = nothing.atLocation(TestLabel(9, 13))
        assertEquals(get.size, 0)
        assertFalse(get.iterator().hasNext())
    }

    @Test
    fun testViewIsNotEmpty() {
        assertFalse(descending.isEmpty())
    }

    @Test
    fun testViewIsEmpty() {
        val nothing = tested.insideSpan(0, 0)
        assertTrue(nothing.isEmpty())
    }

    @Test
    fun testViewContains() {
        assertTrue(descending.contains(TestLabel(2, 6)))
    }

    @Test
    fun testViewNotContains() {
        val nothing = tested.insideSpan(0, 0)
        assertFalse(nothing.contains(TestLabel(2, 6)))
    }

    @Test
    fun testViewContainsSpan() {
        assertTrue(descending.containsSpan(TestLabel(2, 6)))
    }

    @Test
    fun testViewNotContainsSpan() {
        val nothing = tested.insideSpan(0, 0)
        assertFalse(nothing.containsSpan(TestLabel(2, 6)))
    }

    @Test
    fun testViewToTheLeftOf() {
        val toTheLeftOf = ascending.toTheLeftOf(8)

        assertEquals(toTheLeftOf.size, 5)

        val it = toTheLeftOf.iterator()
        assertEquals(it.next(), TestLabel(0, 5))
        assertEquals(it.next(), TestLabel(0, 7))
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertFalse(it.hasNext())
    }

    @Test
    fun testViewToTheRightOf() {
        val toTheRightOf = ascending.toTheRightOf(2)

        assertEquals(toTheRightOf.size, 6)

        val it = toTheRightOf.iterator()
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertFalse(it.hasNext())
    }

    @Test
    fun testViewInsideSpan() {

        assertEquals(insideSpan.size, 6)

        val it = insideSpan.iterator()
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertFalse(it.hasNext())
    }

    @Test
    fun testViewInsideSpanBefore() {
        val insideSpan = ascending.insideSpan(0, 1)

        assertEquals(insideSpan.size, 0)

        val it = insideSpan.iterator()
        assertFalse(it.hasNext())
    }

    @Test
    fun `view beginsInside shouldn't return anything before view`() {
        val tested = StandardLabelIndex(
                TestLabel(0, 5),
                TestLabel(0, 7),
                TestLabel(2, 6),
                TestLabel(6, 8),
                TestLabel(6, 7),
                TestLabel(9, 10),
                TestLabel(9, 13),
                TestLabel(9, 13)
        ).insideSpan(2, 9)

        val beginsInside = tested.beginsInside(0, 9)

        assertEquals(
                expected = listOf(
                        TestLabel(2, 6),
                        TestLabel(6, 7),
                        TestLabel(6, 8)
                ),
                actual = beginsInside.asList()
        )
    }

    @Test
    fun `view beginsInside shouldn't return anything after view`() {
        val tested = StandardLabelIndex(
                TestLabel(0, 5),
                TestLabel(0, 7),
                TestLabel(2, 6),
                TestLabel(6, 8),
                TestLabel(6, 7),
                TestLabel(9, 10),
                TestLabel(9, 13),
                TestLabel(9, 13)
        ).insideSpan(2, 9)

        val beginsInside = tested.beginsInside(0, 10)

        assertEquals(
                expected = listOf(
                        TestLabel(2, 6),
                        TestLabel(6, 7),
                        TestLabel(6, 8)
                ),
                actual = beginsInside.asList()
        )
    }

    @Test
    fun `view beginsInside shouldn't return labels whose beginIndex equals the endIndex parameter`() {
        val tested = StandardLabelIndex(
                TestLabel(0, 5),
                TestLabel(0, 7),
                TestLabel(2, 6),
                TestLabel(6, 8),
                TestLabel(6, 7),
                TestLabel(9, 10),
                TestLabel(9, 13),
                TestLabel(9, 13)
        ).insideSpan(2, 9)

        assertEquals(
                expected = listOf(
                        TestLabel(2, 6)
                ),
                actual = tested.beginsInside(2, 6).asList()
        )
    }

    @Test
    fun `view beginsInside should return labels that overlap with parameter range`() {
        val tested = StandardLabelIndex(
                TestLabel(0, 5),
                TestLabel(0, 7),
                TestLabel(2, 6),
                TestLabel(6, 8),
                TestLabel(6, 7),
                TestLabel(9, 10),
                TestLabel(9, 13),
                TestLabel(9, 13)
        ).insideSpan(2, 9)

        assertEquals(
                expected = listOf(
                        TestLabel(2, 6),
                        TestLabel(6, 7),
                        TestLabel(6, 8)
                ),
                actual = tested.beginsInside(2, 7).asList()
        )
    }

    @Test
    fun testViewInsideSpanAfter() {
        val insideSpan = ascending.insideSpan(14, 15)

        assertEquals(insideSpan.size, 0)

        val it = insideSpan.iterator()
        assertFalse(it.hasNext())
    }

    @Test
    fun testViewContaining() {
        val containing = ascending.containing(TestLabel(2, 4))

        assertEquals(containing.size, 3)

        val it = containing.iterator()
        assertEquals(it.next(), TestLabel(0, 5))
        assertEquals(it.next(), TestLabel(0, 7))
        assertEquals(it.next(), TestLabel(2, 6))
        assertFalse(it.hasNext())
    }

    val viewAsList = insideSpan.asList()

    @Test
    fun testViewAsList() {
        assertEquals(viewAsList, Arrays.asList(TestLabel(2, 6), TestLabel(6, 7), TestLabel(6, 8), TestLabel(9, 10),
                TestLabel(9, 13), TestLabel(9, 13)))
    }

    @Test
    fun testViewAsListSize() {
        assertEquals(viewAsList.size, 6)
    }

    @Test
    fun testViewAsListSizeEmpty() {
        val emptyAsList = ascending.insideSpan(0, -1).asList()

        assertEquals(emptyAsList.size, 0)
    }

    @Test
    fun testViewAsListGet() {
        assertEquals(viewAsList[1], TestLabel(6, 7))
    }

    @Test
    fun testViewAsListIsEmptyFalse() {
        assertFalse(viewAsList.isEmpty())
    }

    @Test
    fun testViewAsListIsEmptyTrue() {
        val emptyAsList = ascending.insideSpan(0, -1).asList()

        assertTrue(emptyAsList.isEmpty())
    }

    @Test
    fun testViewAsListIndexOf() {
        assertEquals(viewAsList.indexOf(TestLabel(9, 13)), 4)
    }

    @Test
    fun testViewAsListIndexOfNone() {
        assertEquals(viewAsList.indexOf(TestLabel(0, 30)), -1)
    }

    @Test
    fun testViewAsListLastIndexOf() {
        assertEquals(viewAsList.lastIndexOf(TestLabel(9, 13)), 5)
    }

    @Test
    fun testViewAsListLastIndexOfNone() {
        assertEquals(viewAsList.lastIndexOf(TestLabel(0, 30)), -1)
    }

    @Test
    fun testViewAsListContainsTrue() {
        assertTrue(viewAsList.contains(TestLabel(9, 13)))
    }

    @Test
    fun testViewAsListContainsFalse() {
        assertFalse(viewAsList.contains(TestLabel(0, 30)))
    }

    @Test
    fun testViewAsListContainsAllTrue() {
        assertTrue(viewAsList.containsAll(Arrays.asList(TestLabel(2, 6), TestLabel(6, 7), TestLabel(9, 13))))
    }

    @Test
    fun testViewAsListContainsAllFalse() {
        assertFalse(viewAsList.containsAll(Arrays.asList(TestLabel(2, 6), TestLabel(6, 7), TestLabel(9, 30))))
    }

    @Test
    fun testViewAsListListIteratorAtIndex() {
        val listIterator = viewAsList.listIterator(3)

        assertEquals(listIterator.nextIndex(), 3)
        assertEquals(listIterator.next(), TestLabel(9, 10))
        assertEquals(listIterator.previousIndex(), 3)

        assertEquals(listIterator.nextIndex(), 4)
        assertEquals(listIterator.next(), TestLabel(9, 13))
        assertEquals(listIterator.previousIndex(), 4)

        assertEquals(listIterator.nextIndex(), 5)
        assertEquals(listIterator.next(), TestLabel(9, 13))
        assertEquals(listIterator.previousIndex(), 5)


        assertFalse(listIterator.hasNext())
    }

    @Test
    fun testViewAsListSubList() {
        val subList = viewAsList.subList(2, 5)

        assertEquals(subList, Arrays.asList(TestLabel(6, 8), TestLabel(9, 10), TestLabel(9, 13)))
    }

    @Test
    fun testAscendingUpdateBounds() {
        val ascendingInsideSpan = ascending.insideSpan(2, 13)

        val it = ascendingInsideSpan.iterator()
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertFalse(it.hasNext())
    }

    @Test
    fun testAscendingAscendingBegin() {
        val ascendingBegin = ascending.ascendingStartIndex()

        assertTrue(ascendingBegin === ascending)
    }

    @Test
    fun testAscendingDescendingBegin() {
        val ascendingToDescending = ascending.descendingStartIndex()

        val it = ascendingToDescending.iterator()
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(0, 5))
        assertEquals(it.next(), TestLabel(0, 7))
        assertFalse(it.hasNext())
    }

    @Test
    fun testAscendingAscendingEnd() {
        val ascendingEnd = ascending.ascendingEndIndex()

        assertTrue(ascendingEnd === ascending)
    }

    @Test
    fun testAscendingDescendingEnd() {
        val descendingEnd = ascending.descendingEndIndex()

        val it = descendingEnd.iterator()
        assertEquals(it.next(), TestLabel(0, 7))
        assertEquals(it.next(), TestLabel(0, 5))
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 10))
        assertFalse(it.hasNext())
    }

    @Test
    fun testDescendingUpdateBounds() {
        val insideSpan = descending.insideSpan(2, 10)

        val it = insideSpan.iterator()
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(2, 6))
        assertFalse(it.hasNext())
    }

    @Test
    fun testDescendingAscendingBegin() {
        val ascendingBegin = descending.ascendingStartIndex()

        val it = ascendingBegin.iterator()
        assertEquals(it.next(), TestLabel(0, 7))
        assertEquals(it.next(), TestLabel(0, 5))
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 10))
        assertFalse(it.hasNext())
    }

    @Test
    fun testDescendingDescendingBegin() {
        val descendingEnd = descending.descendingEndIndex()

        assertTrue(descendingEnd === descending)
    }

    @Test
    fun testDescendingAscendingEnd() {
        val ascendingEnd = descending.ascendingEndIndex()

        val it = ascendingEnd.iterator()
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(0, 5))
        assertEquals(it.next(), TestLabel(0, 7))
        assertFalse(it.hasNext())
    }

    @Test
    fun testDescendingDescendingEnd() {
        val descendingEnd = descending.descendingEndIndex()

        assertTrue(descendingEnd === descending)
    }

    @Test
    fun testTestAscendingReversingUpdateBounds() {
        val insideSpan = ascendingReversing.insideSpan(2, 10)

        val it = insideSpan.iterator()
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(9, 10))
        assertFalse(it.hasNext())
    }

    @Test
    fun testAscendingReversingAscendingBegin() {
        val ascendingBegin = ascendingReversing.ascendingStartIndex()

        assertTrue(ascendingBegin === ascendingReversing)
    }

    @Test
    fun testAscendingReversingDescendingBegin() {
        val descendingBegin = ascendingReversing.descendingStartIndex()

        val it = descendingBegin.iterator()
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(0, 7))
        assertEquals(it.next(), TestLabel(0, 5))
        assertFalse(it.hasNext())
    }

    @Test
    fun testAscendingReversingAscendingEnd() {
        val ascendingEnd = ascendingReversing.ascendingEndIndex()

        val it = ascendingEnd.iterator()
        assertEquals(it.next(), TestLabel(0, 5))
        assertEquals(it.next(), TestLabel(0, 7))
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertFalse(it.hasNext())
    }

    @Test
    fun testAscendingReversingDescendingEnd() {
        val descendingEnd = ascendingReversing.descendingEndIndex()

        assertTrue(descendingEnd === ascendingReversing)
    }

    @Test
    fun testDescendingReversingUpdateBounds() {
        val insideSpan = descendingReversing.insideSpan(2, 10)

        val it = insideSpan.iterator()
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(2, 6))
        assertFalse(it.hasNext())
    }

    @Test
    fun testDescendingReversingAscendingBegin() {
        val ascendingBegin = descendingReversing.ascendingStartIndex()

        val it = ascendingBegin.iterator()
        assertEquals(it.next(), TestLabel(0, 5))
        assertEquals(it.next(), TestLabel(0, 7))
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertFalse(it.hasNext())
    }

    @Test
    fun testDescendingReversingDescendingBegin() {
        val descendingBegin = descendingReversing.descendingStartIndex()

        assertTrue(descendingBegin === descendingReversing)
    }

    @Test
    fun testDescendingReversingAscendingEnd() {
        val ascendingEnd = descendingReversing.ascendingEndIndex()

        assertTrue(ascendingEnd === descendingReversing)
    }

    @Test
    fun testDescendingReversingDescendingEnd() {
        val descendingEnd = descendingReversing.descendingEndIndex()

        val it = descendingEnd.iterator()
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 13))
        assertEquals(it.next(), TestLabel(9, 10))
        assertEquals(it.next(), TestLabel(6, 8))
        assertEquals(it.next(), TestLabel(6, 7))
        assertEquals(it.next(), TestLabel(2, 6))
        assertEquals(it.next(), TestLabel(0, 7))
        assertEquals(it.next(), TestLabel(0, 5))
        assertFalse(it.hasNext())
    }
}
