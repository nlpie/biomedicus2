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

package edu.umn.biomedicus.tnt;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.Aggregator;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.TextView;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.mapdb.DB;
import org.mapdb.DBMaker;

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
      @ProcessorSetting("tnt.train.outputDir") Path outputDir) {
    this.viewName = viewName;

    tntModelTrainer = TntModelTrainer.builder()
        .useMslSuffixModel(false)
        .maxSuffixLength(5)
        .maxWordFrequency(20)
        .restrictToOpenClass(false)
        .useCapitalization(true).build();

    this.outputDir = outputDir;
  }

  @Override
  public void addDocument(Document document) throws BiomedicusException {
    TextView view = document.getTextView(viewName)
        .orElseThrow(
            () -> new BiomedicusException("Specified view " + viewName + " does not exist"));

    LabelIndex<Sentence> sentences = view.getLabelIndex(Sentence.class);
    LabelIndex<ParseToken> tokens = view.getLabelIndex(ParseToken.class);
    LabelIndex<PartOfSpeech> partsOfSpeech = view.getLabelIndex(PartOfSpeech.class);

    for (Label<Sentence> sentence : sentences) {
      List<ParseToken> sentenceTokens = tokens.insideSpan(sentence).valuesAsList();
      List<PartOfSpeech> sentencesPos = partsOfSpeech.insideSpan(sentence).valuesAsList();

      tntModelTrainer.addSentence(sentenceTokens, sentencesPos);
    }
  }

  @Override
  public void done() throws BiomedicusException {
    TntModel model = tntModelTrainer.createModel();

    DB db = DBMaker.fileDB(outputDir.resolve("words.db").toFile()).make();

    try {
      model.write(outputDir, db);
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }

    db.close();
  }
}
