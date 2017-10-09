/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.normalization;

import com.google.inject.Inject;
import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary;
import edu.umn.biomedicus.common.dictionary.StringIdentifier;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.Bootstrapper;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.PathOptionHandler;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for loading a SPECIALIST LRAGR file and creating a normalizer from it.
 *
 * @author Ben Knoll
 * @author Serguei Pakhomov
 */
public final class NormalizerModelBuilder {

  /**
   * Index of the inflectional variant (Term to lookup) in the LRAGR table.
   */
  public static final int LRAGR_INFLECTIONAL_VARIANT = 1;

  /**
   * Index of the syntactic category (part of speech) in the LRAGR table.
   */
  public static final int LRAGR_SYNTACTIC_CATEGORY = 2;

  /**
   * Index of the agreement inflection code in the LRAGR table.
   */
  public static final int LRAGR_AGREEMENT_INFLECTION_CODE = 3;

  /**
   * Index of the base for in the LRAGR table.
   */
  public static final int LRAGR_BASE_FORM = 4;

  private static final Logger LOGGER = LoggerFactory.getLogger(NormalizerModelBuilder.class);

  private static final int IGNORE_WHEN_LONGER = 100;

  private static final Map<LragrPos, PartOfSpeech> LRAGR_TO_PENN;

  private static final Map<LragrPos, PartOfSpeech> LRAGR_TO_PENN_FALLBACK;

  static {
    Map<LragrPos, PartOfSpeech> builder = new HashMap<>();
    builder.put(new LragrPos("noun", "uncount(thr_plur)"), PartOfSpeech.NNS);
    builder.put(new LragrPos("noun", "count(thr_plur)"), PartOfSpeech.NNS);
    builder.put(new LragrPos("noun", "uncount(thr_sing)"), PartOfSpeech.NN);
    builder.put(new LragrPos("noun", "count(thr_sing)"), PartOfSpeech.NN);
    builder.put(new LragrPos("verb", "infinitive"), PartOfSpeech.VB);
    builder.put(new LragrPos("verb", "pres(thr_sing)"), PartOfSpeech.VBZ);
    builder.put(new LragrPos("verb", "past"), PartOfSpeech.VBD);
    builder.put(new LragrPos("verb", "past_part"), PartOfSpeech.VBN);
    builder.put(new LragrPos("verb", "pres_part"), PartOfSpeech.VBG);
    builder.put(new LragrPos("adj", "comparative"), PartOfSpeech.JJR);
    builder.put(new LragrPos("adj", "superlative"), PartOfSpeech.JJS);
    builder.put(new LragrPos("adj", "positive"), PartOfSpeech.JJ);
    builder.put(new LragrPos("adv", "comparative"), PartOfSpeech.RBR);
    builder.put(new LragrPos("adv", "superlative"), PartOfSpeech.RBS);
    builder.put(new LragrPos("adv", "positive"), PartOfSpeech.RB);
    LRAGR_TO_PENN = Collections.unmodifiableMap(builder);
  }

  static {
    Map<LragrPos, PartOfSpeech> builder = new HashMap<>();
    builder.put(new LragrPos("noun", "uncount(thr_plur)"), PartOfSpeech.NN);
    builder.put(new LragrPos("noun", "count(thr_plur)"), PartOfSpeech.NN);
    builder.put(new LragrPos("noun", "uncount(thr_sing)"), PartOfSpeech.NNS);
    builder.put(new LragrPos("noun", "count(thr_sing)"), PartOfSpeech.NNS);
    LRAGR_TO_PENN_FALLBACK = Collections.unmodifiableMap(builder);
  }

  private final BidirectionalDictionary normsIndex;

  private final BidirectionalDictionary wordsIndex;

  @Nullable
  @Option(name = "-l", required = true, handler = PathOptionHandler.class,
      usage = "path to SPECIALIST Lexicon LRAGR file.")
  private Path lragrPath;

  @Nullable
  @Argument(required = true, handler = PathOptionHandler.class, usage = "output path of normalization model")
  private Path dbPath;

  @SuppressWarnings("unchecked")
  @Inject
  public NormalizerModelBuilder(Vocabulary vocabulary) {
    normsIndex = vocabulary.getNormsIndex();
    wordsIndex = vocabulary.getWordsIndex();
  }

  public static void main(String[] args) {
    try {
      Bootstrapper.create().getInstance(NormalizerModelBuilder.class).process(args);
    } catch (IOException | BiomedicusException e) {
      e.printStackTrace();
    }
  }

  public void process(String[] args) throws IOException {
    CmdLineParser parser = new CmdLineParser(this);

    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getLocalizedMessage());
      System.err.println("java edu.umn.biomedicus.normalization.NormalizerModelBuilder "
          + "-l [path-to-lragr] [path-to-po");
      parser.printUsage(System.err);
      return;
    }

    assert lragrPath != null : "should be non-null by this point based on required = true";

    System.out.println("Starting building normalizer model from: " + lragrPath.toString());

    try {
      Files.deleteIfExists(dbPath);
    } catch (IOException e) {
      System.out.println("Failed to delete an existing db at location: " + dbPath.toString());
      e.printStackTrace();
    }

    DB db = DBMaker.fileDB(dbPath.toFile()).make();

    @SuppressWarnings("unchecked")
    Map<TermPos, TermString> norms =  (Map<TermPos, TermString>) db
        .treeMap("norms", Serializer.JAVA, Serializer.JAVA).create();
    NormalizerModel builder = new NormalizerModel(norms, db);

    Pattern exclusionPattern = Pattern.compile(".*[\\|\\$#,@;:<>\\?\\[\\]\\{\\}\\d\\.].*");

    Files.lines(lragrPath)
        .map(line -> line.split("\\|"))
        .forEach(lragrArray -> {
          String inflectionalVariant = lragrArray[LRAGR_INFLECTIONAL_VARIANT];

          Matcher exclusionMatcher = exclusionPattern.matcher(inflectionalVariant);
          if (exclusionMatcher.matches() || inflectionalVariant.length() > IGNORE_WHEN_LONGER) {
            return;
          }

          String syntacticCategory = lragrArray[LRAGR_SYNTACTIC_CATEGORY].trim();
          String agreementInflectionCode = lragrArray[LRAGR_AGREEMENT_INFLECTION_CODE].trim();
          String baseForm = lragrArray[LRAGR_BASE_FORM].trim();

          LragrPos lragrPos = new LragrPos(syntacticCategory, agreementInflectionCode);

          if (!inflectionalVariant.endsWith(baseForm)) {
            PartOfSpeech pennPos = LRAGR_TO_PENN.get(lragrPos);
            StringIdentifier termIdentifier = wordsIndex.getTermIdentifier(inflectionalVariant);
            if (termIdentifier.isUnknown()) {
              return;
            }

            if (pennPos != null) {
              builder.add(termIdentifier, pennPos,
                  normsIndex.getTermIdentifier(baseForm), baseForm);
            }

            PartOfSpeech fallbackPos = LRAGR_TO_PENN_FALLBACK.get(lragrPos);
            if (fallbackPos != null) {
              builder.add(termIdentifier, fallbackPos,
                  normsIndex.getTermIdentifier(baseForm), baseForm);
            }
          }
        });

    try {
      builder.doShutdown();
    } catch (BiomedicusException e) {
      e.printStackTrace();
    }
  }

  private static class LragrPos implements Comparable<LragrPos> {

    private final String syntacticCategory;
    private final String agreementInflectionCode;

    public LragrPos(String syntacticCategory, String agreementInflectionCode) {
      this.syntacticCategory = Objects.requireNonNull(syntacticCategory);
      this.agreementInflectionCode = Objects.requireNonNull(agreementInflectionCode);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      LragrPos lragrPos = (LragrPos) o;

      if (!agreementInflectionCode.equals(lragrPos.agreementInflectionCode)) {
        return false;
      }
      return syntacticCategory.equals(lragrPos.syntacticCategory);

    }

    @Override
    public int hashCode() {
      int result = syntacticCategory.hashCode();
      result = 31 * result + agreementInflectionCode.hashCode();
      return result;
    }

    @Override
    public int compareTo(LragrPos o) {
      int result = syntacticCategory.compareTo(o.syntacticCategory);
      if (result == 0) {
        result = agreementInflectionCode.compareTo(o.agreementInflectionCode);
      }
      return result;
    }
  }
}
