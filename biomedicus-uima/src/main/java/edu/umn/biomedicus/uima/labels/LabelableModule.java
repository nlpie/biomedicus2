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
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
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


    private final LabelAdapter<T, U> labelAdapter = getLabelAdapter();
    private final Class<T> tClass;

    protected abstract LabelAdapter<T, U> getLabelAdapter();

    protected LabelableModule(Class<T> tClass) {
        this.tClass = tClass;
    }

    @Override
    protected void configure() {
        MapBinder<Class, LabelAdapter> labelAdapters = MapBinder.newMapBinder(binder(), Class.class, LabelAdapter.class);
        labelAdapters.addBinding(tClass).toInstance(labelAdapter);
    }

    @Provides
    @Singleton
    LabelAdapter<T, U> provideLabelAdapter() {
        return labelAdapter;
    }

    @Provides
    @DocumentScoped
    Labels<T> provideLabels(JCas jCas) {
        return new UimaLabels<>(jCas, labelAdapter);
    }

    @Provides
    @DocumentScoped
    Labeler<T> provideLabeler(JCas jCas) {
        return new UimaLabeler<>(jCas, labelAdapter);
    }
}
