package edu.umn.biomedicus.vocabulary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
public class UMLSIndexBuilder {
    public static void main(String[] args) {
        Path existingWordsPath = null;
        if (args.length == 3) {
            existingWordsPath = Paths.get(args[2]);
        }
        try {
            Set<String> words = existingWordsPath != null ? Files.lines(existingWordsPath).collect(Collectors.toSet()) : new HashSet<>();

            Path mrxwPath = Paths.get(args[0]);
            Pattern split = Pattern.compile("\\|");
            Files.lines(mrxwPath).map(split::split).map(columns -> columns[1]).forEach(words::add);

            Path outputPath = Paths.get(args[1]);
            Files.write(outputPath, words, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
