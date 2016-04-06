package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.Bootstrapper;
import edu.umn.biomedicus.common.terms.TermIndex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
    private static final Logger LOGGER = LogManager.getLogger();

    private final TermIndex termIndex;

    private Path masterFile;

    private Path dataSet;

    private Path specialistDirectory;

    @Inject
    public AcronymExpansionsBuilder(TermIndex termIndex) {
        this.termIndex = termIndex;
    }

    public void setMasterFile(Path masterFile) {
        this.masterFile = masterFile;
    }

    public void setDataSet(Path dataSet) {
        this.dataSet = dataSet;
    }

    public void setSpecialistDirectory(Path specialistDirectory) {
        this.specialistDirectory = specialistDirectory;
    }

    public void buildAcronymExpansions() throws IOException {
        Map<String, Set<String>> expansions = new HashMap<>();
        Pattern splitter = Pattern.compile("\\|");

        LOGGER.info("Loading CASI data set: {}", dataSet);
        Files.lines(dataSet)
                .map(splitter::split)
                .forEach(splits -> {
                    String abbreviation = splits[0];
                    String sense = splits[1];
                    if ("GENERAL ENGLISH".equals(sense)) {
                        sense = abbreviation.toLowerCase();
                    }
                    Set<String> senses = expansions.get(abbreviation);
                    if (senses == null) {
                        senses = new HashSet<>();
                        expansions.put(abbreviation, senses);
                    }
                    senses.add(sense);
                });

        LOGGER.info("Loading CASI master file: {}", masterFile);
        Files.lines(masterFile)
                .map(splitter::split)
                .forEach(splits -> {
                    String abbreviation = splits[0];
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
                        senses.add(sense);
                    }
                });

        LOGGER.info("Loading LRABR file from SPECIALIST: {}", specialistDirectory);
        Files.lines(specialistDirectory.resolve("LRABR"))
                .map(splitter::split)
                .forEach(splits -> {

                });
    }

    public static void main(String args[]) {
        try {
            Bootstrapper bootstrapper = new Bootstrapper();
            AcronymExpansionsBuilder acronymExpansionsBuilder = bootstrapper.getInstance(AcronymExpansionsBuilder.class);
            acronymExpansionsBuilder.setMasterFile(Paths.get(args[0]));
            acronymExpansionsBuilder.setDataSet(Paths.get(args[1]));
            acronymExpansionsBuilder.setSpecialistDirectory(Paths.get(args[2]));

            acronymExpansionsBuilder.buildAcronymExpansions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
