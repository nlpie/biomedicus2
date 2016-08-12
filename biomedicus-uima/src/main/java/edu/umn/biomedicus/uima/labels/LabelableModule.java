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
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;

import java.lang.reflect.ParameterizedType;

public abstract class LabelableModule extends AbstractModule {
    @SuppressWarnings("unchecked")
    protected <T> void bindLabelable(Class<T> tClass, Class<? extends LabelAdapter<T>> labelAdapter) {
        ParameterizedType labelAdapterParameterizedType = Types.newParameterizedType(LabelAdapter.class, tClass);
        TypeLiteral<LabelAdapter<T>> labelAdapterTypeLiteral = (TypeLiteral<LabelAdapter<T>>) TypeLiteral.get(labelAdapterParameterizedType);

        bind(labelAdapterTypeLiteral).to(labelAdapter).in(DocumentScoped.class);

        ParameterizedType labelsParameterizedType = Types.newParameterizedType(Labels.class, tClass);
        TypeLiteral<Labels<T>> labelsTypeLiteral = (TypeLiteral<Labels<T>>) TypeLiteral.get(labelsParameterizedType);
        ParameterizedType uimaLabelsParameterizedType = Types.newParameterizedType(UimaLabels.class, tClass);
        TypeLiteral<UimaLabels<T>> uimaLabelsTypeLiteral = (TypeLiteral<UimaLabels<T>>) TypeLiteral.get(uimaLabelsParameterizedType);

        bind(labelsTypeLiteral).to(uimaLabelsTypeLiteral).in(DocumentScoped.class);

        ParameterizedType labelerParameterizedType = Types.newParameterizedType(Labeler.class, tClass);
        TypeLiteral<Labeler<T>> labelerTypeLiteral = (TypeLiteral<Labeler<T>>) TypeLiteral.get(labelerParameterizedType);
        ParameterizedType uimaLabelerParameterizedType = Types.newParameterizedType(UimaLabeler.class, tClass);
        TypeLiteral<UimaLabeler<T>> uimaLabelerTypeLiteral = (TypeLiteral<UimaLabeler<T>>) TypeLiteral.get(uimaLabelerParameterizedType);

        bind(labelerTypeLiteral).to(uimaLabelerTypeLiteral).in(DocumentScoped.class);
    }
}
