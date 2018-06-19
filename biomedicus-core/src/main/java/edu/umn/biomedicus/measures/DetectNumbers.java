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

import edu.umn.biomedicus.annotations.ComponentSetting;
import edu.umn.biomedicus.numbers.CombinedNumberDetector;
import edu.umn.biomedicus.numbers.NumberModel;
import edu.umn.biomedicus.numbers.NumberResult;
import edu.umn.biomedicus.numbers.NumberType;
import edu.umn.biomedicus.numbers.Numbers;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.DocumentTask;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import java.math.BigDecimal;
import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Detects and labels instances of numbers in text, either English numerals or in decimal numeral
 * system.
 *
 * @author Ben Knoll
 * @since 1.8.0
 */
public class DetectNumbers implements DocumentTask {

  private final CombinedNumberDetector numberDetector;

  private Labeler<Number> labeler;

  @Inject
  DetectNumbers(
      NumberModel numberModel,
      @ComponentSetting("measures.numbers.includePercent") boolean includePercent,
      @ComponentSetting("measures.numbers.includeFractions") boolean includeFractions
  ) {
    numberDetector = Numbers.createNumberDetector(numberModel);
  }

  void setLabeler(Labeler<Number> labeler) {
    this.labeler = labeler;
  }

  @Override
  public void run(@Nonnull Document document) {
    LabelIndex<Sentence> sentenceLabelIndex = document.labelIndex(Sentence.class);
    LabelIndex<ParseToken> parseTokenLabelIndex = document.labelIndex(ParseToken.class);
    labeler = document.labeler(Number.class);

    for (Sentence sentenceLabel : sentenceLabelIndex) {
      extract(parseTokenLabelIndex.inside(sentenceLabel));
    }
  }

  void extract(Iterable<ParseToken> labels) {
    for (ParseToken tokenLabel : labels) {
      String text = tokenLabel.getText();
      int begin = tokenLabel.getStartIndex();
      int end = tokenLabel.getEndIndex();
      for (NumberResult numberResult : numberDetector.tryToken(text, begin, end)) {
        labelSeq(numberResult);
      }
    }

    for (NumberResult numberResult : numberDetector.finish()) {
      labelSeq(numberResult);
    }

  }

  private void labelSeq(NumberResult numberResult) {
    BigDecimal numerator = numberResult.getNumerator();
    NumberType numberType = numberResult.getNumberType();
    BigDecimal denominator = numberResult.getDenominator();
    if (denominator == null) {
      labeler.add(new Number(numberResult.getBegin(), numberResult.getEnd(), numerator.toString(),
          BigDecimal.ONE.toString(), numberType));
    } else {
      labeler.add(new Number(numberResult.getBegin(), numberResult.getEnd(), numerator.toString(),
          denominator.toString(), numberType));
    }
  }
}
