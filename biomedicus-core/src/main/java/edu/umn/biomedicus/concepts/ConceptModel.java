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

package edu.umn.biomedicus.concepts;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.terms.TermVector;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores UMLS Concepts in a multimap (Map from String to List of Concepts).
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
@Singleton
class ConceptModel {
    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<TermVector, List<SuiCuiTui>> normDictionary;

    private final Map<String, List<SuiCuiTui>> phrases;

    private final Map<String, List<SuiCuiTui>> lowercasePhrases;

    @Inject
    ConceptModel(BiomedicusConfiguration biomedicusConfiguration, Vocabulary vocabulary) throws IOException {
        Pattern splitter = Pattern.compile(",");

        Set<SUI> filteredSuis = Files.lines(biomedicusConfiguration.resolveDataFile("concepts.filters.sui.path"))
                .map(SUI::new).collect(Collectors.toSet());

        Set<CUI> filteredCuis = Files.lines(biomedicusConfiguration.resolveDataFile("concepts.filters.cui.path"))
                .map(CUI::new).collect(Collectors.toSet());

        Path filteredSuiCuisPath = biomedicusConfiguration.resolveDataFile("concepts.filters.suicui.path");
        Set<SuiCui> filteredSuiCuis = Files.lines(filteredSuiCuisPath)
                .map(splitter::split)
                .map(line -> new SuiCui(new SUI(line[0]), new CUI(line[1])))
                .collect(Collectors.toSet());

        Set<TUI> filteredTuis = Files.lines(biomedicusConfiguration.resolveDataFile("concepts.filters.tui.path"))
                .map(TUI::new).collect(Collectors.toSet());

        Path phrasesPath = biomedicusConfiguration.resolveDataFile("concepts.phrases.path");
        LOGGER.info("Loading concepts phrases: {}", phrasesPath);
        phrases = new HashMap<>();
        lowercasePhrases = new HashMap<>();
        try (BufferedReader normsReader = Files.newBufferedReader(phrasesPath)) {
            String line;
            while ((line = normsReader.readLine()) != null) {
                String concepts = normsReader.readLine();
                List<SuiCuiTui> suiCuiTuis = Stream.of(splitter.split(concepts)).map(SuiCuiTui::fromString)
                        .collect(Collectors.toList());
                suiCuiTuis.removeIf(sct -> filteredSuis.contains(sct.sui()) || filteredCuis.contains(sct.cui())
                        || filteredSuiCuis.contains(new SuiCui(sct.sui(), sct.cui()))
                        || filteredTuis.contains(sct.tui()));
                List<SuiCuiTui> unmodifiableList = Collections.unmodifiableList(suiCuiTuis);
                phrases.put(line, unmodifiableList);
                lowercasePhrases.put(line.toLowerCase(), unmodifiableList);
            }
        }


        Path normsPath = biomedicusConfiguration.resolveDataFile("concepts.norms.path");
        LOGGER.info("Loading concept norm vectors: {}", normsPath);
        normDictionary = new HashMap<>();
        TermIndex normIndex = vocabulary.normIndex();
        try (BufferedReader normsReader = Files.newBufferedReader(normsPath)) {
            String line;
            while ((line = normsReader.readLine()) != null) {
                TermVector termVector = normIndex.getTermVector(Arrays.asList(splitter.split(line)));
                String concepts = normsReader.readLine();
                List<SuiCuiTui> suiCuiTuis = Stream.of(splitter.split(concepts)).map(SuiCuiTui::fromString)
                        .collect(Collectors.toList());
                suiCuiTuis.removeIf(sct -> filteredSuis.contains(sct.sui()) || filteredCuis.contains(sct.cui())
                        || filteredSuiCuis.contains(new SuiCui(sct.sui(), sct.cui()))
                        || filteredTuis.contains(sct.tui()));
                List<SuiCuiTui> unmodifiableList = Collections.unmodifiableList(suiCuiTuis);
                normDictionary.put(termVector, unmodifiableList);
            }
        }
    }

    @Nullable
    List<SuiCuiTui> forPhrase(String phrase) {
        return phrases.get(phrase);
    }

    @Nullable
    List<SuiCuiTui> forLowercasePhrase(String phrase) {
        return lowercasePhrases.get(phrase);
    }

    @Nullable
    List<SuiCuiTui> forNorms(TermVector norms) {
        return normDictionary.get(norms);
    }

    private static final class SuiCui {
        private final SUI sui;
        private final CUI cui;

        public SuiCui(SUI sui, CUI cui) {
            this.sui = sui;
            this.cui = cui;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SuiCui suiCui = (SuiCui) o;

            if (!sui.equals(suiCui.sui)) return false;
            return cui.equals(suiCui.cui);

        }

        @Override
        public int hashCode() {
            int result = sui.hashCode();
            result = 31 * result + cui.hashCode();
            return result;
        }
    }
}
