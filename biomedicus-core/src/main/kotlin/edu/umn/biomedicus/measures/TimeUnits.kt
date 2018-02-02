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

package edu.umn.biomedicus.measures

import com.google.inject.Inject
import com.google.inject.Singleton
import edu.umn.biomedicus.annotations.Setting
import edu.umn.biomedicus.common.TextIdentifiers
import edu.umn.biomedicus.framework.DocumentProcessor
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.TextRange
import java.io.File
import java.nio.charset.StandardCharsets

data class TimeUnit(
        override val startIndex: Int,
        override val endIndex: Int
) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

@Singleton
class TimeUnits @Inject constructor(
        @Setting("measures.timeUnitsPath") timeUnitsPath: String
) {
    val words = File(timeUnitsPath).readLines(StandardCharsets.UTF_8)
}

class TimeUnitDetector @Inject constructor(
        val units: TimeUnits
) : DocumentProcessor {
    override fun process(document: Document) {
        val view = TextIdentifiers.getSystemLabeledText(document)

        val parseTokens = view.labelIndex<ParseToken>()

        val labeler = view.labeler<TimeUnit>()

        parseTokens
                .filter {
                    units.words.indexOfFirst { tu ->
                        it.text.compareTo(tu, true) == 0
                    } != -1
                }
                .forEach { labeler.add(TimeUnit(it)) }
    }
}

data class TimeFrequencyUnit(override val startIndex: Int, override val endIndex: Int) : TextRange {
    constructor(textRange: TextRange): this(textRange.startIndex, textRange.endIndex)
}

@Singleton
data class TimeFrequencyUnits(val units: List<String>) {
    @Inject constructor(@Setting("measures.timeFrequencyUnitsPath") path: String) :
            this(File(path).readLines(StandardCharsets.UTF_8))
}

data class TimeFrequencyUnitDetector(val units: List<String>) : DocumentProcessor {

    @Inject constructor(timeFrequencyUnits: TimeFrequencyUnits) : this(timeFrequencyUnits.units)

    override fun process(document: Document) {
        val view = TextIdentifiers.getSystemLabeledText(document)

        val parseTokens = view.labelIndex(ParseToken::class)

        val labeler = view.labeler(TimeFrequencyUnit::class)

        parseTokens
                .filter {
                    units.indexOfFirst { tu ->
                        it.text.compareTo(tu, true) == 0
                    } != -1
                }
                .forEach { labeler.add(TimeFrequencyUnit(it)) }
    }

}