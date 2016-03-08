package edu.umn.biomedicus.concepts;

import edu.umn.biomedicus.serialization.YamlSerialization;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 *
 */
public class UmlsConceptDictionaryBuilder {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Pattern SPLITTER = Pattern.compile("\\|");

    private static final Pattern SPACE_SPLITTER = Pattern.compile(" ");

    private final Path mrconsoFile;

    private final Path mrstyFile;

    private final Path mrxnsFile;

    private final Path outputDir;

    private final Path tuiWhitelistFile;

    public UmlsConceptDictionaryBuilder(Path rrfs, Path outputDir, Path tuiWhitelistFile) {
        this.tuiWhitelistFile = tuiWhitelistFile;
        this.mrconsoFile = rrfs.resolve("MRCONSO.RRF");
        this.mrstyFile = rrfs.resolve("MRSTY.RRF");
        this.mrxnsFile = rrfs.resolve("MRXNS_ENG.RRF");
        this.outputDir = outputDir;
    }

    void process() throws IOException {
        Files.createDirectories(outputDir);

        LOGGER.info("Loading whitelisted TUIs");
        Set<TUI> whitelisted = Files.lines(tuiWhitelistFile).map(TUI::new).collect(Collectors.toSet());

        LOGGER.info("Loading CUI -> TUIs map from MRSTY");
        Map<CUI, List<TUI>> cuiToTUIs = Files.lines(mrstyFile)
                .map(SPLITTER::split)
                .map(line -> {
                    CUI cui = new CUI(line[0]);
                    TUI tui = new TUI(line[1]);
                    List<TUI> tuis = new ArrayList<>();
                    tuis.add(tui);
                    return new AbstractMap.SimpleImmutableEntry<>(cui, tuis);
                })
                .collect(getCollector());

        LOGGER.info("Filtering CUIs based on whitelist");
        cuiToTUIs.entrySet()
                .removeIf(entry -> {
                    boolean noneMatch = entry.getValue().stream().noneMatch(whitelisted::contains);
                    if (noneMatch) {
                        LOGGER.trace("Filtering {} because it has no whitelisted types", entry.getKey());
                    }
                    return noneMatch;
                });

        LOGGER.info("Writing TUIs out");
        Yaml yaml = YamlSerialization.createYaml();
        yaml.dump(cuiToTUIs, Files.newBufferedWriter(outputDir.resolve("tuis.yml")));

        LOGGER.info("Loading phrases from MRCONSO");
        Map<String, List<CUI>> phraseDictionary = Files.lines(mrconsoFile)
                .map(SPLITTER::split)
                .filter(columns -> "ENG".equals(columns[1]))
                .map(line -> {
                    String phrase = line[14];
                    CUI cui = new CUI(line[0]);
                    List<CUI> cuis = new ArrayList<>();
                    cuis.add(cui);
                    return new AbstractMap.SimpleImmutableEntry<>(phrase, cuis);
                })
                // filter short entries since these will be handled by acronym detection / expansion
                .filter(entry -> {
                    boolean longEnough = entry.getKey().length() >= 3;
                    if (!longEnough) {
                        LOGGER.trace("Filtering \"{}\" because it is too short", entry.getKey());
                    }
                    return longEnough;
                })
                .filter(entry -> {
                    entry.getValue().removeIf(cui -> !cuiToTUIs.containsKey(cui));
                    boolean notFiltered = !entry.getValue().isEmpty();
                    if (!notFiltered) {
                        LOGGER.trace("Filtering \"{}\" because it has no whitelisted types", entry.getKey());
                    }
                    return notFiltered;
                })
                .collect(getCollector());

        LOGGER.info("Writing phrases out");
        yaml.dump(phraseDictionary, Files.newBufferedWriter(outputDir.resolve("phrases.yml")));

        LOGGER.info("Loading lowercase normalized strings from MRXNS_ENG");
        Map<List<String>, List<CUI>> normDictionary = Files.lines(mrxnsFile)
                .map(SPLITTER::split)
                .filter(columns -> "ENG".equals(columns[0]))
                .map(line -> {
                    List<String> norms = Arrays.asList(SPACE_SPLITTER.split(line[1]));
                    CUI cui = new CUI(line[2]);
                    List<CUI> cuis = new ArrayList<>();
                    cuis.add(cui);
                    return new AbstractMap.SimpleImmutableEntry<>(norms, cuis);
                })
                // filter short entries since these will be handled by acronym detection / expansion
                .filter(entry -> {
                    boolean longEnough = entry.getKey().size() == 1 && entry.getKey().get(0).length() >= 3 || entry.getKey().size() > 1;
                    if (!longEnough) {
                        LOGGER.trace("Filtering \"{}\" because it is too short", entry.getKey());
                    }
                    return longEnough;
                })
                .filter(entry -> {
                    entry.getValue().removeIf(cui -> !cuiToTUIs.containsKey(cui));
                    boolean notFiltered = !entry.getValue().isEmpty();
                    if (!notFiltered) {
                        LOGGER.trace("Filtering \"{}\" because it has no whitelisted types", entry.getKey());
                    }
                    return notFiltered;
                })
                .collect(getCollector());
        LOGGER.info("Writing lowercase normalized strings");
        yaml.dump(normDictionary, Files.newBufferedWriter(outputDir.resolve("norms.yml")));
    }

    private <T, U> Collector<AbstractMap.SimpleImmutableEntry<U, List<T>>, ?, Map<U, List<T>>> getCollector() {
        return Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue,
                (first, second) -> {
                    for (T secondItem : second) {
                        if (!first.contains(secondItem)) {
                            first.add(secondItem);
                        }
                    }
                    return first;
                });
    }

    public static void main(String[] args) {
        Path rrfs = Paths.get(args[0]);

        Path outputDir = Paths.get(args[1]);

        Path tuiWhitelistFile = Paths.get(args[2]);

        UmlsConceptDictionaryBuilder umlsConceptDictionaryBuilder = new UmlsConceptDictionaryBuilder(rrfs, outputDir, tuiWhitelistFile);

        try {
            umlsConceptDictionaryBuilder.process();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
