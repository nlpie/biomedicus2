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

package edu.umn.biomedicus.uima.labels

import edu.umn.nlpengine.Document
import edu.umn.nlpengine.Label
import edu.umn.nlpengine.Labeler
import org.apache.uima.cas.impl.AnnotationImpl

class UimaLabeler<T : Label>(
        private val labelAdapter: LabelAdapter<T>,
        private val document: Document
) : Labeler<T> {

    override fun add(label: T) {
        val annotationFS = labelAdapter.labelToAnnotation(label)
        label.internalLabeledOnDocument = document
        label.internalLabelIdentifier = (annotationFS as? AnnotationImpl)?.address ?: throw IllegalStateException("Unable to get cas address to use as identifier.")
    }

    override fun addAll(elements: Iterable<T>) {
        elements.forEach { add(it) }
    }
}