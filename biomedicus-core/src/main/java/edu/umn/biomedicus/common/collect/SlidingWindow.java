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

package edu.umn.biomedicus.common.collect;

import edu.umn.biomedicus.common.labels.Label;

import java.util.*;

public class SlidingWindow<T> implements Iterable<List<T>> {

    private final Iterable<T> iterable;
    private final int windowSize;

    public SlidingWindow(Iterable<T> iterable, int windowSize) {
        this.iterable = iterable;
        this.windowSize = windowSize;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Iterator<List<T>>() {
            private final Iterator<T> iterator = iterable.iterator();
            private final LinkedList<T> window = new LinkedList<>();
            private boolean done = false;

            {
                for (int i = 0; i < windowSize; i++) {
                    if (iterator.hasNext()) {
                        window.addLast(iterator.next());
                    }
                }
            }

            @Override
            public boolean hasNext() {
                return !done;
            }

            @Override
            public List<T> next() {
                if (done) {
                    throw new NoSuchElementException();
                }
                ArrayList<T> labels = new ArrayList<>(window);
                window.removeFirst();
                if (iterator.hasNext()) {
                    window.addLast(iterator.next());
                } else {
                    done = true;
                }
                return labels;
            }
        };
    }
}
