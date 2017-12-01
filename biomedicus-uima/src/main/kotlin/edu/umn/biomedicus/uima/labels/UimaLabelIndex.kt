/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.labels

import com.google.inject.Inject
import edu.umn.nlpengine.*
import org.apache.uima.cas.CAS
import org.apache.uima.cas.Type
import org.apache.uima.cas.text.AnnotationFS
import org.apache.uima.cas.text.AnnotationIndex


class UimaLabelIndex<T : Label> @Inject constructor(
        private val cas: CAS, 
        private val labelAdapter: LabelAdapter<T>
) : AbstractLabelIndex<T>() {
    private val index: AnnotationIndex<AnnotationFS>

    private val annotationType: Type

    private val inflated: LabelIndex<T> by lazy {
        if (labelAdapter.isDistinct) DistinctLabelIndex(this) else StandardLabelIndex(this)
    }

    override val size: Int by lazy {
        var count = 0
        val it = iterator()
        while (it.hasNext()) {
            it.next()
            count++
        }
        count
    }

    init {
        annotationType = cas.typeSystem.getType("uima.tcas.Annotation")
        val type = labelAdapter.type
        index = cas.getAnnotationIndex(type)
    }

    override fun containing(startIndex: Int, endIndex: Int): LabelIndex<T> {
        return inflated.containing(startIndex, endIndex)
    }

    override fun insideSpan(startIndex: Int, endIndex: Int): LabelIndex<T> {
        return inflated.insideSpan(startIndex, endIndex)
    }

    override fun ascendingStartIndex(): LabelIndex<T> {
        return inflated.ascendingStartIndex()
    }

    override fun descendingStartIndex(): LabelIndex<T> {
        return inflated.descendingStartIndex()
    }

    override fun ascendingEndIndex(): LabelIndex<T> {
        return inflated.ascendingEndIndex()
    }

    override fun descendingEndIndex(): LabelIndex<T> {
        return inflated.descendingEndIndex()
    }

    override fun toTheLeftOf(index: Int): LabelIndex<T> {
        return inflated.toTheLeftOf(index)
    }

    override fun toTheRightOf(index: Int): LabelIndex<T> {
        return inflated.toTheRightOf(index)
    }

    override fun first(): T? {
        val it = iterator()
        return if (!it.hasNext()) {
            null
        } else it.next()
    }

    override fun atLocation(label: Label): Collection<T> {
        return inflated.atLocation(label)
    }

    override fun asList(): List<T> {
        return inflated.asList()
    }

    override fun containsSpan(label: Label): Boolean {
        return inflated.containsSpan(label)
    }

    override fun isEmpty(): Boolean {
        return !index.iterator().hasNext()
    }

    override fun iterator(): Iterator<T> {
        val iterator = index.iterator()
        return object : Iterator<T> {
            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun next(): T {
                return labelAdapter.annotationToLabel(iterator.next())
            }
        }
    }

    override fun contains(element: T): Boolean {
        return inflated.contains(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        return inflated.containsAll(elements)
    }
}
