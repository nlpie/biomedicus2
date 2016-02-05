package edu.umn.biomedicus.concepts;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 *
 */
public class UmlsConceptDictionaryBuilder {
    private static final Pattern SPLITTER = Pattern.compile("\\|");

    private final Path mrconsoFile;

    private final Path mrstyFile;

    private final Path dictionaryOutput;

    private final Path typesOutput;

    public UmlsConceptDictionaryBuilder(Path mrconsoFile, Path mrstyFile, Path dictionaryOutput, Path typesOutput) {
        this.mrconsoFile = mrconsoFile;
        this.mrstyFile = mrstyFile;
        this.dictionaryOutput = dictionaryOutput;
        this.typesOutput = typesOutput;
    }

    void process() throws IOException {
        Collector<Map.Entry<String, String>, ?, Map<String, List<String>>> mapCollector = Collectors.toMap(Map.Entry::getKey,
                entry -> {
                    List<String> list = new ArrayList<>();
                    list.add(entry.getValue());
                    return list;
                },
                (first, second) -> {
                    for (String secondItem : second) {
                        if (!first.contains(secondItem)) {
                            first.add(secondItem);
                        }
                    }
                    return first;
                });
        Map<String, List<String>> concepts = Files.lines(mrconsoFile)
                .map(SPLITTER::split)
                .filter(columns -> "ENG".equals(columns[1]))
                .map(line -> {
                    String cui = line[0];
                    String term = line[14].trim().toLowerCase();
                    return new AbstractMap.SimpleImmutableEntry<>(term, cui);
                })
                .collect(mapCollector);

        Map<String, List<String>> cuiToTuis = Files.lines(mrstyFile)
                .map(SPLITTER::split)
                .map(line -> {
                    String cui = line[0];
                    String tui = line[1];
                    return new AbstractMap.SimpleImmutableEntry<>(cui, tui);
                })
                .collect(mapCollector);

        Files.createDirectories(dictionaryOutput.getParent());
        Files.createDirectories(typesOutput.getParent());

        Yaml yaml = new Yaml();
        try (Writer conceptsWriter = Files.newBufferedWriter(dictionaryOutput, CREATE, TRUNCATE_EXISTING);
             Writer typesWriter = Files.newBufferedWriter(typesOutput, CREATE, TRUNCATE_EXISTING)) {
            yaml.dump(concepts, conceptsWriter);
            yaml.dump(cuiToTuis, typesWriter);
        }
    }

    public static void main(String[] args) {
        Path mrconsoFile = Paths.get(args[0]);

        Path mrstyFile = Paths.get(args[1]);

        Path dictionaryOutput = Paths.get(args[2]);

        Path typesOutput = Paths.get(args[3]);

        UmlsConceptDictionaryBuilder umlsConceptDictionaryBuilder = new UmlsConceptDictionaryBuilder(mrconsoFile,
                mrstyFile, dictionaryOutput, typesOutput);

        try {
            umlsConceptDictionaryBuilder.process();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
