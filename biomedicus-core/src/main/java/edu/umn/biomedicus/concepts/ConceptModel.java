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
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.terms.TermsBag;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(ConceptModel.class);

    private final Map<TermsBag, List<SuiCuiTui>> normDictionary;

    private final Map<String, List<SuiCuiTui>> phrases;

    private final Map<String, List<SuiCuiTui>> lowercasePhrases;

    @Inject
    ConceptModel(@Setting("concepts.filters.sui.path") Path filteredSuisPath,
                 @Setting("concepts.filters.cui.path") Path filteredCuisPath,
                 @Setting("concepts.filters.suicui.path") Path filteredSuiCuisPath,
                 @Setting("concepts.filters.tui.path") Path filteredTuisPath,
                 @Setting("concepts.phrases.path") Path phrasesPath,
                 @Setting("concepts.norms.path") Path normsPath,
                 Vocabulary vocabulary) throws IOException {
        Pattern splitter = Pattern.compile(",");

        Set<SUI> filteredSuis = Files.lines(filteredSuisPath).map(SUI::new).collect(Collectors.toSet());

        Set<CUI> filteredCuis = Files.lines(filteredCuisPath).map(CUI::new).collect(Collectors.toSet());

        Set<SuiCui> filteredSuiCuis = Files.lines(filteredSuiCuisPath)
                .map(splitter::split)
                .map(line -> new SuiCui(new SUI(line[0]), new CUI(line[1])))
                .collect(Collectors.toSet());

        Set<TUI> filteredTuis = Files.lines(filteredTuisPath).map(TUI::new).collect(Collectors.toSet());

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

        LOGGER.info("Loading concept norm vectors: {}", normsPath);
        normDictionary = new HashMap<>();
        TermIndex normIndex = vocabulary.wordIndex();
        try (BufferedReader normsReader = Files.newBufferedReader(normsPath)) {
            String line;
            while ((line = normsReader.readLine()) != null) {
                String[] split = splitter.split(line);
                List<String> terms = Arrays.asList(split);
                terms.replaceAll(string -> {
                    if (string.equals("scull")) {
                        return "skull";
                    }
                    return string;
                });
                TermsBag termsBag = normIndex.getTermVector(terms);
                String concepts = normsReader.readLine();
                List<SuiCuiTui> suiCuiTuis = Stream.of(splitter.split(concepts)).map(SuiCuiTui::fromString)
                        .collect(Collectors.toList());
                suiCuiTuis.removeIf(sct -> filteredSuis.contains(sct.sui()) || filteredCuis.contains(sct.cui())
                        || filteredSuiCuis.contains(new SuiCui(sct.sui(), sct.cui()))
                        || filteredTuis.contains(sct.tui()));
                List<SuiCuiTui> unmodifiableList = Collections.unmodifiableList(suiCuiTuis);
                normDictionary.put(termsBag, unmodifiableList);
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
    List<SuiCuiTui> forNorms(TermsBag norms) {
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
