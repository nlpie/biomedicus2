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

import edu.umn.nlpengine.TextRange
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

data class HasEnum(
        override val startIndex: Int,
        override val endIndex: Int,
        val foo: Foo
): TextRange

class AutoAdapterTest {
    @Test
    fun testEnumFeatureAnnotationToLabel() {
        val autoAdapter = AutoAdapter<HasEnum>(HasEnum::class, true)

        val ts = TypeSystemDescription_impl()

        autoAdapter.addTypeToTypeSystem(ts)

        ts.addEnum(Foo::class.java)

        autoAdapter.addFeaturesToTypeSystem(emptyMap())

        val cas = CasCreationUtils.createCas(ts, null, null)

        val adapter = autoAdapter.create(cas)

        val annotation = cas.createAnnotation<AnnotationFS>(adapter.type, 0, 5)
        annotation.setStringValue(adapter.type.getFeatureByBaseName("foo"), "BAR")

        val label = adapter.annotationToLabel(annotation)

        assertTrue(label is HasEnum)
        assertEquals((label as HasEnum).foo, Foo.BAR)
    }

    @Test
    fun testEnumFeatureLabelToAnnotation() {
        val autoAdapter = AutoAdapter<HasEnum>(HasEnum::class, true)

        val ts = TypeSystemDescription_impl()

        autoAdapter.addTypeToTypeSystem(ts)

        ts.addEnum(Foo::class.java)

        autoAdapter.addFeaturesToTypeSystem(emptyMap())

        val cas = CasCreationUtils.createCas(ts, null, null)

        val adapter = autoAdapter.create(cas)

        val label = HasEnum(0, 5, Foo.BAR)

        val annotation = adapter.labelToAnnotation(label)

        assertTrue(annotation.type.shortName == "HasEnum")
        assertEquals(annotation.getStringValue(annotation.type.getFeatureByBaseName("foo")),
                "BAR")
    }
}