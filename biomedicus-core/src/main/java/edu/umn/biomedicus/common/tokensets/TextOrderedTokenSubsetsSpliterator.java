/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.common.tokensets;

import edu.umn.biomedicus.common.text.Token;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Used to generate all ordered subsets of a ordered set of tokens given a maximum size.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class TextOrderedTokenSubsetsSpliterator implements Spliterator<TextOrderedTokenSet> {
    /**
     * The index of the first token considered by this spliterator.
     */
    private final int startingIndexBound;

    /**
     * The minimum size of the subsets we are generating
     */
    private final int minWindowSize;

    /**
     * The maximum size of the subsets we are generating
     */
    private final int windowSizeBound;

    /**
     * The ordered token set we are making subsets of.
     */
    private final OrderedTokenSet orderedTokenSet;

    /**
     * The current index that we are starting with.
     */
    private int startingIndex;

    /**
     * The current window size that we are generating
     */
    private int windowSize;

    /**
     * Hidden constructor. Use {@link TextOrderedTokenSubsetsSpliterator#create}. This method is used internally for
     * splitting.
     *
     * @param orderedTokenSet    The ordered token set we are making subsets of.
     * @param startingIndex      The current index that we are starting with.
     * @param startingIndexBound The index of the first token considered by this spliterator.
     * @param windowSize         The current window size that we are generating
     * @param minWindowSize      The minimum size of the subsets we are generating
     * @param windowSizeBound    The maximum size of the subsets we are generating
     */
    private TextOrderedTokenSubsetsSpliterator(OrderedTokenSet orderedTokenSet,
                                               int startingIndex,
                                               int startingIndexBound,
                                               int windowSize,
                                               int minWindowSize,
                                               int windowSizeBound) {
        this.orderedTokenSet = orderedTokenSet;
        this.startingIndexBound = startingIndexBound;
        this.minWindowSize = minWindowSize;
        this.windowSizeBound = windowSizeBound;
        this.startingIndex = startingIndex;
        this.windowSize = windowSize;
    }

    /**
     * Creates a {@code TextOrderedTokenSubsetsSpliterator} given a {@code OrderedTokenSet} and an
     * {@code int windowSizeBound}
     *
     * @param orderedTokenSet the token set to create subsets of.
     * @param windowSizeBound the maximum size of the subsets.
     * @return a spliterator of subsets of the ordered token set.
     */
    public static TextOrderedTokenSubsetsSpliterator create(OrderedTokenSet orderedTokenSet, int windowSizeBound) {
        return new TextOrderedTokenSubsetsSpliterator(orderedTokenSet, 0, orderedTokenSet.getTokens().size(), 1, 1,
                windowSizeBound);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Attempts to advance by checking to make sure the starting index and window size are within bounds, generating a
     * subset if they are. If they are not, then we reset the window size to the minimum window size and advance to the
     * next starting index.
     * </p>
     *
     * @param action consumer for the subset
     * @return true if a subset was consumed, false otherwise
     */
    @Override
    public boolean tryAdvance(Consumer<? super TextOrderedTokenSet> action) {
        if (windowSize == windowSizeBound || startingIndex + windowSize > orderedTokenSet.getTokens().size()) {
            startingIndex++;
            windowSize = minWindowSize;
            if (startingIndex >= startingIndexBound) {
                return false;
            }
        }
        List<Token> tokens = orderedTokenSet.getTokens().subList(startingIndex, startingIndex + windowSize);
        action.accept(new SimpleTextOrderedTokenSet(tokens));
        windowSize++;
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Attempts to split by splitting the start indexes in half, keeping half for this spliterator and returning a new
     * spliterator with the other half. If there is only 1 starting index remaining it will not split.
     * </p>
     */
    @Nullable
    @Override
    public Spliterator<TextOrderedTokenSet> trySplit() {
        int startingIndexes = startingIndexBound - startingIndex - 1;
        if (startingIndexes > 1) {
            // split based on starting index
            int startingIndexesSplit = startingIndexes / 2;
            int oldStartingIndex = startingIndex;
            int oldWindowSize = windowSize;
            startingIndex = startingIndex + startingIndexesSplit;
            windowSize = minWindowSize;
            return new TextOrderedTokenSubsetsSpliterator(orderedTokenSet, oldStartingIndex, startingIndex,
                    oldWindowSize, minWindowSize, windowSizeBound);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calculates an approximate number of windows remaining to generate. This will be a slight overestimate, depending
     * on the number of windows that will be skipped based on part of speech or verb stopwords.
     * </p>
     */
    @Override
    public long estimateSize() {
        return ((long) startingIndexBound - startingIndex) * windowSizeBound;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This is ordered, distinct, nonnull, and immutable.
     * </p>
     */
    @Override
    public int characteristics() {
        return ORDERED | DISTINCT | NONNULL | IMMUTABLE;
    }
}
