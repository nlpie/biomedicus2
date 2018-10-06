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

package edu.umn.biomedicus.family

import edu.umn.biomedicus.annotations.Setting
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.*
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton

class FamilyModule : SystemModule() {
    override fun setup() {
        addLabelClass<Relative>()
    }
}

/**
 * A family relation.
 *
 * @property startIndex the start in text
 * @property endIndex the end in text
 * @property value the word referencing the relative
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class Relative(
        override val startIndex: Int,
        override val endIndex: Int,
        val value: String
) : Label() {
    constructor(textRange: TextRange, value: String) : this(textRange.startIndex, textRange.endIndex, value)
}

/**
 * The shared resource containing a list of different family member words.
 */
@Singleton
class RelativeModel @Inject constructor(
        @Setting("family.relatives.asDataPath") relativesPath: Path
) {
    private val relatives: List<String> = Files.readAllLines(relativesPath)

    fun isRelative(word: String): Boolean {
        return relatives.any { it.compareTo(word, true) == 0 }
    }
}

/**
 * Labels relatives in text.
 */
class RelativeLabeler @Inject internal constructor(
        private val model: RelativeModel
) : DocumentTask {
    override fun run(document: Document) {
        val labeler = document.labeler(Relative::class.java)

        for ((startIndex, endIndex, text, _) in document.labelIndex(ParseToken::class.java)) {
            if (model.isRelative(text)) {
                labeler.add(Relative(startIndex, endIndex, text))
            }
        }
    }
}
