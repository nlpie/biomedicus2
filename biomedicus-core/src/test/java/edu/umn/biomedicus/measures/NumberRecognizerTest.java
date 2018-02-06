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

package edu.umn.biomedicus.measures;

import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.types.text.ImmutableParseToken;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.framework.store.ValueLabeler;
import edu.umn.biomedicus.numbers.CombinedNumberDetector;
import edu.umn.biomedicus.numbers.NumberModel;
import edu.umn.biomedicus.numbers.NumberResult;
import edu.umn.biomedicus.numbers.NumberType;
import edu.umn.biomedicus.numbers.Numbers;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;
import mockit.VerificationsInOrder;
import org.testng.annotations.Test;

public class NumberRecognizerTest {

  @Tested
  NumberRecognizer numberRecognizer;

  @Injectable
  NumberModel numberModel;



  @Injectable("false")
  boolean includePercent;

  @Injectable("false")
  boolean includeFractions;

  @Mocked
  Document document;

  @Mocked
  TextView textView;

  @Mocked
  LabelIndex<Sentence> sentenceLabelIndex;

  @Mocked
  LabelIndex<ParseToken> parseTokenLabelIndex;

  @Mocked
  CombinedNumberDetector combinedNumberDetector;

  @Mocked
  Labeler<Number> numberLabeler;

  @Mocked
  ValueLabeler valueLabeler;

  @Test
  public void testUnconsumedNumber() throws Exception {
    Label<Sentence> sentenceLabel = Label.create(0, 4, new Sentence());

    List<Label<ParseToken>> parseTokenLabels = Arrays.asList(
        Label.create(0, 2,
            ImmutableParseToken.builder().text("25").hasSpaceAfter(true).build()),
        Label.create(3, 4,
            ImmutableParseToken.builder().text("3").hasSpaceAfter(true).build())
    );

    new MockUp<Numbers>() {
      @Mock
      CombinedNumberDetector createNumberDetector(NumberModel numberModel,
          boolean includePercent, boolean includeFractions) {
        return combinedNumberDetector;
      }
    };

    NumberResult twentyFiveResult = new NumberResult(0, 2, BigDecimal.valueOf(25), BigDecimal.ONE, NumberType.DECIMAL);
    NumberResult threeResult = new NumberResult(3, 4, BigDecimal.valueOf(3), BigDecimal.ONE, NumberType.DECIMAL);

    new Expectations() {{
      document.getTextView(StandardViews.SYSTEM); result = Optional.of(textView);
      textView.getLabelIndex(Sentence.class); result = sentenceLabelIndex;
      textView.getLabeler(Number.class); result = numberLabeler;
      sentenceLabelIndex.iterator(); result = Collections.singletonList(sentenceLabel).iterator();
      textView.getLabelIndex(ParseToken.class); result = parseTokenLabelIndex;
      parseTokenLabelIndex.insideSpan(sentenceLabel); result = parseTokenLabelIndex;
      parseTokenLabelIndex.iterator(); result = parseTokenLabels.iterator();

      combinedNumberDetector.tryToken("25", 0, 2); result = Collections.emptyList();
      combinedNumberDetector.tryToken("3", 3, 4); result = Collections.singletonList(twentyFiveResult);
      combinedNumberDetector.finish(); result = Collections.singletonList(threeResult);

      numberLabeler.value((Number) any); result = valueLabeler;
    }};

    numberRecognizer.process(document);

    new VerificationsInOrder() {{
      numberLabeler.value(ImmutableNumber.builder().numerator(new BigDecimal(25).toString())
          .denominator(BigDecimal.ONE.toString()).numberType(NumberType.DECIMAL)
          .build());
      valueLabeler.label(0, 2);
      numberLabeler.value(ImmutableNumber.builder().numerator(new BigDecimal(3).toString())
          .denominator(BigDecimal.ONE.toString()).numberType(NumberType.DECIMAL)
          .build());
      valueLabeler.label(3, 4);
    }};
  }
}
