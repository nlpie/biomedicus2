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

import com.google.common.base.Splitter;
import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.types.semantics.ImmutableNumber;
import edu.umn.biomedicus.common.types.semantics.Number;
import edu.umn.biomedicus.common.types.semantics.NumberType;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DataLoader;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextView;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Detects and labels instances of numbers in text, either English numerals or in decimal numeral
 * system.
 *
 * @author Ben Knoll
 * @since 1.8.0
 */
public class NumberRecognizer implements DocumentProcessor {

  private final FractionAcceptor fractionAcceptor;

  private Labeler<Number> labeler;

  @Inject
  NumberRecognizer(FractionAcceptor fractionAcceptor) {
    this.fractionAcceptor = fractionAcceptor;
  }

  @Override
  public void process(@Nonnull Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<Sentence> sentenceLabelIndex = systemView.getLabelIndex(Sentence.class);
    LabelIndex<ParseToken> parseTokenLabelIndex = systemView.getLabelIndex(ParseToken.class);
    labeler = systemView.getLabeler(Number.class);

    for (Label<Sentence> sentenceLabel : sentenceLabelIndex) {
      extract(parseTokenLabelIndex.insideSpan(sentenceLabel));
    }
  }

  /**
   * Gets any numbers from the labels of parse tokens and labels them as such.
   *
   * @param labels labels of parse tokens
   * @throws BiomedicusException if there is an error labeling the text
   */
  void extract(Iterable<Label<ParseToken>> labels) throws BiomedicusException {
    for (Label<ParseToken> tokenLabel : labels) {
      String num = parseDecimal(tokenLabel.getValue().text());
      if (num != null) {
        labeler.value(ImmutableNumber.builder()
            .numerator(num)
            .denominator("1")
            .numberType(NumberType.DECIMAL)
            .build())
            .label(tokenLabel);
      } else if (fractionAcceptor.tryToken(tokenLabel)) {
        labelSeq();
      }
    }

    if (fractionAcceptor.finish()) {
      labelSeq();
    }
  }

  /**
   * Parses any numeric decimals from the token text
   *
   * @param text text to parse
   * @return {@link BigDecimal} string representation if there is a decimal, null otherwise
   */
  @Nullable
  String parseDecimal(String text) {
    char ch = text.charAt(0);

    StringBuilder digits;
    boolean negative = false;
    if (ch == '+') {
      digits = new StringBuilder();
    } else if (ch == '-') {
      digits = new StringBuilder();
      negative = true;
    } else if (Character.isDigit(ch)) {
      digits = new StringBuilder();
      digits.append(ch);
    } else {
      return null;
    }

    int period = -1;
    boolean percentage = false;
    for (int i = 1; i < text.length(); i++) {
      ch = text.charAt(i);
      if (ch == ',') {
        continue;
      }

      if (ch == '.') {
        period = digits.length();
      } else if (Character.isDigit(ch)) {
        digits.append(ch);
      } else if (i == text.length() - 1 && ch == '%') {
        percentage = true;
      } else {
        return null;
      }
    }

    if (digits.length() == 0) {
      return null;
    }

    BigDecimal value = BigDecimal.ZERO;
    BigDecimal ten = BigDecimal.valueOf(10);

    if (period != -1) {
      for (int i = 0; i < period; i++) {
        value = value.multiply(ten).add(new BigDecimal("" + digits.charAt(i)));
      }
      for (int i = period; i < digits.length(); i++) {
        value = value.add(new BigDecimal("" + digits.charAt(i))
            .divide(ten.pow(i - period + 1)));
      }
    } else {
      for (int i = 0; i < digits.length(); i++) {
        value = value.multiply(ten).add(new BigDecimal("" + digits.charAt(i)));
      }
    }
    if (negative) {
      value = value.negate();
    }
    if (percentage) {
      value = value.divide(new BigDecimal(100));
    }

    return value.toString();
  }

  void setLabeler(Labeler<Number> labeler) {
    this.labeler = labeler;
  }

  private void labelSeq() throws BiomedicusException {
    assert fractionAcceptor.numerator
        != null : "This method should only get called when value is nonnull";
    assert fractionAcceptor.numberType != null : "Number type should never be null at this point";
    if (fractionAcceptor.denominator == null) {
      labeler.value(ImmutableNumber.builder().numerator(fractionAcceptor.numerator.toString())
          .numberType(fractionAcceptor.numberType)
          .denominator(BigInteger.ONE.toString()).build())
          .label(Span.create(fractionAcceptor.begin, fractionAcceptor.end));
    } else {
      labeler.value(ImmutableNumber.builder().numerator(fractionAcceptor.numerator.toString())
          .denominator(fractionAcceptor.denominator.toString())
          .numberType(fractionAcceptor.numberType)
          .build())
          .label(Span.create(fractionAcceptor.begin, fractionAcceptor.end));
    }
    fractionAcceptor.reset();
  }

  enum BasicNumberType {
    DECADE,
    TEEN,
    UNIT,
    MAGNITUDE
  }

  @Singleton
  @ProvidedBy(NumberModelLoader.class)
  static class NumberModel {

    private final Map<String, NumberDefinition> numbers;

    private final Map<String, NumberDefinition> ordinals;

    private final Map<String, NumberDefinition> denominators;

    private NumberModel(Map<String, NumberDefinition> numbers,
        Map<String, NumberDefinition> ordinals,
        Map<String, NumberDefinition> denominators) {
      this.numbers = numbers;
      this.ordinals = ordinals;
      this.denominators = denominators;
    }

    @Nullable
    NumberDefinition getNumberDefinition(String word) {
      return numbers.get(word.toLowerCase());
    }

    @Nullable
    NumberDefinition getOrdinal(String word) {
      return ordinals.get(word.toLowerCase());
    }

    @Nullable
    NumberDefinition getDenominator(String word) {
      return denominators.get(word.toLowerCase());
    }
  }

  @Singleton
  static class NumberModelLoader extends DataLoader<NumberModel> {

    private final Path nrnumPath;

    private final Path nrvarPath;

    @Inject
    NumberModelLoader(@Setting("measures.numbers.nrnumPath") Path nrnumPath,
        @Setting("measures.numbers.nrvarPath") Path nrvarPath) {
      this.nrnumPath = nrnumPath;
      this.nrvarPath = nrvarPath;
    }

    @Nonnull
    @Override
    protected NumberModel loadModel() throws BiomedicusException {
      Splitter splitter = Splitter.on("|");
      try {
        Map<String, NumberDefinition> numbers = new HashMap<>();

        Files.lines(nrnumPath).forEach(line -> {
          Iterator<String> it = splitter.split(line).iterator();

          it.next(); // discard
          String word = it.next();
          BasicNumberType basicNumberType = typeFromString(it.next());

          int value;
          if (basicNumberType == BasicNumberType.MAGNITUDE) {
            it.next();
            it.next();
            value = Integer.valueOf(it.next());
          } else {
            value = Integer.valueOf(it.next());
          }

          NumberDefinition numberDefinition = new NumberDefinition(value, basicNumberType);
          numbers.put(word, numberDefinition);
        });

        Map<String, NumberDefinition> ordinals = new HashMap<>();
        Map<String, NumberDefinition> denominators = new HashMap<>();

        Files.lines(nrvarPath).forEach(line -> {
          Iterator<String> it = splitter.split(line).iterator();
          String word = it.next();
          it.next(); // discard
          String types = it.next();
          String norm = it.next();
          NumberDefinition numberDefinition = numbers.get(norm);
          if (types.contains("ordinal")) {
            ordinals.put(word, numberDefinition);
          }
          if (types.contains("denominator")) {
            denominators.put(word, numberDefinition);
          }
        });

        return new NumberModel(numbers, ordinals, denominators);
      } catch (IOException e) {
        throw new BiomedicusException(e);
      }
    }

    private BasicNumberType typeFromString(String st) {
      switch (st) {
        case "unit":
          return BasicNumberType.UNIT;
        case "teen":
          return BasicNumberType.TEEN;
        case "decade":
          return BasicNumberType.DECADE;
        case "magnitude":
          return BasicNumberType.MAGNITUDE;
      }
      throw new IllegalStateException("Unrecognized number type: " + st);
    }
  }

  /**
   * Information about the number that a specific numeral word represents.
   */
  static class NumberDefinition {

    final int value;

    final BasicNumberType basicNumberType;

    NumberDefinition(int value, BasicNumberType basicNumberType) {
      this.value = value;
      this.basicNumberType = basicNumberType;
    }
  }

  /**
   * Detects basic cardinal numbers.
   */
  static class BasicNumberAcceptor {

    private enum State {
      DECADE,
      DECADE_HYPHEN,
      NONE
    }

    private enum Type {
      UNIT,
      TEEN,
      DECADE,
      DECADE_UNIT
    }

    final NumberModel numberModel;

    int value;

    int begin;

    int end;

    State state;

    Type type;

    boolean consumedLastToken;

    boolean canBeDenominator;

    boolean isDenominator;

    boolean isOrdinal;


    @Inject
    BasicNumberAcceptor(NumberModel numberModel) {
      this.numberModel = numberModel;
      reset();
    }

    void reset() {
      value = -1;
      begin = -1;
      end = -1;
      state = State.NONE;
      type = null;
      consumedLastToken = false;
      canBeDenominator = false;
      isDenominator = false;
      isOrdinal = false;
    }

    boolean tryToken(Label<ParseToken> parseTokenLabel) {
      String word = parseTokenLabel.getValue().text();

      NumberDefinition numberDefinition = null;
      if (canBeDenominator) {
        numberDefinition = numberModel.getDenominator(word);
        if (numberDefinition != null) {
          isDenominator = true;
        }
      }

      if (numberDefinition == null) {
        numberDefinition = numberModel.getNumberDefinition(word);
      }

      if (numberDefinition == null) {
        numberDefinition = numberModel.getOrdinal(word);
        if (numberDefinition != null) {
          isOrdinal = true;
        }
      }

      switch (state) {
        case NONE:
          if (numberDefinition != null) {
            switch (numberDefinition.basicNumberType) {
              case TEEN:
                value = numberDefinition.value;
                begin = parseTokenLabel.getBegin();
                end = parseTokenLabel.getEnd();
                type = Type.TEEN;
                consumedLastToken = true;
                return true;
              case UNIT:
                value = numberDefinition.value;
                begin = parseTokenLabel.getBegin();
                end = parseTokenLabel.getEnd();
                type = Type.UNIT;
                consumedLastToken = true;
                return true;
              case DECADE:
                state = State.DECADE;
                value = numberDefinition.value;
                begin = parseTokenLabel.getBegin();
                end = parseTokenLabel.getEnd();
                type = Type.DECADE;
                if (isDenominator || isOrdinal) {
                  consumedLastToken = true;
                  return true;
                }
                break;
            }
          }
          break;
        case DECADE:
          if ("-".equals(word)) {
            state = State.DECADE_HYPHEN;
            return false;
          }
        case DECADE_HYPHEN:
          if (numberDefinition != null) {
            if (numberDefinition.basicNumberType == BasicNumberType.UNIT) {
              value = value + numberDefinition.value;
              end = parseTokenLabel.getEnd();
              type = Type.DECADE_UNIT;
              consumedLastToken = true;
            }
          }
          return true;
      }

      return false;
    }

    boolean finish() {
      switch (state) {
        case DECADE_HYPHEN:
        case DECADE:
          return true;
      }
      return false;
    }
  }

  /**
   * Finds any english numeral numerators or denominators.
   */
  static class NumberAcceptor {

    enum State {
      NONE,
      HAS_BASIC,
      RANK_01,
      PAST_FIRST_PART,
      POST_MAGNITUDE
    }


    private final NumberModel numberModel;

    private final BasicNumberAcceptor basicNumberAcceptor;

    private State state;

    @Nullable
    BigInteger value;

    private int valueBuilder = 0;

    int begin = -1;

    int end = -1;

    private boolean canBeDenominator;

    boolean isDenominator;

    boolean isOrdinal;

    boolean consumedLastWord;

    @Inject
    NumberAcceptor(NumberModel numberModel, BasicNumberAcceptor basicNumberAcceptor) {
      this.basicNumberAcceptor = basicNumberAcceptor;
      this.numberModel = numberModel;
      reset();
    }

    private void reset() {
      state = State.NONE;
      value = null;
      valueBuilder = -1;
      begin = -1;
      end = -1;
      consumedLastWord = false;
      canBeDenominator = false;
      isDenominator = false;
      isOrdinal = false;

      basicNumberAcceptor.reset();
    }

    boolean tryToken(Label<ParseToken> parseTokenLabel) {
      String word = parseTokenLabel.getValue().text();
      NumberDefinition numberDefinition = numberModel.getNumberDefinition(word);

      switch (state) {
        case NONE:
          if (value != null && "and".equalsIgnoreCase(word)) {
            return false;
          }

          if (basicNumberAcceptor.tryToken(parseTokenLabel)) {
            state = State.HAS_BASIC;
            if (value == null) {
              begin = basicNumberAcceptor.begin;
            }
            end = basicNumberAcceptor.end;
            valueBuilder = basicNumberAcceptor.value;
            if (basicNumberAcceptor.isDenominator || basicNumberAcceptor.isOrdinal) {
              value = BigInteger.valueOf(valueBuilder);
              return true;
            }
          } else if (value != null) {
            break;
          }
          return false;
        case HAS_BASIC:
          if ("hundred".equalsIgnoreCase(word)) {
            state = State.RANK_01;
            valueBuilder = valueBuilder * 100;
            end = parseTokenLabel.getEnd();
            basicNumberAcceptor.reset();
            return false;
          }
          if ("hundredth".equalsIgnoreCase(word)) {
            if (canBeDenominator) {
              isDenominator = true;
            } else {
              isOrdinal = true;
            }
            valueBuilder = valueBuilder * 100;
            end = parseTokenLabel.getEnd();
            basicNumberAcceptor.reset();
            value = BigInteger.valueOf(valueBuilder);
            return true;
          }
          if ("hundredths".equalsIgnoreCase(word)) {
            isDenominator = true;
            valueBuilder = valueBuilder * 100;
            end = parseTokenLabel.getEnd();
            basicNumberAcceptor.reset();
            value = BigInteger.valueOf(valueBuilder);
            return true;
          }
          break;
        case RANK_01:
          if ("and".equalsIgnoreCase(word)) {
            return false;
          }

          if (basicNumberAcceptor.state != BasicNumberAcceptor.State.NONE) {
            if (basicNumberAcceptor.tryToken(parseTokenLabel)) {
              valueBuilder += basicNumberAcceptor.value;
              end = basicNumberAcceptor.end;
              state = State.PAST_FIRST_PART;
            }
          } else {
            basicNumberAcceptor.tryToken(parseTokenLabel);
            if (basicNumberAcceptor.state == BasicNumberAcceptor.State.NONE) {
              break;
            }
          }
          return false;
        default:
      }

      // if we fall through to here look for a magnitude

      if (numberDefinition != null) {
        if (BasicNumberType.MAGNITUDE == numberDefinition.basicNumberType) {
          if (value != null) {
            value = value.add(BigInteger.valueOf(1000).pow(numberDefinition.value)
                .multiply(BigInteger.valueOf(valueBuilder)));
          } else {
            value = BigInteger.valueOf(1000).pow(numberDefinition.value)
                .multiply(BigInteger.valueOf(valueBuilder));
          }
          valueBuilder = 0;
          end = parseTokenLabel.getEnd();
          state = State.NONE;
          return false;
        }
      }

      if (canBeDenominator) {
        numberDefinition = numberModel.getDenominator(word);
        if (numberDefinition != null
            && numberDefinition.basicNumberType == BasicNumberType.MAGNITUDE) {
          if (value != null) {
            value = value.add(BigInteger.valueOf(1000).pow(numberDefinition.value)
                .multiply(BigInteger.valueOf(valueBuilder)));
          } else {
            value = BigInteger.valueOf(1000).pow(numberDefinition.value)
                .multiply(BigInteger.valueOf(valueBuilder));
          }
          end = parseTokenLabel.getEnd();
          return true;
        }
      }

      // if we fall through to here we've reached the end of the number

      if (value == null) {
        value = BigInteger.valueOf(valueBuilder);
      } else {
        value = value.add(BigInteger.valueOf(valueBuilder));
      }

      return true;
    }

    boolean finish() {
      switch (state) {
        case NONE:
          if (basicNumberAcceptor.finish()) {
            state = State.HAS_BASIC;
            if (value == null) {
              begin = basicNumberAcceptor.begin;
            }
            end = basicNumberAcceptor.end;
            valueBuilder = basicNumberAcceptor.value;
          }
          break;
        case HAS_BASIC:
        case RANK_01:
          if (basicNumberAcceptor.state != BasicNumberAcceptor.State.NONE) {
            if (basicNumberAcceptor.finish()) {
              valueBuilder += basicNumberAcceptor.value;
              end = basicNumberAcceptor.end;
              state = State.PAST_FIRST_PART;
            }
          }
        default:
      }
      if (value == null) {
        if (state == State.NONE) {
          return false;
        }
        value = BigInteger.valueOf(valueBuilder);
      } else {
        value = value.add(BigInteger.valueOf(valueBuilder));
      }

      return true;
    }

    boolean inProgress() {
      return state != State.NONE || basicNumberAcceptor.state != BasicNumberAcceptor.State.NONE;
    }

    void setDenominator() {
      this.canBeDenominator = true;
      basicNumberAcceptor.canBeDenominator = true;
    }
  }

  /**
   * Detects numerator and denominator english numeral fractions.
   */
  static class FractionAcceptor {

    private final NumberAcceptor numberAcceptor;

    @Nullable
    BigInteger numerator;

    @Nullable
    BigInteger denominator;

    int begin;

    int end;

    @Nullable
    NumberType numberType;

    int andHalf = 0;

    @Inject
    FractionAcceptor(NumberAcceptor numberAcceptor) {
      this.numberAcceptor = numberAcceptor;
    }

    private void reset() {
      numerator = null;
      denominator = null;
      numberAcceptor.reset();
      numberType = null;
      andHalf = 0;
    }

    @Nullable
    BigDecimal getValue() {
      if (numerator == null) {
        return BigDecimal.ZERO;
      }

      if (denominator == null) {
        return new BigDecimal(numerator);
      }

      BigDecimal first = new BigDecimal(numerator);
      BigDecimal second = new BigDecimal(denominator);
      return first.divide(second, BigDecimal.ROUND_HALF_UP);
    }

    boolean tryToken(Label<ParseToken> parseTokenLabel) {
      if (numerator == null) {
        if (numberAcceptor.tryToken(parseTokenLabel)) {
          numerator = numberAcceptor.value;
          begin = numberAcceptor.begin;
          end = numberAcceptor.end;
          numberAcceptor.reset();
          numberAcceptor.setDenominator();

          if (numberAcceptor.isOrdinal) {
            numberType = NumberType.ORDINAL;
            return true;
          }

          numberType = NumberType.CARDINAL;

          if (numberAcceptor.consumedLastWord) {
            return false;
          }
        } else {
          return false;
        }
      }

      String word = parseTokenLabel.getValue().text();

      if (andHalf == 1 && word.equalsIgnoreCase("a")) {
        andHalf = 2;
      } else if (andHalf == 2 && word.equalsIgnoreCase("half")) {
        denominator = BigInteger.valueOf(2);
        numerator = numerator.multiply(denominator).add(BigInteger.ONE);
        numberType = NumberType.FRACTION;
        end = parseTokenLabel.getEnd();
        return true;
      } else if (word.equalsIgnoreCase("and")) {
        andHalf = 1;
      } else if (numberAcceptor.tryToken(parseTokenLabel)) {
        denominator = numberAcceptor.value;
        end = numberAcceptor.end;
        numberType = NumberType.FRACTION;
        return true;
      } else {
        return !numberAcceptor.inProgress();
      }
      return false;
    }

    boolean finish() {
      if (numerator == null) {
        if (numberAcceptor.finish()) {
          numerator = numberAcceptor.value;
          return true;
        }
      } else {
        if (numberAcceptor.finish()) {
          denominator = numberAcceptor.value;
          return true;
        }
      }
      return false;
    }
  }
}
