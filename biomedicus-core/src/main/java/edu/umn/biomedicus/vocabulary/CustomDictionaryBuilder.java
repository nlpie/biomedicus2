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
public class CustomDictionaryBuilder {
    public static void main(String[] args) {
        Path existingWordsPath = null;
        if (args.length == 3) {
            existingWordsPath = Paths.get(args[2]);
        }
        try {
            Set<String> words = existingWordsPath != null ? Files.lines(existingWordsPath).collect(Collectors.toSet()) : new HashSet<>();

            Pattern comma = Pattern.compile(",");
            Files.lines(Paths.get(args[0])).map(comma::split)
                    .map(columns -> columns[1])
                    .map(s -> s.substring(s.indexOf('"') + 1, s.lastIndexOf('"')))
                    .forEach(words::add);

            Path outputPath = Paths.get(args[1]);
            Files.write(outputPath, words, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
