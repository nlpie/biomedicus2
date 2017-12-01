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

import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.numbers.CombinedNumberDetector;
import edu.umn.biomedicus.numbers.NumberModel;
import edu.umn.biomedicus.numbers.NumberType;
import edu.umn.biomedicus.numbers.Numbers;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Detects and labels instances of numbers in text, either English numerals or in decimal numeral
 * system.
 *
 * @author Ben Knoll
 * @since 1.8.0
 */
public class NumberRecognizer implements DocumentProcessor {

  private final CombinedNumberDetector numberDetector;

  private Labeler<Number> labeler;

  @Inject
  NumberRecognizer(
      NumberModel numberModel,
      @ProcessorSetting("measures.numbers.includePercent") boolean includePercent,
      @ProcessorSetting("measures.numbers.includeFractions") boolean includeFractions
  ) {
    numberDetector = Numbers.createNumberDetector(numberModel, includePercent, includeFractions);
  }

  void setLabeler(Labeler<Number> labeler) {
    this.labeler = labeler;
  }

  @Override
  public void process(@Nonnull Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<Sentence> sentenceLabelIndex = systemView.getLabelIndex(Sentence.class);
    LabelIndex<ParseToken> parseTokenLabelIndex = systemView.getLabelIndex(ParseToken.class);
    labeler = systemView.getLabeler(Number.class);

    for (Sentence sentence : sentenceLabelIndex) {
      extract(parseTokenLabelIndex.insideSpan(sentence));
    }
  }

  /**
   * Gets any numbers from the labels of parse tokens and labels them as such.
   *
   * @param labels labels of parse tokens
   * @throws BiomedicusException if there is an error labeling the text
   */
  void extract(Iterable<ParseToken> labels) throws BiomedicusException {
    Iterator<ParseToken> iterator = labels.iterator();
    ParseToken tokenLabel = null;
    while (true) {
      if (tokenLabel == null) {
        if (!iterator.hasNext()) {
          break;
        }
        tokenLabel = iterator.next();
      }

      String text = tokenLabel.getText();
      int begin = tokenLabel.getStartIndex();
      int end = tokenLabel.getEndIndex();
      if (numberDetector.tryToken(text, begin, end)) {
        labelSeq();
        if (!numberDetector.getConsumedLastToken()) {
          continue;
        }
      }
      tokenLabel = null;
    }

    if (numberDetector.finish()) {
      labelSeq();
    }

  }

  private void labelSeq() throws BiomedicusException {
    BigDecimal numerator = numberDetector.getNumerator();
    assert numerator != null
        : "This method should only get called when value is nonnull";
    NumberType numberType = numberDetector.getNumberType();
    assert numberType != null
        : "Number type should never be null at this point";
    BigDecimal denominator = numberDetector.getDenominator();
    if (denominator == null) {
      labeler.add(
          new Number(numberDetector.getBegin(), numberDetector.getEnd(), numerator.toString(),
              BigInteger.ONE.toString(), numberType)
      );
    } else {
      labeler.add(
          new Number(numberDetector.getBegin(), numberDetector.getEnd(), numerator.toString(),
              denominator.toString(), numberType)
      );
    }
  }
}
