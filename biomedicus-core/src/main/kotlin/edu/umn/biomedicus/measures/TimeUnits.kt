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
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.*
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Some kind of time unit: days, months, years, hours
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class TimeUnit(
        override val startIndex: Int,
        override val endIndex: Int
) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * A time frequency: hourly, daily, weekly, yearly.
 */
@LabelMetadata(versionId = "2_0", distinct = true)
data class TimeFrequencyUnit(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange): this(textRange.startIndex, textRange.endIndex)
}

@Singleton
class TimeUnits @Inject constructor(
        @Setting("measures.timeUnitsPath") timeUnitsPath: String
) {
    val words = File(timeUnitsPath).readLines(StandardCharsets.UTF_8)
}

class TimeUnitDetector @Inject constructor(
        val units: TimeUnits
) : DocumentOperation {
    override fun process(document: Document) {
        val parseTokens = document.labelIndex<ParseToken>()

        val labeler = document.labeler<TimeUnit>()

        parseTokens
                .filter {
                    units.words.indexOfFirst { tu ->
                        it.text.compareTo(tu, true) == 0
                    } != -1
                }
                .forEach { labeler.add(TimeUnit(it)) }
    }
}

@Singleton
data class TimeFrequencyUnits(val units: List<String>) {
    @Inject constructor(@Setting("measures.timeFrequencyUnitsPath") path: String) :
            this(File(path).readLines(StandardCharsets.UTF_8))
}

data class TimeFrequencyUnitDetector(val units: List<String>) : DocumentOperation {

    @Inject constructor(timeFrequencyUnits: TimeFrequencyUnits) : this(timeFrequencyUnits.units)

    override fun process(document: Document) {
        val parseTokens = document.labelIndex<ParseToken>()

        val labeler = document.labeler<TimeFrequencyUnit>()

        parseTokens
                .filter {
                    units.indexOfFirst { tu ->
                        it.text.compareTo(tu, true) == 0
                    } != -1
                }
                .forEach { labeler.add(TimeFrequencyUnit(it)) }
    }

}