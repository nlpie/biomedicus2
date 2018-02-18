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
import org.apache.uima.cas.*
import org.apache.uima.cas.text.AnnotationFS
import org.apache.uima.resource.metadata.impl.TypeSystemDescription_impl
import org.apache.uima.util.CasCreationUtils
import org.testng.Assert.*
import org.testng.annotations.Test
import java.math.BigDecimal


enum class Foo {
    BAR,
    BAZ
}

@LabelMetadata(versionId = "1_0", distinct = true)
data class HasEnum(
        override val startIndex: Int,
        override val endIndex: Int,
        val foo: Foo
) : Label()

@LabelMetadata(versionId = "test")
data class HasEnumArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val enums: Array<Foo>
) : Label()

@LabelMetadata(versionId = "test")
data class HasEnumList(
        override val startIndex: Int,
        override val endIndex: Int,
        val enums: List<Foo>
) : Label()

@LabelMetadata(versionId = "test")
data class HasBigDecimal(
        override val startIndex: Int,
        override val endIndex: Int,
        val bigDecimal: BigDecimal
) : Label()

@LabelMetadata(versionId = "test")
data class HasSpan(
        override val startIndex: Int,
        override val endIndex: Int,
        val span: Span
) : Label()

@LabelMetadata(versionId = "test")
data class HasLabel(
        override val startIndex: Int,
        override val endIndex: Int,
        val hasBoolean: HasBoolean
) : Label()


@LabelMetadata(versionId = "test", distinct = true)
data class HasBoolean(
        override val startIndex: Int,
        override val endIndex: Int,
        val boolean: Boolean
) : Label()

@LabelMetadata(versionId = "test", distinct = true)
data class HasByte(
        val byte: Byte,
        override val startIndex: Int,
        override val endIndex: Int
) : Label()

@LabelMetadata(versionId = "test", distinct = true)
data class HasShort(
        override val startIndex: Int,
        override val endIndex: Int,
        val short: Short
) : Label()

@LabelMetadata(versionId = "test")
data class HasInt(override val startIndex: Int, override val endIndex: Int, val int: Int) : Label()

@LabelMetadata(versionId = "test")
data class HasLong(
        override val startIndex: Int,
        override val endIndex: Int,
        val long: Long
) : Label()

@LabelMetadata(versionId = "test")
data class HasFloat(
        override val startIndex: Int,
        override val endIndex: Int,
        val float: Float
) : Label()

@LabelMetadata(versionId = "test")
data class HasDouble(
        override val startIndex: Int,
        override val endIndex: Int,
        val double: Double
) : Label()

@LabelMetadata(versionId = "test")
data class HasString(
        override val startIndex: Int,
        override val endIndex: Int,
        val string: String
) : Label()

@LabelMetadata(versionId = "test")
data class HasBooleanArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val booleanArray: BooleanArray
) : Label()

@LabelMetadata(versionId = "test")
data class HasByteArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val byteArray: ByteArray
) : Label()

@LabelMetadata(versionId = "test")
data class HasShortArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val shortArray: ShortArray
) : Label()

@LabelMetadata(versionId = "test")
data class HasIntArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val intArray: IntArray
) : Label()

@LabelMetadata(versionId = "test")
data class HasLongArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val longArray: LongArray
) : Label()

@LabelMetadata(versionId = "test")
data class HasFloatArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val floatArray: FloatArray
) : Label()

@LabelMetadata(versionId = "test")
data class HasDoubleArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val doubleArray: DoubleArray
) : Label()

@LabelMetadata(versionId = "test")
data class HasStringArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val stringArray: Array<String>
) : Label()

@LabelMetadata(versionId = "test")
data class HasBooleanList(
        override val startIndex: Int,
        override val endIndex: Int,
        val booleans: List<Boolean>
) : Label()

@LabelMetadata(versionId = "test")
data class HasByteList(
        override val startIndex: Int,
        override val endIndex: Int,
        val bytes: List<Byte>
) : Label()

@LabelMetadata(versionId = "test")
data class HasShortList(
        override val startIndex: Int,
        override val endIndex: Int,
        val shorts: List<Short>
) : Label()

@LabelMetadata(versionId = "test")
data class HasIntList(
        override val startIndex: Int,
        override val endIndex: Int,
        val ints: List<Int>
) : Label()

@LabelMetadata(versionId = "test")
data class HasLongList(
        override val startIndex: Int,
        override val endIndex: Int,
        val longs: List<Long>
) : Label()

@LabelMetadata(versionId = "test")
data class HasFloatList(
        override val startIndex: Int,
        override val endIndex: Int,
        val floats: List<Float>
) : Label()

@LabelMetadata(versionId = "test")
data class HasDoubleList(
        override val startIndex: Int,
        override val endIndex: Int,
        val doubles: List<Double>
) : Label()

@LabelMetadata(versionId = "test")
data class HasStringList(
        override val startIndex: Int,
        override val endIndex: Int,
        val strings: List<String>
) : Label()

class AutoAdapterTest {

    val labelAdapters: LabelAdapters

    val cas: CAS

    init {
        val autoAdapters = AutoAdapters(LabelAdapters(null), null)

        autoAdapters.addEnumClass(Foo::class.java)

        autoAdapters.addLabelClass(HasEnum::class.java)
        autoAdapters.addLabelClass(HasEnumArray::class.java)
        autoAdapters.addLabelClass(HasEnumList::class.java)
        autoAdapters.addLabelClass(HasBigDecimal::class.java)
        autoAdapters.addLabelClass(HasSpan::class.java)
        autoAdapters.addLabelClass(HasLabel::class.java)

        autoAdapters.addLabelClass(HasBoolean::class.java)
        autoAdapters.addLabelClass(HasByte::class.java)
        autoAdapters.addLabelClass(HasShort::class.java)
        autoAdapters.addLabelClass(HasInt::class.java)
        autoAdapters.addLabelClass(HasLong::class.java)
        autoAdapters.addLabelClass(HasFloat::class.java)
        autoAdapters.addLabelClass(HasDouble::class.java)
        autoAdapters.addLabelClass(HasString::class.java)

        autoAdapters.addLabelClass(HasBooleanArray::class.java)
        autoAdapters.addLabelClass(HasByteArray::class.java)
        autoAdapters.addLabelClass(HasShortArray::class.java)
        autoAdapters.addLabelClass(HasIntArray::class.java)
        autoAdapters.addLabelClass(HasLongArray::class.java)
        autoAdapters.addLabelClass(HasFloatArray::class.java)
        autoAdapters.addLabelClass(HasDoubleArray::class.java)
        autoAdapters.addLabelClass(HasStringArray::class.java)

        autoAdapters.addLabelClass(HasBooleanList::class.java)
        autoAdapters.addLabelClass(HasByteList::class.java)
        autoAdapters.addLabelClass(HasShortList::class.java)
        autoAdapters.addLabelClass(HasIntList::class.java)
        autoAdapters.addLabelClass(HasLongList::class.java)
        autoAdapters.addLabelClass(HasFloatList::class.java)
        autoAdapters.addLabelClass(HasDoubleList::class.java)
        autoAdapters.addLabelClass(HasStringList::class.java)

        val ts = TypeSystemDescription_impl()
        autoAdapters.addToTypeSystem(ts)

        labelAdapters = autoAdapters.labelAdapters

        cas = CasCreationUtils.createCas(ts, null, null)
    }

    @Test
    fun testEnumFeatureAnnotationToLabel() {
        val hasEnumAdapter = labelAdapters.getLabelAdapterFactory(HasEnum::class.java).create(cas)

        val annotation = cas.createAnnotation<AnnotationFS>(hasEnumAdapter.type, 0, 5)
        annotation.setStringValue(hasEnumAdapter.type.getFeatureByBaseName("foo"), "BAR")

        val label = hasEnumAdapter.annotationToLabel(annotation)

        assertEquals(label.foo, Foo.BAR)
    }

    @Test
    fun testEnumFeatureLabelToAnnotation() {
        val hasEnumAdapter = labelAdapters.getLabelAdapterFactory(HasEnum::class.java).create(cas)

        val label = HasEnum(0, 5, Foo.BAR)

        val annotation = hasEnumAdapter.labelToAnnotation(label)

        assertEquals(annotation.getStringValue(annotation.type.getFeatureByBaseName("foo")),
                "BAR")
    }

    @Test
    fun testEnumArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasEnumArray::class.java).create(cas)
        val arrayFS = cas.createStringArrayFS(3)
        arrayFS[0] = "BAR"
        arrayFS[1] = "BAZ"
        arrayFS[2] = "BAR"
        cas.addFsToIndexes(arrayFS)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("enums"), arrayFS)

        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.enums, arrayOf(Foo.BAR, Foo.BAZ, Foo.BAR))
    }

    @Test
    fun testEnumArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasEnumArray::class.java).create(cas)
        val label = HasEnumArray(0, 5, arrayOf(Foo.BAR, Foo.BAZ, Foo.BAR))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("enums")

        val arrayFS = annotation.getFeatureValue(feature) as StringArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS[0], "BAR")
            assertEquals(arrayFS[1], "BAZ")
            assertEquals(arrayFS[2], "BAR")
        }
    }

    @Test
    fun testBigDecimalFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBigDecimal::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val feature = annotation.type.getFeatureByBaseName("bigDecimal")
        annotation.setStringValue(feature, BigDecimal(45.0).toString())
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.bigDecimal, BigDecimal(45.0))
    }

    @Test
    fun testBigDecimalFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBigDecimal::class.java).create(cas)
        val label = HasBigDecimal(0, 5, BigDecimal(45.0))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("bigDecimal")

        assertEquals(annotation.getStringValue(feature), BigDecimal(45.0).toString())
    }

    @Test
    fun testSpanFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasSpan::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val feature = annotation.type.getFeatureByBaseName("span")
        val spannotation = cas.createAnnotation<AnnotationFS>(cas.annotationType, 10, 20)
        cas.addFsToIndexes(spannotation)
        annotation.setFeatureValue(feature, spannotation)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.span, Span(10, 20))
    }

    @Test
    fun testSpanFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasSpan::class.java).create(cas)
        val label = HasSpan(0, 5, Span(10, 20))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("span")
        val spannotation = annotation.getFeatureValue(feature) as? AnnotationFS

        assertNotNull(spannotation)
        if (spannotation != null) {
            assertEquals(spannotation.begin, 10)
            assertEquals(spannotation.end, 20)
        }
    }

    @Test
    fun testLabelFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabel::class.java).create(cas)
        val booleanAdapter = labelAdapters.getLabelAdapterFactory(HasBoolean::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val booleanAnnotation = cas.createAnnotation<AnnotationFS>(booleanAdapter.type, 10, 15)
        booleanAnnotation.setBooleanValue(booleanAdapter.type.getFeatureByBaseName("boolean"), true)
        cas.addFsToIndexes(booleanAnnotation)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("hasBoolean"), booleanAnnotation)

        val label = adapter.annotationToLabel(annotation)

        val hasBoolean = label.hasBoolean

        assertNotNull(hasBoolean)
        assertEquals(hasBoolean.startIndex, 10)
        assertEquals(hasBoolean.endIndex, 15)
        assertEquals(hasBoolean.boolean, true)
    }

    @Test
    fun testLabelFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabel::class.java).create(cas)
        val label = HasLabel(0, 5, HasBoolean(10, 15, true))
        val annotation = adapter.labelToAnnotation(label)
        val hasBooleanFeature = annotation.type.getFeatureByBaseName("hasBoolean")
        val booleanFeature = hasBooleanFeature.range.getFeatureByBaseName("boolean")

        val hasBooleanAnnotation = annotation.getFeatureValue(hasBooleanFeature) as AnnotationFS?

        assertNotNull(hasBooleanAnnotation)
        if (hasBooleanAnnotation != null) {
            assertEquals(hasBooleanAnnotation.begin, 10)
            assertEquals(hasBooleanAnnotation.end, 15)
            assertEquals(hasBooleanAnnotation.getBooleanValue(booleanFeature), true)
        }

    }

    @Test
    fun testBooleanFeatureToLabel() {
        val hasBooleanAdapter = labelAdapters.getLabelAdapterFactory(HasBoolean::class.java).create(cas)

        val annotation = cas.createAnnotation<AnnotationFS>(hasBooleanAdapter.type, 0, 5)

        val booleanFeature = annotation.type.getFeatureByBaseName("boolean")
        annotation.setBooleanValue(booleanFeature, true)

        val label = hasBooleanAdapter.annotationToLabel(annotation)

        assertTrue(label.boolean)
    }

    @Test
    fun testBooleanFeatureToAnnotation() {
        val create = labelAdapters.getLabelAdapterFactory(HasBoolean::class.java).create(cas)

        val label = HasBoolean(0, 5, true)

        val annotation = create.labelToAnnotation(label)

        val feature = annotation.type.getFeatureByBaseName("boolean")
        assertTrue(annotation.getBooleanValue(feature))
    }

    @Test
    fun testByteFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByte::class.java).create(cas)

        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)

        annotation.setByteValue(annotation.type.getFeatureByBaseName("byte"), 0xA)

        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.byte, 0xA)
    }

    @Test
    fun testByteFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByte::class.java).create(cas)

        val label = HasByte(0xA, 0, 5)

        val annotation = adapter.labelToAnnotation(label)

        val feature = adapter.type.getFeatureByBaseName("byte")

        assertEquals(annotation.getByteValue(feature), 0xA)
    }

    @Test
    fun testShortFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShort::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setShortValue(annotation.type.getFeatureByBaseName("short"), 5)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.short, 5.toShort())
    }

    @Test
    fun testShortFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShort::class.java).create(cas)
        val label = HasShort(0, 5, 5)
        val annotation = adapter.labelToAnnotation(label)
        val feature = adapter.type.getFeatureByBaseName("short")
        assertEquals(annotation.getShortValue(feature), 5)
    }

    @Test
    fun testIntFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasInt::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setIntValue(annotation.type.getFeatureByBaseName("int"), 5)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.int, 5)
    }

    @Test
    fun testIntFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasInt::class.java).create(cas)
        val label = HasInt(0, 5, 5)
        val annotation = adapter.labelToAnnotation(label)
        val feature = adapter.type.getFeatureByBaseName("int")

        assertEquals(annotation.getIntValue(feature), 5)
    }

    @Test
    fun testLongFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLong::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setLongValue(annotation.type.getFeatureByBaseName("long"), 5)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.long, 5)
    }

    @Test
    fun testLongFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLong::class.java).create(cas)
        val annotation = adapter.labelToAnnotation(HasLong(0, 5, 5))
        val feature = adapter.type.getFeatureByBaseName("long")

        assertEquals(annotation.getLongValue(feature), 5)
    }

    @Test
    fun testFloatFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloat::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setFloatValue(annotation.type.getFeatureByBaseName("float"), 5.0f)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.float, 5.0f)
    }

    @Test
    fun testFloatFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloat::class.java).create(cas)
        val annotation = adapter.labelToAnnotation(HasFloat(0, 5, 5.0f))
        val feature = adapter.type.getFeatureByBaseName("float")

        assertEquals(annotation.getFloatValue(feature), 5.0f)
    }

    @Test
    fun testDoubleFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDouble::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setDoubleValue(annotation.type.getFeatureByBaseName("double"), 5.0)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.double, 5.0)
    }

    @Test
    fun testDoubleFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDouble::class.java).create(cas)
        val annotation = adapter.labelToAnnotation(HasDouble(0, 5, 5.0))
        val feature = annotation.type.getFeatureByBaseName("double")

        assertEquals(annotation.getDoubleValue(feature), 5.0)
    }

    @Test
    fun testStringFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasString::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setStringValue(annotation.type.getFeatureByBaseName("string"), "blah")
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.string, "blah")
    }

    @Test
    fun testStringFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasString::class.java).create(cas)
        val annotation = adapter.labelToAnnotation(HasString(0, 5, "blah"))
        val feature = annotation.type.getFeatureByBaseName("string")

        assertEquals(annotation.getStringValue(feature), "blah")
    }

    @Test
    fun testBooleanArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBooleanArray::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createBooleanArrayFS(3)
        arrayFS[0] = true
        arrayFS[1] = false
        arrayFS[2] = true
        cas.addFsToIndexes(arrayFS)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("booleanArray"), arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.booleanArray, booleanArrayOf(true, false, true))
    }

    @Test
    fun testBooleanArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBooleanArray::class.java).create(cas)
        val annotation = adapter.labelToAnnotation(HasBooleanArray(0, 5, booleanArrayOf(true, false, true)))
        val feature = annotation.type.getFeatureByBaseName("booleanArray")
        val arrayFS = annotation.getFeatureValue(feature) as BooleanArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], true)
            assertEquals(arrayFS[1], false)
            assertEquals(arrayFS[2], true)
        }
    }

    @Test
    fun testByteArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByteArray::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createByteArrayFS(3)
        arrayFS[0] = 1
        arrayFS[1] = 0
        arrayFS[2] = 2
        cas.addFsToIndexes(arrayFS)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("byteArray"), arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.byteArray, byteArrayOf(1, 0, 2))
    }

    @Test
    fun testByteArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByteArray::class.java).create(cas)
        val annotation = adapter.labelToAnnotation(HasByteArray(0, 5, byteArrayOf(1, 0, 2)))
        val feature = annotation.type.getFeatureByBaseName("byteArray")
        val arrayFS = annotation.getFeatureValue(feature) as ByteArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], 1)
            assertEquals(arrayFS[1], 0)
            assertEquals(arrayFS[2], 2)
        }
    }

    @Test
    fun testShortArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShortArray::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createShortArrayFS(3)
        arrayFS[0] = 32
        arrayFS[1] = 64
        arrayFS[2] = 128
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("shortArray")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.shortArray, shortArrayOf(32, 64, 128))
    }

    @Test
    fun testShortArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShortArray::class.java).create(cas)
        val label = HasShortArray(0, 5, shortArrayOf(32, 64, 128))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("shortArray")
        val arrayFS = annotation.getFeatureValue(feature) as ShortArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], 32)
            assertEquals(arrayFS[1], 64)
            assertEquals(arrayFS[2], 128)
        }
    }

    @Test
    fun testIntArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasIntArray::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createIntArrayFS(3)
        arrayFS[0] = 32
        arrayFS[1] = 64
        arrayFS[2] = 128
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("intArray")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.intArray, intArrayOf(32, 64, 128))
    }

    @Test
    fun testIntArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasIntArray::class.java).create(cas)
        val label = HasIntArray(0, 5, intArrayOf(32, 64, 128))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("intArray")
        val arrayFS = annotation.getFeatureValue(feature) as IntArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], 32)
            assertEquals(arrayFS[1], 64)
            assertEquals(arrayFS[2], 128)
        }
    }

    @Test
    fun testLongArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLongArray::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createLongArrayFS(3)
        arrayFS[0] = 32
        arrayFS[1] = 64
        arrayFS[2] = 128
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("longArray")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.longArray, longArrayOf(32, 64, 128))
    }

    @Test
    fun testLongArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLongArray::class.java).create(cas)
        val label = HasLongArray(0, 5, longArrayOf(32, 64, 128))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("longArray")
        val arrayFS = annotation.getFeatureValue(feature) as LongArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], 32)
            assertEquals(arrayFS[1], 64)
            assertEquals(arrayFS[2], 128)
        }
    }

    @Test
    fun testFloatArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloatArray::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createFloatArrayFS(3)
        arrayFS[0] = 32f
        arrayFS[1] = 64f
        arrayFS[2] = 128f
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("floatArray")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.floatArray, floatArrayOf(32f, 64f, 128f))
    }

    @Test
    fun testFloatArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloatArray::class.java).create(cas)
        val label = HasFloatArray(0, 5, floatArrayOf(32f, 64f, 128f))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("floatArray")
        val arrayFS = annotation.getFeatureValue(feature) as FloatArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], 32f)
            assertEquals(arrayFS[1], 64f)
            assertEquals(arrayFS[2], 128f)
        }
    }

    @Test
    fun testDoubleArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDoubleArray::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createDoubleArrayFS(3)
        arrayFS[0] = 32.0
        arrayFS[1] = 64.0
        arrayFS[2] = 128.0
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("doubleArray")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.doubleArray, doubleArrayOf(32.0, 64.0, 128.0))
    }

    @Test
    fun testDoubleArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDoubleArray::class.java).create(cas)
        val label = HasDoubleArray(0, 5, doubleArrayOf(32.0, 64.0, 128.0))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("doubleArray")
        val arrayFS = annotation.getFeatureValue(feature) as DoubleArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], 32.0)
            assertEquals(arrayFS[1], 64.0)
            assertEquals(arrayFS[2], 128.0)
        }
    }

    @Test
    fun testStringArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasStringArray::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createStringArrayFS(3)
        arrayFS[0] = "a"
        arrayFS[1] = "b"
        arrayFS[2] = "c"
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("stringArray")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.stringArray, arrayOf("a", "b", "c"))
    }

    @Test
    fun testStringArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasStringArray::class.java).create(cas)
        val label = HasStringArray(0, 5, arrayOf("a", "b", "c"))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("stringArray")
        val arrayFS = annotation.getFeatureValue(feature) as StringArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], "a")
            assertEquals(arrayFS[1], "b")
            assertEquals(arrayFS[2], "c")
        }
    }

    @Test
    fun testBooleanListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBooleanList::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createBooleanArrayFS(3)
        arrayFS[0] = true
        arrayFS[1] = false
        arrayFS[2] = true
        cas.addFsToIndexes(arrayFS)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("booleans"), arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.booleans, listOf(true, false, true))
    }

    @Test
    fun testBooleanListFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBooleanList::class.java).create(cas)
        val annotation = adapter.labelToAnnotation(HasBooleanList(0, 5, listOf(true, false, true)))
        val feature = annotation.type.getFeatureByBaseName("booleans")
        val arrayFS = annotation.getFeatureValue(feature) as BooleanArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], true)
            assertEquals(arrayFS[1], false)
            assertEquals(arrayFS[2], true)
        }
    }

    @Test
    fun testByteListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByteList::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createByteArrayFS(3)
        arrayFS[0] = 1
        arrayFS[1] = 0
        arrayFS[2] = 2
        cas.addFsToIndexes(arrayFS)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("bytes"), arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.bytes, listOf(1.toByte(), 0.toByte(), 2.toByte()))
    }

    @Test
    fun testByteListFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByteList::class.java).create(cas)
        val annotation = adapter.labelToAnnotation(HasByteList(0, 5, listOf(1, 0, 2)))
        val feature = annotation.type.getFeatureByBaseName("bytes")
        val arrayFS = annotation.getFeatureValue(feature) as ByteArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], 1)
            assertEquals(arrayFS[1], 0)
            assertEquals(arrayFS[2], 2)
        }
    }

    @Test
    fun testShortListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShortList::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createShortArrayFS(3)
        arrayFS[0] = 32
        arrayFS[1] = 64
        arrayFS[2] = 128
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("shorts")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.shorts, listOf(32.toShort(), 64.toShort(), 128.toShort()))
    }

    @Test
    fun testShortListFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShortList::class.java).create(cas)
        val label = HasShortList(0, 5, listOf(32, 64, 128))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("shorts")
        val arrayFS = annotation.getFeatureValue(feature) as ShortArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], 32)
            assertEquals(arrayFS[1], 64)
            assertEquals(arrayFS[2], 128)
        }
    }

    @Test
    fun testIntListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasIntList::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createIntArrayFS(3)
        arrayFS[0] = 32
        arrayFS[1] = 64
        arrayFS[2] = 128
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("ints")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.ints, listOf(32, 64, 128))
    }

    @Test
    fun testIntListFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasIntList::class.java).create(cas)
        val label = HasIntList(0, 5, listOf(32, 64, 128))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("ints")
        val arrayFS = annotation.getFeatureValue(feature) as IntArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], 32)
            assertEquals(arrayFS[1], 64)
            assertEquals(arrayFS[2], 128)
        }
    }

    @Test
    fun testLongListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLongList::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createLongArrayFS(3)
        arrayFS[0] = 32
        arrayFS[1] = 64
        arrayFS[2] = 128
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("longs")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.longs, listOf(32.toLong(), 64.toLong(), 128.toLong()))
    }

    @Test
    fun testLongListFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLongList::class.java).create(cas)
        val label = HasLongList(0, 5, listOf(32, 64, 128))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("longs")
        val arrayFS = annotation.getFeatureValue(feature) as LongArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], 32)
            assertEquals(arrayFS[1], 64)
            assertEquals(arrayFS[2], 128)
        }
    }

    @Test
    fun testFloatListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloatList::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createFloatArrayFS(3)
        arrayFS[0] = 32f
        arrayFS[1] = 64f
        arrayFS[2] = 128f
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("floats")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.floats, listOf(32f, 64f, 128f))
    }

    @Test
    fun testFloatListFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloatList::class.java).create(cas)
        val label = HasFloatList(0, 5, listOf(32f, 64f, 128f))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("floats")
        val arrayFS = annotation.getFeatureValue(feature) as FloatArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], 32f)
            assertEquals(arrayFS[1], 64f)
            assertEquals(arrayFS[2], 128f)
        }
    }

    @Test
    fun testDoubleListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDoubleList::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createDoubleArrayFS(3)
        arrayFS[0] = 32.0
        arrayFS[1] = 64.0
        arrayFS[2] = 128.0
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("doubles")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.doubles, listOf(32.0, 64.0, 128.0))
    }

    @Test
    fun testDoubleListFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDoubleList::class.java).create(cas)
        val label = HasDoubleList(0, 5, listOf(32.0, 64.0, 128.0))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("doubles")
        val arrayFS = annotation.getFeatureValue(feature) as DoubleArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], 32.0)
            assertEquals(arrayFS[1], 64.0)
            assertEquals(arrayFS[2], 128.0)
        }
    }

    @Test
    fun testStringListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasStringList::class.java).create(cas)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createStringArrayFS(3)
        arrayFS[0] = "a"
        arrayFS[1] = "b"
        arrayFS[2] = "c"
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("strings")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.strings, listOf("a", "b", "c"))
    }

    @Test
    fun testStringListFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasStringList::class.java).create(cas)
        val label = HasStringList(0, 5, listOf("a", "b", "c"))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("strings")
        val arrayFS = annotation.getFeatureValue(feature) as StringArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 3)
            assertEquals(arrayFS[0], "a")
            assertEquals(arrayFS[1], "b")
            assertEquals(arrayFS[2], "c")
        }
    }
}
