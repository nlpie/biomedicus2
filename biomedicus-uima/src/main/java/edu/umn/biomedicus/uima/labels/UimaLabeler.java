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

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.ValueLabeler;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.TextLocation;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.uima.cas.CAS;

final class UimaLabeler<T> implements Labeler<T> {

    private final LabelAdapter<T> labelAdapter;

    UimaLabeler(LabelAdapter<T> labelAdapter) {
        this.labelAdapter = labelAdapter;
    }

    @Override
    public ValueLabeler value(T value) {
        return new ValueLabeler() {
            @Override
            public void label(int begin, int end) throws BiomedicusException {
                label(Span.create(begin, end));
            }

            @Override
            public void label(TextLocation textLocation) throws BiomedicusException {
                labelAdapter.labelToAnnotation(new Label<>(textLocation.toSpan(), value));
            }
        };
    }
}
