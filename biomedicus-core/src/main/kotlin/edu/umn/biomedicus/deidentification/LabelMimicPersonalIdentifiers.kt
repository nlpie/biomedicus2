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

package edu.umn.biomedicus.deidentification

import com.google.inject.ImplementedBy
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.DocumentTask
import edu.umn.nlpengine.labeler
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for locating MIMIC personal identifiers.
 */
@ImplementedBy(RegexMimicIdentifiers::class)
interface MimicIdentifiers {
    /**
     * Finds personal identifiers in MIMIC [text] and returns them as a [Sequence] of
     * [PersonalIdentifier] labels.
     */
    fun findPersonalIdentifiers(text: String): Sequence<PersonalIdentifier>
}

/**
 * Implementation using regular expressions.
 */
@Singleton
class RegexMimicIdentifiers : MimicIdentifiers {
    private val regex: Regex = Regex("\\[\\*\\*..*?\\*\\*]")

    override fun findPersonalIdentifiers(text: String) = regex.findAll(text)
        .map {
            PersonalIdentifier(it.range.start, it.range.endInclusive + 1)
        }
}

/**
 * Document task that locates MIMIC personal identifiers using a [MimicIdentifiers] and labels them
 * on the document.
 *
 * @property identifiers the [MimicIdentifiers] implementation to use
 */
class LabelMimicPersonalIdentifiers @Inject constructor(
    private val identifiers: MimicIdentifiers
) : DocumentTask {
    override fun run(document: Document) {
        val labeler = document.labeler<PersonalIdentifier>()
        identifiers.findPersonalIdentifiers(document.text).forEach { labeler.add(it) }
    }
}
