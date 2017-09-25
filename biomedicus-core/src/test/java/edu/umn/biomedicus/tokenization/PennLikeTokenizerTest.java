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
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.framework.store.ValueLabeler;
import edu.umn.biomedicus.measures.UnitRecognizer;
import java.util.Iterator;
import java.util.Optional;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.testng.annotations.Test;

public class PennLikeTokenizerTest {
  @Tested
  PennLikeTokenizer tokenizer;

  @Injectable
  UnitRecognizer unitRecognizer;

  @Mocked
  Document document;

  @Mocked
  TextView textView;

  @Mocked
  LabelIndex<Sentence> sentenceLabelIndex;

  @Mocked
  Labeler<ParseToken> parseTokenLabeler;

  @Mocked
  ValueLabeler valueLabeler;

  @Mocked
  Iterator<Label<Sentence>> sentenceLabelIt;

  @Test
  public void testSplitsUnits() throws Exception {
    String text = "2.5cm";
    Label<Sentence> sentenceLabel = Label.create(Span.create(0, 5), new Sentence());

    new Expectations() {{
      document.getTextView(StandardViews.SYSTEM); result = Optional.of(textView);
      textView.getText(); result = text;
      textView.getLabelIndex(Sentence.class); result = sentenceLabelIndex;
      sentenceLabelIndex.iterator(); result = sentenceLabelIt;
      sentenceLabelIt.hasNext(); returns(true, false);
      sentenceLabelIt.next(); result = sentenceLabel;
      unitRecognizer.isUnitOfMeasureWord("cm"); result = true;
      parseTokenLabeler
          .value(ImmutableParseToken.builder().text("2.5").hasSpaceAfter(false).build()); result = valueLabeler;
      parseTokenLabeler.value(ImmutableParseToken.builder().text("cm").hasSpaceAfter(false).build()); result = valueLabeler;

    }};

    tokenizer.process(document);

    new Verifications() {{
      valueLabeler.label(0, 3);
      valueLabeler.label(3, 5);
    }};
  }
}
