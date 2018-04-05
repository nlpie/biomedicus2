/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import edu.umn.biomedicus.acronyms.ScoredSense;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DataLoader;
import edu.umn.biomedicus.serialization.YamlSerialization;
import edu.umn.biomedicus.tokenization.Token;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * An implementation of an acronym model that uses word vectors and a cosine distance metric
 *
 * @author Greg Finley
 * @since 1.5.0
 */
@ProvidedBy(AcronymVectorModel.Loader.class)
class AcronymVectorModel implements AcronymModel {

  private static final Logger LOGGER = LoggerFactory.getLogger(AcronymVectorModel.class);

  /**
   * A vector space with a built dictionary to use at test time
   */
  private final WordVectorSpace wordVectorSpace;

  private final AcronymExpansionsModel acronymExpansionsModel;

  /**
   * Maps long forms to their trained word vectors
   */
  private final SenseVectors senseVectors;

  /**
   * The alignment model will guess an acronym's full form based on its alignment if we don't know
   * what it is.
   */
  @Nullable
  private final AlignmentModel alignmentModel;

  private final double cutoffScore;

  /**
   * Constructor. Needs several things already made:
   *  @param wordVectorSpace the vector space (most importantly dictionary) used to build context
   * vectors
   * @param senseVectors which maps between senses and their context vectors
   * @param alignmentModel a model used for alignment of unknown acronyms
   * @param cutoffScore
   */
  AcronymVectorModel(
      WordVectorSpace wordVectorSpace,
      SenseVectors senseVectors,
      AcronymExpansionsModel acronymExpansionsModel,
      @Nullable AlignmentModel alignmentModel,
      double cutoffScore
  ) {
    this.acronymExpansionsModel = acronymExpansionsModel;
    this.senseVectors = senseVectors;
    this.wordVectorSpace = wordVectorSpace;
    this.alignmentModel = alignmentModel;
    this.cutoffScore = cutoffScore;
  }

  /**
   * Will return a list of the possible senses for this acronym
   *
   * @param token a Token
   * @return a List of Strings of all possible senses
   */
  public Collection<String> getExpansions(Token token) {
    String acronym = Acronyms.standardAcronymForm(token);
    Collection<String> expansions = acronymExpansionsModel.getExpansions(acronym);
    if (expansions != null) {
      return expansions;
    }
    return Collections.emptyList();
  }

  /**
   * Does the model know about this acronym?
   *
   * @param token a token
   * @return true if this token's text is a known acronym
   */
  public boolean hasAcronym(Token token) {
    String acronym = Acronyms.standardAcronymForm(token);
    return acronymExpansionsModel.hasExpansions(acronym);
  }

  /**
   * Will return the model's best guess for the sense of this acronym
   *
   * @param context a list of tokens including the full context for this acronym
   * @param forThisIndex an integer specifying the index of the acronym
   */
  @Override
  public List<ScoredSense> findBestSense(List<? extends Token> context, int forThisIndex) {

    String acronym = Acronyms.standardAcronymForm(context.get(forThisIndex));

    // If the model doesn't contain this acronym, make sure it doesn't contain an upper-case version of it
    Collection<String> senses = acronymExpansionsModel.getExpansions(acronym);
    if (senses == null) {
      senses = acronymExpansionsModel.getExpansions(acronym.toUpperCase());
    }
    if (senses == null) {
      senses = acronymExpansionsModel.getExpansions(acronym.replace(".", ""));
    }
    if (senses == null) {
      senses = acronymExpansionsModel.getExpansions(acronym.toLowerCase());
    }
    if (senses == null && alignmentModel != null) {
      senses = alignmentModel.findBestLongforms(acronym);
    }
    if (senses == null || senses.size() == 0) {
      return Collections.emptyList();
    }

    // If the acronym is unambiguous, our work is done
    if (senses.size() == 1) {
      return Collections.singletonList(new ScoredSense(senses.iterator().next(), 1));
    }

    List<Pair<String, SparseVector>> usableSenses = new ArrayList<>();
    // Be sure that there even are disambiguation vectors for senses
    for (String sense : senses) {
      SparseVector sparseVector = senseVectors.get(sense);
      if (sparseVector != null) {
        usableSenses.add(Pair.of(sense, sparseVector));
      }
    }

    // If no senses good for disambiguation were found, try the upper-case version
    if (usableSenses.size() == 0 && acronymExpansionsModel.hasExpansions(acronym.toUpperCase())) {
      for (String sense : senses) {
        SparseVector sparseVector = senseVectors.get(sense);
        if (sparseVector != null) {
          usableSenses.add(Pair.of(sense, sparseVector));
        }
      }
    }

    // Should this just guess the first sense instead?
    if (usableSenses.size() == 0) {
      return Collections.emptyList();
    }

    double best = -Double.MAX_VALUE;
    String winner = Acronyms.UNKNOWN;

    SparseVector vector = wordVectorSpace.vectorize(context, forThisIndex);
    // Loop through all possible senses for this acronym
    for (Pair<String, SparseVector> senseAndVector : usableSenses) {
      double score = vector.dot(senseAndVector.getSecond());
      if (score > best) {
        best = score;
        winner = senseAndVector.first();
      }
    }
    return usableSenses.stream()
        .map(pair -> {
          double score = vector.dot(pair.getSecond());
          return new ScoredSense(pair.first(), score);
        })
        .filter(scored -> scored.getScore() >= cutoffScore)
        .sorted(Comparator.comparing(ScoredSense::getScore).reversed())
        .collect(Collectors.toList());
  }

  /**
   * Remove a single word from the model
   *
   * @param word the word to remove
   */
  public void removeWord(String word) {
    Integer ind = wordVectorSpace.removeWord(word);
    if (ind != null) {
      senseVectors.removeWord(ind);
    }
  }

  /**
   * Remove all words from the model except those given
   *
   * @param wordsToRemove the set of words to keep
   */
  public void removeWordsExcept(Set<String> wordsToRemove) {
    Set<Integer> removed = wordVectorSpace.removeWordsExcept(wordsToRemove);
    removed.remove(null);
    senseVectors.removeWords(removed);
  }

  void writeToDirectory(Path outputDir,
      @Nullable Map<String, SparseVector> senseVectors) throws IOException {
    Yaml yaml = YamlSerialization.createYaml();

    if (alignmentModel != null) {
      yaml.dump(alignmentModel, Files.newBufferedWriter(outputDir.resolve("alignment.yml")));
    }
    yaml.dump(wordVectorSpace, Files.newBufferedWriter(outputDir.resolve("vectorSpace.yml")));

    if (senseVectors != null) {
      RocksDBSenseVectors rocksDBSenseVectors = new RocksDBSenseVectors(
          outputDir.resolve("senseVectors"), true);
      rocksDBSenseVectors.putAll(senseVectors);
      rocksDBSenseVectors.close();
    }
  }

  /**
   *
   */
  @Singleton
  static class Loader extends DataLoader<AcronymVectorModel> {

    @Nullable
    private final Provider<AlignmentModel> alignmentModel;

    private final Path vectorSpacePath;

    private final Path senseMapPath;

    private final boolean useAlignment;

    private final Boolean sensesInMemory;

    private final AcronymExpansionsModel expansionsModel;
    private final Double cutoffScore;


    @Inject
    public Loader(
        @Nullable Provider<AlignmentModel> alignmentModel,
        @Setting("acronym.useAlignment") Boolean useAlignment,
        @Setting("acronym.vector.model.path") Path vectorSpacePath,
        @Setting("acronym.senseMap.path") Path senseMapPath,
        @Setting("acronym.senseMap.inMemory") Boolean sensesInMemory,
        @Setting("acronym.cutoffScore") Double cutoffScore,
        AcronymExpansionsModel expansionsModel
    ) {
      this.alignmentModel = alignmentModel;
      this.useAlignment = useAlignment;
      this.vectorSpacePath = vectorSpacePath;
      this.senseMapPath = senseMapPath;
      this.sensesInMemory = sensesInMemory;
      this.expansionsModel = expansionsModel;
      this.cutoffScore = cutoffScore;
    }

    @Override
    protected AcronymVectorModel loadModel() throws BiomedicusException {

      Yaml yaml = YamlSerialization.createYaml();

      try {
        LOGGER.info("Loading acronym vector space: {}", vectorSpacePath);
        @SuppressWarnings("unchecked")
        WordVectorSpace wordVectorSpace = yaml
            .load(Files.newBufferedReader(vectorSpacePath));

        LOGGER.info("Loading acronym sense map: {}. inMemory = {}", senseMapPath, sensesInMemory);
        SenseVectors senseVectors = new RocksDBSenseVectors(senseMapPath, false)
            .inMemory(sensesInMemory);

        return new AcronymVectorModel(wordVectorSpace, senseVectors, expansionsModel,
            useAlignment ? alignmentModel.get() : null, cutoffScore);
      } catch (IOException e) {
        throw new BiomedicusException(e);
      }
    }
  }
}
