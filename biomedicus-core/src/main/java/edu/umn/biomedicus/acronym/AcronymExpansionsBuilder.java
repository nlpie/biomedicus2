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

package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.Biomedicus;
import edu.umn.biomedicus.application.Bootstrapper;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.spelling.SpecialistAgreementModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * <p>Main class for building the acronym expansions data file. Uses three files to build the set of acronym expansions:
 * First, from the Clinical Abbreviations Sense Inventory (CASI), available
 * <a href="http://conservancy.umn.edu/handle/11299//137703">here</a>, the clinical sense inventory II master file.
 * Second, also from CASI, the Anonymized Clinical Abbreviations And Acronyms Data Set.
 * Finally, it also uses the acronyms from the LRABR file in the specialist lexicon.</p>
 * <br>
 * <p>The argument order is [path-to-master-file] [path-to-data-set] [path-to-specialist-home].</p>
 *
 * @author Greg Finley (original python script)
 * @author Ben Knoll (java conversion)
 * @since 1.5.0
 */
public class AcronymExpansionsBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcronymExpansionsBuilder.class);

    private Path masterFile;

    private SpecialistAgreementModel specialistAgreementModel;

    private Path dataSet;

    private Path specialistLrabrPath;

    private Path outPath;
    private Map<String, Set<String>> expansions;

    @Inject
    public AcronymExpansionsBuilder(SpecialistAgreementModel specialistAgreementModel,
                                    @Setting("specialist.path") Path specialistPath) {
        this.specialistAgreementModel = specialistAgreementModel;
        specialistLrabrPath = specialistPath.resolve("LRABR");
    }

    private void setMasterFile(Path masterFile) {
        this.masterFile = masterFile;
    }

    private void setDataSet(Path dataSet) {
        this.dataSet = dataSet;
    }

    private void setOutPath(Path outPath) {
        this.outPath = outPath;
    }

    private void buildAcronymExpansions() throws IOException {
        expansions = new HashMap<>();
        // acronyms in the casi data set shouldn't have their sense set expanded beyond that data
        Set<String> casiOverride = new HashSet<>();
        Map<String, Integer> casiSenseCounts = new HashMap<>();
        Pattern splitter = Pattern.compile("\\|");

        LOGGER.info("Loading CASI data set: {}", dataSet);
        Files.lines(dataSet, StandardCharsets.ISO_8859_1)
                .filter(line -> line.length() > 0)
                .map(splitter::split)
                .forEach(splits -> {
                            String sense = splits[1];
                            if ("GENERAL ENGLISH".equals(sense)) {
                                sense = splits[0].toLowerCase();
                            }
                            casiSenseCounts.put(sense, casiSenseCounts.getOrDefault(sense, 0) + 1);
                        });
        Files.lines(dataSet, StandardCharsets.ISO_8859_1)
                .filter(line -> line.length() > 0)
                .map(splitter::split)
                .forEach(splits -> {
                    String abbreviation = Acronyms.standardAcronymForm(splits[0]);
                    String sense = splits[1];
                    if ("GENERAL ENGLISH".equals(sense)) {
                        sense = abbreviation.toLowerCase();
                    }
                    Set<String> senses = getOrAdd(abbreviation);
                    casiOverride.add(abbreviation);
                    if (!senses.contains(sense) && !"UNSURED SENSE".equals(sense) && casiSenseCounts.get(sense) >= 5) {
                        senses.add(sense);
                    }
                });

        LOGGER.info("Loading LRABR file from SPECIALIST: {}", specialistLrabrPath);
        Files.lines(specialistLrabrPath)
                .map(splitter::split)
                .forEach(splits -> {
                    String abbreviation = Acronyms.standardAcronymForm(splits[1]);
                    if (!containsFormOf(casiOverride, abbreviation)) {
                        String longform = splits[4];

                        if (longform == null) {
                            return;
                        }

                        Collection<String> specialistSenses = specialistAgreementModel.getCanonicalFormForBase(longform);
                        if (specialistSenses != null) {
                            Set<String> senses = getOrAdd(abbreviation);
                            specialistSenses.stream().filter(sense -> !senses.contains(sense)).forEach(senses::add);
                        } else if (splits[3].equals("")) {
                            // some expansions don't have EUIs (or appear in LRAGR), so deal with them here
                            Set<String> senses = getOrAdd(abbreviation);
                            senses.add(longform);
                        }
                    }
                });

        LOGGER.info("Loading CASI master file: {}", masterFile);
        Files.lines(masterFile)
                .map(splitter::split)
                .forEach(splits -> {
                    String abbreviation = Acronyms.standardAcronymForm(splits[0]);
                    String sense = splits[1];
                    double frequency;
                    try {
                        frequency = Double.parseDouble(splits[4]);
                    } catch (NumberFormatException e) {
                        return;
                    }

                    if (frequency >= 0.95 && !"UNSURED SENSE".equals(sense)) {
                        Set<String> senses = expansions.get(abbreviation);
                        if (senses == null) {
                            senses = new HashSet<>();
                            expansions.put(abbreviation, senses);
                        } else {
                            senses.clear();
                        }
                        // Many senses in CASI, for some reason, are many redundant sense joined by semicolons
                        if (sense.contains(";")) {
                            String[] senseVersions = sense.split(";");
                            // Sometimes the first sense restates or simply is the abbreviation
                            if (senseVersions[0].startsWith(abbreviation)) {
                                sense = senseVersions[1];
                            } else {
                                sense = senseVersions[0];
                            }
                        }
                        senses.add(sense);
                    }
                });

        LOGGER.info("Writing expansions: {}", outPath);
        try (BufferedWriter writer = Files.newBufferedWriter(outPath, CREATE, TRUNCATE_EXISTING)) {
            for (Map.Entry<String, Set<String>> abbrExpansion : expansions.entrySet()) {
                writer.write(abbrExpansion.getKey());
                writer.newLine();
                StringJoiner stringJoiner = new StringJoiner("|");
                for (String expansion : abbrExpansion.getValue()) {
                    if (expansion.contains("|")) {
                        throw new IllegalStateException("Expansion contains a | character");
                    }
                    stringJoiner.add(expansion);
                }
                writer.write(stringJoiner.toString());
                writer.newLine();
            }
        }
    }

    private static boolean containsFormOf(Set<String> set, String abbr) {
        if (set.contains(abbr)) return true;
        if (set.contains(abbr.replace(".", ""))) return true;
        return false;
    }

    private Set<String> getOrAdd(String abbreviation) {
        Set<String> senses = expansions.get(abbreviation);
        if (senses == null) {
            senses = new HashSet<>();
            expansions.put(abbreviation, senses);
        }
        return senses;
    }

    public static void main(String args[]) {
        try {
            Biomedicus biomedicus = Bootstrapper.create();
            AcronymExpansionsBuilder acronymExpansionsBuilder = biomedicus.getInstance(AcronymExpansionsBuilder.class);
            acronymExpansionsBuilder.setMasterFile(Paths.get(args[0]));
            acronymExpansionsBuilder.setDataSet(Paths.get(args[1]));
            acronymExpansionsBuilder.setOutPath(Paths.get(args[2]));

            acronymExpansionsBuilder.buildAcronymExpansions();
        } catch (IOException | BiomedicusException e) {
            e.printStackTrace();
        }
    }
}
