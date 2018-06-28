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
import java.math.BigDecimal
import kotlin.test.*


enum class Foo {
    BAR,
    BAZ
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class HasEnum(
        override val startIndex: Int,
        override val endIndex: Int,
        val foo: Foo
) : Label()

@LabelMetadata(classpath = "test")
data class HasNullableEnum(
        override val startIndex: Int,
        override val endIndex: Int,
        val foo: Foo?
) : Label()

@LabelMetadata(classpath = "test")
data class HasEnumArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val enums: Array<Foo>
) : Label()

@LabelMetadata(classpath = "test")
data class HasNullableEnumArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val enums: Array<Foo>?
) : Label()

@LabelMetadata(classpath = "test")
data class HasEnumList(
        override val startIndex: Int,
        override val endIndex: Int,
        val enums: List<Foo>
) : Label()

@LabelMetadata(classpath = "test")
data class HasNullableEnumList(
        override val startIndex: Int,
        override val endIndex: Int,
        val enums: List<Foo>?
) : Label()

@LabelMetadata(classpath = "test")
data class HasBigDecimal(
        override val startIndex: Int,
        override val endIndex: Int,
        val bigDecimal: BigDecimal
) : Label()

@LabelMetadata(classpath = "test")
data class HasNullableBigDecimal(
        override val startIndex: Int,
        override val endIndex: Int,
        val bigDecimal: BigDecimal?
) : Label()

@LabelMetadata(classpath = "test")
data class HasSpan(
        override val startIndex: Int,
        override val endIndex: Int,
        val span: Span
) : Label()

@LabelMetadata(classpath = "test")
data class HasNullableSpan(
        override val startIndex: Int,
        override val endIndex: Int,
        val span: Span?
) : Label()

@LabelMetadata(classpath = "test")
data class HasSpanArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val spans: Array<Span>
) : Label()

@LabelMetadata(classpath = "test")
data class HasNullableSpanArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val spans: Array<Span>?
) : Label()

@LabelMetadata(classpath = "test")
data class HasSpanList(
        override val startIndex: Int,
        override val endIndex: Int,
        val spans: List<Span>
) : Label()

@LabelMetadata(classpath = "test")
data class HasLabel(
        override val startIndex: Int,
        override val endIndex: Int,
        val hasBoolean: HasBoolean?
) : Label()

@LabelMetadata(classpath = "test")
data class HasLabelArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val hasBooleans: Array<HasBoolean>?
) : Label()

@LabelMetadata(classpath = "test")
data class HasLabelList(
        override val startIndex: Int,
        override val endIndex: Int,
        val hasBooleans: List<HasBoolean>?
) : Label()

@LabelMetadata(classpath = "test", distinct = true)
data class HasBoolean(
        override val startIndex: Int,
        override val endIndex: Int,
        val boolean: Boolean
) : Label()

@LabelMetadata(classpath = "test", distinct = true)
data class HasByte(
        val byte: Byte,
        override val startIndex: Int,
        override val endIndex: Int
) : Label()

@LabelMetadata(classpath = "test", distinct = true)
data class HasShort(
        override val startIndex: Int,
        override val endIndex: Int,
        val short: Short
) : Label()

@LabelMetadata(classpath = "test")
data class HasInt(override val startIndex: Int, override val endIndex: Int, val int: Int) : Label()

@LabelMetadata(classpath = "test")
data class HasLong(
        override val startIndex: Int,
        override val endIndex: Int,
        val long: Long
) : Label()

@LabelMetadata(classpath = "test")
data class HasFloat(
        override val startIndex: Int,
        override val endIndex: Int,
        val float: Float
) : Label()

@LabelMetadata(classpath = "test")
data class HasDouble(
        override val startIndex: Int,
        override val endIndex: Int,
        val double: Double
) : Label()

@LabelMetadata(classpath = "test")
data class HasString(
        override val startIndex: Int,
        override val endIndex: Int,
        val string: String?
) : Label()

@LabelMetadata(classpath = "test")
data class HasBooleanArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val booleanArray: BooleanArray?
) : Label()

@LabelMetadata(classpath = "test")
data class HasByteArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val byteArray: ByteArray?
) : Label()

@LabelMetadata(classpath = "test")
data class HasShortArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val shortArray: ShortArray?
) : Label()

@LabelMetadata(classpath = "test")
data class HasIntArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val intArray: IntArray?
) : Label()

@LabelMetadata(classpath = "test")
data class HasLongArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val longArray: LongArray?
) : Label()

@LabelMetadata(classpath = "test")
data class HasFloatArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val floatArray: FloatArray?
) : Label()

@LabelMetadata(classpath = "test")
data class HasDoubleArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val doubleArray: DoubleArray?
) : Label()

@LabelMetadata(classpath = "test")
data class HasStringArray(
        override val startIndex: Int,
        override val endIndex: Int,
        val stringArray: Array<String>?
) : Label()

@LabelMetadata(classpath = "test")
data class HasBooleanList(
        override val startIndex: Int,
        override val endIndex: Int,
        val booleans: List<Boolean>?
) : Label()

@LabelMetadata(classpath = "test")
data class HasByteList(
        override val startIndex: Int,
        override val endIndex: Int,
        val bytes: List<Byte>?
) : Label()

@LabelMetadata(classpath = "test")
data class HasShortList(
        override val startIndex: Int,
        override val endIndex: Int,
        val shorts: List<Short>?
) : Label()

@LabelMetadata(classpath = "test")
data class HasIntList(
        override val startIndex: Int,
        override val endIndex: Int,
        val ints: List<Int>?
) : Label()

@LabelMetadata(classpath = "test")
data class HasLongList(
        override val startIndex: Int,
        override val endIndex: Int,
        val longs: List<Long>?
) : Label()

@LabelMetadata(classpath = "test")
data class HasFloatList(
        override val startIndex: Int,
        override val endIndex: Int,
        val floats: List<Float>?
) : Label()

@LabelMetadata(classpath = "test")
data class HasDoubleList(
        override val startIndex: Int,
        override val endIndex: Int,
        val doubles: List<Double>?
) : Label()

@LabelMetadata(classpath = "test")
data class HasStringList(
        override val startIndex: Int,
        override val endIndex: Int,
        val strings: List<String>?
) : Label()

class AutoAdapterTest {

    val labelAdapters: LabelAdapters

    val ts = TypeSystemDescription_impl()

    var _cas: CAS? = null
    val cas: CAS get() = _cas ?: throw IllegalStateException("Cas not set")

    init {
        val autoAdapters = AutoAdapters(LabelAdapters(null), null)

        autoAdapters.addEnumClass(Foo::class.java)

        autoAdapters.addLabelClass(HasEnum::class.java)
        autoAdapters.addLabelClass(HasNullableEnum::class.java)
        autoAdapters.addLabelClass(HasEnumArray::class.java)
        autoAdapters.addLabelClass(HasNullableEnumArray::class.java)
        autoAdapters.addLabelClass(HasEnumList::class.java)
        autoAdapters.addLabelClass(HasNullableEnumList::class.java)
        autoAdapters.addLabelClass(HasBigDecimal::class.java)
        autoAdapters.addLabelClass(HasNullableBigDecimal::class.java)
        autoAdapters.addLabelClass(HasSpan::class.java)
        autoAdapters.addLabelClass(HasNullableSpan::class.java)
        autoAdapters.addLabelClass(HasSpanArray::class.java)
        autoAdapters.addLabelClass(HasNullableSpanArray::class.java)
        autoAdapters.addLabelClass(HasSpanList::class.java)
        autoAdapters.addLabelClass(HasLabel::class.java)
        autoAdapters.addLabelClass(HasLabelArray::class.java)
        autoAdapters.addLabelClass(HasLabelList::class.java)

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

        autoAdapters.addToTypeSystem(ts)

        labelAdapters = autoAdapters.labelAdapters
    }

    @BeforeTest
    fun setUp() {
        _cas = CasCreationUtils.createCas(ts, null, null)
    }

    @Test
    fun testEnumFeatureAnnotationToLabel() {
        val hasEnumAdapter = labelAdapters.getLabelAdapterFactory(HasEnum::class.java).create(cas, null)

        val annotation = cas.createAnnotation<AnnotationFS>(hasEnumAdapter.type, 0, 5)
        annotation.setStringValue(hasEnumAdapter.type.getFeatureByBaseName("foo"), "BAR")

        val label = hasEnumAdapter.annotationToLabel(annotation)

        assertEquals(label.foo, Foo.BAR)
    }

    @Test
    fun testEnumFeatureLabelToAnnotation() {
        val hasEnumAdapter = labelAdapters.getLabelAdapterFactory(HasEnum::class.java).create(cas, null)
        val label = HasEnum(0, 5, Foo.BAR)
        val annotation = hasEnumAdapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("foo")
        assertEquals(annotation.getStringValue(feature), "BAR")
    }

    @Test
    fun testNullableEnumFeatureAnnotationToLabel() {
        val hasEnumAdapter = labelAdapters.getLabelAdapterFactory(HasNullableEnum::class.java)
                .create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(hasEnumAdapter.type, 0, 5)
        val feature = hasEnumAdapter.type.getFeatureByBaseName("foo")
        annotation.setStringValue(feature, "BAR")
        val label = hasEnumAdapter.annotationToLabel(annotation)

        assertEquals(label.foo, Foo.BAR)
    }

    @Test
    fun testNullableEnumFeatureLabelToAnnotation() {
        val hasEnumAdapter = labelAdapters.getLabelAdapterFactory(HasNullableEnum::class.java)
                .create(cas, null)

        val label = HasNullableEnum(0, 5, Foo.BAR)

        val annotation = hasEnumAdapter.labelToAnnotation(label)

        val feature = annotation.type.getFeatureByBaseName("foo")
        assertEquals(annotation.getStringValue(feature), "BAR")
    }

    @Test
    fun testNullableEnumFeatureAnnotationToLabelNullValue() {
        val hasEnumAdapter = labelAdapters.getLabelAdapterFactory(HasNullableEnum::class.java)
                .create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(hasEnumAdapter.type, 0, 5)
        val label = hasEnumAdapter.annotationToLabel(annotation)
        assertEquals(label.foo, null)
    }

    @Test
    fun testNullableEnumFeatureLabelToAnnotationValue() {
        val hasEnumAdapter = labelAdapters.getLabelAdapterFactory(HasNullableEnum::class.java)
                .create(cas, null)
        val label = HasNullableEnum(0, 5, null)
        val annotation = hasEnumAdapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("foo")
        assertEquals(annotation.getStringValue(feature), null)
    }

    @Test
    fun testEnumArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasEnumArray::class.java).create(cas, null)
        val arrayFS = cas.createStringArrayFS(3)
        arrayFS[0] = "BAR"
        arrayFS[1] = "BAZ"
        arrayFS[2] = "BAR"
        cas.addFsToIndexes(arrayFS)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("enums"), arrayFS)

        val label = adapter.annotationToLabel(annotation)

        assertTrue(assertNotNull(label.enums) contentEquals arrayOf(Foo.BAR, Foo.BAZ, Foo.BAR))
    }

    @Test
    fun testEnumArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasEnumArray::class.java).create(cas, null)
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
    fun testNullableEnumArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableEnumArray::class.java).create(cas, null)
        val arrayFS = cas.createStringArrayFS(3)
        arrayFS[0] = "BAR"
        arrayFS[1] = "BAZ"
        arrayFS[2] = "BAR"
        cas.addFsToIndexes(arrayFS)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("enums"), arrayFS)

        val label = adapter.annotationToLabel(annotation)

        assertTrue(assertNotNull(label.enums) contentEquals arrayOf(Foo.BAR, Foo.BAZ, Foo.BAR))
    }

    @Test
    fun testNullableEnumArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableEnumArray::class.java).create(cas, null)
        val label = HasNullableEnumArray(0, 5, arrayOf(Foo.BAR, Foo.BAZ, Foo.BAR))
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
    fun testNullableEnumArrayFeatureToLabelNullValue() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableEnumArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)

        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.enums, null)
    }

    @Test
    fun testNullableEnumArrayFeatureToAnnotationNullValue() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableEnumArray::class.java).create(cas, null)
        val label = HasNullableEnumArray(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("enums")

        val arrayFS = annotation.getFeatureValue(feature) as StringArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testEnumListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasEnumList::class.java).create(cas, null)
        val arrayFS = cas.createStringArrayFS(3)
        arrayFS[0] = "BAR"
        arrayFS[1] = "BAZ"
        arrayFS[2] = "BAR"
        cas.addFsToIndexes(arrayFS)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("enums"), arrayFS)

        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.enums, listOf(Foo.BAR, Foo.BAZ, Foo.BAR))
    }

    @Test
    fun testEnumListFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasEnumList::class.java).create(cas, null)
        val label = HasEnumList(0, 5, listOf(Foo.BAR, Foo.BAZ, Foo.BAR))
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
    fun testNullableEnumListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableEnumList::class.java).create(cas, null)
        val arrayFS = cas.createStringArrayFS(3)
        arrayFS[0] = "BAR"
        arrayFS[1] = "BAZ"
        arrayFS[2] = "BAR"
        cas.addFsToIndexes(arrayFS)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("enums"), arrayFS)

        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.enums, listOf(Foo.BAR, Foo.BAZ, Foo.BAR))
    }

    @Test
    fun testNullableEnumListFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableEnumList::class.java).create(cas, null)
        val label = HasNullableEnumList(0, 5, listOf(Foo.BAR, Foo.BAZ, Foo.BAR))
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
    fun testNullableEnumListFeatureToLabelNullValue() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableEnumList::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)

        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.enums, null)
    }

    @Test
    fun testNullableEnumListFeatureToAnnotationNullValue() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableEnumList::class.java).create(cas, null)
        val label = HasNullableEnumList(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("enums")

        val arrayFS = annotation.getFeatureValue(feature) as StringArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testBigDecimalFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBigDecimal::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val feature = annotation.type.getFeatureByBaseName("bigDecimal")
        annotation.setStringValue(feature, BigDecimal(45.0).toString())
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.bigDecimal, BigDecimal(45.0))
    }

    @Test
    fun testBigDecimalFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBigDecimal::class.java).create(cas, null)
        val label = HasBigDecimal(0, 5, BigDecimal(45.0))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("bigDecimal")

        assertEquals(annotation.getStringValue(feature), BigDecimal(45.0).toString())
    }

    @Test
    fun testNullableBigDecimalFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableBigDecimal::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val feature = annotation.type.getFeatureByBaseName("bigDecimal")
        annotation.setStringValue(feature, BigDecimal(45.0).toString())
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.bigDecimal, BigDecimal(45.0))
    }

    @Test
    fun testNullableBigDecimalFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableBigDecimal::class.java).create(cas, null)
        val label = HasNullableBigDecimal(0, 5, BigDecimal(45.0))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("bigDecimal")

        assertEquals(annotation.getStringValue(feature), BigDecimal(45.0).toString())
    }

    @Test
    fun testNullableBigDecimalFeatureToLabelNullValue() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableBigDecimal::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.bigDecimal, null)
    }

    @Test
    fun testNullableBigDecimalFeatureToAnnotationNullValue() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableBigDecimal::class.java).create(cas, null)
        val label = HasNullableBigDecimal(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("bigDecimal")

        assertEquals(annotation.getStringValue(feature), null)
    }

    @Test
    fun testSpanFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasSpan::class.java).create(cas, null)
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
        val adapter = labelAdapters.getLabelAdapterFactory(HasSpan::class.java).create(cas, null)
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
    fun testNullableSpanFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableSpan::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val feature = annotation.type.getFeatureByBaseName("span")
        val spannotation = cas.createAnnotation<AnnotationFS>(cas.annotationType, 10, 20)
        cas.addFsToIndexes(spannotation)
        annotation.setFeatureValue(feature, spannotation)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.span, Span(10, 20))
    }

    @Test
    fun testNullableSpanFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableSpan::class.java).create(cas, null)
        val label = HasNullableSpan(0, 5, Span(10, 20))
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
    fun testNullableSpanFeatureToLabelNullValue() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableSpan::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.span, null)
    }

    @Test
    fun testNullableSpanFeatureToAnnotationNullValue() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableSpan::class.java).create(cas, null)
        val label = HasNullableSpan(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("span")
        val spannotation = annotation.getFeatureValue(feature) as AnnotationFS?

        assertNull(spannotation)
    }

    @Test
    fun testSpanArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasSpanArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val span1 = cas.createAnnotation<AnnotationFS>(cas.annotationType, 10, 15)
        cas.addFsToIndexes(span1)
        val span2 = cas.createAnnotation<AnnotationFS>(cas.annotationType, 15, 20)
        cas.addFsToIndexes(span2)
        val arrayFS = cas.createArrayFS(2)
        arrayFS[0] = span1
        arrayFS[1] = span2
        cas.addFsToIndexes(arrayFS)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("spans"), arrayFS)

        val label = adapter.annotationToLabel(annotation)
        val spans = assertNotNull(label.spans)
        assertTrue(spans contentEquals arrayOf(Span(10, 15), Span(15, 20)))
    }

    @Test
    fun testSpanArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasSpanArray::class.java).create(cas, null)
        val label = HasSpanArray(0, 5, arrayOf(Span(10, 15), Span(15, 20)))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("spans")

        val arrayFS = annotation.getFeatureValue(feature) as ArrayFS?
        assertNotNull(arrayFS)
        if (arrayFS != null) {
            val first = arrayFS[0] as AnnotationFS
            assertEquals(first.begin, 10)
            assertEquals(first.end, 15)
            val second = arrayFS[1] as AnnotationFS
            assertEquals(second.begin, 15)
            assertEquals(second.end, 20)
        }
    }

    @Test
    fun testNullableSpanArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableSpanArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val span1 = cas.createAnnotation<AnnotationFS>(cas.annotationType, 10, 15)
        cas.addFsToIndexes(span1)
        val span2 = cas.createAnnotation<AnnotationFS>(cas.annotationType, 15, 20)
        cas.addFsToIndexes(span2)
        val arrayFS = cas.createArrayFS(2)
        arrayFS[0] = span1
        arrayFS[1] = span2
        cas.addFsToIndexes(arrayFS)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("spans"), arrayFS)

        val label = adapter.annotationToLabel(annotation)
        val spans = assertNotNull(label.spans)
        assertTrue(spans contentEquals arrayOf(Span(10, 15), Span(15, 20)))
    }

    @Test
    fun testNullableSpanArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableSpanArray::class.java).create(cas, null)
        val label = HasNullableSpanArray(0, 5, arrayOf(Span(10, 15), Span(15, 20)))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("spans")

        val arrayFS = annotation.getFeatureValue(feature) as ArrayFS?
        assertNotNull(arrayFS)
        if (arrayFS != null) {
            val first = arrayFS[0] as AnnotationFS
            assertEquals(first.begin, 10)
            assertEquals(first.end, 15)
            val second = arrayFS[1] as AnnotationFS
            assertEquals(second.begin, 15)
            assertEquals(second.end, 20)
        }
    }

    @Test
    fun testNullableSpanArrayFeatureToLabelNullValue() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableSpanArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)

        val label = adapter.annotationToLabel(annotation)
        val spans = label.spans
        assertEquals(spans, null)
    }

    @Test
    fun testNullableSpanArrayFeatureToAnnotationNullValue() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasNullableSpanArray::class.java).create(cas, null)
        val label = HasNullableSpanArray(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("spans")

        val arrayFS = annotation.getFeatureValue(feature) as ArrayFS?
        assertNull(arrayFS)
    }

    @Test
    fun testSpanListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasSpanList::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val span1 = cas.createAnnotation<AnnotationFS>(cas.annotationType, 10, 15)
        cas.addFsToIndexes(span1)
        val span2 = cas.createAnnotation<AnnotationFS>(cas.annotationType, 15, 20)
        cas.addFsToIndexes(span2)
        val arrayFS = cas.createArrayFS(2)
        arrayFS[0] = span1
        arrayFS[1] = span2
        cas.addFsToIndexes(arrayFS)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("spans"), arrayFS)

        val label = adapter.annotationToLabel(annotation)
        val spans = label.spans
        assertEquals(spans, listOf(Span(10, 15), Span(15, 20)))
    }

    @Test
    fun testSpanListFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasSpanList::class.java).create(cas, null)
        val label = HasSpanList(0, 5, listOf(Span(10, 15), Span(15, 20)))
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("spans")

        val arrayFS = annotation.getFeatureValue(feature) as ArrayFS?
        assertNotNull(arrayFS)
        if (arrayFS != null) {
            val first = arrayFS[0] as AnnotationFS
            assertEquals(first.begin, 10)
            assertEquals(first.end, 15)
            val second = arrayFS[1] as AnnotationFS
            assertEquals(second.begin, 15)
            assertEquals(second.end, 20)
        }
    }

    @Test
    fun testLabelFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabel::class.java).create(cas, null)
        val booleanAdapter = labelAdapters.getLabelAdapterFactory(HasBoolean::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val booleanAnnotation = cas.createAnnotation<AnnotationFS>(booleanAdapter.type, 10, 15)
        booleanAnnotation.setBooleanValue(booleanAdapter.type.getFeatureByBaseName("boolean"), true)
        cas.addFsToIndexes(booleanAnnotation)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("hasBoolean"), booleanAnnotation)

        val label = adapter.annotationToLabel(annotation)

        val hasBoolean = label.hasBoolean

        assertNotNull(hasBoolean)
        assertEquals(hasBoolean?.startIndex, 10)
        assertEquals(hasBoolean?.endIndex, 15)
        assertEquals(hasBoolean?.boolean, true)
    }

    @Test
    fun testLabelFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabel::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.hasBoolean)
    }

    @Test
    fun testLabelFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabel::class.java).create(cas, null)
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
    fun testLabelFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabel::class.java).create(cas, null)
        val label = HasLabel(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val hasBooleanFeature = annotation.type.getFeatureByBaseName("hasBoolean")

        val hasBooleanAnnotation = annotation.getFeatureValue(hasBooleanFeature) as AnnotationFS?

        assertNull(hasBooleanAnnotation)
    }

    @Test
    fun testLabelArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabelArray::class.java).create(cas, null)
        val booleanAdapter = labelAdapters.getLabelAdapterFactory(HasBoolean::class.java).create(cas, null)
        val booleanFeature = booleanAdapter.type.getFeatureByBaseName("boolean")

        val arrayFS = cas.createArrayFS(2)

        val booleanAnnotation1 = cas.createAnnotation<AnnotationFS>(booleanAdapter.type, 10, 15)
        booleanAnnotation1.setBooleanValue(booleanFeature, true)
        cas.addFsToIndexes(booleanAnnotation1)
        arrayFS[0] = booleanAnnotation1

        val booleanAnnotation2 = cas.createAnnotation<AnnotationFS>(booleanAdapter.type, 15, 20)
        booleanAnnotation2.setBooleanValue(booleanFeature, false)
        cas.addFsToIndexes(booleanAnnotation2)
        arrayFS[1] = booleanAnnotation2

        cas.addFsToIndexes(arrayFS)

        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setFeatureValue(adapter.type.getFeatureByBaseName("hasBooleans"), arrayFS)

        val label = adapter.annotationToLabel(annotation)

        val hasBooleans = label.hasBooleans
        assertNotNull(hasBooleans)
        assertEquals(hasBooleans?.size, 2)
        assertEquals(hasBooleans?.get(0), HasBoolean(10, 15, true))
        assertEquals(hasBooleans?.get(1), HasBoolean(15, 20, false))
    }

    @Test
    fun testLabelArrayFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabelArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.hasBooleans)
    }

    @Test
    fun testLabelArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabelArray::class.java).create(cas, null)
        val label = HasLabelArray(0, 5, arrayOf(
                HasBoolean(10, 15, true),
                HasBoolean(15, 20, false)
        ))
        val annotation = adapter.labelToAnnotation(label)

        val arrayFS = annotation.getFeatureValue(annotation.type.getFeatureByBaseName("hasBooleans")) as ArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 2)
            val first = arrayFS[0] as AnnotationFS
            val booleanFeature = first.type.getFeatureByBaseName("boolean")
            assertEquals(first.begin, 10)
            assertEquals(first.end, 15)
            assertEquals(first.getBooleanValue(booleanFeature), true)

            val second = arrayFS[1] as AnnotationFS
            assertEquals(second.begin, 15)
            assertEquals(second.end, 20)
            assertEquals(second.getBooleanValue(booleanFeature), false)
        }
    }

    @Test
    fun testLabelArrayFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabelArray::class.java).create(cas, null)
        val label = HasLabelArray(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("hasBooleans")
        assertNull(annotation.getFeatureValue(feature))
    }

    @Test
    fun testLabelListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabelList::class.java).create(cas, null)
        val booleanAdapter = labelAdapters.getLabelAdapterFactory(HasBoolean::class.java).create(cas, null)
        val booleanFeature = booleanAdapter.type.getFeatureByBaseName("boolean")

        val arrayFS = cas.createArrayFS(2)

        val booleanAnnotation1 = cas.createAnnotation<AnnotationFS>(booleanAdapter.type, 10, 15)
        booleanAnnotation1.setBooleanValue(booleanFeature, true)
        cas.addFsToIndexes(booleanAnnotation1)
        arrayFS[0] = booleanAnnotation1

        val booleanAnnotation2 = cas.createAnnotation<AnnotationFS>(booleanAdapter.type, 15, 20)
        booleanAnnotation2.setBooleanValue(booleanFeature, false)
        cas.addFsToIndexes(booleanAnnotation2)
        arrayFS[1] = booleanAnnotation2

        cas.addFsToIndexes(arrayFS)

        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setFeatureValue(adapter.type.getFeatureByBaseName("hasBooleans"), arrayFS)

        val label = adapter.annotationToLabel(annotation)

        val hasBooleans = label.hasBooleans

        assertEquals(hasBooleans?.size, 2)
        assertEquals(hasBooleans?.get(0), HasBoolean(10, 15, true))
        assertEquals(hasBooleans?.get(1), HasBoolean(15, 20, false))
    }

    @Test
    fun testLabelListFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabelList::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)
        assertNull(label.hasBooleans)
    }

    @Test
    fun testLabelListFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabelList::class.java).create(cas, null)
        val label = HasLabelList(0, 5, listOf(
                HasBoolean(10, 15, true),
                HasBoolean(15, 20, false)
        ))
        val annotation = adapter.labelToAnnotation(label)

        val arrayFS = annotation.getFeatureValue(annotation.type.getFeatureByBaseName("hasBooleans")) as ArrayFS?

        assertNotNull(arrayFS)
        if (arrayFS != null) {
            assertEquals(arrayFS.size(), 2)
            val first = arrayFS[0] as AnnotationFS
            val booleanFeature = first.type.getFeatureByBaseName("boolean")
            assertEquals(first.begin, 10)
            assertEquals(first.end, 15)
            assertEquals(first.getBooleanValue(booleanFeature), true)

            val second = arrayFS[1] as AnnotationFS
            assertEquals(second.begin, 15)
            assertEquals(second.end, 20)
            assertEquals(second.getBooleanValue(booleanFeature), false)
        }
    }

    @Test
    fun testLabelListFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLabelList::class.java).create(cas, null)
        val label = HasLabelList(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val arrayFS = annotation.getFeatureValue(annotation.type.getFeatureByBaseName("hasBooleans")) as ArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testBooleanFeatureToLabel() {
        val hasBooleanAdapter = labelAdapters.getLabelAdapterFactory(HasBoolean::class.java).create(cas, null)

        val annotation = cas.createAnnotation<AnnotationFS>(hasBooleanAdapter.type, 0, 5)

        val booleanFeature = annotation.type.getFeatureByBaseName("boolean")
        annotation.setBooleanValue(booleanFeature, true)

        val label = hasBooleanAdapter.annotationToLabel(annotation)

        assertTrue(label.boolean)
    }

    @Test
    fun testBooleanFeatureToAnnotation() {
        val create = labelAdapters.getLabelAdapterFactory(HasBoolean::class.java).create(cas, null)

        val label = HasBoolean(0, 5, true)

        val annotation = create.labelToAnnotation(label)

        val feature = annotation.type.getFeatureByBaseName("boolean")
        assertTrue(annotation.getBooleanValue(feature))
    }

    @Test
    fun testByteFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByte::class.java).create(cas, null)

        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)

        annotation.setByteValue(annotation.type.getFeatureByBaseName("byte"), 0xA)

        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.byte, 0xA)
    }

    @Test
    fun testByteFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByte::class.java).create(cas, null)

        val label = HasByte(0xA, 0, 5)

        val annotation = adapter.labelToAnnotation(label)

        val feature = adapter.type.getFeatureByBaseName("byte")

        assertEquals(annotation.getByteValue(feature), 0xA)
    }

    @Test
    fun testShortFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShort::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setShortValue(annotation.type.getFeatureByBaseName("short"), 5)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.short, 5.toShort())
    }

    @Test
    fun testShortFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShort::class.java).create(cas, null)
        val label = HasShort(0, 5, 5)
        val annotation = adapter.labelToAnnotation(label)
        val feature = adapter.type.getFeatureByBaseName("short")
        assertEquals(annotation.getShortValue(feature), 5)
    }

    @Test
    fun testIntFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasInt::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setIntValue(annotation.type.getFeatureByBaseName("int"), 5)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.int, 5)
    }

    @Test
    fun testIntFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasInt::class.java).create(cas, null)
        val label = HasInt(0, 5, 5)
        val annotation = adapter.labelToAnnotation(label)
        val feature = adapter.type.getFeatureByBaseName("int")

        assertEquals(annotation.getIntValue(feature), 5)
    }

    @Test
    fun testLongFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLong::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setLongValue(annotation.type.getFeatureByBaseName("long"), 5)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.long, 5)
    }

    @Test
    fun testLongFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLong::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasLong(0, 5, 5))
        val feature = adapter.type.getFeatureByBaseName("long")

        assertEquals(annotation.getLongValue(feature), 5)
    }

    @Test
    fun testFloatFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloat::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setFloatValue(annotation.type.getFeatureByBaseName("float"), 5.0f)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.float, 5.0f)
    }

    @Test
    fun testFloatFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloat::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasFloat(0, 5, 5.0f))
        val feature = adapter.type.getFeatureByBaseName("float")

        assertEquals(annotation.getFloatValue(feature), 5.0f)
    }

    @Test
    fun testDoubleFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDouble::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setDoubleValue(annotation.type.getFeatureByBaseName("double"), 5.0)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.double, 5.0)
    }

    @Test
    fun testDoubleFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDouble::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasDouble(0, 5, 5.0))
        val feature = annotation.type.getFeatureByBaseName("double")

        assertEquals(annotation.getDoubleValue(feature), 5.0)
    }

    @Test
    fun testStringFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasString::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setStringValue(annotation.type.getFeatureByBaseName("string"), "blah")
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.string, "blah")
    }

    @Test
    fun testStringFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasString::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.string)
    }

    @Test
    fun testStringFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasString::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasString(0, 5, "blah"))
        val feature = annotation.type.getFeatureByBaseName("string")

        assertEquals(annotation.getStringValue(feature), "blah")
    }

    @Test
    fun testStringFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasString::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasString(0, 5, null))
        val feature = annotation.type.getFeatureByBaseName("string")

        assertEquals(annotation.getStringValue(feature), null)
    }

    @Test
    fun testBooleanArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBooleanArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createBooleanArrayFS(3)
        arrayFS[0] = true
        arrayFS[1] = false
        arrayFS[2] = true
        cas.addFsToIndexes(arrayFS)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("booleanArray"), arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertTrue(assertNotNull(label.booleanArray) contentEquals booleanArrayOf(true, false, true))
    }

    @Test
    fun testBooleanArrayFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBooleanArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("booleanArray"), null)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.booleanArray, null)
    }

    @Test
    fun testBooleanArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBooleanArray::class.java).create(cas, null)
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
    fun testBooleanArrayFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBooleanArray::class.java).create(cas, null)
        val booleanArray = HasBooleanArray(0, 5, null)
        val annotation = adapter.labelToAnnotation(booleanArray)
        val feature = annotation.type.getFeatureByBaseName("booleanArray")
        val arrayFS = annotation.getFeatureValue(feature) as BooleanArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testByteArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByteArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createByteArrayFS(3)
        arrayFS[0] = 1
        arrayFS[1] = 0
        arrayFS[2] = 2
        cas.addFsToIndexes(arrayFS)
        annotation.setFeatureValue(annotation.type.getFeatureByBaseName("byteArray"), arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertTrue(label.byteArray?.contentEquals(byteArrayOf(1, 0, 2)) ?: false)
    }

    @Test
    fun testByteArrayFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByteArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val feature = annotation.type.getFeatureByBaseName("byteArray")
        annotation.setFeatureValue(feature, null)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.byteArray, null)
    }

    @Test
    fun testByteArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByteArray::class.java).create(cas, null)
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
    fun testByteArrayFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByteArray::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasByteArray(0, 5, null))
        val feature = annotation.type.getFeatureByBaseName("byteArray")
        val arrayFS = annotation.getFeatureValue(feature) as ByteArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testShortArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShortArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createShortArrayFS(3)
        arrayFS[0] = 32
        arrayFS[1] = 64
        arrayFS[2] = 128
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("shortArray")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertTrue(shortArrayOf(32, 64, 128) contentEquals (label.shortArray ?: fail()))
    }

    @Test
    fun testShortArrayFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShortArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertEquals(label.shortArray, null)
    }

    @Test
    fun testShortArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShortArray::class.java).create(cas, null)
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
    fun testShortArrayFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShortArray::class.java).create(cas, null)
        val label = HasShortArray(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("shortArray")
        val arrayFS = annotation.getFeatureValue(feature) as ShortArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testIntArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasIntArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createIntArrayFS(3)
        arrayFS[0] = 32
        arrayFS[1] = 64
        arrayFS[2] = 128
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("intArray")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertTrue(assertNotNull(label.intArray) contentEquals intArrayOf(32, 64, 128))
    }

    @Test
    fun testIntArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasIntArray::class.java).create(cas, null)
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
    fun testIntArrayFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasIntArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.intArray)
    }

    @Test
    fun testIntArrayFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasIntArray::class.java).create(cas, null)
        val label = HasIntArray(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("intArray")
        val arrayFS = annotation.getFeatureValue(feature) as IntArrayFS?

        assertNull(arrayFS)
    }
    
    @Test
    fun testLongArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLongArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createLongArrayFS(3)
        arrayFS[0] = 32
        arrayFS[1] = 64
        arrayFS[2] = 128
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("longArray")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertTrue(longArrayOf(32, 64, 128) contentEquals assertNotNull(label.longArray))
    }

    @Test
    fun testLongArrayFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLongArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.longArray)
    }

    @Test
    fun testLongArrayFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLongArray::class.java).create(cas, null)
        val label = HasLongArray(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("longArray")
        val arrayFS = annotation.getFeatureValue(feature) as LongArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testLongArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLongArray::class.java).create(cas, null)
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
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloatArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createFloatArrayFS(3)
        arrayFS[0] = 32f
        arrayFS[1] = 64f
        arrayFS[2] = 128f
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("floatArray")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertTrue(assertNotNull(label.floatArray) contentEquals floatArrayOf(32f, 64f, 128f))
    }

    @Test
    fun testFloatArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloatArray::class.java).create(cas, null)
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
    fun testFloatArrayFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloatArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.floatArray)
    }

    @Test
    fun testFloatArrayFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloatArray::class.java).create(cas, null)
        val label = HasFloatArray(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("floatArray")
        val arrayFS = annotation.getFeatureValue(feature) as FloatArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testDoubleArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDoubleArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createDoubleArrayFS(3)
        arrayFS[0] = 32.0
        arrayFS[1] = 64.0
        arrayFS[2] = 128.0
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("doubleArray")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertTrue(assertNotNull(label.doubleArray) contentEquals doubleArrayOf(32.0, 64.0, 128.0))
    }

    @Test
    fun testDoubleArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDoubleArray::class.java).create(cas, null)
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
    fun testDoubleArrayFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDoubleArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.doubleArray)
    }

    @Test
    fun testDoubleArrayFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDoubleArray::class.java).create(cas, null)
        val label = HasDoubleArray(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("doubleArray")
        val arrayFS = annotation.getFeatureValue(feature) as DoubleArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testStringArrayFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasStringArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val arrayFS = cas.createStringArrayFS(3)
        arrayFS[0] = "a"
        arrayFS[1] = "b"
        arrayFS[2] = "c"
        cas.addFsToIndexes(arrayFS)
        val feature = annotation.type.getFeatureByBaseName("stringArray")
        annotation.setFeatureValue(feature, arrayFS)
        val label = adapter.annotationToLabel(annotation)

        assertTrue(arrayOf("a", "b", "c") contentEquals (label.stringArray ?: fail()))
    }

    @Test
    fun testStringArrayFeatureToAnnotation() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasStringArray::class.java).create(cas, null)
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
    fun testStringArrayFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasStringArray::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.stringArray)
    }

    @Test
    fun testStringArrayFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasStringArray::class.java).create(cas, null)
        val label = HasStringArray(0, 5, null)
        val annotation = adapter.labelToAnnotation(label)
        val feature = annotation.type.getFeatureByBaseName("stringArray")
        val arrayFS = annotation.getFeatureValue(feature) as StringArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testBooleanListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBooleanList::class.java).create(cas, null)
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
        val adapter = labelAdapters.getLabelAdapterFactory(HasBooleanList::class.java).create(cas, null)
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
    fun testBooleanListFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBooleanList::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.booleans)
    }

    @Test
    fun testBooleanListFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasBooleanList::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasBooleanList(0, 5, null))
        val feature = annotation.type.getFeatureByBaseName("booleans")
        val arrayFS = annotation.getFeatureValue(feature) as BooleanArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testByteListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByteList::class.java).create(cas, null)
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
        val adapter = labelAdapters.getLabelAdapterFactory(HasByteList::class.java).create(cas, null)
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
    fun testByteListFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByteList::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.bytes)
    }

    @Test
    fun testByteListFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasByteList::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasByteList(0, 5, null))
        val feature = annotation.type.getFeatureByBaseName("bytes")
        val arrayFS = annotation.getFeatureValue(feature) as ByteArrayFS?

        assertNull(arrayFS)
    }
    
    @Test
    fun testShortListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShortList::class.java).create(cas, null)
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
        val adapter = labelAdapters.getLabelAdapterFactory(HasShortList::class.java).create(cas, null)
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
    fun testShortListFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShortList::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.shorts)
    }

    @Test
    fun testShortListFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasShortList::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasShortList(0, 5, null))
        val feature = annotation.type.getFeatureByBaseName("shorts")
        val arrayFS = annotation.getFeatureValue(feature) as ShortArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testIntListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasIntList::class.java).create(cas, null)
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
        val adapter = labelAdapters.getLabelAdapterFactory(HasIntList::class.java).create(cas, null)
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
    fun testIntListFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasIntList::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.ints)
    }

    @Test
    fun testIntListFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasIntList::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasIntList(0, 5, null))
        val feature = annotation.type.getFeatureByBaseName("ints")
        val arrayFS = annotation.getFeatureValue(feature) as IntArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testLongListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLongList::class.java).create(cas, null)
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
        val adapter = labelAdapters.getLabelAdapterFactory(HasLongList::class.java).create(cas, null)
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
    fun testLongListFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLongList::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.longs)
    }

    @Test
    fun testLongListFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasLongList::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasLongList(0, 5, null))
        val feature = annotation.type.getFeatureByBaseName("longs")
        val arrayFS = annotation.getFeatureValue(feature) as LongArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testFloatListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloatList::class.java).create(cas, null)
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
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloatList::class.java).create(cas, null)
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
    fun testFloatListFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloatList::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.floats)
    }

    @Test
    fun testFloatListFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasFloatList::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasFloatList(0, 5, null))
        val feature = annotation.type.getFeatureByBaseName("floats")
        val arrayFS = annotation.getFeatureValue(feature) as FloatArrayFS?

        assertNull(arrayFS)
    }
    
    @Test
    fun testDoubleListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDoubleList::class.java).create(cas, null)
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
        val adapter = labelAdapters.getLabelAdapterFactory(HasDoubleList::class.java).create(cas, null)
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
    fun testDoubleListFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDoubleList::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.doubles)
    }

    @Test
    fun testDoubleListFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasDoubleList::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasDoubleList(0, 5, null))
        val feature = annotation.type.getFeatureByBaseName("doubles")
        val arrayFS = annotation.getFeatureValue(feature) as DoubleArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testStringListFeatureToLabel() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasStringList::class.java).create(cas, null)
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
        val adapter = labelAdapters.getLabelAdapterFactory(HasStringList::class.java).create(cas, null)
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

    @Test
    fun testStringListFeatureToLabelNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasStringList::class.java).create(cas, null)
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        val label = adapter.annotationToLabel(annotation)

        assertNull(label.strings)
    }

    @Test
    fun testStringListFeatureToAnnotationNull() {
        val adapter = labelAdapters.getLabelAdapterFactory(HasStringList::class.java).create(cas, null)
        val annotation = adapter.labelToAnnotation(HasStringList(0, 5, null))
        val feature = annotation.type.getFeatureByBaseName("strings")
        val arrayFS = annotation.getFeatureValue(feature) as StringArrayFS?

        assertNull(arrayFS)
    }

    @Test
    fun testLabelFeatureUsesExisting() {
        val hasBooleanAdapter = labelAdapters.getLabelAdapterFactory(HasBoolean::class.java).create(cas, null)
        val hasLabelAdapter = labelAdapters.getLabelAdapterFactory(HasLabel::class.java).create(cas, null)

        val hasBoolean = HasBoolean(0, 5, true)
        hasBooleanAdapter.labelToAnnotation(hasBoolean)

        val hasLabel = HasLabel(0, 10, hasBoolean)
        hasLabelAdapter.labelToAnnotation(hasLabel)

        assertEquals(cas.getAnnotationIndex<AnnotationFS>(hasBooleanAdapter.type).size(), 1)
        assertNotNull(cas.getAnnotationIndex<AnnotationFS>(hasLabelAdapter.type).first().getFeatureValue(hasLabelAdapter.type.getFeatureByBaseName("hasBoolean")))
    }

    @Test
    fun testLabelArrayUsesExisting() {
        val hasBooleanAdapter = labelAdapters.getLabelAdapterFactory(HasBoolean::class.java).create(cas, null)
        val hasLabelAdapter = labelAdapters.getLabelAdapterFactory(HasLabelArray::class.java).create(cas, null)

        val hasBoolean = HasBoolean(0, 5, true)
        hasBooleanAdapter.labelToAnnotation(hasBoolean)
        val hasBoolean2 = HasBoolean(0, 20, false)
        hasBooleanAdapter.labelToAnnotation(hasBoolean2)

        val hasLabelList = HasLabelArray(0, 2, arrayOf(hasBoolean, hasBoolean2))
        hasLabelAdapter.labelToAnnotation(hasLabelList)

        assertEquals(cas.getAnnotationIndex<AnnotationFS>(hasBooleanAdapter.type).size(), 2)
        assertEquals((cas.getAnnotationIndex<AnnotationFS>(hasLabelAdapter.type).first().getFeatureValue(hasLabelAdapter.type.getFeatureByBaseName("hasBooleans")) as ArrayFS).size(), 2)
    }

    @Test
    fun testLabelListUsesExisting() {
        val hasBooleanAdapter = labelAdapters.getLabelAdapterFactory(HasBoolean::class.java).create(cas, null)
        val hasLabelAdapter = labelAdapters.getLabelAdapterFactory(HasLabelList::class.java).create(cas, null)

        val hasBoolean = HasBoolean(0, 5, true)
        hasBooleanAdapter.labelToAnnotation(hasBoolean)
        val hasBoolean2 = HasBoolean(0, 20, false)
        hasBooleanAdapter.labelToAnnotation(hasBoolean2)

        val hasLabelList = HasLabelList(0, 2, listOf(hasBoolean, hasBoolean2))
        hasLabelAdapter.labelToAnnotation(hasLabelList)

        assertEquals(cas.getAnnotationIndex<AnnotationFS>(hasBooleanAdapter.type).size(), 2)
        assertEquals((cas.getAnnotationIndex<AnnotationFS>(hasLabelAdapter.type).first().getFeatureValue(hasLabelAdapter.type.getFeatureByBaseName("hasBooleans")) as ArrayFS).size(), 2)
    }
}
