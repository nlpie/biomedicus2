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

import edu.umn.biomedicus.common.TextIdentifiers;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.LabeledText;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import edu.umn.nlpengine.Span;
import java.util.Iterator;
import javax.annotation.Nullable;

public final class PennLikeTokenizer implements DocumentProcessor {

  @Nullable
  private Labeler<ParseToken> parseTokenLabeler = null;

  @Nullable
  private CharSequence text = null;

  @Nullable
  private Span prev = null;

  @Override
  public void process(Document document) throws BiomedicusException {
    LabeledText systemView = TextIdentifiers.getSystemLabeledText(document);

    LabelIndex<Sentence> sentenceLabelIndex = systemView.labelIndex(Sentence.class);
    parseTokenLabeler = systemView.labeler(ParseToken.class);

    for (Sentence sentence : sentenceLabelIndex) {
      text = sentence.coveredText(systemView.getText());

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

  private void labelToken(Sentence sentence, boolean hasSpaceAfter)
      throws BiomedicusException {
    assert parseTokenLabeler != null : "this should never be null when the function is called";
    assert text != null : "this should never be null when the function is called";
    assert prev != null : "this is checked before the function is called";
    String tokenText = text.subSequence(prev.getStartIndex(), prev.getEndIndex()).toString();

    parseTokenLabeler.add(new ParseToken(sentence.normalize(prev), tokenText, hasSpaceAfter));
  }
}
