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

import edu.umn.biomedicus.exc.BiomedicusException
import edu.umn.nlpengine.TextRange
import org.apache.uima.cas.*
import org.apache.uima.cas.text.AnnotationFS
import org.apache.uima.resource.metadata.FeatureDescription
import org.apache.uima.resource.metadata.TypeDescription
import org.apache.uima.resource.metadata.TypeSystemDescription
import org.apache.uima.resource.metadata.impl.AllowedValue_impl
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

private val featureDesc = "Automatically generated feature"

@Singleton
class AutoAdapters @Inject constructor(val labelAdapters: LabelAdapters) {
    private val distincts = ArrayList<Class<out TextRange>>()
    private val indistinct = ArrayList<Class<out TextRange>>()
    private val enums = ArrayList<Class<*>>()

    fun addDistinctAutoAdaptedClass(clazz: Class<out TextRange>) {
        distincts.add(clazz)
    }

    fun addAllDistinctAutoAdaptedClasses(elements: Iterable<Class<out TextRange>>) {
        elements.forEach { addDistinctAutoAdaptedClass(it) }
    }

    fun addIndistinctAutoAdaptedClass(clazz: Class<out TextRange>) {
        indistinct.add(clazz)
    }

    fun addAllIndistinctAutoAdaptedClasses(elements: Iterable<Class<out TextRange>>) {
        elements.forEach { addIndistinctAutoAdaptedClass(it) }
    }

    fun addEnumClass(clazz: Class<*>) {
        enums.add(clazz)
    }

    fun addAllEnumClasses(elements: Iterable<Class<*>>) {
        elements.forEach { addEnumClass(it) }
    }

    fun addToTypeSystem(typeSystemDescription: TypeSystemDescription) {

        enums.forEach { typeSystemDescription.addEnum(it) }

        val map = distincts.asSequence().map { Pair(it, AutoAdapter<TextRange>(it, true)) }
                .plus(indistinct.asSequence()
                        .map { Pair(it, AutoAdapter<TextRange>(it, false)) })
                .toMap()

        map.values.onEach { it.addTypeToTypeSystem(typeSystemDescription) }
                .onEach { it.addFeaturesToTypeSystem(map) }

        map.forEach { clazz, adapter ->
            labelAdapters.addLabelAdapter(clazz, adapter)
        }
    }
}

fun TypeSystemDescription.addEnum(clazz: Class<*>) {
    val type = addType("edu.umn.biomedicus.types.auto." + clazz.simpleName,
            "Automatically generated type from ${clazz.canonicalName}",
            "uima.cas.String")
    type.allowedValues = clazz.enumConstants.map { it as Enum<*> }.map { it.name }
            .map { AllowedValue_impl(it, "Auto generated from enum constant") }
            .toList()
            .toTypedArray()
}

class AutoAdapter<T : TextRange>(
        private val clazz: KClass<out TextRange>,
        private val distinct: Boolean
) : LabelAdapterFactory {
    constructor(clazz: Class<out TextRange>, distinct: Boolean) : this(clazz.kotlin, distinct)

    private val primaryConstructor = clazz.primaryConstructor!!

    private var typeDescription: TypeDescription? = null

    private val propertyMappings = clazz.memberProperties
            .filter { it.name != "startIndex" && it.name != "endIndex" }
            .map { PropertyMapping(it) }

    private var isInitialized = false

    fun addTypeToTypeSystem(description: TypeSystemDescription) {
        typeDescription = description.addType("edu.umn.biomedicus.types.auto." + clazz.simpleName,
                "Automatically generated type from ${clazz.qualifiedName}",
                "uima.tcas.Annotation")
    }

    fun addFeaturesToTypeSystem(map: Map<Class<out TextRange>, AutoAdapter<out TextRange>>) {
        for (propertyMapping in propertyMappings) {
            propertyMapping.addFeatures(map)
            propertyMapping.initType(map)
        }
    }

    override fun create(cas: CAS): AbstractLabelAdapter<T> {
        val typeDescription = checkNotNull(typeDescription)

        synchronized(this) {
            if (!isInitialized) {
                propertyMappings.forEach { it.initFeat(cas) }
            }
        }

        return object : AbstractLabelAdapter<T>(cas, cas.typeSystem.getType(typeDescription.name)) {
            override fun annotationToLabel(annotationFS: AnnotationFS): TextRange {
                val parameters = HashMap<KParameter, Any>()
                for (propertyMapping in propertyMappings) {
                    val value = propertyMapping.copyFromAnnotation(annotationFS)

                    val parameter = primaryConstructor
                            .findParameterByName(propertyMapping.property.name)

                    parameters[parameter!!] = value
                }

                parameters[primaryConstructor.findParameterByName("startIndex")!!] =
                        annotationFS.begin

                parameters[primaryConstructor.findParameterByName("endIndex")!!] =
                        annotationFS.end

                return primaryConstructor.callBy(parameters)
            }

            override fun fillAnnotation(label: T, annotationFS: AnnotationFS) {
                for (propertyMapping in propertyMappings) {
                    propertyMapping.copyToAnnotation(label, annotationFS)
                }
            }

            override fun isDistinct(): Boolean {
                return distinct
            }
        }
    }


    inner class PropertyMapping<T : TextRange>(
            val property: KProperty1<T, *>
    ) {
        private val returnType: KClassifier = property.returnType.classifier!!

        private var featureDescription: FeatureDescription? = null

        private var copyToAnnotation: ((T, AnnotationFS) -> Unit)? = null

        private var copyFromAnnotation: ((AnnotationFS) -> Any)? = null

        private var feat: Feature? = null

        fun initFeat(cas: CAS) {
            feat = cas.typeSystem.getType(typeDescription!!.name).getFeatureByBaseName(property.name)
        }

        @Suppress("UNCHECKED_CAST")
        fun copyToAnnotation(label: TextRange, annotationFS: AnnotationFS) =
                copyToAnnotation!!.invoke(label as T, annotationFS)

        fun copyFromAnnotation(annotationFS: AnnotationFS): Any =
                copyFromAnnotation!!.invoke(annotationFS)

        fun addFeatures(
                map: Map<Class<out TextRange>, AutoAdapter<out TextRange>>) {
            val typeDescription = checkNotNull(typeDescription)

            featureDescription = when (returnType) {
                Boolean::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.Boolean"
                )
                Byte::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.Byte"
                )
                Short::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.Short"
                )
                Int::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.Integer"
                )
                Float::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.Float"
                )
                Double::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.Double"
                )
                String::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.String"
                )
                BooleanArray::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.BooleanArray"
                )
                ByteArray::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.ByteArray"
                )
                ShortArray::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.ShortArray"
                )
                IntArray::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.IntArray"
                )
                FloatArray::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.FloatArray"
                )
                DoubleArray::class -> typeDescription.addFeature(
                        property.name,
                        featureDesc,
                        "uima.cas.DoubleArray"
                )
                Array<Any>::class -> {
                    if ((returnType as KClass<*>).java.componentType == String::class.java)
                        typeDescription.addFeature(
                                property.name,
                                featureDesc,
                                "uima.cas.StringArray"
                        )
                    else
                        typeDescription.addFeature(
                                property.name,
                                featureDesc,
                                "uima.cas.FSArray"
                        )
                }
                else -> {
                    val javaClass = (returnType as KClass<*>).java
                    if (javaClass.isEnum) {
                        typeDescription.addFeature(
                                property.name,
                                featureDesc,
                                "edu.umn.biomedicus.types.auto.${javaClass.simpleName}"
                        )
                    } else {
                        val referenceClass = javaClass.asSubclass(TextRange::class.java)
                        val adapter = map[referenceClass]
                                ?: throw BiomedicusException("Type not found: $referenceClass")

                        typeDescription.addFeature(
                                property.name,
                                featureDesc,
                                adapter.typeDescription?.name
                                        ?: throw BiomedicusException("Type description not set on $referenceClass")
                        )
                    }
                }
            }
        }

        fun initType(map: Map<Class<out TextRange>, AutoAdapter<out TextRange>>) {
            when (returnType) {
                Boolean::class -> {
                    copyToAnnotation = { label, annotation ->
                        annotation.setBooleanValue(feat, property.get(label) as Boolean)
                    }
                    copyFromAnnotation = { annotation ->
                        annotation.getBooleanValue(feat)
                    }
                }
                Byte::class -> {
                    copyToAnnotation = { label, annotation ->
                        annotation.setByteValue(feat, property.get(label) as Byte)
                    }
                    copyFromAnnotation = { annotation ->
                        annotation.getByteValue(feat)
                    }
                }
                Short::class -> {
                    copyToAnnotation = { label, annotation ->
                        annotation.setShortValue(feat, property.get(label) as Short)
                    }
                    copyFromAnnotation = { annotation ->
                        annotation.getShortValue(feat)
                    }
                }
                Int::class -> {
                    copyToAnnotation = { label, annotation ->
                        annotation.setIntValue(feat, property.get(label) as Int)
                    }
                    copyFromAnnotation = { annotation ->
                        annotation.getIntValue(feat)
                    }
                }
                Float::class -> {
                    copyToAnnotation = { label, annotation ->
                        annotation.setFloatValue(feat, property.get(label) as Float)
                    }
                    copyFromAnnotation = { annotation ->
                        annotation.getFloatValue(feat)
                    }
                }
                Double::class -> {
                    copyToAnnotation = { label, annotation ->
                        annotation.setDoubleValue(feat, property.get(label) as Double)
                    }
                    copyFromAnnotation = { annotation ->
                        annotation.getDoubleValue(feat)
                    }
                }
                String::class -> {
                    copyToAnnotation = { label, annotation ->
                        annotation.setStringValue(feat, property.get(label) as String)
                    }
                    copyFromAnnotation = { annotation ->
                        annotation.getStringValue(feat)
                    }
                }
                BooleanArray::class -> {
                    copyToAnnotation = { label, annotation ->
                        val from = property.get(label) as BooleanArray
                        val to = annotation.cas.createBooleanArrayFS(from.size)
                        for (i in 0 until from.size) {
                            to[i] = from[i]
                        }
                        annotation.cas.addFsToIndexes(to)
                        annotation.setFeatureValue(feat, to)
                    }
                    copyFromAnnotation = { annotation ->
                        val from = annotation.getFeatureValue(feat) as BooleanArrayFS
                        BooleanArray(from.size(), { i -> from[i] })
                    }
                }
                ByteArray::class -> {
                    copyToAnnotation = { label, annotation ->
                        val from = property.get(label) as ByteArray
                        val to = annotation.cas.createByteArrayFS(from.size)
                        for (i in 0 until from.size) {
                            to[i] = from[i]
                        }
                        annotation.cas.addFsToIndexes(to)
                        annotation.setFeatureValue(feat, to)
                    }
                    copyFromAnnotation = { annotation ->
                        val from = annotation.getFeatureValue(feat) as ByteArrayFS
                        ByteArray(from.size(), { i -> from[i] })
                    }
                }
                ShortArray::class -> {
                    copyToAnnotation = { label, annotation ->
                        val from = property.get(label) as ShortArray
                        val to = annotation.cas.createShortArrayFS(from.size)
                        for (i in 0 until from.size) {
                            to[i] = from[i]
                        }
                        annotation.cas.addFsToIndexes(to)
                        annotation.setFeatureValue(feat, to)
                    }
                    copyFromAnnotation = { annotation ->
                        val from = annotation.getFeatureValue(feat) as ShortArrayFS
                        ShortArray(from.size(), { i -> from[i] })
                    }
                }
                IntArray::class -> {
                    copyToAnnotation = { label, annotation ->
                        val from = property.get(label) as IntArray
                        val to = annotation.cas.createIntArrayFS(from.size)
                        for (i in 0 until from.size) {
                            to[i] = from[i]
                        }
                        annotation.cas.addFsToIndexes(to)
                        annotation.setFeatureValue(feat, to)
                    }
                    copyFromAnnotation = { annotation ->
                        val from = annotation.getFeatureValue(feat) as IntArrayFS
                        IntArray(from.size(), { i -> from[i] })
                    }
                }
                FloatArray::class -> {
                    copyToAnnotation = { label, annotation ->
                        val from = property.get(label) as FloatArray
                        val to = annotation.cas.createFloatArrayFS(from.size)
                        for (i in 0 until from.size) {
                            to[i] = from[i]
                        }
                        annotation.cas.addFsToIndexes(to)
                        annotation.setFeatureValue(feat, to)
                    }
                    copyFromAnnotation = { annotation ->
                        val from = annotation.getFeatureValue(feat) as FloatArrayFS
                        FloatArray(from.size(), { i -> from[i] })
                    }
                }
                DoubleArray::class -> {
                    copyToAnnotation = { label, annotation ->
                        val from = property.get(label) as DoubleArray
                        val to = annotation.cas.createDoubleArrayFS(from.size)
                        for (i in 0 until from.size) {
                            to[i] = from[i]
                        }
                        annotation.cas.addFsToIndexes(to)
                        annotation.setFeatureValue(feat, to)
                    }
                    copyFromAnnotation = { annotation ->
                        val from = annotation.getFeatureValue(feat) as DoubleArrayFS
                        DoubleArray(from.size(), { i -> from[i] })
                    }
                }
                Array<Any>::class -> {
                    if ((returnType as KClass<*>).java.componentType == String::class.java) {
                        @Suppress("UNCHECKED_CAST")
                        copyToAnnotation = { label, annotation ->
                            val from = property.get(label) as Array<String>
                            val to = annotation.cas.createStringArrayFS(from.size)
                            for (i in 0 until from.size) {
                                to[i] = from[i]
                            }
                            annotation.cas.addFsToIndexes(to)
                            annotation.setFeatureValue(feat, to)
                        }
                        copyFromAnnotation = { annotation ->
                            val from = annotation.getFeatureValue(feat) as StringArrayFS
                            Array(from.size(), { i -> from[i] })
                        }
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        copyToAnnotation = { label, annotation ->
                            val from = property.get(label) as Array<TextRange>
                            val cas = annotation.cas
                            val to = cas.createArrayFS(from.size)
                            val valuesAdapter = (map[returnType.java.componentType]
                                    ?: throw BiomedicusException("")).create(cas)
                            for (i in 0 until from.size) {
                                to[i] = valuesAdapter.labelToAnnotation(from[i])
                            }
                            cas.addFsToIndexes(to)
                            annotation.setFeatureValue(feat, to)
                        }
                        copyFromAnnotation = { annotation ->
                            val from = annotation.getFeatureValue(feat) as ArrayFS
                            Array(from.size(), { i -> from[i] })
                        }
                    }
                }
                else -> {
                    if ((returnType as KClass<*>).java.isEnum) {
                        copyToAnnotation = { label, annotation ->
                            val enumVal = property.get(label) as Enum<*>
                            annotation.setStringValue(feat, enumVal.name)
                        }
                        copyFromAnnotation = {
                            val from = it.getStringValue(feat)
                            returnType.java.enumConstants
                                    .map { it as Enum<*> }
                                    .find { it.name == from }
                                    ?: throw IllegalStateException("Enum value not found")
                        }
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        copyToAnnotation = { label, annotation ->
                            val from = property.get(label) as Array<TextRange>
                            val cas = annotation.cas
                            val to = cas.createArrayFS(from.size)
                            val valuesAdapter = (map[returnType.java.componentType]
                                    ?: throw BiomedicusException("")).create(cas)
                            for (i in 0 until from.size) {
                                to[i] = valuesAdapter.labelToAnnotation(from[i])
                            }
                            cas.addFsToIndexes(to)
                            annotation.setFeatureValue(feat, to)
                        }
                        copyFromAnnotation = { annotation ->
                            val from = annotation.getFeatureValue(feat) as ArrayFS
                            Array(from.size(), { i -> from[i] })
                        }
                    }
                }
            }
        }
    }
}