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

package edu.umn.biomedicus.common.viterbi;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;

/**
 * An internal data structure used to store the chain of hidden state types.
 *
 * @author Ben Knoll
 * @param <S> the hidden state type
 * @since 1.2.0
 */
class HistoryChain<S> {
    /**
     * The previous entry / node in the history chain.
     */
    @Nullable
    private final HistoryChain<S> previous;

    /**
     * The state of this node / entry in the history chain.
     */
    @Nullable
    private final S state;

    HistoryChain(@Nullable HistoryChain<S> previous, @Nullable S state) {
        this.previous = previous;
        this.state = state;
    }

    HistoryChain<S> append(S state) {
        return new HistoryChain<>(this, state);
    }

    HistoryChain<S> skip() {
        return new HistoryChain<>(this, null);
    }

    S getNonnullPayload(int fromThis) {
        HistoryChain<S> pointer = this;
        int counter = 0;
        while (counter < fromThis || pointer.state == null) {
            if (pointer.state != null) {
                counter++;
            }

            pointer = pointer.previous;
            if (pointer == null) {
                throw new NoSuchElementException();
            }
        }
        return pointer.state;
    }

    @Nullable
    S getState() {
        return state;
    }

    @Nullable
    HistoryChain<S> getPrevious() {
        return previous;
    }
}
