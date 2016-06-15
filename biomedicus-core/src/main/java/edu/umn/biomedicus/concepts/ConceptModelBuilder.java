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

package edu.umn.biomedicus.concepts;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.Bootstrapper;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.terms.TermsBag;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
public class ConceptModelBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConceptModelBuilder.class);

    private static final Pattern SPLITTER = Pattern.compile("\\|");

    private static final Pattern SPACE_SPLITTER = Pattern.compile(" ");

    private final Vocabulary vocabulary;

    @Nullable
    private Path rrfs;

    @Nullable
    private Path outputDir;

    @Nullable
    private Path tuisOfInterestFile;

    @Nullable
    private Path ttysBannedFile;

    @Inject
    public ConceptModelBuilder(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public void setRRFsPath(Path rrfs) {
        if (this.rrfs != null) {
            throw new IllegalStateException("RRFs path already set.");
        }
        if (rrfs == null) {
            throw new IllegalArgumentException("RRFs path is null.");
        }

        this.rrfs = rrfs;
    }

    public void setTuisOfInterestFile(Path tuisOfInterestFile) {
        if (this.tuisOfInterestFile != null) {
            throw new IllegalStateException("Tui whitelist path already set.");
        }
        if (tuisOfInterestFile == null) {
            throw new IllegalArgumentException("TUI whitelist path is null");
        }

        this.tuisOfInterestFile = tuisOfInterestFile;
    }

    public void setTtysBannedFile(Path ttysBannedFile) {
        if (this.ttysBannedFile != null) {
            throw new IllegalStateException("TTY banlist path already set.");
        }
        if (ttysBannedFile == null) {
            throw new IllegalArgumentException("TTY banlist path is null");
        }

        this.ttysBannedFile = ttysBannedFile;
    }

    public void setOutputDir(Path outputDir) {
        if (this.outputDir != null) {
            throw new IllegalStateException("Output dir already set.");
        }
        if (outputDir == null) {
            throw new IllegalArgumentException("Output dir already set.");
        }

        this.outputDir = outputDir;
    }

    void process() throws IOException {
        if (rrfs == null) {
            throw new IllegalStateException("RRFS path not set");
        }
        if (tuisOfInterestFile == null) {
            throw new IllegalStateException("TUI whitelist path not set");
        }
        if (ttysBannedFile == null) {
            throw new IllegalStateException("TTY banlist path not set");
        }
        if (outputDir == null) {
            throw new IllegalStateException("Output dir not set");
        }

        Files.createDirectories(outputDir);

        LOGGER.info("Loading TUIs of interest: {}", tuisOfInterestFile);
        Set<TUI> whitelist = Files.lines(tuisOfInterestFile).map(SPLITTER::split)
                .filter(line -> line.length >= 3)
                .map(line -> line[1])
                .map(TUI::new)
                .collect(Collectors.toSet());

        Set<String> ttyBanlist = Files.lines(ttysBannedFile).collect(Collectors.toSet());

        Path mrstyPath = rrfs.resolve("MRSTY.RRF");
        LOGGER.info("Loading CUI -> TUIs map from MRSTY: {}", mrstyPath);
        Map<CUI, List<TUI>> cuiToTUIs = Files.lines(mrstyPath)
                .map(SPLITTER::split)
                .map(line -> {
                    CUI cui = new CUI(line[0]);
                    TUI tui = new TUI(line[1]);
                    List<TUI> tuis = new ArrayList<>();
                    tuis.add(tui);
                    return new AbstractMap.SimpleImmutableEntry<>(cui, tuis);
                })
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        (first, second) -> {
                            for (TUI secondItem : second) {
                                if (!first.contains(secondItem)) {
                                    first.add(secondItem);
                                }
                            }
                            return first;
                        }));

        LOGGER.info("Filtering CUIs based on interest list");
        cuiToTUIs.entrySet()
                .removeIf(entry -> {
                    boolean noneMatch = entry.getValue()
                            .stream()
                            .noneMatch(whitelist::contains);
                    if (noneMatch) {
                        LOGGER.trace("Filtering {} because it has no interesting types", entry.getKey());
                    }
                    return noneMatch;
                });

        TermIndex normIndex = vocabulary.wordIndex();

        Path mrconsoPath = rrfs.resolve("MRCONSO.RRF");
        LOGGER.info("Loading phrases and SUI -> CUIs from MRCONSO: {}", mrconsoPath);
        Set<SUI> bannedSUIs = new HashSet<>();

        // block so phrase dictionary gets freed once it's no longer needed
        {
            Map<String, List<SuiCuiTui>> phraseDictionary = new HashMap<>();
            Files.lines(mrconsoPath)
                    .map(SPLITTER::split)
                    .filter(columns -> "ENG".equals(columns[1]))
                    .forEach(columns -> {
                        String phrase = columns[14];
                        CUI cui = new CUI(columns[0]);
                        SUI sui = new SUI(columns[5]);
                        String obsoleteOrSuppressible = columns[16];
                        String tty = columns[12];

                        if (phrase.length() < 3) {
                            return;
                        }

                        if (!"N".equals(obsoleteOrSuppressible)) {
                            bannedSUIs.add(sui);
                            return;
                        }

                        if (ttyBanlist.contains(tty)) {
                            bannedSUIs.add(sui);
                            return;
                        }

                        List<TUI> tuis = cuiToTUIs.get(cui);
                        if (tuis == null || tuis.size() == 0) {
                            LOGGER.trace("Filtering \"{}\" because it has no interesting types", phrase);
                            return;
                        }
                        for (TUI tui : tuis) {
                            multimapPut(phraseDictionary, phrase, new SuiCuiTui(sui, cui, tui));
                        }
                    });
            Path phrasesPath = outputDir.resolve("phrases.txt");
            LOGGER.info("Writing phrases out: {}", phrasesPath);
            try (BufferedWriter normsWriter = Files.newBufferedWriter(phrasesPath)) {
                for (Map.Entry<String, List<SuiCuiTui>> entry : phraseDictionary.entrySet()) {
                    normsWriter.write(entry.getKey());
                    normsWriter.newLine();
                    normsWriter.write(entry.getValue().stream().map(SuiCuiTui::toString).collect(Collectors.joining(",")));
                    normsWriter.newLine();
                }
            }
        }

        Path mrxnsPath = rrfs.resolve("MRXNS_ENG.RRF");
        LOGGER.info("Loading lowercase normalized strings from MRXNS_ENG: {}", mrxnsPath);
        Path normsPath = outputDir.resolve("norms.txt");
        Map<TermsBag, List<SuiCuiTui>> normMap = new HashMap<>();
        Files.lines(mrxnsPath)
                .map(SPLITTER::split)
                .filter(columns -> "ENG".equals(columns[0]))
                .forEach(line -> {
                    List<String> norms = Arrays.asList(SPACE_SPLITTER.split(line[1]));
                    CUI cui = new CUI(line[2]);
                    SUI sui = new SUI(line[4]);

                    if (norms.size() < 2) {
                        return;
                    }

                    if (bannedSUIs.contains(sui)) {
                        return;
                    }

                    TermsBag termsBag = normIndex.getTermVector(norms);
                    List<TUI> tuis = cuiToTUIs.get(cui);
                    if (tuis == null || tuis.size() == 0) {
                        LOGGER.trace("Filtering \"{}\" because it has no interesting types", termsBag);
                        return;
                    }
                    for (TUI tui : tuis) {
                        multimapPut(normMap, termsBag, new SuiCuiTui(sui, cui, tui));
                    }
                });

        LOGGER.info("Writing lowercase normalized strings: {}", normsPath);
        try (BufferedWriter normsWriter = Files.newBufferedWriter(normsPath)) {
            for (Map.Entry<TermsBag, List<SuiCuiTui>> entry : normMap.entrySet()) {
                normsWriter.write(normIndex.getTerms(entry.getKey()).stream().collect(Collectors.joining(",")));
                normsWriter.newLine();
                normsWriter.write(entry.getValue().stream().map(SuiCuiTui::toString).collect(Collectors.joining(",")));
                normsWriter.newLine();
            }
        }
    }

    private <K, V> void multimapPut(Map<K, List<V>> map, K key, V value) {
        if (map.containsKey(key)) {
            List<V> list = map.get(key);
            if (!list.contains(value)) {
                list.add(value);
            }
        } else {
            List<V> list = new ArrayList<>();
            list.add(value);
            map.put(key, list);
        }
    }

    public static void main(String[] args) {
        Path rrfs = Paths.get(args[0]);

        Path outputDir = Paths.get(args[1]);

        Path tuisOfInterestFile = Paths.get(args[2]);

        Path ttyBanlistFile = Paths.get(args[3]);

        try {
            ConceptModelBuilder conceptModelBuilder = Bootstrapper.create().createClass(ConceptModelBuilder.class);
            conceptModelBuilder.setRRFsPath(rrfs);
            conceptModelBuilder.setOutputDir(outputDir);
            conceptModelBuilder.setTuisOfInterestFile(tuisOfInterestFile);
            conceptModelBuilder.setTtysBannedFile(ttyBanlistFile);
            conceptModelBuilder.process();
        } catch (IOException | BiomedicusException e) {
            e.printStackTrace();
        }
    }
}
