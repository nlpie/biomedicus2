/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.common.tuples.WordCap;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.syntax.PartsOfSpeech;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.tokenization.ParseToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOGGER = LoggerFactory.getLogger(TntModelTrainer.class);

  /**
   * The collection of frequency counters for word - part of speech pairs.
   */
  private final List<FilteredWordPosFrequencies> filteredWordPosFrequencies;

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
   * Whether or not we should restrict to the {@link PartsOfSpeech#OPEN_CLASS}.
   */
  private final boolean restrictToOpenClass;

  private final DataStoreFactory dataStoreFactory;

  /**
   * Private constructor, initialized by builder.
   *
   * @param filteredWordPosFrequencies The collection of frequency counters for word - part of
   * speech pairs.
   * @param posCapTrigramModelTrainer The trainer for the PosCap trigram probabilities.
   * @param maxSuffixLength The maximum suffix length to use in a suffix model.
   * @param maxWordFrequency The maximum word frequency in the suffix / unknown words model.
   * @param useMslSuffixModel Whether or not the MSL suffix model should be used.
   * @param restrictToOpenClass Whether or not we should restrict to the {@link
   * PartsOfSpeech#OPEN_CLASS}.
   */
  private TntModelTrainer(List<FilteredWordPosFrequencies> filteredWordPosFrequencies,
      PosCapTrigramModelTrainer posCapTrigramModelTrainer,
      int maxSuffixLength,
      int maxWordFrequency,
      boolean useMslSuffixModel,
      boolean restrictToOpenClass,
      DataStoreFactory dataStoreFactory) {
    this.filteredWordPosFrequencies = filteredWordPosFrequencies;
    this.posCapTrigramModelTrainer = posCapTrigramModelTrainer;
    this.maxSuffixLength = maxSuffixLength;
    this.maxWordFrequency = maxWordFrequency;
    this.useMslSuffixModel = useMslSuffixModel;
    this.restrictToOpenClass = restrictToOpenClass;
    this.dataStoreFactory = dataStoreFactory;
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
   * Adds the counts of trigrams, bigrams, and unigrams of Part of Speech capitalizations to the
   * trigram model, and stores word frequencies for building the
   *
   * @param tokens the tokens in the sentence
   * @param partOfSpeeches the part of speeches of the tokens in the sentence.
   */
  public void addSentence(List<ParseToken> tokens, List<PosTag> partOfSpeeches) {
    for (int i = 0; i < tokens.size(); i++) {
      ParseToken token = tokens.get(i);
      String tokenText = token.getText();
      boolean isCapitalized = Character.isUpperCase(tokenText.charAt(0));
      WordCap wordCap = new WordCap(tokenText, isCapitalized);
      PartOfSpeech partOfSpeech = partOfSpeeches.get(i).getPartOfSpeech();
      for (FilteredWordPosFrequencies filteredWordPosFrequencies : this.filteredWordPosFrequencies) {
        if (partOfSpeech != null) {
          filteredWordPosFrequencies.addWord(wordCap, partOfSpeech);
        }
      }
    }

    posCapTrigramModelTrainer.addSentence(tokens, partOfSpeeches);
  }

  /**
   * Builds the Tnt model using the statistics collected by passing sentences in.
   *
   * @return tnt model for use or serialization
   */
  public TntModel createModel() {
    PosCapTrigramModel posCapTrigramModel = posCapTrigramModelTrainer.build();

    Set<PartOfSpeech> tagSet =
        restrictToOpenClass ? PartsOfSpeech.getOpenClass() : PartsOfSpeech.getRealTags();

    final List<WordProbabilityModel> knownWordModels = new ArrayList<>();
    final List<WordProbabilityModel> suffixModels = new ArrayList<>();
    int priority = 0;
    for (FilteredWordPosFrequencies filteredFreqs : filteredWordPosFrequencies) {
      WordPosFrequencies wordPosFrequencies = filteredFreqs.getWordPosFrequencies();

      WordCapFilter filter = filteredFreqs.getFilter();
      WordCapAdapter wordCapAdapter = filteredFreqs.getWordCapAdapter();

      KnownWordProbabilityModel knownWordProbabilityModel = new KnownWordProbabilityModel();
      knownWordProbabilityModel.setId(priority);
      knownWordProbabilityModel.setFilter(filter);
      knownWordProbabilityModel.setWordCapAdapter(wordCapAdapter);
      knownWordProbabilityModel.createDataStore(dataStoreFactory);
      knownWordProbabilityModel.train(wordPosFrequencies, tagSet);

      knownWordModels.add(knownWordProbabilityModel);
      WordPosFrequencies suffixFrequencies = wordPosFrequencies
          .onlyWordsOccurringUpTo(maxWordFrequency)
          .expandSuffixes(maxSuffixLength);

      SuffixWordProbabilityModel suffixWordProbabilityModel = new SuffixWordProbabilityModel();
      suffixWordProbabilityModel.setMaxSuffixLength(maxSuffixLength);
      suffixWordProbabilityModel.setId(filteredWordPosFrequencies.size() + priority++);
      suffixWordProbabilityModel.setWordCapAdapter(wordCapAdapter);
      suffixWordProbabilityModel.createDataStore(dataStoreFactory);
      suffixWordProbabilityModel.setFilter(filter);
      if (useMslSuffixModel) {
        throw new UnsupportedOperationException();
      } else {
        suffixWordProbabilityModel.trainPI(suffixFrequencies, tagSet);
      }

      suffixModels.add(suffixWordProbabilityModel);
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
     * some integer max word frequency, words occurring more than this many times are not included
     * in the suffix model.
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

    private DataStoreFactory dataStoreFactory;

    /**
     * Private constructor.
     */
    private Builder() {
    }

    /**
     * Sets the max suffix length, which determines the longest suffixes that have probabilities
     * stored in the suffix model.
     *
     * @param maxSuffixLength some integer max suffix length, 0 is equivalent to not using a suffix
     * model.
     * @return this builder
     */
    public Builder maxSuffixLength(int maxSuffixLength) {
      this.maxSuffixLength = maxSuffixLength;
      return this;
    }

    /**
     * Sets the max word frequency: where words should be cut off from being included in the suffix
     * model.
     *
     * @param maxWordFrequency some integer max word frequency, words occurring more than this many
     * times are not included in the suffix model
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
     * @param useMslSuffixModel true if we should the MSL suffix model, false if we should use the
     * probability interpolation suffix model.
     * @return this builder
     */
    public Builder useMslSuffixModel(boolean useMslSuffixModel) {
      this.useMslSuffixModel = useMslSuffixModel;
      return this;
    }

    /**
     * Whether we should restrict suffix models to the open class of
     * {@link PartOfSpeech}
     *
     * @param restrictToOpenClass true if suffix models should only develop probabilities for open
     * class parts of speech.
     * @return this builder
     */
    public Builder restrictToOpenClass(boolean restrictToOpenClass) {
      this.restrictToOpenClass = restrictToOpenClass;
      return this;
    }

    public Builder dataStoreFactory(DataStoreFactory dataStoreFactory) {
      this.dataStoreFactory = dataStoreFactory;
      return this;
    }

    /**
     * Finish building a trainer.
     *
     * @return trainer with specified parameters
     */
    public TntModelTrainer build() {
      List<FilteredWordPosFrequencies> filteredWordPosFrequencies = new ArrayList<>();
      if (useCapitalization) {
        filteredWordPosFrequencies
            .add(new FilteredWordPosFrequencies(new WordCapFilter(true, false),
                new WordCapAdapter(true, false)));
        filteredWordPosFrequencies
            .add(new FilteredWordPosFrequencies(new WordCapFilter(false, true),
                new WordCapAdapter(true, false)));
      } else {
        filteredWordPosFrequencies
            .add(new FilteredWordPosFrequencies(new WordCapFilter(false, false),
                new WordCapAdapter(true, true)));
      }

      PosCapTrigramModelTrainer posCapTrigramModelTrainer = new PosCapTrigramModelTrainer();

      return new TntModelTrainer(filteredWordPosFrequencies, posCapTrigramModelTrainer,
          maxSuffixLength, maxWordFrequency, useMslSuffixModel, restrictToOpenClass,
          dataStoreFactory);
    }
  }
}
