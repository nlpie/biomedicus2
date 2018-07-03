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

package edu.umn.biomedicus.examples

import edu.umn.biomedicus.annotations.ComponentSetting
import edu.umn.biomedicus.annotations.Setting
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.DocumentTask
import edu.umn.nlpengine.labelIndex
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton

class ExampleKotlinTask @Inject constructor(
    @ComponentSetting("example.doSomething") val doSomething: Boolean,
    val resource: ExampleKotlinResource
) : DocumentTask {
    override fun run(document: Document) {
        for (sentence in document.labelIndex<Sentence>()) {
            val coveredText = sentence.coveredText ?: error("no covered text")
            if (doSomething && resource.isInValues(coveredText)) {
                // do something
            }
        }
    }
}

@Singleton
class ExampleKotlinResource(val values: List<String>) {
    @Inject constructor(
        @Setting("example.valuesFile") valuesFile: Path
    ) : this(Files.readAllLines(valuesFile))

    fun isInValues(cs: CharSequence): Boolean {
        return values.any { it.contentEquals(cs) }
    }
}
