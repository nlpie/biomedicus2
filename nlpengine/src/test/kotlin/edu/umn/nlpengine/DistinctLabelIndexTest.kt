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

import org.testng.Assert
import org.testng.Assert.*
import org.testng.annotations.Test
import java.util.*

class DistinctLabelIndexTest {
    data class TestLabel(override val startIndex: Int, override val endIndex: Int): Label()

    val tested = DistinctLabelIndex(
            TestLabel(0, 3),
            TestLabel(3, 5),
            TestLabel(6, 10),
            TestLabel(11, 15),
            TestLabel(16, 20)
    )

    @Test
    fun testHigherIndexTwoEqual() {
        assertEquals(tested.higherIndex(3), 1)
    }

    @Test
    fun testHigherIndexBeginEquals() {
        assertEquals(tested.higherIndex(6, 1, 3), 2)
    }

    @Test
    fun testHigherIndexInside() {
        assertEquals(tested.higherIndex(7, 1, 4), 3)
    }

    @Test
    fun testHigherIndexEndEquals() {
        assertEquals(tested.higherIndex(5, 1, 3), 2)
    }

    @Test
    fun testHigherIndexBeforeFirst() {
        assertEquals(tested.higherIndex(2, 1, 3), 1)
    }

    @Test
    fun testHigherIndexAfterEnd() {
        assertEquals(tested.higherIndex(16, 1, 3), -1)
    }

    @Test
    fun testLowerIndexTwoEqual() {
        assertEquals(tested.lowerIndex(3), 0)
    }

    @Test
    fun testLowerIndexBeginEquals() {
        assertEquals(tested.lowerIndex(6, 1, 3), 1)
    }

    @Test
    fun testLowerIndexEndEquals() {
        assertEquals(tested.lowerIndex(5, 1, 3), 1)
    }

    @Test
    fun testLowerIndexInside() {
        assertEquals(tested.lowerIndex(7, 1, 3), 1)
    }

    @Test
    fun testLowerIndexBeforeFirst() {
        assertEquals(tested.lowerIndex(2, 1, 3), -1)
    }

    @Test
    fun testLowerIndexAfterEnd() {
        assertEquals(tested.lowerIndex(16, 1, 3), 2)
    }

    @Test
    fun testContaining() {
        val containing = tested.containing(TestLabel(6, 10))

        assertEquals(containing.size, 1)

        val it = containing.iterator()
        assertEquals(it.next(), TestLabel(6, 10))
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
        val insideSpan = tested.insideSpan(1, 16)

        assertEquals(insideSpan.size, 3)

        val it = insideSpan.iterator()
        assertEquals(it.next(), TestLabel(3, 5))
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.next(), TestLabel(11, 15))
        assertFalse(it.hasNext())
    }

    @Test
    fun testInsideSpanBefore() {
        val insideSpan = tested.insideSpan(0, 2)

        assertEquals(insideSpan.size, 0)

        val it = insideSpan.iterator()
        assertFalse(it.hasNext())
    }

    @Test
    fun testInsideSpanAfter() {
        val insideSpan = tested.insideSpan(21, 25)

        assertEquals(insideSpan.size, 0)

        val it = insideSpan.iterator()
        assertFalse(it.hasNext())
    }

    @Test
    fun testAscendingBegin() {
        val ascendingBegin = tested.ascendingStartIndex()

        Assert.assertTrue(ascendingBegin === tested)
    }

    @Test
    fun testDescendingBegin() {
        val descendingBegin = tested.descendingStartIndex()

        assertEquals(descendingBegin.size, 5)

        val it = descendingBegin.iterator()
        assertEquals(it.next(), TestLabel(16, 20))
        assertEquals(it.next(), TestLabel(11, 15))
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.next(), TestLabel(3, 5))
        assertEquals(it.next(), TestLabel(0, 3))
        assertFalse(it.hasNext())
    }


    @Test
    fun testAscendingEnd() {
        val ascendingEnd = tested.ascendingEndIndex()

        Assert.assertTrue(ascendingEnd === tested)
    }

    @Test
    fun testDescendingEnd() {
        val descendingEnd = tested.descendingEndIndex()

        Assert.assertTrue(descendingEnd === tested)
    }

    @Test
    fun testToTheLeftOf() {
        val toTheLeftOf = tested.toTheLeftOf(10)

        assertEquals(toTheLeftOf.size, 3)

        val it = toTheLeftOf.iterator()
        assertEquals(it.next(), TestLabel(0, 3))
        assertEquals(it.next(), TestLabel(3, 5))
        assertEquals(it.next(), TestLabel(6, 10))
        assertFalse(it.hasNext())
    }

    @Test
    fun testToTheRightOf() {
        val toTheRightOf = tested.toTheRightOf(3)

        assertEquals(toTheRightOf.size, 4)

        val it = toTheRightOf.iterator()
        assertEquals(it.next(), TestLabel(3, 5))
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.next(), TestLabel(11, 15))
        assertEquals(it.next(), TestLabel(16, 20))
        assertFalse(it.hasNext())
    }

    @Test
    fun testFirstEmpty() {
        val standardLabelIndex = StandardLabelIndex<TestLabel>()

        Assert.assertNull(standardLabelIndex.first())
    }

    @Test
    fun testFirst() {
        assertEquals(tested.first(), TestLabel(0, 3))
    }

    @Test
    fun testGetOne() {
        val get = tested.atLocation(TestLabel(3, 5))

        assertEquals(get.size, 1)
        assertEquals(get.iterator().next(), TestLabel(3, 5))
    }

    @Test
    fun testGetNone() {
        val get = tested.atLocation(TestLabel(0, 30))

        assertEquals(get.size, 0)
    }

    @Test
    fun testContainsTrue() {
        Assert.assertTrue(tested.contains(TestLabel(3, 5)))
    }

    @Test
    fun testContainsFalse() {
        assertFalse(tested.contains(TestLabel(0, 30)))
    }

    @Test
    fun testContainsSpanTrue() {
        Assert.assertTrue(tested.containsSpan(TestLabel(3, 5)))
    }

    @Test
    fun testContainsSpanFalse() {
        assertFalse(tested.containsSpan(TestLabel(0, 30)))
    }

    @Test
    fun testAsList() {
        val asList = tested.asList()

        assertEquals(asList,
                Arrays.asList(TestLabel(0, 3),
                        TestLabel(3, 5),
                        TestLabel(6, 10),
                        TestLabel(11, 15),
                        TestLabel(16, 20))
        )
    }

    @Test
    fun testAsListIndexOf() {
        val asList = tested.asList()

        assertEquals(asList.indexOf(TestLabel(3, 5)), 1)
    }

    @Test
    fun testAsListIndexOfNone() {
        val asList = tested.asList()

        assertEquals(asList.indexOf(TestLabel(0, 30)), -1)
    }

    @Test
    fun testAsListLastIndexOf() {
        val asList = tested.asList()

        assertEquals(asList.lastIndexOf(TestLabel(3, 5)), 1)
    }

    @Test
    fun testAsListLastIndexOfNone() {
        val asList = tested.asList()

        assertEquals(asList.indexOf(TestLabel(0, 30)), -1)
    }

    @Test
    fun testAsListContains() {
        val asList = tested.asList()

        Assert.assertTrue(asList.contains(TestLabel(3, 5)))
    }

    @Test
    fun testAsListContainsFalse() {
        val asList = tested.asList()

        assertFalse(asList.contains(TestLabel(0, 30)))
    }

    val ascending = tested.insideSpan(0, 20)
    val insideSpan = ascending.insideSpan(1, 20)
    val descending = tested.descendingStartIndex()

    @Test
    fun testAscendingViewSize() {
        assertEquals(ascending.size, 5)
    }

    @Test
    fun testDescendingViewSize() {
        assertEquals(descending.size, 5)
    }

    @Test
    fun testAscendingFirst() {
        assertEquals(ascending.first(), TestLabel(0, 3))
    }

    @Test
    fun testDescendingFirst() {
        assertEquals(descending.first(), TestLabel(16, 20))
    }

    @Test
    fun testViewGet() {
        val get = descending.atLocation(TestLabel(16, 20))
        assertEquals(get.size, 1)

        assertEquals(get.iterator().next(), TestLabel(16, 20))
    }

    @Test
    fun testViewGetNotInsideView() {
        val get = descending.atLocation(TestLabel(0, 0))
        assertEquals(get.size, 0)
        assertFalse(get.iterator().hasNext())
    }

    @Test
    fun testViewGetOutsideView() {
        val get = insideSpan.atLocation(0, 3)
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
        assertTrue(descending.contains(TestLabel(3, 5)))
    }

    @Test
    fun testViewContainsNotFound() {
        assertFalse(descending.contains(TestLabel(0, 0)))
    }

    @Test
    fun testViewNotContains() {
        val nothing = tested.insideSpan(0, 0)
        assertFalse(nothing.contains(TestLabel(3, 5)))
    }

    @Test
    fun testViewContainsSpan() {
        assertTrue(descending.containsSpan(3, 5))
    }

    @Test
    fun testViewContainsSpanFalse() {
        assertFalse(descending.containsSpan(0, 0))
    }

    @Test
    fun testViewContainsSpanOutsideView() {
        val nothing = tested.insideSpan(0, 0)
        assertFalse(nothing.containsSpan(3, 5))
    }

    @Test
    fun testViewToTheLeftOf() {
        val toTheLeft = tested.toTheLeftOf(10)

        assertEquals(toTheLeft.size, 3)

        val it = toTheLeft.iterator()
        assertEquals(it.next(), TestLabel(0, 3))
        assertEquals(it.next(), TestLabel(3, 5))
        assertEquals(it.next(), TestLabel(6, 10))
    }

    @Test
    fun testViewToTheRightOf() {
        val toTheRight = tested.toTheRightOf(6)

        assertEquals(toTheRight.size, 3)

        val it = toTheRight.iterator()
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.next(), TestLabel(11, 15))
        assertEquals(it.next(), TestLabel(16, 20))
        assertFalse(it.hasNext())
    }

    @Test
    fun testViewInsideSpan() {
        assertEquals(insideSpan.size, 4)

        val it = insideSpan.iterator()
        assertEquals(it.next(), TestLabel(3, 5))
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.next(), TestLabel(11, 15))
        assertEquals(it.next(), TestLabel(16, 20))
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
    fun testViewInsideSpanAfter() {
        val insideSpan = ascending.insideSpan(21, 24)

        assertEquals(insideSpan.size, 0)

        val it = insideSpan.iterator()
        assertFalse(it.hasNext())
    }

    @Test
    fun testViewContaining() {
        val containing = ascending.containing(7, 10)

        assertEquals(containing.size, 1)

        val it = containing.iterator()
        assertEquals(it.next(), TestLabel(6, 10))
        assertFalse(it.hasNext())
    }

    @Test
    fun testDescendingUpdateBounds() {
        val descendingInsideSpan = descending.insideSpan(3, 17)

        assertEquals(descendingInsideSpan.size, 3)

        val it = descendingInsideSpan.iterator()
        assertEquals(it.next(), TestLabel(11, 15))
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.next(), TestLabel(3, 5))
        assertFalse(it.hasNext())
    }

    @Test
    fun testDescendingAscendingBegin() {
        val ascendingBegin = descending.ascendingStartIndex()

        assertEquals(ascendingBegin.size, 5)

        val it = ascendingBegin.iterator()
        assertEquals(it.next(), TestLabel(0, 3))
        assertEquals(it.next(), TestLabel(3, 5))
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.next(), TestLabel(11, 15))
        assertEquals(it.next(), TestLabel(16, 20))
        assertFalse(it.hasNext())
    }

    @Test
    fun testDescendingDescendingBegin() {
        val descendingBegin = descending.descendingStartIndex()

        assertTrue(descendingBegin === descending)
    }

    @Test
    fun testDescendingAscendingEnd() {
        val ascendingEnd = descending.ascendingEndIndex()

        assertTrue(descending === ascendingEnd)
    }

    @Test
    fun testDescendingDescendingEnd() {
        val descendingEnd = descending.descendingEndIndex()

        assertTrue(descendingEnd === descendingEnd)
    }

    val descendingList = descending.asList()

    @Test
    fun testDescendingListIsEmpty() {
        assertFalse(descendingList.isEmpty())
    }

    @Test
    fun testDescendingListSize() {
        assertEquals(descendingList.size, 5)
    }

    @Test
    fun testDescendingListContainsTrue() {
        assertTrue(descendingList.contains(TestLabel(16, 20)))
    }

    @Test
    fun testDescendingListContainsFalse() {
        assertFalse(descendingList.contains(TestLabel(0, 40)))
    }

    @Test
    fun testDescendingListContainsAll() {
        assertTrue(descendingList.containsAll(listOf(TestLabel(3, 5), TestLabel(6, 10), TestLabel(11, 15))))
    }

    @Test
    fun testDescendingListContainsAllFalse() {
        assertFalse(descendingList.containsAll(listOf(TestLabel(0, 3), TestLabel(3, 5), TestLabel(6, 10), TestLabel(3, 20))))
    }

    @Test
    fun testDescendingListGet() {
        assertEquals(descendingList[0], TestLabel(16, 20))
    }

    @Test
    fun testDescendingListIndexOf() {
        assertEquals(descendingList.indexOf(TestLabel(3, 5)), 3)
    }

    @Test
    fun testDescendingListSubList() {
        val subList = descendingList.subList(1, 4)

        val it = subList.iterator()
        assertEquals(it.next(), TestLabel(11, 15))
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.next(), TestLabel(3, 5))
        assertFalse(it.hasNext())
    }

    @Test
    fun testDescendingListIterator() {
        val it = descending.asList().listIterator()

        assertFalse(it.hasPrevious())

        assertEquals(it.nextIndex(), 0)
        assertEquals(it.next(), TestLabel(16, 20))
        assertEquals(it.previousIndex(), 0)

        assertEquals(it.nextIndex(), 1)
        assertEquals(it.next(), TestLabel(11, 15))
        assertEquals(it.previousIndex(), 1)

        assertEquals(it.nextIndex(), 2)
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.previousIndex(), 2)

        assertEquals(it.nextIndex(), 3)
        assertEquals(it.next(), TestLabel(3, 5))
        assertEquals(it.previousIndex(), 3)

        assertEquals(it.nextIndex(), 4)
        assertEquals(it.next(), TestLabel(0, 3))
        assertEquals(it.previousIndex(), 4)

        assertFalse(it.hasNext())
        assertTrue(it.hasPrevious())

        assertEquals(it.previous(), TestLabel(0, 3))
        assertEquals(it.previous(), TestLabel(3, 5))
        assertEquals(it.previous(), TestLabel(6, 10))
        assertEquals(it.previous(), TestLabel(11, 15))
        assertEquals(it.previous(), TestLabel(16, 20))

        assertFalse(it.hasPrevious())
        assertTrue(it.hasNext())
    }

    /**
     * Start of ascending view tests
     */

    @Test
    fun testAscendingUpdateBounds() {
        val ascendingInsideSpan = ascending.insideSpan(3, 17)

        assertEquals(ascendingInsideSpan.size, 3)

        val it = ascendingInsideSpan.iterator()
        assertEquals(it.next(), TestLabel(3, 5))
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.next(), TestLabel(11, 15))
        assertFalse(it.hasNext())
    }

    @Test
    fun testAscendingAscendingBegin() {
        val ascendingBegin = ascending.ascendingStartIndex()

        assertTrue(ascendingBegin === ascending)
    }

    @Test
    fun testAscendingDescendingBegin() {
        val descendingBegin = ascending.descendingStartIndex()

        assertEquals(descendingBegin.size, 5)

        val it = descendingBegin.iterator()
        assertEquals(it.next(), TestLabel(16, 20))
        assertEquals(it.next(), TestLabel(11, 15))
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.next(), TestLabel(3, 5))
        assertEquals(it.next(), TestLabel(0, 3))
        assertFalse(it.hasNext())
    }

    @Test
    fun testAscendingAscendingEnd() {
        val ascendingEnd = ascending.ascendingEndIndex()

        assertTrue(ascending === ascendingEnd)
    }

    @Test
    fun testAscendingDescendingEnd() {
        val descendingEnd = ascending.descendingEndIndex()

        assertTrue(ascending === descendingEnd)
    }

    val ascendingList = ascending.asList()
    val insideSpanList = insideSpan.asList()

    @Test
    fun testAscendingListIsEmpty() {
        assertFalse(ascendingList.isEmpty())
    }

    @Test
    fun testAscendingListSize() {
        assertEquals(insideSpanList.size, 4)
    }

    @Test
    fun testAscendingListContainsTrue() {
        assertTrue(insideSpanList.contains(TestLabel(16, 20)))
    }

    @Test
    fun testAscendingListContainsFalse() {
        assertFalse(insideSpanList.contains(TestLabel(0, 3)))
    }

    @Test
    fun testAscendingListContainsAll() {
        assertTrue(insideSpanList.containsAll(listOf(TestLabel(3, 5), TestLabel(6, 10), TestLabel(11, 15))))
    }

    @Test
    fun testAscendingListContainsAllFalse() {
        assertFalse(insideSpanList.containsAll(listOf(TestLabel(0, 3), TestLabel(3, 5), TestLabel(6, 10))))
    }

    @Test
    fun testAscendingListGet() {
        assertEquals(insideSpanList[0], TestLabel(3, 5))
    }

    @Test
    fun testAscendingListIndexOf() {
        assertEquals(insideSpanList.indexOf(TestLabel(3, 5)), 0)
    }

    @Test
    fun testAscendingListSubList() {
        val subList = insideSpanList.subList(1, 4)

        val it = subList.iterator()
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.next(), TestLabel(11, 15))
        assertEquals(it.next(), TestLabel(16, 20))
        assertFalse(it.hasNext())
    }

    @Test
    fun testAscendingListIterator() {
        val it = ascending.asList().listIterator()

        assertFalse(it.hasPrevious())

        assertEquals(it.nextIndex(), 0)
        assertEquals(it.next(), TestLabel(0, 3))
        assertEquals(it.previousIndex(), 0)

        assertEquals(it.nextIndex(), 1)
        assertEquals(it.next(), TestLabel(3, 5))
        assertEquals(it.previousIndex(), 1)

        assertEquals(it.nextIndex(), 2)
        assertEquals(it.next(), TestLabel(6, 10))
        assertEquals(it.previousIndex(), 2)

        assertEquals(it.nextIndex(), 3)
        assertEquals(it.next(), TestLabel(11, 15))
        assertEquals(it.previousIndex(), 3)

        assertEquals(it.nextIndex(), 4)
        assertEquals(it.next(), TestLabel(16, 20))
        assertEquals(it.previousIndex(), 4)

        assertFalse(it.hasNext())
        assertTrue(it.hasPrevious())

        assertEquals(it.previous(), TestLabel(16, 20))
        assertEquals(it.previous(), TestLabel(11, 15))
        assertEquals(it.previous(), TestLabel(6, 10))
        assertEquals(it.previous(), TestLabel(3, 5))
        assertEquals(it.previous(), TestLabel(0, 3))

        assertFalse(it.hasPrevious())
        assertTrue(it.hasNext())
    }
}