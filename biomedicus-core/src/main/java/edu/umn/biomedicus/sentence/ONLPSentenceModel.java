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

package edu.umn.biomedicus.sentence;

import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DataLoader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

/**
 * A factory which creates sentence detectors that use OpenNLP's Maximum entropy model to detect
 * sentences.
 *
 * @author Ben Knoll
 * @since 1.1.0
 */
@Singleton
@ProvidedBy(ONLPSentenceModel.Loader.class)
public class ONLPSentenceModel {

  private SentenceModel model;

  /**
   * Default constructor. Initializes with the OpenNLP {@link SentenceModel} used to create
   * the OpenNLP sentence detector.
   *
   * @param model sentence model.
   */
  private ONLPSentenceModel(SentenceModel model) {
    this.model = model;
  }

  SentenceDetectorME createSentenceDetector() {
    return new SentenceDetectorME(model);
  }

  @Singleton
  public static class Loader extends DataLoader<ONLPSentenceModel> {

    private final Path path;

    @Inject
    public Loader(@Setting("opennlp.sentence.model.path") Path path) {
      this.path = path;
    }

    @Override
    protected ONLPSentenceModel loadModel() throws BiomedicusException {
      try (InputStream inputStream = Files.newInputStream(path)) {
        SentenceModel sentenceModel = new SentenceModel(inputStream);
        return new ONLPSentenceModel(sentenceModel);
      } catch (IOException e) {
        throw new BiomedicusException(e);
      }
    }
  }
}
