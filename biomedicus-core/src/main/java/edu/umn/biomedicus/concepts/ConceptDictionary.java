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
import com.google.inject.ProvidedBy;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.terms.TermsBag;
import edu.umn.biomedicus.concepts.ConceptDictionary.Loader;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DataLoader;
import edu.umn.biomedicus.framework.LifecycleManaged;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.mapdb.Serializer;
import org.mapdb.SortedTableMap;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores UMLS Concepts in a multimap (Map from String to List of Concepts).
 *
 * @author Ben Knoll
 * @since 1.0.0
 */
@Singleton
@ProvidedBy(Loader.class)
class ConceptDictionary implements LifecycleManaged {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConceptDictionary.class);

  @Nullable
  private final List<Volume> volumes;

  private final Map<TermsBag, List<SuiCuiTui>> normDictionary;

  private final Map<String, List<SuiCuiTui>> phrases;

  private final Map<String, List<SuiCuiTui>> lowercasePhrases;

  ConceptDictionary(
      @Nullable List<Volume> volumes,
      Map<TermsBag, List<SuiCuiTui>> normDictionary,
      Map<String, List<SuiCuiTui>> phrases,
      Map<String, List<SuiCuiTui>> lowercasePhrases) {
    this.volumes = volumes;
    this.normDictionary = normDictionary;
    this.phrases = phrases;
    this.lowercasePhrases = lowercasePhrases;
  }

  @Nullable
  List<SuiCuiTui> forPhrase(String phrase) {
    return phrases.get(phrase);
  }

  @Nullable
  List<SuiCuiTui> forLowercasePhrase(String phrase) {
    return lowercasePhrases.get(phrase);
  }

  @Nullable
  List<SuiCuiTui> forNorms(TermsBag norms) {
    if (norms.uniqueTerms() == 0) {
      return null;
    }
    return normDictionary.get(norms);
  }

  @Override
  public void doShutdown() throws BiomedicusException {
    if (volumes != null) {
      for (Volume volume : volumes) {
        volume.close();
      }
    }
  }

  @Singleton
  static final class Loader extends DataLoader<ConceptDictionary> {

    private final boolean inMemory;

    private final Path dbPath;

    @Inject
    Loader(@Setting("concepts.db.path") Path dbPath,
        @Setting("concepts.inMemory") boolean inMemory) {
      this.dbPath = dbPath;
      this.inMemory = inMemory;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ConceptDictionary loadModel() throws BiomedicusException {

      LOGGER.info("Loading concepts database: {}", dbPath);

      Volume normsVol = MappedFileVol.FACTORY
          .makeVolume(dbPath.resolve("norms.db").toString(), true);

      Map<TermsBag, List<SuiCuiTui>> normDictionary = SortedTableMap
          .open(normsVol, Serializer.JAVA, Serializer.JAVA);

      Volume phrasesVol = MappedFileVol.FACTORY
          .makeVolume(dbPath.resolve("phrases.db").toString(), true);

      Map<String, List<SuiCuiTui>> phrases = SortedTableMap
          .open(phrasesVol, Serializer.STRING, Serializer.JAVA);

      Volume lowercaseVol = MappedFileVol.FACTORY
          .makeVolume(dbPath.resolve("lowercase.db").toString(), true);

      Map<String, List<SuiCuiTui>> lowercase = SortedTableMap
          .open(lowercaseVol, Serializer.STRING, Serializer.JAVA);

      List<Volume> volumes = Arrays.asList(normsVol, phrasesVol, lowercaseVol);
      if (inMemory) {
        LOGGER.info("Transferring concepts to memory.");
        normDictionary = new HashMap<>(normDictionary);
        phrases = new HashMap<>(phrases);
        lowercase = new HashMap<>(lowercase);

        for (Volume volume : volumes) {
          volume.close();
        }

        volumes = null;
      }
      return new ConceptDictionary(volumes, normDictionary, phrases, lowercase);
    }
  }
}
