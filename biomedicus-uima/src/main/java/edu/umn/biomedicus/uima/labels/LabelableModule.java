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
import com.google.inject.multibindings.MapBinder;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import org.apache.uima.cas.CAS;

/**
 * Needs to be subclassed to give proper bounds after type erasure.
 *
 * @param <T>
 */
public abstract class LabelableModule<T> extends AbstractModule {
    private final LabelAdapterFactory<T> labelAdapterFactory = createFactory();
    private final Class<T> tClass;

    protected abstract LabelAdapterFactory<T> createFactory();

    protected LabelableModule(Class<T> tClass) {
        this.tClass = tClass;
    }

    @Override
    protected void configure() {
        MapBinder<Class, LabelAdapterFactory> labelAdapterFactoryBinder = MapBinder.newMapBinder(binder(), Class.class,
                LabelAdapterFactory.class);
        labelAdapterFactoryBinder.addBinding(tClass).toInstance(labelAdapterFactory);
    }

    @Provides
    @DocumentScoped
    Labels<T> provideLabels(CAS cas) {
        return new UimaLabels<>(cas, labelAdapterFactory.create(cas));
    }

    @Provides
    @DocumentScoped
    Labeler<T> provideLabeler(CAS cas) {
        return new UimaLabeler<>(labelAdapterFactory.create(cas));
    }
}
