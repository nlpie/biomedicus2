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

package edu.umn.biomedicus.vocabulary;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.exc.BiomedicusException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * Class responsible for building the vocabulary db.
 *
 * @author Ben knoll
 * @since 1.6.0
 */
public class MapDbVocabularyBuilder extends VocabularyBuilder {

  @Nullable
  private DB db;

  @Inject
  public MapDbVocabularyBuilder(@Setting("vocabulary.db.path") Path dbPath) {
    db = DBMaker.fileDB(dbPath.toString()).fileMmapEnableIfSupported()
        .readOnly().make();
  }

  @Override
  TermIndexBuilder createWordsIndexBuilder() {
    checkState(db != null, "Not open");
    return new MapDbTermIndex(db, "words");
  }

  @Override
  TermIndexBuilder createTermsIndexBuilder() {
    checkState(db != null, "Not open");
    return new MapDbTermIndex(db, "terms");
  }

  @Override
  TermIndexBuilder createNormsIndexBuilder() {
    checkState(db != null, "Not open");
    return new MapDbTermIndex(db, "norms");
  }

  @Override
  public void doShutdown() throws BiomedicusException {
    if (db != null) {
      db.close();
    }
    db = null;
  }
}
