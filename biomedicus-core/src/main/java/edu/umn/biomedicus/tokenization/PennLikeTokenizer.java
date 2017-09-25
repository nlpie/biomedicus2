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

import com.google.inject.Inject;
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
import edu.umn.biomedicus.measures.UnitRecognizer;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public final class PennLikeTokenizer implements DocumentProcessor {

  private static final Pattern NUMBER_WORD = Pattern.compile(".*?[0-9]++(?<suffix>[\\p{Alpha}]++)");

  private final UnitRecognizer unitRecognizer;

  @Nullable
  private Labeler<ParseToken> parseTokenLabeler = null;

  @Nullable
  private CharSequence text = null;

  @Nullable
  private Span prev = null;

  @Inject
  public PennLikeTokenizer(UnitRecognizer unitRecognizer) {
    this.unitRecognizer = unitRecognizer;
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<Sentence> sentenceLabelIndex = systemView.getLabelIndex(Sentence.class);
    parseTokenLabeler = systemView.getLabeler(ParseToken.class);

    for (Label<Sentence> sentence : sentenceLabelIndex) {
      text = sentence.getCovered(systemView.getText());

      Iterator<Span> iterator = PennLikePhraseTokenizer.tokenizeSentence(text).iterator();

      prev = null;

      while (iterator.hasNext()) {
        Span current = iterator.next();
        if (current.length() == 0) {
          continue;
        }
        if (prev != null) {
          boolean hasSpaceAfter = prev.getEnd() != current.getBegin();
          labelToken(sentence, hasSpaceAfter);
        }
        prev = current;
      }
      if (prev != null) {
        labelToken(sentence, false);
      }
    }
  }

  private void labelToken(Label<Sentence> sentence, boolean hasSpaceAfter)
      throws BiomedicusException {
    assert parseTokenLabeler != null : "this should never be null when the function is called";
    assert text != null : "this should never be null when the function is called";
    assert prev != null : "this is checked before the function is called";
    String tokenText = text.subSequence(prev.getBegin(), prev.getEnd()).toString();

    Matcher matcher = NUMBER_WORD.matcher(tokenText);
    if (matcher.matches()) {
      String suffix = matcher.group("suffix");
      if (suffix != null && unitRecognizer.isUnitOfMeasureWord(suffix)) {
        int numBegin = prev.getBegin();
        int numEnd = prev.getEnd() - suffix.length();
        String number = text.subSequence(numBegin, numEnd).toString();
        parseTokenLabeler.value(ImmutableParseToken.builder()
            .text(number)
            .hasSpaceAfter(false)
            .build())
            .label(sentence.derelativize(numBegin), sentence.derelativize(numEnd));
        parseTokenLabeler.value(ImmutableParseToken.builder()
            .text(suffix)
            .hasSpaceAfter(hasSpaceAfter)
            .build())
            .label(sentence.derelativize(numEnd), sentence.derelativize(prev.getEnd()));

        return;
      }

    }

    parseTokenLabeler.value(ImmutableParseToken.builder()
        .text(tokenText)
        .hasSpaceAfter(hasSpaceAfter)
        .build())
        .label(sentence.derelativize(prev));
  }
}
