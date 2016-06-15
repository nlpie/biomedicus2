/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.common.semantics.SubstanceUsageBuilder;
import edu.umn.biomedicus.common.text.SpanLike;
import edu.umn.biomedicus.type.*;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Uima builder for substance usage annotations.
 *
 * @author Ben Knoll
 * @since 1.4
 */
class UimaSubstanceUsageBuilder implements SubstanceUsageBuilder {
    private final JCas jCas;

    private final SubstanceUsageAnnotation substanceUsageAnnotation;

    private final List<Annotation> amounts;

    private final List<Annotation> frequencies;

    private final List<Annotation> types;

    private final List<Annotation> statuses;

    private final List<Annotation> temporals;

    private final List<Annotation> methods;

    public UimaSubstanceUsageBuilder(JCas jCas, SubstanceUsageAnnotation substanceUsageAnnotation) {
        this.jCas = jCas;
        this.substanceUsageAnnotation = substanceUsageAnnotation;
        amounts = new ArrayList<>();
        frequencies = new ArrayList<>();
        types = new ArrayList<>();
        statuses = new ArrayList<>();
        temporals = new ArrayList<>();
        methods = new ArrayList<>();
    }

    private static Annotation init(Annotation annotation, SpanLike spanLike) {
        annotation.setBegin(spanLike.getBegin());
        annotation.setEnd(spanLike.getEnd());
        annotation.addToIndexes();
        return annotation;
    }

    @Override
    public void addAmount(SpanLike spanLike) {
        amounts.add(init(new SubstanceUsageAmount(jCas), spanLike));
    }

    @Override
    public void addFrequency(SpanLike spanLike) {
        frequencies.add(init(new SubstanceUsageFrequency(jCas), spanLike));
    }

    @Override
    public void addType(SpanLike spanLike) {
        types.add(init(new SubstanceUsageType(jCas), spanLike));
    }

    @Override
    public void addStatus(SpanLike spanLike) {
        statuses.add(init(new SubstanceUsageStatus(jCas), spanLike));
    }

    @Override
    public void addTemporal(SpanLike spanLike) {
        temporals.add(init(new SubstanceUsageTemporal(jCas), spanLike));
    }

    @Override
    public void addMethod(SpanLike spanLike) {
        methods.add(init(new SubstanceUsageMethod(jCas), spanLike));
    }

    private FSArray toFsArray(List<Annotation> annotations) {
        FSArray array = new FSArray(jCas, annotations.size());

        for (int i = 0; i < annotations.size(); i++) {
            array.set(i, annotations.get(i));
        }

        array.addToIndexes();

        return array;
    }

    @Override
    public void build() {
        substanceUsageAnnotation.setAmounts(toFsArray(amounts));
        substanceUsageAnnotation.setFrequencies(toFsArray(frequencies));
        substanceUsageAnnotation.setFrequencies(toFsArray(types));
        substanceUsageAnnotation.setStatuses(toFsArray(statuses));
        substanceUsageAnnotation.setTemporal(toFsArray(temporals));
        substanceUsageAnnotation.setMethods(toFsArray(methods));

        substanceUsageAnnotation.addToIndexes();
    }
}
