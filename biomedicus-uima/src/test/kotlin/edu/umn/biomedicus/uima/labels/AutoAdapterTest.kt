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
import org.apache.uima.cas.CAS
import org.apache.uima.cas.text.AnnotationFS
import org.apache.uima.resource.metadata.impl.TypeSystemDescription_impl
import org.apache.uima.util.CasCreationUtils
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test


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

class AutoAdapterTest {

    val labelAdapters: LabelAdapters

    val cas: CAS

    init {
        val autoAdapters = AutoAdapters(LabelAdapters(null), null)

        autoAdapters.addEnumClass(Foo::class.java)
        autoAdapters.addLabelClass(HasEnum::class.java)
        autoAdapters.addLabelClass(HasBoolean::class.java)
        autoAdapters.addLabelClass(HasByte::class.java)
        autoAdapters.addLabelClass(HasShort::class.java)
        autoAdapters.addLabelClass(HasInt::class.java)

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
        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0,5)
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
}