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
import edu.umn.biomedicus.common.terms.TermsBagSerializer;
import edu.umn.biomedicus.concepts.ConceptDictionary.Loader;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DataLoader;
import edu.umn.biomedicus.framework.LifecycleManaged;
import edu.umn.biomedicus.vocabulary.Vocabulary;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
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
  private final DB db;

  private final Map<TermsBag, List<SuiCuiTui>> normDictionary;

  private final Map<String, List<SuiCuiTui>> phrases;

  private final Map<String, List<SuiCuiTui>> lowercasePhrases;

  ConceptDictionary(
      @Nullable DB db,
      Map<TermsBag, List<SuiCuiTui>> normDictionary,
      Map<String, List<SuiCuiTui>> phrases,
      Map<String, List<SuiCuiTui>> lowercasePhrases) {
    this.db = db;
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
    if (norms.size() == 0) {
      return null;
    }
    return normDictionary.get(norms);
  }

  @Override
  public void doShutdown() throws BiomedicusException {
    if (db != null) {

      db.close();
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

      DB db = DBMaker.fileDB(dbPath.toFile()).readOnly()
          .fileMmapEnableIfSupported()
          .make();

      Map<TermsBag, List<SuiCuiTui>> normDictionary = (Map<TermsBag, List<SuiCuiTui>>) db
          .treeMap("norms", Serializer.JAVA, Serializer.JAVA)
          .open();

      Map<String, List<SuiCuiTui>> phrases = (Map<String, List<SuiCuiTui>>) db
          .treeMap("phrases", Serializer.STRING, Serializer.JAVA)
          .open();

      Map<String, List<SuiCuiTui>> lowercase = (Map<String, List<SuiCuiTui>>) db
          .treeMap("lowercase", Serializer.STRING, Serializer.JAVA)
          .open();

      if (inMemory) {
        normDictionary = new HashMap<>(normDictionary);
        phrases = new HashMap<>(phrases);
        lowercase = new HashMap<>(lowercase);

        db.close();
        db = null;
      }
      return new ConceptDictionary(db, normDictionary, phrases, lowercase);
    }
  }
}
