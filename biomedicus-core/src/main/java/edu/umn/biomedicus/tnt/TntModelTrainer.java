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

package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.model.semantics.PartOfSpeech;
import edu.umn.biomedicus.model.text.Sentence;
import edu.umn.biomedicus.model.text.Token;
import edu.umn.biomedicus.model.tuples.WordCap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Trains a model for the TnT part of speech tagger.
 *
 * @author Ben Knoll
 * @since 1.1.0
 */
public class TntModelTrainer {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * The collection of frequency counters for word - part of speech pairs.
     */
    private final Collection<FilteredWordPosFrequencies> wordPosFrequenciesCollection;

    /**
     * The trainer for the PosCap trigram probabilities.
     */
    private final PosCapTrigramModelTrainer posCapTrigramModelTrainer;

    /**
     * The maximum suffix length to use in a suffix model.
     */
    private final int maxSuffixLength;

    /**
     * The maximum word frequency in the suffix / unknown words model.
     */
    private final int maxWordFrequency;

    /**
     * Whether or not the MSL suffix model should be used.
     */
    private final boolean useMslSuffixModel;

    /**
     * Whether or not we should restrict to the {@link PartOfSpeech#OPEN_CLASS}.
     */
    private final boolean restrictToOpenClass;

    /**
     * Private constructor, initialized by builder.
     *
     * @param wordPosFrequenciesCollection The collection of frequency counters for word - part of speech pairs.
     * @param posCapTrigramModelTrainer    The trainer for the PosCap trigram probabilities.
     * @param maxSuffixLength              The maximum suffix length to use in a suffix model.
     * @param maxWordFrequency             The maximum word frequency in the suffix / unknown words model.
     * @param useMslSuffixModel            Whether or not the MSL suffix model should be used.
     * @param restrictToOpenClass          Whether or not we should restrict to the {@link PartOfSpeech#OPEN_CLASS}.
     */
    private TntModelTrainer(Collection<FilteredWordPosFrequencies> wordPosFrequenciesCollection,
                            PosCapTrigramModelTrainer posCapTrigramModelTrainer,
                            int maxSuffixLength,
                            int maxWordFrequency,
                            boolean useMslSuffixModel,
                            boolean restrictToOpenClass) {
        this.wordPosFrequenciesCollection = wordPosFrequenciesCollection;
        this.posCapTrigramModelTrainer = posCapTrigramModelTrainer;
        this.maxSuffixLength = maxSuffixLength;
        this.maxWordFrequency = maxWordFrequency;
        this.useMslSuffixModel = useMslSuffixModel;
        this.restrictToOpenClass = restrictToOpenClass;
    }

    /**
     * Creates a new builder for a Tnt Model trainer.
     *
     * @return newly created builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Adds the counts of trigrams, bigrams, and unigrams of Part of Speech capitalizations to the trigram model,
     * and stores word frequencies for building the
     *
     * @param sentence sentence to add tokens from
     */
    public void addSentence(Sentence sentence) {
        for (Token token : sentence.getTokens()) {
            WordCap wordCap = new WordCap(token.getText(), token.isCapitalized());
            for (FilteredWordPosFrequencies filteredWordPosFrequencies : wordPosFrequenciesCollection) {
                PartOfSpeech partOfSpeech = token.getPartOfSpeech();
                if (partOfSpeech != null) {
                    filteredWordPosFrequencies.addWord(wordCap, partOfSpeech);
                }
            }
        }
        posCapTrigramModelTrainer.addSentence(sentence);
    }

    /**
     * Builds the Tnt model using the statistics collected by passing sentences in.
     *
     * @return tnt model for use or serialization
     */
    public TntModel createModel() {
        PosCapTrigramModel posCapTrigramModel = posCapTrigramModelTrainer.build();

        Set<PartOfSpeech> tagSet = restrictToOpenClass ? PartOfSpeech.OPEN_CLASS : PartOfSpeech.REAL_TAGS;

        final List<WordProbabilityModel> knownWordModels = new ArrayList<>();
        final List<WordProbabilityModel> suffixModels = new ArrayList<>();
        for (FilteredWordPosFrequencies filteredWordPosFrequencies : wordPosFrequenciesCollection) {
            WordPosFrequencies wordPosFrequencies = filteredWordPosFrequencies.getWordPosFrequencies();
            WordCapFilter filter = filteredWordPosFrequencies.getFilter();
            WordCapAdapter wordCapAdapter = filteredWordPosFrequencies.getWordCapAdapter();
            knownWordModels.add(new KnownWordModel(KnownWordModelTrainer.get().apply(wordPosFrequencies), filter,
                    wordCapAdapter));
            WordPosFrequencies suffixFrequencies = wordPosFrequencies.onlyWordsOccurringUpTo(maxWordFrequency)
                    .expandSuffixes(maxSuffixLength);
            if (!filteredWordPosFrequencies.isRepeatedWordCapFilter()) {
                if (useMslSuffixModel) {
                    suffixModels.add(new SuffixModel(MslSuffixModelTrainer.get(tagSet).apply(suffixFrequencies),
                            maxSuffixLength, filter, wordCapAdapter));
                } else {
                    suffixModels.add(new SuffixModel(PiSuffixModelTrainer.get(tagSet).apply(suffixFrequencies),
                            maxSuffixLength, filter, wordCapAdapter));
                }
            }
        }

        knownWordModels.addAll(suffixModels);

        LOGGER.debug("Word models: {}", knownWordModels);

        return new TntModel(posCapTrigramModel, knownWordModels);
    }

    /**
     * builder for the
     */
    public static final class Builder {
        /**
         * some integer max suffix length, 0 is equivalent to not using a suffix model.
         */
        private int maxSuffixLength = 5;

        /**
         * some integer max word frequency, words occurring more than this many times are not included in the suffix
         * model.
         */
        private int maxWordFrequency = 10;

        /**
         * Whether or not we should consider capitalization.
         */
        private boolean useCapitalization = true;

        /**
         * Use the MSL suffix model.
         */
        private boolean useMslSuffixModel = false;

        /**
         * Restrict to the open class of parts of speech.
         */
        private boolean restrictToOpenClass = false;

        /**
         * Private constructor.
         */
        private Builder() {
        }

        /**
         * Sets the max suffix length, which determines the longest suffixes that have probabilities stored in the
         * suffix model.
         *
         * @param maxSuffixLength some integer max suffix length, 0 is equivalent to not using a suffix model.
         * @return this builder
         */
        public Builder maxSuffixLength(int maxSuffixLength) {
            this.maxSuffixLength = maxSuffixLength;
            return this;
        }

        /**
         * Sets the max word frequency: where words should be cut off from being included in the suffix model.
         *
         * @param maxWordFrequency some integer max word frequency, words occurring more than this many times are not
         *                         included in the suffix model
         * @return this builder
         */
        public Builder maxWordFrequency(int maxWordFrequency) {
            this.maxWordFrequency = maxWordFrequency;
            return this;
        }

        /**
         * Whether the models should use the capitalization of words to segment probability sets.
         *
         * @param useCapitalization true if capitalization should be used, false if it shouldn't
         * @return this builder
         */
        public Builder useCapitalization(boolean useCapitalization) {
            this.useCapitalization = useCapitalization;
            return this;
        }

        /**
         * Whether we should use the maximum suffix length based suffix model.
         *
         * @param useMslSuffixModel true if we should the MSL suffix model, false if we should use the probability
         *                          interpolation suffix model.
         * @return this builder
         */
        public Builder useMslSuffixModel(boolean useMslSuffixModel) {
            this.useMslSuffixModel = useMslSuffixModel;
            return this;
        }

        /**
         * Whether we should restrict suffix models to the open class of
         * {@link edu.umn.biomedicus.model.semantics.PartOfSpeech}
         *
         * @param restrictToOpenClass true if suffix models should only develop probabilities for open class parts of
         *                            speech.
         * @return this builder
         */
        public Builder restrictToOpenClass(boolean restrictToOpenClass) {
            this.restrictToOpenClass = restrictToOpenClass;
            return this;
        }

        /**
         * Predicate for a word cap which is the inverse of the capitalized predicate.
         *
         * @param wordCap word capitalization pair
         * @return true if the word cap is not capitalized, false otherwise.
         */
        private static boolean notCapitalized(WordCap wordCap) {
            return !wordCap.isCapitalized();
        }

        /**
         * Finish building a trainer.
         *
         * @return trainer with specified parameters
         */
        public TntModelTrainer build() {
            Collection<FilteredWordPosFrequencies> wordPosFrequenciesCollection = new ArrayList<>();
            if (useCapitalization) {
                wordPosFrequenciesCollection.add(new FilteredWordPosFrequencies(WordCap::isCapitalized,
                        WordCap::identity, false));
                wordPosFrequenciesCollection.add(new FilteredWordPosFrequencies(WordCap::isCapitalized,
                        WordCap::lowercase, false));
                wordPosFrequenciesCollection.add(new FilteredWordPosFrequencies(Builder::notCapitalized,
                        WordCap::identity, false));
                wordPosFrequenciesCollection.add(new FilteredWordPosFrequencies(Builder::notCapitalized,
                        WordCap::lowercase, false));
                wordPosFrequenciesCollection.add(new FilteredWordPosFrequencies(x -> true,
                        WordCap::lowercaseIgnoreCapitalization, false));
            } else {
                wordPosFrequenciesCollection.add(new FilteredWordPosFrequencies(x -> true,
                        WordCap::lowercaseIgnoreCapitalization, false));
            }

            PosCapTrigramModelTrainer posCapTrigramModelTrainer = new PosCapTrigramModelTrainer();

            return new TntModelTrainer(wordPosFrequenciesCollection, posCapTrigramModelTrainer, maxSuffixLength,
                    maxWordFrequency, useMslSuffixModel, restrictToOpenClass);
        }
    }
}
