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

package edu.umn.biomedicus.uima.labels;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Needs to be subclassed to give proper bounds after type erasure.
 *
 * @param <T>
 * @param <U>
 */
public abstract class LabelableModule<T, U extends Annotation> extends AbstractModule {


    protected abstract LabelAdapter<T, U> getLabelAdapter();

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    LabelAdapter<T, U> provideLabelAdapter() {
        return getLabelAdapter();
    }

    @Provides
    @DocumentScoped
    Labels<T> provideLabels(JCas jCas, LabelAdapter<T, U> labelAdapter) {
        return new UimaLabels<>(jCas, labelAdapter);
    }

    @Provides
    @DocumentScoped
    Labeler<T> provideLabeler(JCas jCas, LabelAdapter<T, U> labelAdapter) {
        return new UimaLabeler<>(jCas, labelAdapter);
    }
}
