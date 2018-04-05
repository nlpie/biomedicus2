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

package edu.umn.biomedicus.tnt;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.grams.Bigram;
import edu.umn.biomedicus.common.tuples.PosCap;
import edu.umn.biomedicus.common.tuples.WordCap;
import edu.umn.biomedicus.common.viterbi.CandidateProbability;
import edu.umn.biomedicus.common.viterbi.EmissionProbabilityModel;
import edu.umn.biomedicus.common.viterbi.TransitionProbabilityModel;
import edu.umn.biomedicus.common.viterbi.Viterbi;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DataLoader;
import edu.umn.biomedicus.serialization.YamlSerialization;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 *
 */
@Singleton
@ProvidedBy(TntModel.Loader.class)
public class TntModel implements EmissionProbabilityModel<PosCap, WordCap>,
    TransitionProbabilityModel<PosCap, Bigram<PosCap>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TntModel.class);


  /**
   * Trigram model used for transition probability.
   */
  private final PosCapTrigramModel posCapTrigramModel;

  /**
   * Word probability models used for emission probability.
   */
  private final List<WordProbabilityModel> wordModels;

  TntModel(PosCapTrigramModel posCapTrigramModel,
      List<WordProbabilityModel> wordModels) {
    this.posCapTrigramModel = posCapTrigramModel;
    this.wordModels = wordModels;
  }

  public void write(Path folder) throws IOException {
    Yaml yaml = YamlSerialization.createYaml();

    Files.createDirectories(folder);

    Map<String, Object> store = posCapTrigramModel.createStore();
    yaml.dump(store, Files.newBufferedWriter(folder.resolve("trigram.yml")));

    yaml.dump(wordModels, Files.newBufferedWriter(folder.resolve("wordMetadata.yml")));

    for (WordProbabilityModel wordModel : wordModels) {
      wordModel.writeData();
    }
  }

  private WordProbabilityModel getWordProbabilityModel(WordCap emittedValue) {
    WordProbabilityModel filteredAdaptedWordProbability = null;
    for (WordProbabilityModel probabilityModel : wordModels) {
      if (probabilityModel.isKnown(emittedValue)) {
        filteredAdaptedWordProbability = probabilityModel;
        break;
      }
    }

    if (filteredAdaptedWordProbability == null) {
      throw new AssertionError("could not find any word probability model");
    }
    return filteredAdaptedWordProbability;
  }

  @Override
  public Collection<CandidateProbability<PosCap>> getCandidates(WordCap emittedValue) {
    WordProbabilityModel filteredAdaptedWordProbability = getWordProbabilityModel(emittedValue);

    return filteredAdaptedWordProbability.getCandidates(emittedValue)
        .stream()
        .map(candidate -> {
          double emissionLogProbability = filteredAdaptedWordProbability
              .logProbabilityOfWord(candidate, emittedValue);
          PosCap candidatePosCap = PosCap.create(candidate, emittedValue.isCapitalized());
          return Viterbi.candidateOf(candidatePosCap, emissionLogProbability);
        })
        .collect(Collectors.toList());
  }

  @Override
  public double getTransitionLogProbability(Bigram<PosCap> statesReduction, PosCap candidate) {
    return Math.log10(posCapTrigramModel.getTrigramProbability(statesReduction.getFirst(),
        statesReduction.getSecond(), candidate));
  }

  /**
   *
   */
  @Singleton
  static class Loader extends DataLoader<TntModel> {

    private final Path trigram;
    private final Path wordMetadata;
    private final DataStoreFactory dataStoreFactory;

    @Inject
    public Loader(@Setting("tnt.trigram.path") Path trigram,
        @Setting("tnt.word.metadataPath") Path wordMetadata,
        DataStoreFactory dataStoreFactory) {
      this.trigram = trigram;
      this.wordMetadata = wordMetadata;
      this.dataStoreFactory = dataStoreFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected TntModel loadModel() throws BiomedicusException {
      Yaml yaml = YamlSerialization.createYaml();

      try {
        LOGGER.info("Loading TnT trigram model: {}", trigram);
        Map<String, Object> store = yaml.load(Files.newInputStream(trigram));

        PosCapTrigramModel posCapTrigramModel = PosCapTrigramModel.createFromStore(store);

        List<WordProbabilityModel> wordModels = yaml.load(Files.newInputStream(wordMetadata));

        LOGGER.info("Loading TnT word models.");
        wordModels.forEach(wm -> wm.openDataStore(dataStoreFactory));

        return new TntModel(posCapTrigramModel, wordModels);
      } catch (IOException e) {
        throw new BiomedicusException(e);
      }
    }
  }
}
