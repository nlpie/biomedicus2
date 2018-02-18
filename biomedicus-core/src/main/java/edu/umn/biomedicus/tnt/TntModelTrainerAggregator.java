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

package edu.umn.biomedicus.tnt;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.Aggregator;
import edu.umn.nlpengine.Artifact;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.LabelIndex;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Trains the TnT model using the tagged parts of speech in all documents.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class TntModelTrainerAggregator implements Aggregator {

  private final String viewName;

  private final TntModelTrainer tntModelTrainer;

  private final Path outputDir;

  @Inject
  TntModelTrainerAggregator(@ProcessorSetting("tnt.train.viewName") String viewName,
      @ProcessorSetting("tnt.train.outputDir") Path outputDir,
      DataStoreFactory dataStoreFactory) {
    this.viewName = viewName;

    dataStoreFactory.setDbPath(outputDir.resolve("words/"));

    tntModelTrainer = TntModelTrainer.builder()
        .useMslSuffixModel(false)
        .maxSuffixLength(5)
        .maxWordFrequency(20)
        .restrictToOpenClass(false)
        .useCapitalization(true)
        .dataStoreFactory(dataStoreFactory)
        .build();

    this.outputDir = outputDir;
  }

  @Override
  public void done() {
    TntModel model = tntModelTrainer.createModel();
    try {
      model.write(outputDir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void process(Artifact artifact) {
    Document view = artifact.getDocuments().get(viewName);

    if (view == null) {
      throw new RuntimeException("View was null: " + viewName);
    }

    LabelIndex<Sentence> sentences = view.labelIndex(Sentence.class);
    LabelIndex<ParseToken> tokens = view.labelIndex(ParseToken.class);
    LabelIndex<PosTag> partsOfSpeech = view.labelIndex(PosTag.class);

    for (Sentence sentence : sentences) {
      List<ParseToken> sentenceTokens = tokens.insideSpan(sentence).asList();
      List<PosTag> sentencesPos = partsOfSpeech.insideSpan(sentence).asList();

      tntModelTrainer.addSentence(sentenceTokens, sentencesPos);
    }
  }
}
