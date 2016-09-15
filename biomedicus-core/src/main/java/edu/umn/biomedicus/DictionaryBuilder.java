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

package edu.umn.biomedicus;

import edu.umn.biomedicus.common.tuples.Pair;
import edu.umn.biomedicus.vocabulary.VocabularyBuilder;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 */
public class DictionaryBuilder {
    private static final Pattern PIPE_SPLITTER = Pattern.compile("\\|");

    @Option(name = "-s", required = true, handler = PathOptionHandler.class, usage = "path to SPECIALIST Lexicon installation.")
    private Path specialistPath;

    @Option(name = "-u", required = true, handler = PathOptionHandler.class, usage = "path to UMLS Lexicon installation.")
    private Path umlsPath;

    @Argument(handler = PathOptionHandler.class)
    private Path outputFile;

    private void doMain(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getLocalizedMessage());
            System.err.println("java edu.umn.biomedicus.DictionaryBuilder [options...] /path/to/outputFile");
            parser.printUsage(System.err);
            return;
        }

        VocabularyBuilder vocabularyBuilder = new VocabularyBuilder(outputFile);
        Path lragr = specialistPath.resolve("LRAGR");

        Iterator<String[]> iterator;
        try {
            iterator = Files.lines(lragr).map(PIPE_SPLITTER::split).iterator();
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return;
        }
        int count = 0;
        while (iterator.hasNext()) {
            String[] line = iterator.next();
            String inflectionalVariant = line[1];

            vocabularyBuilder.addPhrase(inflectionalVariant);

            String uninflected = line[4];

            vocabularyBuilder.addNormPhrase(uninflected);

            if ((++count) % 10000 == 0) {
                System.out.println("Read " + count + " lines from LRAGR.");
            }
        }

        Path mrConso = umlsPath.resolve("MRCONSO.RRF");
        Map<Pair<String, String>, Boolean> sourceTtyIgnored = new HashMap<>();

        Iterator<String[]> mrconsoIt;
        try {
            mrconsoIt = Files.lines(mrConso).map(PIPE_SPLITTER::split).iterator();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        count = 0;

        Console console = System.console();

        while (mrconsoIt.hasNext()) {
            String[] line = mrconsoIt.next();
            String source = line[11];
            String tty = line[12];
            Pair<String, String> sourceTTY = new Pair<>(source, tty);
            Boolean ignored = sourceTtyIgnored.get(sourceTTY);
            if (ignored == null) {
                ignored = Pattern.compile("(?is)^y.*$").matcher(console.readLine()).matches();
                sourceTtyIgnored.put(sourceTTY, ignored);
            }
            if (ignored) {
                continue;
            }

            String string = line[14];

            vocabularyBuilder.addPhrase(string);

            if ((++count) % 10000 == 0) {
                System.out.println("Read " + count + " lines from MRCONSO.RRF.");
            }
        }

    }


    public static void main(String[] args) {
        new DictionaryBuilder().doMain(args);
    }
}
