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

package edu.umn.biomedicus.tokenization;

import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.types.text.ImmutableParseToken;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextView;
import java.util.Iterator;

public final class PennLikeTokenizer implements DocumentProcessor {

  @Override
  public void process(Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<Sentence> sentenceLabelIndex = systemView.getLabelIndex(Sentence.class);
    Labeler<ParseToken> parseTokenLabeler = systemView.getLabeler(ParseToken.class);

    for (Label<Sentence> sentence : sentenceLabelIndex) {
      CharSequence text = sentence.getCovered(systemView.getText());

      Iterator<Span> iterator = PennLikePhraseTokenizer
          .tokenizeSentence(text).iterator();

      Span last = null;
      while (iterator.hasNext()) {
        Span current = iterator.next();
        if (current.length() == 0) {
          continue;
        }
        if (last != null) {
          String tokenText = text
              .subSequence(last.getBegin(), last.getEnd())
              .toString();
          boolean hasSpaceAfter = last.getEnd() != current.getBegin();
          parseTokenLabeler
              .value(ImmutableParseToken.builder()
                  .text(tokenText)
                  .hasSpaceAfter(hasSpaceAfter)
                  .build())
              .label(sentence.derelativize(last));
        }
        last = current;
      }
      if (last != null) {
        Span derelativized = sentence.derelativize(last);
        String tokenText = text
            .subSequence(last.getBegin(), last.getEnd()).toString();
        parseTokenLabeler
            .value(ImmutableParseToken.builder()
                .text(tokenText)
                .hasSpaceAfter(false)
                .build())
            .label(derelativized);
      }
    }
  }
}
