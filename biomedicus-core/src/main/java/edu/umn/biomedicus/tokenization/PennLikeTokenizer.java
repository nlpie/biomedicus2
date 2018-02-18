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
import edu.umn.nlpengine.Span;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public final class PennLikeTokenizer implements DocumentProcessor {

  private Labeler<ParseToken> parseTokenLabeler = null;

  private CharSequence text = null;

  private Span prev = null;

  @Override
  public void process(@NotNull Document document) {
    LabelIndex<Sentence> sentenceLabelIndex = document.labelIndex(Sentence.class);
    parseTokenLabeler = document.labeler(ParseToken.class);

    for (Sentence sentence : sentenceLabelIndex) {
      text = sentence.coveredText(document.getText());

      Iterator<Span> iterator = PennLikePhraseTokenizer.tokenizeSentence(text).iterator();

      prev = null;

      while (iterator.hasNext()) {
        Span current = iterator.next();
        if (current.length() == 0) {
          continue;
        }
        if (prev != null) {
          boolean hasSpaceAfter = prev.getEndIndex() != current.getStartIndex();
          labelToken(sentence, hasSpaceAfter);
        }
        prev = current;
      }
      if (prev != null) {
        labelToken(sentence, false);
      }
    }
  }

  private void labelToken(Sentence sentence, boolean hasSpaceAfter) {
    String tokenText = text.subSequence(prev.getStartIndex(), prev.getEndIndex()).toString();

    parseTokenLabeler.add(new ParseToken(sentence.normalize(prev), tokenText, hasSpaceAfter));
  }
}
