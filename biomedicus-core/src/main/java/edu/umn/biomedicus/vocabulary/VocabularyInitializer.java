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

import com.google.inject.Inject;
import edu.umn.biomedicus.application.Bootstrapper;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.LabelsUtilities;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.common.types.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.tokenization.PennLikePhraseTokenizer;
import edu.umn.biomedicus.tokenization.TermTokenMerger;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 */
public class VocabularyInitializer {
    private static final Pattern PIPE_SPLITTER = Pattern.compile("\\|");
    private final TermIndexBuilder normsIndexBuilder;
    private final TermIndexBuilder termsIndexBuilder;
    private final TermIndexBuilder wordsIndexBuilder;
    private final VocabularyBuilder builder;

    @Option(name = "-s", required = true, handler = PathOptionHandler.class,
            usage = "path to SPECIALIST Lexicon installation.")
    private Path specialistPath;

    @Option(name = "-u", required = true, handler = PathOptionHandler.class,
            usage = "path to UMLS Lexicon installation.")
    private Path umlsPath;

    @Argument(handler = PathOptionHandler.class)
    private Path outputPath;

    @Inject
    private VocabularyInitializer(VocabularyBuilder builder) {
        this.builder = builder;
        normsIndexBuilder = builder.createNormsIndexBuilder();
        termsIndexBuilder = builder.createTermsIndexBuilder();
        wordsIndexBuilder = builder.createWordsIndexBuilder();
    }

    public static void main(String[] args) {
        try {
            Bootstrapper.create().getInstance(VocabularyInitializer.class)
                    .doMain(args);
        } catch (BiomedicusException e) {
            e.printStackTrace();
        }
    }

    void addPhrase(String phrase) {
        Iterator<Span> tokensIterator = PennLikePhraseTokenizer
                .tokenizePhrase(phrase).iterator();
        List<Label<Token>> parseTokens = new ArrayList<>();
        Span prev = null;
        while (tokensIterator.hasNext() || prev != null) {
            Span span = null;
            if (tokensIterator.hasNext()) {
                span = tokensIterator.next();
            }
            if (prev != null) {
                String term = prev.getCovered(phrase).toString();
                wordsIndexBuilder.addTerm(term);
                boolean hasSpaceAfter = span != null && prev.getEnd() != span
                        .getBegin();
                ParseToken parseToken = new ParseToken(term, hasSpaceAfter);
                Label<ParseToken> parseTokenLabel = new Label<>(prev,
                        parseToken);
                parseTokens.add(LabelsUtilities.cast(parseTokenLabel));
            }
            prev = span;
        }

        TermTokenMerger termTokenMerger = new TermTokenMerger(parseTokens);
        while (termTokenMerger.hasNext()) {
            Label<TermToken> termToken = termTokenMerger.next();
            termsIndexBuilder.addTerm(termToken.value().text());
        }
    }

    void addNormPhrase(String normPhrase) {
        Iterator<Span> normsIt = PennLikePhraseTokenizer
                .tokenizePhrase(normPhrase)
                .iterator();

        while (normsIt.hasNext()) {
            Span span = normsIt.next();
            CharSequence norm = span.getCovered(normPhrase);
            normsIndexBuilder.addTerm(norm.toString());
        }
    }

    private void doMain(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getLocalizedMessage());
            System.err.println(
                    "java edu.umn.biomedicus.vocabulary.VocabularyInitializer "
                            + "[options...] /path/to/outputPath");
            parser.printUsage(System.err);
            return;
        }

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

            addPhrase(inflectionalVariant);

            String uninflected = line[4];

            addNormPhrase(uninflected);

            if ((++count) % 10000 == 0) {
                System.out.println("Read " + count + " lines from LRAGR.");
            }
        }

        Path mrConso = umlsPath.resolve("MRCONSO.RRF");

        Iterator<String[]> mrconsoIt;
        try {
            mrconsoIt = Files.lines(mrConso).map(PIPE_SPLITTER::split)
                    .iterator();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        count = 0;

        while (mrconsoIt.hasNext()) {
            String[] line = mrconsoIt.next();

            String string = line[14];

            addPhrase(string);

            if ((++count) % 10000 == 0) {
                System.out
                        .println("Read " + count + " lines from MRCONSO.RRF.");
            }
        }

        try {
            builder.doShutdown();
        } catch (BiomedicusException e) {
            e.printStackTrace();
        }
    }
}
