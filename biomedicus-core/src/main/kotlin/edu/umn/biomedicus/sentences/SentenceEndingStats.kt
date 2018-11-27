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

package edu.umn.biomedicus.sentences

import edu.umn.nlpengine.Document
import edu.umn.nlpengine.DocumentsProcessor
import edu.umn.nlpengine.labelIndex

class SentenceEndingStats : DocumentsProcessor {

    val periodRegex = Regex("[.]\\s*$")
    val exRegex = Regex("[!]\\s*$")
    val questRegex = Regex("[?]\\s*$")
    val semiRegex = Regex("[;]\\s*$")
    val colonRegex = Regex("[:]\\s*$")
    val quoteRegex = Regex("[.!?;:][\"”]\\s*\$")

    var periods = 0
    var exclams = 0
    var questions = 0
    var semicolon = 0
    var colon = 0
    var quote = 0
    var none = 0

    override fun process(document: Document) {

        val sentences = document.labelIndex<Sentence>()
        for (sentence in sentences) {
            if (sentence.sentenceClass == Sentence.unknown) continue
            val text = sentence.coveredText(document.text)
            when {
                periodRegex.containsMatchIn(text) -> periods++
                exRegex.containsMatchIn(text) -> exclams++
                questRegex.containsMatchIn(text) -> questions++
                semiRegex.containsMatchIn(text) -> semicolon++
                colonRegex.containsMatchIn(text) -> colon++
                quoteRegex.containsMatchIn(text) -> quote++
                else -> none++
            }
        }
    }

    override fun done() {
        println("periods: $periods")
        println("exclams: $exclams")
        println("questions: $questions")
        println("semicolon: $semicolon")
        println("colon: $colon")
        println("quote: $quote")
        println("none: $none")
    }
}
