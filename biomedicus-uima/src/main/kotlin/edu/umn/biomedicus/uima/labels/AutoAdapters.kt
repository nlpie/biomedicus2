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

import edu.umn.nlpengine.Label
import edu.umn.nlpengine.LabelMetadata
import edu.umn.nlpengine.Span
import edu.umn.nlpengine.Systems
import org.apache.uima.cas.*
import org.apache.uima.cas.text.AnnotationFS
import org.apache.uima.resource.metadata.FeatureDescription
import org.apache.uima.resource.metadata.TypeDescription
import org.apache.uima.resource.metadata.TypeSystemDescription
import org.apache.uima.resource.metadata.impl.AllowedValue_impl
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

private val featureDesc = "Automatically generated feature"

@Singleton
class AutoAdapters @Inject constructor(
        internal val labelAdapters: LabelAdapters,
        systems: Systems?
) {
    private val labels = ArrayList<Class<out Label>>()
    private val enums = ArrayList<Class<*>>()

    init {
        systems?.forEachEnumClass { enums.add(it) }
        systems?.forEachLabelClass { labels.add(it) }
    }

    fun addLabelClass(clazz: Class<out Label>) {
        labels.add(clazz)
    }

    fun addEnumClass(clazz: Class<*>) {
        enums.add(clazz)
    }


    fun addToTypeSystem(typeSystemDescription: TypeSystemDescription) {

        enums.forEach { typeSystemDescription.addEnum(it) }

        labels.asSequence()
                .map { it -> AutoAdapter(it, labelAdapters) }
                .onEach {
                    labelAdapters.addFactory(it)
                }
                .onEach {
                    it.addTypeToTypeSystem(typeSystemDescription)
                }
                .forEach {
                    it.addFeaturesToTypeSystem()
                }
    }

}


class AutoAdapter<T : Label>(
        override val labelClass: Class<T>,
        private var labelAdapters: LabelAdapters
) : LabelAdapterFactory<T> {
    private val clazz = labelClass.kotlin

    private val distinct: Boolean

    override val typeName: String

    @Suppress("UNCHECKED_CAST")
    private val primaryConstructor = (clazz.primaryConstructor
            ?: throw IllegalArgumentException("Class does not have primary constructor"))

    private var typeDescription: TypeDescription? = null

    private val propertyMappings: Collection<PropertyMapping<*>> = clazz.primaryConstructor
            ?.parameters
            ?.filter { it.name != "startIndex" && it.name != "endIndex" }
            ?.map { parameter ->
                Pair(parameter, clazz.memberProperties.firstOrNull { it.name == parameter.name }
                        ?: throw IllegalStateException(
                                "Property not found for primary constructor parameter " +
                                        "${parameter.name}"
                        )
                )
            }
            ?.map { (parameter, property) -> createPropertyMapping(property, parameter) }
            ?: throw IllegalStateException("")

    private var isInitialized = false

    init {
        distinct = (labelClass.kotlin.findAnnotation<LabelMetadata>()
                ?: throw IllegalStateException("Label class without @Label annotation")).distinct
        typeName = uimaTypeName(labelClass)
    }

    fun addTypeToTypeSystem(description: TypeSystemDescription) {
        typeDescription = description.addType(typeName,
                "Automatically generated type from ${clazz.qualifiedName}",
                "uima.tcas.Annotation")
    }

    fun addFeaturesToTypeSystem() {
        for (propertyMapping in propertyMappings) {
            propertyMapping.createFeatureDescription()
        }
    }

    override fun create(cas: CAS): LabelAdapter<T> {
        synchronized(this) {
            if (!isInitialized) {
                propertyMappings.forEach { it.initFeat(cas) }
                isInitialized = true
            }
        }

        return object : LabelAdapter<T> {
            override val type get() = cas.typeSystem.getType(typeName)

            override val labelClass: Class<T> = this@AutoAdapter.labelClass
            override val distinct = this@AutoAdapter.distinct

            override fun annotationToLabel(annotationFS: AnnotationFS): T {
                val parameters = HashMap<KParameter, Any?>()
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

            override fun labelToAnnotation(label: T): AnnotationFS {
                val annotation = cas
                        .createAnnotation<AnnotationFS>(type, label.startIndex, label.endIndex)

                propertyMappings.forEach { it.copyToAnnotation(label, cas, annotation) }

                cas.addFsToIndexes(annotation)
                return annotation
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createPropertyMapping(
            property: KProperty1<T, *>,
            parameter: KParameter
    ): PropertyMapping<*> {
        return when (property.returnType.classifier) {
            Boolean::class -> {
                checkPrimitiveProperty(property)
                BooleanPropertyMapping(property as KProperty1<T, Boolean>, parameter)
            }
            Byte::class -> {
                checkPrimitiveProperty(property)
                BytePropertyMapping(property as KProperty1<T, Byte>, parameter)
            }
            Short::class -> {
                checkPrimitiveProperty(property)
                ShortPropertyMapping(property as KProperty1<T, Short>, parameter)
            }
            Int::class -> {
                checkPrimitiveProperty(property)
                IntPropertyMapping(property as KProperty1<T, Int>, parameter)
            }
            Long::class -> {
                checkPrimitiveProperty(property)
                LongPropertyMapping(property as KProperty1<T, Long>, parameter)
            }
            Float::class -> {
                checkPrimitiveProperty(property)
                FloatPropertyMapping(property as KProperty1<T, Float>, parameter)
            }
            Double::class -> {
                checkPrimitiveProperty(property)
                DoublePropertyMapping(property as KProperty1<T, Double>, parameter)
            }
            String::class -> {
                StringPropertyMapping(property as KProperty1<T, String>, parameter)
            }
            BigDecimal::class -> {
                BigDecimalPropertyMapping(property as KProperty1<T, BigDecimal>, parameter)
            }
            Span::class -> {
                SpanPropertyMapping(property as KProperty1<T, Span>, parameter)
            }
            BooleanArray::class -> {
                BooleanArrayPropertyMapping(property as KProperty1<T, BooleanArray>, parameter)
            }
            ByteArray::class -> {
                ByteArrayPropertyMapping(property as KProperty1<T, ByteArray>, parameter)
            }
            ShortArray::class -> {
                ShortArrayPropertyMapping(property as KProperty1<T, ShortArray>, parameter)
            }
            IntArray::class -> {
                IntArrayPropertyMapping(property as KProperty1<T, IntArray>, parameter)
            }
            LongArray::class -> {
                LongArrayPropertyMapping(property as KProperty1<T, LongArray>, parameter)
            }
            FloatArray::class -> {
                FloatArrayPropertyMapping(property as KProperty1<T, FloatArray>, parameter)
            }
            DoubleArray::class -> {
                DoubleArrayPropertyMapping(property as KProperty1<T, DoubleArray>, parameter)
            }
            List::class -> {
                val componentClass = property.returnType.arguments.firstOrNull()
                        ?.type
                        ?.classifier
                        ?.let { it as KClass<*> }
                        ?: throw IllegalStateException("List should have a component type.")

                when {
                    componentClass == Boolean::class -> {
                        BooleanListPropertyMapping(
                                property as KProperty1<T, List<Boolean>>,
                                parameter
                        )
                    }
                    componentClass == Byte::class -> {
                        ByteListPropertyMapping(
                                property as KProperty1<T, List<Byte>>,
                                parameter
                        )
                    }
                    componentClass == Short::class -> {
                        ShortListPropertyMapping(
                                property as KProperty1<T, List<Short>>,
                                parameter
                        )
                    }
                    componentClass == Int::class -> {
                        IntListPropertyMapping(property as KProperty1<T, List<Int>>, parameter)
                    }
                    componentClass == Long::class -> {
                        LongListPropertyMapping(
                                property as KProperty1<T, List<Long>>,
                                parameter
                        )
                    }
                    componentClass == Float::class -> {
                        FloatListPropertyMapping(
                                property as KProperty1<T, List<Float>>,
                                parameter
                        )
                    }
                    componentClass == Double::class -> {
                        DoubleListPropertyMapping(
                                property as KProperty1<T, List<Double>>,
                                parameter
                        )
                    }
                    componentClass == String::class -> {
                        StringListPropertyMapping(
                                property as KProperty1<T, List<String>>,
                                parameter
                        )
                    }
                    componentClass.java.isEnum -> {
                        EnumListPropertyMapping(property as KProperty1<T, List<Any>>, parameter,
                                componentClass.java)
                    }
                    componentClass.isSubclassOf(Label::class) -> {
                        LabelListPropertyMapping(
                                property as KProperty1<T, List<Label>>,
                                parameter,
                                componentClass.java as Class<Label>
                        )
                    }
                    componentClass == Span::class -> {
                        SpanListPropertyMapping(
                                property as KProperty1<T, List<Span>>,
                                parameter
                        )
                    }
                    else -> {
                        throw IllegalStateException(
                                "Not able to map property ${property.name} with type " +
                                        "${property.returnType} on ${labelClass.canonicalName}"
                        )
                    }
                }
            }
            else -> {
                val clazz = (property.returnType.classifier as KClass<*>)
                when {
                    clazz.java.isEnum -> EnumPropertyMapping(property, parameter)
                    clazz.isSubclassOf(Label::class) -> {
                        LabelPropertyMapping(property as KProperty1<T, Label>, parameter)
                    }
                    clazz.java.isArray -> {
                        val componentType = clazz.java.componentType
                        when {
                            componentType == String::class.java -> {
                                StringArrayPropertyMapping(
                                        property as KProperty1<T, Array<String>>,
                                        parameter
                                )
                            }
                            componentType.isEnum -> {
                                EnumArrayPropertyMapping(
                                        property as KProperty1<T, Array<Any>>,
                                        parameter,
                                        componentType
                                )
                            }
                            Label::class.java.isAssignableFrom(componentType) -> {
                                LabelArrayPropertyMapping(
                                        property as KProperty1<T, Array<Label>>,
                                        parameter,
                                        componentType as Class<Label>
                                )
                            }
                            componentType == Span::class -> {
                                SpanArrayPropertyMapping(
                                        property as KProperty1<T, Array<Span>>,
                                        parameter
                                )
                            }
                            else -> {
                                throw IllegalStateException(
                                        "Not able to map property ${property.name} with type " +
                                                "${property.returnType} on ${labelClass.canonicalName}. " +
                                                "If you are trying to create a primitive array, use " +
                                                "the kotlin primitive array types, e.g. BooleanArray, IntArray, etc."
                                )
                            }
                        }
                    }
                    else -> {
                        throw IllegalStateException(
                                "Not able to map property ${property.name} with type " +
                                        "${property.returnType} on ${labelClass.canonicalName}"
                        )
                    }
                }
            }
        }
    }

    abstract inner class PropertyMapping<R : Any>(
            val property: KProperty1<T, R>,
            val parameter: KParameter
    ) {
        @Suppress("UNCHECKED_CAST")
        protected val returnType: KClass<R> = property.returnType.classifier as KClass<R>

        private var featureDescription: FeatureDescription? = null

        private var _feat: Feature? = null
        protected val feat: Feature
            get() = _feat ?: throw IllegalStateException("Feature not initialized")

        abstract val uimaType: String

        open fun initFeat(cas: CAS) {
            _feat = cas.typeSystem.getType(typeName)
                    .getFeatureByBaseName(featureDescription!!.name)
        }

        fun createFeatureDescription() {
            featureDescription = typeDescription
                    ?.addFeature(
                            property.name,
                            featureDesc,
                            uimaType
                    ) ?: throw IllegalStateException("typeDescription was null")
        }

        open fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            copyValueToAnnotation(property.get(label), cas, annotationFS)
        }

        open fun copyValueToAnnotation(value: R?, cas: CAS, annotationFS: AnnotationFS) {
            throw UnsupportedOperationException("Not implemented")
        }

        abstract fun copyFromAnnotation(annotationFS: AnnotationFS): R?
    }

    inner class BooleanPropertyMapping(
            property: KProperty1<T, Boolean>,
            parameter: KParameter
    ) : PropertyMapping<Boolean>(property, parameter) {
        override val uimaType: String = CAS.TYPE_NAME_BOOLEAN

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            annotationFS.setBooleanValue(feat, property.get(label))
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): Boolean? {
            return annotationFS.getBooleanValue(feat)
        }
    }

    inner class BytePropertyMapping(
            property: KProperty1<T, Byte>, parameter: KParameter
    ) : PropertyMapping<Byte>(property, parameter) {
        override val uimaType: String = CAS.TYPE_NAME_BYTE

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            annotationFS.setByteValue(feat, property.get(label))
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): Byte? {
            return annotationFS.getByteValue(feat)
        }
    }

    inner class ShortPropertyMapping(
            property: KProperty1<T, Short>,
            parameter: KParameter
    ) : PropertyMapping<Short>(property, parameter) {
        override val uimaType: String = CAS.TYPE_NAME_SHORT

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            annotationFS.setShortValue(feat, property.get(label))
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): Short? {
            return annotationFS.getShortValue(feat)
        }
    }

    inner class IntPropertyMapping(
            property: KProperty1<T, Int>,
            parameter: KParameter
    ) : PropertyMapping<Int>(property, parameter) {
        override val uimaType: String = CAS.TYPE_NAME_INTEGER

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            annotationFS.setIntValue(feat, property.get(label))
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): Int {
            return annotationFS.getIntValue(feat)
        }
    }

    inner class LongPropertyMapping(
            property: KProperty1<T, Long>,
            parameter: KParameter
    ) : PropertyMapping<Long>(property, parameter) {
        override val uimaType: String = CAS.TYPE_NAME_LONG

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            annotationFS.setLongValue(feat, property.get(label))
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): Long {
            return annotationFS.getLongValue(feat)
        }
    }

    inner class FloatPropertyMapping(
            property: KProperty1<T, Float>,
            parameter: KParameter
    ) : PropertyMapping<Float>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_FLOAT

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            annotationFS.setFloatValue(feat, property.get(label))
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): Float {
            return annotationFS.getFloatValue(feat)
        }
    }

    inner class DoublePropertyMapping(
            property: KProperty1<T, Double>,
            parameter: KParameter
    ) : PropertyMapping<Double>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_DOUBLE

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            annotationFS.setDoubleValue(feat, property.get(label))
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): Double {
            return annotationFS.getDoubleValue(feat)
        }
    }

    inner class StringPropertyMapping(
            property: KProperty1<T, String?>,
            parameter: KParameter
    ) : PropertyMapping<String?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_STRING

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            val get = property.get(label) ?: return
            annotationFS.setStringValue(feat, get)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): String? {
            return annotationFS.getStringValue(feat)
        }
    }

    inner class BooleanArrayPropertyMapping(
            property: KProperty1<T, BooleanArray?>,
            parameter: KParameter
    ) : PropertyMapping<BooleanArray?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_BOOLEAN_ARRAY

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            val from = property.get(label) ?: return

            val to = cas.createBooleanArrayFS(from.size)
            to.copyFromArray(from, 0, 0, from.size)
            cas.addFsToIndexes(to)
            annotationFS.setFeatureValue(feat, to)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): BooleanArray? {
            return (annotationFS.getFeatureValue(feat) as BooleanArrayFS?)
                    ?.run { BooleanArray(size()) { get(it) } }
        }
    }

    inner class BooleanListPropertyMapping(
            property: KProperty1<T, List<Boolean>?>,
            parameter: KParameter
    ) : PropertyMapping<List<Boolean>?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_BOOLEAN_ARRAY

        override fun copyValueToAnnotation(
                value: List<Boolean>?,
                cas: CAS,
                annotationFS: AnnotationFS
        ) {
            if (value == null) return

            val booleanArrayFS = cas.createBooleanArrayFS(value.size)
            value.forEachIndexed { i, b -> booleanArrayFS[i] = b }
            cas.addFsToIndexes(booleanArrayFS)

            annotationFS.setFeatureValue(feat, booleanArrayFS)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): List<Boolean>? {
            return (annotationFS.getFeatureValue(feat) as BooleanArrayFS?)
                    ?.run { List(size()) { get(it) } }
        }
    }

    inner class ByteArrayPropertyMapping(
            property: KProperty1<T, ByteArray?>,
            parameter: KParameter
    ) : PropertyMapping<ByteArray?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_BYTE_ARRAY

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            val from = property.get(label) ?: return

            val to = cas.createByteArrayFS(from.size)

            to.copyFromArray(from, 0, 0, from.size)

            cas.addFsToIndexes(to)

            annotationFS.setFeatureValue(feat, to)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): ByteArray? {
            return (annotationFS.getFeatureValue(feat) as ByteArrayFS?)
                    ?.run { ByteArray(size()) { get(it) } }
        }
    }

    inner class ByteListPropertyMapping(
            property: KProperty1<T, List<Byte>?>,
            parameter: KParameter
    ) : PropertyMapping<List<Byte>?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_BYTE_ARRAY

        override fun copyFromAnnotation(annotationFS: AnnotationFS): List<Byte>? {
            return (annotationFS.getFeatureValue(feat) as ByteArrayFS?)
                    ?.run { List(size()) { get(it) } }
        }

        override fun copyValueToAnnotation(
                value: List<Byte>?,
                cas: CAS,
                annotationFS: AnnotationFS
        ) {
            if (value == null) return
            val arrayFS = cas.createByteArrayFS(value.size)
            value.forEachIndexed { i, byte -> arrayFS[i] = byte }
            cas.addFsToIndexes(arrayFS)
            annotationFS.setFeatureValue(feat, arrayFS)
        }
    }

    inner class ShortArrayPropertyMapping(
            property: KProperty1<T, ShortArray?>,
            parameter: KParameter
    ) : PropertyMapping<ShortArray?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_SHORT_ARRAY

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            val from = property.get(label) ?: return
            val to = cas.createShortArrayFS(from.size)
            to.copyFromArray(from, 0, 0, from.size)
            cas.addFsToIndexes(to)
            annotationFS.setFeatureValue(feat, to)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): ShortArray? {
            return (annotationFS.getFeatureValue(feat) as ShortArrayFS?)
                    ?.run { ShortArray(size()) { get(it) } }
        }
    }

    inner class ShortListPropertyMapping(
            property: KProperty1<T, List<Short>>,
            parameter: KParameter
    ) : PropertyMapping<List<Short>>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_SHORT_ARRAY

        override fun copyValueToAnnotation(
                value: List<Short>?,
                cas: CAS,
                annotationFS: AnnotationFS
        ) {
            if (value == null) return
            val shortArrayFS = cas.createShortArrayFS(value.size)
            value.forEachIndexed { index, sh -> shortArrayFS[index] = sh }
            cas.addFsToIndexes(shortArrayFS)
            annotationFS.setFeatureValue(feat, shortArrayFS)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): List<Short>? {
            return (annotationFS.getFeatureValue(feat) as ShortArrayFS?)
                    ?.run { List(size()) { get(it) } }
        }
    }

    inner class IntArrayPropertyMapping(
            property: KProperty1<T, IntArray?>,
            parameter: KParameter
    ) : PropertyMapping<IntArray?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_INTEGER_ARRAY

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            val from = property.get(label) ?: return
            val to = cas.createIntArrayFS(from.size)
            to.copyFromArray(from, 0, 0, from.size)
            cas.addFsToIndexes(to)
            annotationFS.setFeatureValue(feat, to)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): IntArray? {
            return (annotationFS.getFeatureValue(feat) as IntArrayFS?)
                    ?.run { IntArray(size()) { get(it) } }
        }
    }

    inner class IntListPropertyMapping(
            property: KProperty1<T, List<Int>?>,
            parameter: KParameter
    ) : PropertyMapping<List<Int>?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_INTEGER_ARRAY

        override fun copyValueToAnnotation(
                value: List<Int>?,
                cas: CAS,
                annotationFS: AnnotationFS
        ) {
            if (value == null) return
            val intArrayFS = cas.createIntArrayFS(value.size)
            value.forEachIndexed { index, i -> intArrayFS[index] = i }
            cas.addFsToIndexes(intArrayFS)
            annotationFS.setFeatureValue(feat, intArrayFS)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): List<Int>? {
            return (annotationFS.getFeatureValue(feat) as IntArrayFS?)
                    ?.run { List(size()) { get(it) } }
        }
    }

    inner class LongArrayPropertyMapping(
            property: KProperty1<T, LongArray?>,
            parameter: KParameter
    ) : PropertyMapping<LongArray?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_LONG_ARRAY

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            val from = property.get(label) ?: return
            val to = cas.createLongArrayFS(from.size)
            from.forEachIndexed { index, l -> to[index] = l }
            cas.addFsToIndexes(to)
            annotationFS.setFeatureValue(feat, to)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): LongArray? {
            return (annotationFS.getFeatureValue(feat) as LongArrayFS?)
                    ?.run { LongArray(size()) { get(it) } }
        }
    }

    inner class LongListPropertyMapping(
            property: KProperty1<T, List<Long>?>,
            parameter: KParameter
    ) : PropertyMapping<List<Long>?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_LONG_ARRAY

        override fun copyValueToAnnotation(
                value: List<Long>?,
                cas: CAS,
                annotationFS: AnnotationFS
        ) {
            if (value == null) return
            val longArrayFS = cas.createLongArrayFS(value.size)
            value.forEachIndexed { index, l -> longArrayFS[index] = l }
            cas.addFsToIndexes(longArrayFS)
            annotationFS.setFeatureValue(feat, longArrayFS)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): List<Long>? {
            return (annotationFS.getFeatureValue(feat) as LongArrayFS?)
                    ?.run { List(size()) { get(it) } }
        }
    }

    inner class FloatArrayPropertyMapping(
            property: KProperty1<T, FloatArray?>,
            parameter: KParameter
    ) : PropertyMapping<FloatArray?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_FLOAT_ARRAY

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            val from = property.get(label) ?: return
            val to = cas.createFloatArrayFS(from.size)
            to.copyFromArray(from, 0, 0, from.size)
            cas.addFsToIndexes(to)
            annotationFS.setFeatureValue(feat, to)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): FloatArray? {
            return (annotationFS.getFeatureValue(feat) as FloatArrayFS?)
                    ?.run { FloatArray(size()) { get(it) } }
        }
    }

    inner class FloatListPropertyMapping(
            property: KProperty1<T, List<Float>?>,
            parameter: KParameter
    ) : PropertyMapping<List<Float>?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_FLOAT_ARRAY

        override fun copyValueToAnnotation(
                value: List<Float>?,
                cas: CAS,
                annotationFS: AnnotationFS
        ) {
            if (value == null) return
            val floatArrayFS = cas.createFloatArrayFS(value.size)
            value.forEachIndexed { index, fl -> floatArrayFS[index] = fl }
            cas.addFsToIndexes(floatArrayFS)
            annotationFS.setFeatureValue(feat, floatArrayFS)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): List<Float>? {
            return (annotationFS.getFeatureValue(feat) as FloatArrayFS?)
                    ?.run { List(size()) { get(it) } }
        }
    }

    inner class DoubleArrayPropertyMapping(
            property: KProperty1<T, DoubleArray?>,
            parameter: KParameter
    ) : PropertyMapping<DoubleArray?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_DOUBLE_ARRAY

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            val from = property.get(label) ?: return
            val to = cas.createDoubleArrayFS(from.size)
            to.copyFromArray(from, 0, 0, from.size)
            cas.addFsToIndexes(to)
            annotationFS.setFeatureValue(feat, to)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): DoubleArray? {
            return (annotationFS.getFeatureValue(feat) as DoubleArrayFS?)
                    ?.run { DoubleArray(size()) { get(it) } }
        }
    }

    inner class DoubleListPropertyMapping(
            property: KProperty1<T, List<Double>?>,
            parameter: KParameter
    ) : PropertyMapping<List<Double>?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_DOUBLE_ARRAY

        override fun copyValueToAnnotation(
                value: List<Double>?,
                cas: CAS,
                annotationFS: AnnotationFS
        ) {
            if (value == null) return
            val doubleArrayFS = cas.createDoubleArrayFS(value.size)
            value.forEachIndexed { index, d -> doubleArrayFS[index] = d }
            cas.addFsToIndexes(doubleArrayFS)
            annotationFS.setFeatureValue(feat, doubleArrayFS)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): List<Double>? {
            return (annotationFS.getFeatureValue(feat) as DoubleArrayFS?)
                    ?.run { List(size()) { get(it) } }
        }
    }

    inner class StringArrayPropertyMapping(
            property: KProperty1<T, Array<String>?>,
            parameter: KParameter
    ) : PropertyMapping<Array<String>?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_STRING_ARRAY

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            val from = property.get(label) ?: return
            val to = cas.createStringArrayFS(from.size)
            to.copyFromArray(from, 0, 0, from.size)
            cas.addFsToIndexes(to)
            annotationFS.setFeatureValue(feat, to)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): Array<String>? {
            return (annotationFS.getFeatureValue(feat) as StringArrayFS?)
                    ?.run { Array(size()) { get(it) } }
        }
    }

    inner class StringListPropertyMapping(
            property: KProperty1<T, List<String>?>,
            parameter: KParameter
    ) : PropertyMapping<List<String>?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_STRING_ARRAY

        override fun copyValueToAnnotation(
                value: List<String>?,
                cas: CAS,
                annotationFS: AnnotationFS
        ) {
            if (value == null) return
            val stringArrayFS = cas.createStringArrayFS(value.size)
            value.forEachIndexed { index, s -> stringArrayFS[index] = s }
            cas.addFsToIndexes(stringArrayFS)
            annotationFS.setFeatureValue(feat, stringArrayFS)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): List<String>? {
            return (annotationFS.getFeatureValue(feat) as StringArrayFS?)
                    ?.run { List(size()) { get(it) } }
        }
    }

    inner class EnumPropertyMapping(
            property: KProperty1<T, Any?>,
            parameter: KParameter
    ) : PropertyMapping<Any?>(property, parameter) {
        override val uimaType: String
            get() = createEnumTypeName(returnType.java)

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            val enumVal = property.get(label) as Enum<*>
            annotationFS.setStringValue(feat, enumVal.name)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): Any? {
            val value = annotationFS.getStringValue(feat)
            return returnType.java.enumConstants
                    .map { it as Enum<*> }
                    .find { it.name == value }
        }
    }

    inner class EnumArrayPropertyMapping(
            property: KProperty1<T, Array<*>?>,
            parameter: KParameter,
            private val componentClass: Class<*>
    ) : PropertyMapping<Array<*>?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_STRING_ARRAY

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            val from = property.get(label) ?: return
            val to = cas.createStringArrayFS(from.size)
            for (i in 0 until from.size) {
                to[i] = from[i].let { it as Enum<*> }.name
            }
            cas.addFsToIndexes(to)
            annotationFS.setFeatureValue(feat, to)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): Array<*>? {
            val from = annotationFS.getFeatureValue(feat) as StringArrayFS? ?: return null
            @Suppress("UNCHECKED_CAST")
            val array = java.lang.reflect.Array.newInstance(componentClass, from.size()) as Array<Enum<*>>
            for (i in 0 until from.size()) {
                array[i] = componentClass.enumConstants
                        .map { it as Enum<*> }
                        .findLast { it.name == from[i] }
                        ?: throw IllegalStateException("Could not find enum constant value")
            }
            return array
        }
    }

    inner class EnumListPropertyMapping(
            property: KProperty1<T, List<Any>?>,
            parameter: KParameter,
            private val componentClass: Class<*>
    ) : PropertyMapping<List<Any>?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_STRING_ARRAY

        override fun copyValueToAnnotation(value: List<Any>?, cas: CAS, annotationFS: AnnotationFS) {
            if (value == null) return
            val stringArrayFS = cas.createStringArrayFS(value.size)
            value.forEachIndexed { index, enum -> stringArrayFS[index] = (enum as Enum<*>).name }
            cas.addFsToIndexes(stringArrayFS)
            annotationFS.setFeatureValue(feat, stringArrayFS)
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): List<Any>? {
            val from = annotationFS.getFeatureValue(feat) as StringArrayFS? ?: return null
            return List(from.size()) { index ->
                componentClass.enumConstants
                        .map { it as Enum<*> }
                        .find { it.name == from[index] }
                        ?: throw IllegalStateException("Problem mapping enum")
            }
        }
    }

    inner class BigDecimalPropertyMapping(
            property: KProperty1<T, BigDecimal?>,
            parameter: KParameter
    ) : PropertyMapping<BigDecimal?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_STRING

        override fun copyToAnnotation(label: T, cas: CAS, annotationFS: AnnotationFS) {
            val bigDecimal = property.get(label) ?: return
            annotationFS.setStringValue(feat, bigDecimal.toString())
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): BigDecimal? {
            return annotationFS.getStringValue(feat)?.let { BigDecimal(it) }
        }
    }

    inner class SpanPropertyMapping(
            property: KProperty1<T, Span?>,
            parameter: KParameter
    ) : PropertyMapping<Span?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_ANNOTATION

        override fun copyFromAnnotation(annotationFS: AnnotationFS): Span? {
            return (annotationFS.getFeatureValue(feat) as AnnotationFS?)
                    ?.run { Span(begin, end) }

        }

        override fun copyValueToAnnotation(value: Span?, cas: CAS, annotationFS: AnnotationFS) {
            value ?: return
            val spannotation = cas.createAnnotation<AnnotationFS>(
                    cas.annotationType,
                    value.startIndex,
                    value.endIndex
            )
            cas.addFsToIndexes(spannotation)
            annotationFS.setFeatureValue(
                    feat,
                    spannotation
            )
        }
    }

    inner class SpanArrayPropertyMapping(
            property: KProperty1<T, Array<Span>?>,
            parameter: KParameter
    ) : PropertyMapping<Array<Span>?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_FS_ARRAY

        override fun copyFromAnnotation(annotationFS: AnnotationFS): Array<Span>? {
            return (annotationFS.getFeatureValue(feat) as ArrayFS?)?.run {
                Array(size()) {
                    val annotation = get(it) as AnnotationFS
                    Span(annotation.begin, annotation.end)
                }
            }
        }

        override fun copyValueToAnnotation(
                value: Array<Span>?,
                cas: CAS,
                annotationFS: AnnotationFS
        ) {
            value ?: return
            val arrayFS = cas.createArrayFS(value.size)
            value.forEachIndexed { index, span ->
                val spannotation = cas.createAnnotation<AnnotationFS>(
                        cas.annotationType,
                        span.startIndex,
                        span.endIndex
                )
                cas.addFsToIndexes(spannotation)
                arrayFS[index] = spannotation
            }
        }
    }

    inner class SpanListPropertyMapping(
            property: KProperty1<T, List<Span>?>,
            parameter: KParameter
    ) : PropertyMapping<List<Span>?>(property, parameter) {
        override val uimaType: String
            get() = CAS.TYPE_NAME_FS_ARRAY

        override fun copyFromAnnotation(annotationFS: AnnotationFS): List<Span>? {
            return (annotationFS.getFeatureValue(feat) as ArrayFS?)?.run {
                List(size()) {
                    val annotation = get(it) as AnnotationFS
                    Span(annotation.begin, annotation.end)
                }
            }
        }

        override fun copyValueToAnnotation(value: List<Span>?, cas: CAS, annotationFS: AnnotationFS) {
            value ?: return
            val arrayFS = cas.createArrayFS(value.size)
            value.forEachIndexed { index, span ->
                val spannotation = cas.createAnnotation<AnnotationFS>(
                        cas.annotationType,
                        span.startIndex,
                        span.endIndex
                )
                cas.addFsToIndexes(spannotation)
                arrayFS[index] = spannotation
            }
        }
    }

    inner class LabelPropertyMapping<R : Label>(
            property: KProperty1<T, R?>,
            parameter: KParameter
    ) : PropertyMapping<R?>(property, parameter) {
        private var _factory: LabelAdapterFactory<R>? = null
        private val factory: LabelAdapterFactory<R>
            get() = _factory
                    ?: throw IllegalStateException("Label adapter not initialized")

        override val uimaType: String
            @Suppress("UNCHECKED_CAST")
            get() = uimaTypeName((property.returnType.classifier as KClass<out Label>).java)

        override fun initFeat(cas: CAS) {
            super.initFeat(cas)
            @Suppress("UNCHECKED_CAST")
            _factory = labelAdapters.getLabelAdapterFactory(returnType.java as Class<out Label>) as LabelAdapterFactory<R>
        }

        override fun copyFromAnnotation(annotationFS: AnnotationFS): R? {
            val adapter = factory.create(annotationFS.cas)
            val from = annotationFS.getFeatureValue(feat) as AnnotationFS? ?: return null
            return adapter.annotationToLabel(from)
        }

        override fun copyValueToAnnotation(value: R?, cas: CAS, annotationFS: AnnotationFS) {
            value ?: return
            val adapter = factory.create(annotationFS.cas)
            annotationFS.setFeatureValue(feat, adapter.labelToAnnotation(value))
        }
    }

    inner class LabelArrayPropertyMapping<R : Label>(
            property: KProperty1<T, Array<R>>,
            parameter: KParameter,
            private val componentClass: Class<R>
    ) : PropertyMapping<Array<R>>(property, parameter) {
        private var _factory: LabelAdapterFactory<R>? = null
        private val factory: LabelAdapterFactory<R>
            get() = _factory
                    ?: throw IllegalStateException("Label adapter not initialized")

        override val uimaType: String
            get() = CAS.TYPE_NAME_FS_ARRAY

        override fun initFeat(cas: CAS) {
            _factory = labelAdapters.getLabelAdapterFactory(componentClass)
        }

        @Suppress("UNCHECKED_CAST")
        override fun copyFromAnnotation(annotationFS: AnnotationFS): Array<R>? {
            val arrayFS = annotationFS.getFeatureValue(feat) as ArrayFS? ?: return null
            val adapter = factory.create(annotationFS.cas)
            return Array<Label>(arrayFS.size()) {
                adapter.annotationToLabel(arrayFS[it] as AnnotationFS)
            } as Array<R>
        }

        override fun copyValueToAnnotation(
                value: Array<R>?,
                cas: CAS,
                annotationFS: AnnotationFS
        ) {
            value ?: return
            val arrayFS = cas.createArrayFS(value.size)
            val adapter = factory.create(cas)
            value.forEachIndexed { index, r -> arrayFS[index] = adapter.labelToAnnotation(r) }
            cas.addFsToIndexes(arrayFS)
            annotationFS.setFeatureValue(feat, arrayFS)
        }
    }

    inner class LabelListPropertyMapping<R : Label>(
            property: KProperty1<T, List<R>>,
            parameter: KParameter,
            componentClass: Class<R>
    ) : PropertyMapping<List<R>>(property, parameter) {
        private val factory = labelAdapters.getLabelAdapterFactory(componentClass)

        override val uimaType: String
            get() = CAS.TYPE_NAME_FS_ARRAY

        override fun copyFromAnnotation(annotationFS: AnnotationFS): List<R>? {
            val arrayFS = annotationFS.getFeatureValue(feat) as ArrayFS? ?: return null
            val adapter = factory.create(annotationFS.cas)
            return List(arrayFS.size()) {
                adapter.annotationToLabel(arrayFS[it] as AnnotationFS)
            }
        }

        override fun copyValueToAnnotation(
                value: List<R>?,
                cas: CAS,
                annotationFS: AnnotationFS
        ) {
            value ?: return
            val arrayFS = cas.createArrayFS(value.size)
            val adapter = factory.create(cas)
            value.forEachIndexed { index, r -> arrayFS[index] = adapter.labelToAnnotation(r) }
            cas.addFsToIndexes(arrayFS)
            annotationFS.setFeatureValue(feat, arrayFS)
        }
    }

    private fun checkPrimitiveProperty(property: KProperty1<T, *>) {
        if (property.returnType.isMarkedNullable) {
            throw IllegalStateException(
                    "Nullable primitive property, UIMA does not support nullable primitives"
            )
        }
    }
}

private fun <T : Label> uimaTypeName(labelClass: Class<T>) =
        "edu.umn.nlpengine.generated${(labelClass.kotlin.findAnnotation<LabelMetadata>()
                ?: throw IllegalStateException("Label class without @Label annotation")).versionId}.${labelClass.simpleName}"


fun TypeSystemDescription.addEnum(clazz: Class<*>) {
    val type = addType(createEnumTypeName(clazz),
            "Automatically generated type from ${clazz.canonicalName}",
            "uima.cas.String")
    type.allowedValues = clazz.enumConstants.map { it as Enum<*> }.map { it.name }
            .map { AllowedValue_impl(it, "Auto generated from enum constant") }
            .toList()
            .toTypedArray()
}

private fun createEnumTypeName(clazz: Class<*>) =
        "edu.umn.biomedicus.types.auto.${clazz.simpleName}"

