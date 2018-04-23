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

package edu.umn.biomedicus.tokenization;

import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.DocumentProcessor;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import org.jetbrains.annotations.NotNull;

public final class TermTokenMergerProcessor implements DocumentProcessor {

  @Override
  public void process(@NotNull Document document) {
    LabelIndex<ParseToken> parseTokens = document.labelIndex(ParseToken.class);
    LabelIndex<Sentence> sentenceLabelIndex = document.labelIndex(Sentence.class);
    Labeler<TermToken> termTokenLabeler = document.labeler(TermToken.class);

    for (Sentence sentence : sentenceLabelIndex) {
      LabelIndex<ParseToken> labelIndex = parseTokens.inside(sentence);
      TermTokenMerger tokenMerger = new TermTokenMerger(labelIndex);
      while (tokenMerger.hasNext()) {
        TermToken termToken = tokenMerger.next();
        termTokenLabeler.add(termToken);
      }
    }
  }
}
