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

import com.google.inject.Inject
import com.google.inject.Singleton
import edu.umn.biomedicus.framework.LabelAliases
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.Label
import org.apache.uima.cas.CAS
import org.apache.uima.cas.Type
import org.apache.uima.cas.text.AnnotationFS
import java.util.*


@Singleton
class LabelAdapters @Inject
constructor(private val labelAliases: LabelAliases?) {

    private val factoryMap = HashMap<Class<*>, LabelAdapterFactory<*>>()
    private val backMap = HashMap<String, LabelAdapterFactory<*>>()

    fun addFactory(labelAdapterFactory: LabelAdapterFactory<*>) {
        val tClass = labelAdapterFactory.labelClass
        factoryMap[labelAdapterFactory.labelClass] = labelAdapterFactory
        backMap[labelAdapterFactory.typeName] = labelAdapterFactory
        labelAliases?.addAlias(tClass.simpleName, tClass)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Label> getLabelAdapterFactory(tClass: Class<T>): LabelAdapterFactory<T> {
        return factoryMap[tClass]?.let { it as LabelAdapterFactory<T> }
                ?: throw IllegalArgumentException("No label adapter found for class: ${tClass.canonicalName}")
    }

    fun getLabelAdapterFactory(type: Type): LabelAdapterFactory<*> {
        return backMap[type.name] ?: throw NoSuchElementException("No label adapter for type:  ${type.name}")
    }
}

interface LabelAdapterFactory<T : Label> {

    val labelClass: Class<T>

    val typeName: String

    fun create(cas: CAS, document: Document?): LabelAdapter<T>
}

interface LabelAdapter<T : Label> {

    val labelClass: Class<T>

    val distinct: Boolean

    val type: Type

    fun labelToAnnotation(label: T): AnnotationFS

    fun annotationToLabel(annotationFS: AnnotationFS): T
}
