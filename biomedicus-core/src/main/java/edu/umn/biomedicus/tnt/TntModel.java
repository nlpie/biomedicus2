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

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.common.grams.Bigram;
import edu.umn.biomedicus.common.tuples.PosCap;
import edu.umn.biomedicus.common.tuples.WordCap;
import edu.umn.biomedicus.common.viterbi.CandidateProbability;
import edu.umn.biomedicus.common.viterbi.EmissionProbabilityModel;
import edu.umn.biomedicus.common.viterbi.TransitionProbabilityModel;
import edu.umn.biomedicus.common.viterbi.Viterbi;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.serialization.YamlSerialization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@ProvidedBy(TntModel.Loader.class)
public class TntModel implements EmissionProbabilityModel<PosCap, WordCap>, TransitionProbabilityModel<PosCap, Bigram<PosCap>> {
    private static final Logger LOGGER = LogManager.getLogger();


    /**
     * Trigram model used for transition probability.
     */
    private final PosCapTrigramModel posCapTrigramModel;

    /**
     * Word probability models used for emission probability.
     */
    private final List<FilteredAdaptedWordProbabilityModel> filteredAdaptedWordProbabilities;

    TntModel(PosCapTrigramModel posCapTrigramModel, List<FilteredAdaptedWordProbabilityModel> filteredAdaptedWordProbabilities) {
        this.posCapTrigramModel = posCapTrigramModel;
        this.filteredAdaptedWordProbabilities = filteredAdaptedWordProbabilities;
    }

    public void write(Path folder) throws IOException {
        Yaml yaml = YamlSerialization.createYaml();

        Files.createDirectories(folder);

        Map<String, Object> store = posCapTrigramModel.createStore();
        yaml.dump(store, Files.newBufferedWriter(folder.resolve("trigram.yml")));

        Path words = folder.resolve("words");
        Files.createDirectories(words);

        for (FilteredAdaptedWordProbabilityModel filteredAdaptedWordProbability : filteredAdaptedWordProbabilities) {
            filteredAdaptedWordProbability.reduce();
            yaml.dump(filteredAdaptedWordProbability,
                    Files.newBufferedWriter(words.resolve(filteredAdaptedWordProbability.getPriority() + ".yml")));
        }
    }

    private FilteredAdaptedWordProbabilityModel getWordProbabilityModel(WordCap emittedValue) {
        FilteredAdaptedWordProbabilityModel filteredAdaptedWordProbability = null;
        for (FilteredAdaptedWordProbabilityModel probabilityModel : filteredAdaptedWordProbabilities) {
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
        FilteredAdaptedWordProbabilityModel filteredAdaptedWordProbability = getWordProbabilityModel(emittedValue);

        return filteredAdaptedWordProbability.getCandidates(emittedValue)
                .stream()
                .map(candidate -> {
                    double emissionLogProbability = filteredAdaptedWordProbability.logProbabilityOfWord(candidate, emittedValue);
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
        private static final Logger LOGGER = LogManager.getLogger();

        private final Path trigram;

        private final Path wordModels;

        @Inject
        public Loader(@Setting("tnt.trigram.path") Path trigram, @Setting("tnt.word.path") Path wordModels) {
            this.trigram = trigram;
            this.wordModels = wordModels;
        }

        @Override
        protected TntModel loadModel() throws BiomedicusException {
            Yaml yaml = YamlSerialization.createYaml();

            try {
                LOGGER.info("Loading TnT trigram model: {}", trigram);
                @SuppressWarnings("unchecked")
                Map<String, Object> store = (Map<String, Object>) yaml.load(Files.newInputStream(trigram));
                PosCapTrigramModel posCapTrigramModel = PosCapTrigramModel.createFromStore(store);

                List<FilteredAdaptedWordProbabilityModel> filteredAdaptedWordProbabilities = new ArrayList<>();
                Files.walkFileTree(wordModels, Collections.emptySet(), 1, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.getFileName().toString().endsWith(".yml")) {
                            LOGGER.info("Loading TnT word model #{}: {}", filteredAdaptedWordProbabilities.size() + 1, file);
                            FilteredAdaptedWordProbabilityModel filteredAdaptedWordProbabilityModel = (FilteredAdaptedWordProbabilityModel) yaml.load(Files.newInputStream(file));
                            filteredAdaptedWordProbabilities.add(filteredAdaptedWordProbabilityModel);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });

                Collections.sort(filteredAdaptedWordProbabilities, (m1, m2) -> Integer.compare(m1.getPriority(), m2.getPriority()));

                return new TntModel(posCapTrigramModel, filteredAdaptedWordProbabilities);
            } catch (IOException e) {
                throw new BiomedicusException(e);
            }
        }
    }
}
