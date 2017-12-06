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

package edu.umn.biomedicus.measures;

import edu.umn.biomedicus.common.TextIdentifiers;
import edu.umn.biomedicus.numbers.CombinedNumberDetector;
import edu.umn.biomedicus.numbers.NumberModel;
import edu.umn.biomedicus.numbers.NumberType;
import edu.umn.biomedicus.numbers.Numbers;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.LabeledText;
import edu.umn.nlpengine.Labeler;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;
import mockit.VerificationsInOrder;
import org.testng.annotations.Test;
import org.testng.collections.Maps;

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
  LabeledText labeledText;

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

    Map<String, LabeledText> labeledTextMap = Maps.newHashMap();
    labeledTextMap.put(TextIdentifiers.SYSTEM, labeledText);

    new Expectations() {{
      document.getLabeledTexts(); result = labeledTextMap;
      labeledText.labelIndex(Sentence.class); result = sentenceLabelIndex;
      labeledText.labeler(Number.class); result = numberLabeler;
      sentenceLabelIndex.iterator(); result = Collections.singletonList(sentenceLabel).iterator();
      labeledText.labelIndex(ParseToken.class); result = parseTokenLabelIndex;
      parseTokenLabelIndex.insideSpan(sentenceLabel); result = parseTokenLabelIndex;
      parseTokenLabelIndex.iterator(); result = parseTokenLabels.iterator();

      combinedNumberDetector.tryToken("25", 0, 2); result = false;
      combinedNumberDetector.tryToken("3", 3, 4); returns(true, false);
      combinedNumberDetector.finish(); result = true;

      combinedNumberDetector.getNumerator(); returns(new BigDecimal(25), new BigDecimal(3));
      combinedNumberDetector.getDenominator(); returns(new BigDecimal(1), new BigDecimal(1));
      combinedNumberDetector.getNumberType(); result = NumberType.DECIMAL; times = 2;
      combinedNumberDetector.getBegin(); returns(0, 3);
      combinedNumberDetector.getEnd(); returns(2, 4);
      combinedNumberDetector.getNumberType(); returns(NumberType.DECIMAL, NumberType.DECIMAL);

      combinedNumberDetector.getConsumedLastToken(); result = false;


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
