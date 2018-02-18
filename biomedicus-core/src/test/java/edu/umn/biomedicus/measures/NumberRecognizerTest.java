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

import edu.umn.biomedicus.numbers.CombinedNumberDetector;
import edu.umn.biomedicus.numbers.NumberModel;
import edu.umn.biomedicus.numbers.NumberResult;
import edu.umn.biomedicus.numbers.NumberType;
import edu.umn.biomedicus.numbers.Numbers;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
  LabelIndex<Sentence> sentenceLabelIndex;

  @Mocked
  LabelIndex<ParseToken> parseTokenLabelIndex;

  @Mocked
  CombinedNumberDetector combinedNumberDetector;

  @Mocked
  Labeler<Number> numberLabeler;

  @Test
  public void testUnconsumedNumber() throws Exception {
    Sentence sentenceLabel = new Sentence(0, 4);

    List<ParseToken> parseTokenLabels = Arrays.asList(
        new ParseToken(0, 2, "25", true),
        new ParseToken(3, 4, "3", true)
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
      document.labelIndex(Sentence.class); result = sentenceLabelIndex;
      document.labeler(Number.class); result = numberLabeler;
      sentenceLabelIndex.iterator(); result = Collections.singletonList(sentenceLabel).iterator();
      document.labelIndex(ParseToken.class); result = parseTokenLabelIndex;
      parseTokenLabelIndex.insideSpan(sentenceLabel); result = parseTokenLabelIndex;
      parseTokenLabelIndex.iterator(); result = parseTokenLabels.iterator();

      combinedNumberDetector.tryToken("25", 0, 2); result = Collections.emptyList();
      combinedNumberDetector.tryToken("3", 3, 4); result = Collections.singletonList(twentyFiveResult);
      combinedNumberDetector.finish(); result = Collections.singletonList(threeResult);
    }};

    numberRecognizer.process(document);

    new VerificationsInOrder() {{
      numberLabeler.add(new Number(0, 2, new BigDecimal(25).toString(),
          BigDecimal.ONE.toString(), NumberType.DECIMAL));
      numberLabeler.add(new Number(3, 4, new BigDecimal(3).toString(),
          BigDecimal.ONE.toString(), NumberType.DECIMAL));
    }};
  }
}
