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
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.DocumentOperation;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import org.jetbrains.annotations.NotNull;

public class LabelStopWords implements DocumentOperation {

  private final Stopwords stopwords;

  @Inject
  public LabelStopWords(Stopwords stopwords) {
    this.stopwords = stopwords;
  }

  @Override
  public void process(@NotNull Document document) {
    LabelIndex<ParseToken> parseTokenLabelIndex = document.labelIndex(ParseToken.class);
    Labeler<StopWord> stopWordsLabeler = document.labeler(StopWord.class);

    for (ParseToken parseTokenLabel : parseTokenLabelIndex) {
      if (stopwords.isStopWord(parseTokenLabel)) {
        stopWordsLabeler.add(new StopWord(parseTokenLabel));
      }
    }
  }
}
