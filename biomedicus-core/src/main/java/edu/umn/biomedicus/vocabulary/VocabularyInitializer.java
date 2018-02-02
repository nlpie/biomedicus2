/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.Bootstrapper;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.biomedicus.tokenization.PennLikePhraseTokenizer;
import edu.umn.biomedicus.tokenization.TermToken;
import edu.umn.biomedicus.tokenization.TermTokenMerger;
import edu.umn.nlpengine.Span;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;

/**
 * Initializes the BioMedICUS vocabulary using the specialist lexicon and a UMLS installation
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
public class VocabularyInitializer {

  private static final Pattern PIPE_SPLITTER = Pattern.compile("\\|");

  private final VocabularyBuilder builder;

  private TermIndexBuilder normsIndexBuilder;

  private TermIndexBuilder termsIndexBuilder;

  private TermIndexBuilder wordsIndexBuilder;


  @Nullable
  @Option(name = "-s", required = true, handler = PathOptionHandler.class,
      usage = "path to SPECIALIST Lexicon installation.")
  private Path specialistPath;

  @Nullable
  @Option(name = "-u", required = true, handler = PathOptionHandler.class,
      usage = "path to UMLS installation.")
  private Path umlsPath;

  @Inject
  private VocabularyInitializer(VocabularyBuilder builder) {
    this.builder = builder;
  }

  @Argument(required = true, handler = PathOptionHandler.class)
  public void setOutputPath(Path outputPath) {
    builder.setOutputPath(outputPath);
    normsIndexBuilder = builder.createNormsIndexBuilder();
    termsIndexBuilder = builder.createTermsIndexBuilder();
    wordsIndexBuilder = builder.createWordsIndexBuilder();
  }

  public static void main(String[] args) {
    try {
      Bootstrapper.create().getInstance(VocabularyInitializer.class).doMain(args);
    } catch (BiomedicusException e) {
      e.printStackTrace();
    }
  }

  void addPhrase(String phrase) throws BiomedicusException {
    Iterator<Span> tokensIterator = PennLikePhraseTokenizer
        .tokenizePhrase(phrase).iterator();
    List<ParseToken> parseTokens = new ArrayList<>();
    Span prev = null;
    while (tokensIterator.hasNext() || prev != null) {
      Span span = null;
      if (tokensIterator.hasNext()) {
        span = tokensIterator.next();
      }
      if (prev != null) {
        String term = prev.coveredString(phrase);
        wordsIndexBuilder.addTerm(term);
        boolean hasSpaceAfter = span != null && prev.getStartIndex() != span
            .getEndIndex();
        ParseToken parseToken = new ParseToken(prev, term, hasSpaceAfter);
        parseTokens.add(parseToken);
      }
      prev = span;
    }

    TermTokenMerger termTokenMerger = new TermTokenMerger(parseTokens.iterator());
    while (termTokenMerger.hasNext()) {
      TermToken termToken = termTokenMerger.next();
      termsIndexBuilder.addTerm(termToken.getText());
    }
  }

  void addNormPhrase(String normPhrase) throws BiomedicusException {
    Iterator<Span> normsIt = PennLikePhraseTokenizer
        .tokenizePhrase(normPhrase)
        .iterator();

    while (normsIt.hasNext()) {
      Span span = normsIt.next();
      CharSequence norm = span.coveredString(normPhrase);
      normsIndexBuilder.addTerm(norm.toString());
    }
  }

  private void doMain(String[] args) throws BiomedicusException {
    CmdLineParser parser = new CmdLineParser(this);

    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getLocalizedMessage());
      System.err.println(
          "java edu.umn.biomedicus.vocabulary.VocabularyInitializer [options...]");
      parser.printUsage(System.err);
      return;
    }



    Path mrConso = umlsPath.resolve("MRCONSO.RRF");
    if (Files.notExists(mrConso)) {
      throw new BiomedicusException("Could not find MRCNSO at " + umlsPath.toString());
    }

    Path lragr = specialistPath.resolve("LRAGR");

    long lragrLines;
    try {
      lragrLines = Files.lines(lragr).count();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

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
        System.out.println("Read " + count + " / " + lragrLines + " lines from LRAGR.");
      }
    }

    long mrConsoLines;
    try {
      mrConsoLines = Files.lines(mrConso).count();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

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
            .println("Read " + count + " / " + mrConsoLines + " lines from MRCONSO.RRF.");
      }
    }

    System.out.println("Writing words");
    wordsIndexBuilder.doWrite();
    System.out.println("Writing norms");
    normsIndexBuilder.doWrite();
    System.out.println("Writing terms");
    termsIndexBuilder.doWrite();
    System.out.println("Done writing");

    try {
      builder.doShutdown();
    } catch (BiomedicusException e) {
      e.printStackTrace();
    }
  }
}
