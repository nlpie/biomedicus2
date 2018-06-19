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

package edu.umn.biomedicus.uima.util

import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Stage
import edu.umn.biomedicus.framework.Bootstrapper
import edu.umn.biomedicus.uima.labels.AutoAdapters
import org.apache.uima.UIMAFramework
import org.apache.uima.util.XMLInputSource
import java.io.File

/**
 * Writes the type system including runtime generated types to a file
 *
 * Usage: java edu.umn.biomedicus.uima.util.OutputTypeSystemKt [output file]
 */
fun main(args: Array<String>) {
    Bootstrapper.create(Guice.createInjector(Stage.DEVELOPMENT))
            .getInstance(OutputTypeSystem::class.java)
            .writeTypeSystem(args[0])
}

/**
 * Writes the type system including generated types to a XML file.
 */
class OutputTypeSystem @Inject constructor(
        private val adapters: AutoAdapters
) {
    fun writeTypeSystem(outputFile: String) {
        val tsDesc = OutputTypeSystem::class.java
                .getResourceAsStream("/edu/umn/biomedicus/types/TypeSystem.xml")
                .use {
                    UIMAFramework.getXMLParser()
                            .parseTypeSystemDescription(XMLInputSource(it, null))
                }

        adapters.addToTypeSystem(tsDesc)

        val out = File(outputFile)
        out.parentFile?.mkdirs()
        out.outputStream().use { tsDesc.toXML(it) }
    }
}
