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

import edu.umn.biomedicus.exc.BiomedicusException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 *
 */
public class VocabularyBuilder {
    private static final Pattern PIPE_SPLITTER = Pattern.compile("\\|");

    @Option(name = "-s", required = true, handler = PathOptionHandler.class, usage = "path to SPECIALIST Lexicon installation.")
    private Path specialistPath;

    @Option(name = "-u", required = true, handler = PathOptionHandler.class, usage = "path to UMLS Lexicon installation.")
    private Path umlsPath;

    @Argument(handler = PathOptionHandler.class)
    private Path outputPath;

    private void doMain(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getLocalizedMessage());
            System.err.println("java edu.umn.biomedicus.vocabulary.VocabularyBuilder [options...] /path/to/outputPath");
            parser.printUsage(System.err);
            return;
        }

        Vocabulary vocabulary = new Vocabulary(outputPath);
        vocabulary.openForWriting();

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

            vocabulary.addPhrase(inflectionalVariant);

            String uninflected = line[4];

            vocabulary.addNormPhrase(uninflected);

            if ((++count) % 10000 == 0) {
                System.out.println("Read " + count + " lines from LRAGR.");
            }
        }

        Path mrConso = umlsPath.resolve("MRCONSO.RRF");

        Iterator<String[]> mrconsoIt;
        try {
            mrconsoIt = Files.lines(mrConso).map(PIPE_SPLITTER::split).iterator();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        count = 0;

        while (mrconsoIt.hasNext()) {
            String[] line = mrconsoIt.next();

            String string = line[14];

            vocabulary.addPhrase(string);

            if ((++count) % 10000 == 0) {
                System.out.println("Read " + count + " lines from MRCONSO.RRF.");
            }
        }

        try {
            vocabulary.doShutdown();
        } catch (BiomedicusException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new VocabularyBuilder().doMain(args);
    }
}
