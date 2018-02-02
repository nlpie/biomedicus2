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
package edu.umn.biomedicus.stopwords;

import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DataLoader;
import edu.umn.biomedicus.tokenization.Token;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class assigns a boolean value to tokens that are in a specified stopword file.
 */
@ProvidedBy(StopwordsModel.Loader.class)
public class StopwordsModel implements Stopwords {

  private static final Logger LOGGER = LoggerFactory.getLogger(StopwordsModel.class);

  private final Set<String> stopwordsList;

  private StopwordsModel(Set<String> stopwordsList) {
    this.stopwordsList = Collections.unmodifiableSet(Objects.requireNonNull(stopwordsList));
  }

  @Override
  public boolean isStopWord(Token token) {
    String value = token.getText().toLowerCase().trim();
    return stopwordsList.contains(value);
  }

  @Singleton
  public static class Loader extends DataLoader<StopwordsModel> {

    private final Path stopwordsPath;

    @Inject
    public Loader(@Setting("stopwords.fileBased.path") Path stopwordsPath) {
      this.stopwordsPath = stopwordsPath;
    }

    @Override
    protected StopwordsModel loadModel() throws BiomedicusException {
      LOGGER.info("Building stopwords list from input stream");
      Set<String> stopwordsListBuilder = new HashSet<>();

      try (BufferedReader bufferedReader = Files.newBufferedReader(stopwordsPath)) {
        String nextLine;
        while ((nextLine = bufferedReader.readLine()) != null) {
          if (!nextLine.isEmpty()) {
            stopwordsListBuilder.add(nextLine.toLowerCase());
          }
        }
      } catch (IOException e) {
        throw new BiomedicusException(e);
      }

      return new StopwordsModel(stopwordsListBuilder);
    }
  }
}
