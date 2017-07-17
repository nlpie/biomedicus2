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

package edu.umn.biomedicus.concepts;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.terms.TermVector;
import edu.umn.biomedicus.common.terms.TermsBag;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.Bootstrapper;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.PathOptionHandler;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConceptDictionaryBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConceptDictionaryBuilder.class);

  private static final Pattern SPLITTER = Pattern.compile("\\|");

  private static final Pattern SPACE_SPLITTER = Pattern.compile(" ");

  private final Set<SUI> filteredSuis;

  private final Set<CUI> filteredCuis;

  private final Set<SuiCui> filteredSuiCuis;

  private final Set<TUI> filteredTuis;

  private final Vocabulary vocabulary;

  @Nullable
  @Argument(required = true, handler = PathOptionHandler.class, usage = "Path to UMLS installation")
  private Path umlsPath;

  @Nullable
  @Argument(index = 1, required = true, handler = PathOptionHandler.class,
      usage = "Path to TUIs of interest")
  private Path tuisOfInterestFile;

  @Nullable
  @Argument(index = 2, required = true, handler = PathOptionHandler.class,
      usage = "Banned TTYs file")
  private Path bannedTtysFile;

  @Nullable
  @Argument(index = 3, handler = PathOptionHandler.class, usage = "Path to write db out to.")
  private Path dbPath;

  @Inject
  ConceptDictionaryBuilder(@Setting("concepts.filters.sui.path") Path filteredSuisPath,
      @Setting("concepts.filters.cui.path") Path filteredCuisPath,
      @Setting("concepts.filters.suicui.path") Path filteredSuiCuisPath,
      @Setting("concepts.filters.tui.path") Path filteredTuisPath,
      Vocabulary vocabulary
  ) throws IOException {
    Pattern splitter = Pattern.compile(",");

    filteredSuis = Files.lines(filteredSuisPath).map(SUI::new)
        .collect(Collectors.toSet());

    filteredCuis = Files.lines(filteredCuisPath).map(CUI::new)
        .collect(Collectors.toSet());

    filteredSuiCuis = Files.lines(filteredSuiCuisPath)
        .map(splitter::split)
        .map(line -> new ConceptDictionaryBuilder.SuiCui(new SUI(line[0]), new CUI(line[1])))
        .collect(Collectors.toSet());

    filteredTuis = Files.lines(filteredTuisPath).map(TUI::new)
        .collect(Collectors.toSet());

    this.vocabulary = vocabulary;
  }

  public static void main(String[] args) {
    try {
      Bootstrapper.create().getInstance(ConceptDictionaryBuilder.class).doWork(args);
    } catch (BiomedicusException | IOException e) {
      e.printStackTrace();
    }
  }

  private void doWork(String[] args) throws IOException {
    CmdLineParser parser = new CmdLineParser(this);

    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getLocalizedMessage());
      System.err.println("java edu.umn.biomedicus.concepts.ConceptDictionaryBuilder [umls path]"
          + " [tuis of interest path] [banned ttys path] [output path]");
      parser.printUsage(System.err);
      return;
    }

    Files.deleteIfExists(dbPath);

    DB db = DBMaker.fileDB(dbPath.toFile()).make();

    System.out.println("Loading TUIs of interest");
    Set<TUI> whitelist = Files.lines(tuisOfInterestFile).map(SPLITTER::split)
        .filter(line -> line.length >= 3)
        .map(line -> line[1])
        .map(TUI::new)
        .collect(Collectors.toSet());

    Set<String> ttyBanlist = Files.lines(bannedTtysFile).collect(Collectors.toSet());

    Path mrstyPath = umlsPath.resolve("MRSTY.RRF");
    System.out.println("Loading CUI -> TUIs map from MRSTY: " + mrstyPath);
    Map<CUI, List<TUI>> cuiToTUIs = Files.lines(mrstyPath)
        .map(SPLITTER::split)
        .map(line -> {
          CUI cui = new CUI(line[0]);
          TUI tui = new TUI(line[1]);
          List<TUI> tuis = new ArrayList<>();
          if (whitelist.contains(tui)) {
            tuis.add(tui);
            return new AbstractMap.SimpleImmutableEntry<>(cui, tuis);
          }
          return null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Map.Entry::getKey,
            Map.Entry::getValue,
            (firstList, secondList) -> {
              for (TUI tui : secondList) {
                if (!firstList.contains(tui)) {
                  firstList.add(tui);
                }
              }
              return firstList;
            }));

    TermIndex normIndex = vocabulary.getWordsIndex();

    Path mrconsoPath = umlsPath.resolve("MRCONSO.RRF");
    System.out.println("Loading phrases and SUI -> CUIs from MRCONSO: " + mrconsoPath);
    Set<SUI> bannedSUIs = new HashSet<>();

    // block so phrase dictionary gets freed once it's no longer needed
    {
      Map<String, List<SuiCuiTui>> phraseDictionary = new HashMap<>();
      Map<String, List<SuiCuiTui>> phrasesLowerCase = new HashMap<>();

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

            List<TUI> tuis = cuiToTUIs.get(cui);
            if (tuis == null || tuis.size() == 0) {
              LOGGER.trace("Filtering \"{}\" because it has no interesting types", phrase);
              return;
            }
            for (TUI tui : tuis) {
              if (filteredCuis.contains(cui) || filteredTuis.contains(tui)
                  || filteredSuiCuis.contains(new SuiCui(sui, cui)) || filteredSuis.contains(sui)) {
                continue;
              }

              SuiCuiTui value = new SuiCuiTui(sui, cui, tui);
              multimapPut(phraseDictionary, phrase, value);
              multimapPut(phrasesLowerCase, phrase.toLowerCase(), value);
            }
          });


      @SuppressWarnings("unchecked")
      BTreeMap<String, List<SuiCuiTui>> dbPhraseDictionary = (BTreeMap<String, List<SuiCuiTui>>) db
          .treeMap("phrases", Serializer.STRING, Serializer.JAVA).create();
      dbPhraseDictionary.putAll(phraseDictionary);

      @SuppressWarnings("unchecked")
      BTreeMap<String, List<SuiCuiTui>> dbPhrasesLowerCase = (BTreeMap<String, List<SuiCuiTui>>) db
          .treeMap("lowercase", Serializer.STRING, Serializer.JAVA).create();
      dbPhrasesLowerCase.putAll(phrasesLowerCase);
    }

    Path mrxnsPath = umlsPath.resolve("MRXNS_ENG.RRF");
    System.out.println("Loading lowercase normalized strings from MRXNS_ENG: " + mrxnsPath);

    Map<TermsBag, List<SuiCuiTui>> normMap = new HashMap<>();

    Files.lines(mrxnsPath)
        .map(SPLITTER::split)
        .filter(columns -> "ENG".equals(columns[0]))
        .forEach(line -> {
          List<String> norms = Arrays.asList(SPACE_SPLITTER.split(line[1]));
          CUI cui = new CUI(line[2]);
          SUI sui = new SUI(line[4]);

          if (norms.size() < 2) {
            return;
          }

          if (bannedSUIs.contains(sui)) {
            return;
          }

          TermVector termsBag = normIndex.getTermVector(norms);
          List<TUI> tuis = cuiToTUIs.get(cui);
          if (tuis == null || tuis.size() == 0) {
            LOGGER.trace("Filtering \"{}\" because it has no interesting types", termsBag);
            return;
          }
          for (TUI tui : tuis) {
            multimapPut(normMap, termsBag.toBag(), new SuiCuiTui(sui, cui, tui));
          }
        });

    @SuppressWarnings("unchecked")
    BTreeMap<TermsBag, List<SuiCuiTui>> dbNormMap = (BTreeMap<TermsBag, List<SuiCuiTui>>) db
        .treeMap("norms", Serializer.JAVA, Serializer.JAVA).create();

    db.close();
  }

  private <K, V> void multimapPut(Map<K, List<V>> map, K key, V value) {
    map.compute(key, (unused, list) -> {
      if (list == null) {
        list = new ArrayList<>();
      }
      list.add(value);
      return list;
    });
  }

  private static final class SuiCui {

    private final SUI sui;
    private final CUI cui;

    public SuiCui(SUI sui, CUI cui) {
      this.sui = sui;
      this.cui = cui;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      SuiCui suiCui = (SuiCui) o;

      if (!sui.equals(suiCui.sui)) {
        return false;
      }
      return cui.equals(suiCui.cui);

    }

    @Override
    public int hashCode() {
      int result = sui.hashCode();
      result = 31 * result + cui.hashCode();
      return result;
    }
  }
}
