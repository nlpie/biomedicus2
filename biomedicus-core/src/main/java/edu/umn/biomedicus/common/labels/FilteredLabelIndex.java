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

package edu.umn.biomedicus.common.labels;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FilteredLabelIndex<T> extends AbstractLabelIndex<T> {

    private final LabelIndex<T> labelIndex;
    private final Predicate<Label<T>> predicate;

    public FilteredLabelIndex(LabelIndex<T> labelIndex, Predicate<Label<T>> predicate) {
        this.labelIndex = labelIndex;
        this.predicate = predicate;
    }

    @Override
    public Iterator<Label<T>> iterator() {
        Iterator<Label<T>> iterator = labelIndex.iterator();
        return new Iterator<Label<T>>() {
            @Nullable private Label<T> current;

            {
                forward();
            }

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Label<T> next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                Label<T> label = current;
                forward();
                return label;
            }

            private void forward() {
                while (iterator.hasNext()) {
                    Label<T> next = iterator.next();
                    if (predicate.test(next)) {
                        current = next;
                        return;
                    }
                }
                current = null;
            }
        };
    }

    @Override
    public Stream<Label<T>> stream() {
        return labelIndex.stream().filter(predicate);
    }
}
