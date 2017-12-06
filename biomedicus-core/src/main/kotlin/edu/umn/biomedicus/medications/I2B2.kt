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

package edu.umn.biomedicus.medications

import java.io.File
import java.util.*


fun main(args: Array<String>) {

    val inputDirectory = File(args[0])

    val counts = HashMap<String, Int>()

    val columnSplitter = Regex("\\|\\|")

    val dosageRegex = Regex("do=\"([\\p{all}&&[^\"]]*)\"")

    inputDirectory.walkBottomUp().forEach {
        if (!it.isFile) return@forEach

        it.forEachLine {
            val splits = columnSplitter.split(it)
            val dosageMatch = dosageRegex.find(splits[1])
            val dosageString = dosageMatch!!.groupValues[1]

            counts.compute(dosageString) { _, v ->
                (v ?: 0) + 1
            }
        }
    }

    val dosages = TreeMap<Int, ArrayList<String>>()

    counts.forEach { dosage, count ->
        dosages.compute(count) { _, v ->
            val result = v ?: ArrayList()
            result.add(dosage)
            result
        }
    }

    dosages.descendingMap().asSequence().take(100).forEach {
        println("dosage: \"${it.value}\" times: ${it.key}")
    }


}