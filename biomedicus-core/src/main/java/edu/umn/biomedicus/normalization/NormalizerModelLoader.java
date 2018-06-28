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

package edu.umn.biomedicus.normalization;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DataLoader;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the normalizer model for Guice to provide to anything that needs it.
 */
@Singleton
public final class NormalizerModelLoader extends DataLoader<NormalizerModel> {

  private static final Logger LOGGER = LoggerFactory.getLogger(NormalizerModelLoader.class);

  private final Path dbPath;

  private final boolean inMemory;

  @Inject
  NormalizerModelLoader(@Setting("normalization.db.path") Path dbPath,
      @Setting("normalization.inMemory") boolean inMemory) {
    this.dbPath = dbPath;
    this.inMemory = inMemory;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected NormalizerModel loadModel() {
    LOGGER.info("Loading normalization model: {}. inMemory = {}.", dbPath, inMemory);

    return new RocksDBNormalizerModel(dbPath).inMemory(inMemory);
  }
}
