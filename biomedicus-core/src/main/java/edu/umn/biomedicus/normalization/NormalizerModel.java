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
import com.google.inject.ProvidedBy;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.terms.TermIdentifier;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DataLoader;
import edu.umn.biomedicus.framework.LifecycleManaged;
import edu.umn.biomedicus.normalization.NormalizerModel.Loader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a map backed normalizer model. Will either use a MapDB bt
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
@Singleton
@ProvidedBy(Loader.class)
final class NormalizerModel implements LifecycleManaged {

  private static final Logger LOGGER = LoggerFactory.getLogger(NormalizerModel.class);

  private final Map<TermPos, TermString> normalizationMap;

  @Nullable
  private final DB db;

  @Inject
  NormalizerModel(Map<TermPos, TermString> normalizationMap, @Nullable DB db) {
    this.normalizationMap = normalizationMap;
    this.db = db;
  }

  void add(TermIdentifier variant, PartOfSpeech pos, TermIdentifier normIndex, String baseForm) {
    normalizationMap.put(new TermPos(variant, pos), new TermString(normIndex, baseForm));
  }

  /**
   * Gets the term index identifier and its string form
   *
   * @param termPos
   * @return
   */
  @Nullable
  public TermString get(TermPos termPos) {
    return normalizationMap.get(termPos);
  }

  @Override
  public void doShutdown() throws BiomedicusException {
    if (db != null) {
      ((BTreeMap) normalizationMap).close();
      db.close();
    }
  }

  @Singleton
  public static final class Loader extends DataLoader<NormalizerModel> {

    private final Path dbPath;

    private final boolean inMemory;

    @Inject
    Loader(@Setting("normalization.db.path") Path dbPath,
        @Setting("normalization.inMemory") boolean inMemory) {
      this.dbPath = dbPath;
      this.inMemory = inMemory;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected NormalizerModel loadModel() throws BiomedicusException {
      DB db = DBMaker.fileDB(dbPath.toFile()).readOnly().make();

      LOGGER.info("Loading normalization model: " + dbPath.toString());

      BTreeMap<TermPos, TermString> norms = (BTreeMap<TermPos, TermString>) db
          .treeMap("norms", Serializer.JAVA, Serializer.JAVA)
          .open();

      if (inMemory) {
        LOGGER.info("Transferring normalization model to memory.");
        HashMap<TermPos, TermString> normalizationMap = new HashMap<>(norms);
        db.close();
        return new NormalizerModel(normalizationMap, null);
      } else {
        return new NormalizerModel(norms, db);
      }
    }
  }
}
