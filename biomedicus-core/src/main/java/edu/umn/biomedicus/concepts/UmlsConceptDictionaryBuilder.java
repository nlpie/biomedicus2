package edu.umn.biomedicus.concepts;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.Bootstrapper;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.terms.TermVector;
import edu.umn.biomedicus.serialization.YamlSerialization;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class UmlsConceptDictionaryBuilder {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Pattern SPLITTER = Pattern.compile("\\|");

    private static final Pattern SPACE_SPLITTER = Pattern.compile(" ");

    private final SemanticTypeNetwork semanticTypeNetwork;
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
    public UmlsConceptDictionaryBuilder(SemanticTypeNetwork semanticTypeNetwork, Vocabulary vocabulary) {
        this.semanticTypeNetwork = semanticTypeNetwork;
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
                            .noneMatch(tui -> whitelist.stream().anyMatch(whitelisted -> semanticTypeNetwork.isa(tui, whitelisted)));
                    if (noneMatch) {
                        LOGGER.trace("Filtering {} because it has no interesting types", entry.getKey());
                    }
                    return noneMatch;
                });

        Path tuisPath = outputDir.resolve("tuis.yml");
        LOGGER.info("Writing CUI -> TUIs out: {}", tuisPath);
        TermIndex normIndex = vocabulary.normIndex();
        Yaml yaml = YamlSerialization.createYaml(normIndex);
        yaml.dump(cuiToTUIs, Files.newBufferedWriter(tuisPath));

        Path mrconsoPath = rrfs.resolve("MRCONSO.RRF");
        LOGGER.info("Loading phrases and SUI -> CUIs from MRCONSO: {}", mrconsoPath);
        Set<SUI> bannedSUIs = new HashSet<>();

        // block so phrase dictionary gets freed once it's no longer needed
        {
            Map<String, SUI> phraseDictionary = new HashMap<>();
            Map<SUI, List<CUI>> suiCUIs = new HashMap<>();
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

                        if (!cuiToTUIs.containsKey(cui)) {
                            LOGGER.trace("Filtering \"{}\" because it has no interesting types", phrase);
                            return;
                        }

                        phraseDictionary.put(phrase, sui);
                        multimapPut(suiCUIs, sui, cui);
                    });
            Path phrasesPath = outputDir.resolve("phrases.yml");
            LOGGER.info("Writing phrases out: {}", phrasesPath);
            yaml.dump(phraseDictionary, Files.newBufferedWriter(phrasesPath));

            Path suiCUIsPath = outputDir.resolve("suiCUIs.yml");
            yaml.dump(suiCUIs, Files.newBufferedWriter(suiCUIsPath));
        }

        Path mrxnsPath = rrfs.resolve("MRXNS_ENG.RRF");
        LOGGER.info("Loading lowercase normalized strings from MRXNS_ENG: {}", mrxnsPath);
        Path normsPath = outputDir.resolve("norms.txt");
        Map<TermVector, List<CUI>> normMap = new HashMap<>();
        Files.lines(mrxnsPath)
                .map(SPLITTER::split)
                .filter(columns -> "ENG".equals(columns[0]))
                .forEach(line -> {
                    List<String> norms = Arrays.asList(SPACE_SPLITTER.split(line[1]));
                    CUI cui = new CUI(line[2]);
                    SUI sui = new SUI(line[4]);

                    if (norms.size() == 1 && norms.get(0).length() < 3) {
                        return;
                    }

                    if (bannedSUIs.contains(sui)) {
                        return;
                    }

                    if (!cuiToTUIs.containsKey(cui)) {
                        LOGGER.trace("Filtering \"{}\" because it has no interesting types", norms);
                        return;
                    }

                    TermVector termVector = normIndex.lookup(norms).get();

                    List<CUI> cuis;
                    if (normMap.containsKey(termVector)) {
                        cuis = normMap.get(termVector);
                    } else {
                        cuis = new ArrayList<>();
                        normMap.put(termVector, cuis);
                    }
                    if (!cuis.contains(cui)) {
                        cuis.add(cui);
                    }
                });

        LOGGER.info("Writing lowercase normalized strings: {}", normsPath);

        try (BufferedWriter normsWriter = Files.newBufferedWriter(normsPath)) {
            for (Map.Entry<TermVector, List<CUI>> entry : normMap.entrySet()) {
                normsWriter.write(normIndex.getStrings(entry.getKey()).stream().collect(Collectors.joining(",")));
                normsWriter.newLine();
                normsWriter.write(entry.getValue().stream().map(CUI::toString).collect(Collectors.joining(",")));
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
            UmlsConceptDictionaryBuilder umlsConceptDictionaryBuilder = Bootstrapper.create().getInstance(UmlsConceptDictionaryBuilder.class);
            umlsConceptDictionaryBuilder.setRRFsPath(rrfs);
            umlsConceptDictionaryBuilder.setOutputDir(outputDir);
            umlsConceptDictionaryBuilder.setTuisOfInterestFile(tuisOfInterestFile);
            umlsConceptDictionaryBuilder.setTtysBannedFile(ttyBanlistFile);
            umlsConceptDictionaryBuilder.process();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
