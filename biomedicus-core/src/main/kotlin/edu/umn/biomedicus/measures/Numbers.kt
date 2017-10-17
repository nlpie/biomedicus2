/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.measures

import edu.umn.biomedicus.common.StandardViews
import edu.umn.biomedicus.exc.BiomedicusException
import edu.umn.biomedicus.framework.DocumentProcessor
import edu.umn.biomedicus.framework.SearchExprFactory
import edu.umn.biomedicus.framework.store.Document
import edu.umn.biomedicus.framework.store.Label
import java.math.BigDecimal
import javax.inject.Inject

data class NumberRange(val lower: BigDecimal, val upper: BigDecimal)

class NumberRangesLabeler @Inject internal constructor(
        searchExprFactory: SearchExprFactory
): DocumentProcessor {
    val expr = searchExprFactory.parse(
            "[Sentence (?<range> [lower:Number] ParseToken<text=\"-\"|i\"to\"> [upper:Number!]) | " +
                    "(?<range> ParseToken<text=i\"between\"> [lower:Number] ParseToken<text=\"and\"> [upper:Number!])]"
    )

    override fun process(document: Document) {
        val systemView = StandardViews.getSystemView(document)

        val labeler = systemView.getLabeler(NumberRange::class.java)

        val searcher = expr.createSearcher(systemView)

        while (searcher.search()) {
            @Suppress("UNCHECKED_CAST")
            val lower = searcher.getLabel("lower").orElseThrow {
                BiomedicusException("No lower")
            } as Label<Number>

            @Suppress("UNCHECKED_CAST")
            val upper = searcher.getLabel("upper").orElseThrow {
                BiomedicusException("No upper")
            } as Label<Number>


            val lowerValue = lower.value.value()
            val upperValue = upper.value.value()
            if (upperValue > lowerValue) {
                labeler.value(NumberRange(lowerValue, upperValue)).label(searcher.getSpan("range").get())
            }
        }
    }
}