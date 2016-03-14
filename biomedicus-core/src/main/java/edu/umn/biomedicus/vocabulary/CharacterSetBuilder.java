package edu.umn.biomedicus.vocabulary;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class CharacterSetBuilder {
    public static void main(String args[]) {
        Path existingChars = args.length == 3 ? Paths.get(args[2]) : null;
        Set<Character> characters = new HashSet<>();

        if (existingChars != null) {
            try {
                Files.lines(existingChars).flatMapToInt(String::chars).forEach(c -> characters.add((char) c));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Path inputFile = Paths.get(args[0]);
        try {
            Files.lines(inputFile).flatMapToInt(String::chars).forEach(c -> characters.add((char) c));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String charString = characters.stream().map(c -> "" + c).collect(Collectors.joining());
        Path outPath = Paths.get(args[1]);
        try {
            Files.write(outPath, Collections.singletonList(charString), StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
