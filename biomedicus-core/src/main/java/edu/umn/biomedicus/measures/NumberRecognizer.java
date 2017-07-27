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
import edu.umn.biomedicus.common.types.semantics.ImmutableNumber;
import edu.umn.biomedicus.common.types.semantics.Number;
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
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

public class NumberRecognizer implements DocumentProcessor {

  private static final NumberFormat numberFormat = NumberFormat.getNumberInstance();

  private final Sequence sequence;
  private Labeler<Number> labeler;

  @Inject
  public NumberRecognizer(Sequence sequence) {
    this.sequence = sequence;
  }

  @Override
  public void process(@Nonnull Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);


    LabelIndex<Sentence> sentenceLabelIndex = systemView.getLabelIndex(Sentence.class);
    LabelIndex<ParseToken> parseTokenLabelIndex = systemView.getLabelIndex(ParseToken.class);
    labeler = systemView.getLabeler(Number.class);

    for (Label<Sentence> sentenceLabel : sentenceLabelIndex) {
      for (Label<ParseToken> tokenLabel : parseTokenLabelIndex.insideSpan(sentenceLabel)) {
        try {
          java.lang.Number number = numberFormat.parse(tokenLabel.getValue().text());
          sequence.reset();
          labeler.value(ImmutableNumber.builder().value(number.toString()).build()).label(tokenLabel);
        } catch (ParseException e) {
          if (sequence.tryToken(tokenLabel)) {
            labelSeq();
          }
        }

      }

      if (sequence.finish()) {
        labelSeq();
      }

      sequence.reset();
    }
  }

  private void labelSeq() throws BiomedicusException {
    labeler.value(ImmutableNumber.builder().value(sequence.value.toString()).build())
        .label(Span.create(sequence.begin, sequence.end));
  }

  public enum NumberType {
    DECADE,
    TEEN,
    UNIT,
    MAGNITUDE
  }

  @Singleton
  public static class NumberModel {

    private Map<String, NumberDefinition> numbers;

    public Optional<NumberDefinition> getNumberDefinition(String word) {
      return Optional.ofNullable(numbers.get(word));
    }
  }

  public static class NumberDefinition {

    final int value;

    final boolean plural;

    final boolean singular;

    final NumberType numberType;

    public NumberDefinition(int value, boolean plural, boolean singular, NumberType numberType) {
      this.value = value;
      this.plural = plural;
      this.singular = singular;
      this.numberType = numberType;
    }
  }


  private enum BasicState {
    DECADE,
    DECADE_HYPHEN,
    NONE
  }

  enum BasicType {
    UNIT,
    TEEN,
    DECADE,
    DECADE_UNIT
  }


  static class Basic {

    final NumberModel numberModel;

    int value;

    int begin;

    int end;

    BasicState state;

    BasicType type;

    @Inject
    Basic(NumberModel numberModel) {
      this.numberModel = numberModel;
      reset();
    }

    void reset() {
      value = -1;
      begin = -1;
      end = -1;
      state = BasicState.NONE;
      type = null;
    }

    boolean tryToken(Label<ParseToken> parseTokenLabel) {
      String word = parseTokenLabel.getValue().text();
      Optional<NumberDefinition> optionallyNumberDefinition = numberModel.getNumberDefinition(word);
      switch (state) {
        case NONE:
          if (optionallyNumberDefinition.isPresent()) {
            NumberDefinition numberDefinition = optionallyNumberDefinition.get();
            switch (numberDefinition.numberType) {
              case TEEN:
                value = numberDefinition.value;
                begin = parseTokenLabel.getBegin();
                end = parseTokenLabel.getEnd();
                type = BasicType.TEEN;
                return true;
              case UNIT:
                value = numberDefinition.value;
                begin = parseTokenLabel.getBegin();
                end = parseTokenLabel.getEnd();
                type = BasicType.UNIT;
                return true;
              case DECADE:
                state = BasicState.DECADE;
                value = numberDefinition.value;
                begin = parseTokenLabel.getBegin();
                end = parseTokenLabel.getEnd();
                type = BasicType.DECADE;
                break;
            }
          }
          break;
        case DECADE:
          if ("-".equals(word)) {
            state = BasicState.DECADE_HYPHEN;
            return false;
          }
        case DECADE_HYPHEN:
          if (optionallyNumberDefinition.isPresent()) {
            NumberDefinition numberDefinition = optionallyNumberDefinition.get();
            if (numberDefinition.numberType == NumberType.UNIT) {
              value = value + numberDefinition.value;
              end = parseTokenLabel.getEnd();
            }
            type = BasicType.DECADE_UNIT;
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

  enum SequenceState {
    NONE,
    HAS_BASIC,
    RANK_01,
    PAST_FIRST_PART,
    POST_MAGNITUDE
  }

  static class Sequence {

    private final NumberModel numberModel;

    private final Basic basic;

    private SequenceState state;

    @Nullable
    BigInteger value;

    private int valueBuilder = 0;

    int begin = -1;

    int end = -1;

    @Inject
    public Sequence(NumberModel numberModel, Basic basic) {
      this.basic = basic;
      this.numberModel = numberModel;
      reset();
    }

    private void reset() {
      state = SequenceState.NONE;
      value = null;
      valueBuilder = -1;
      begin = -1;
      end = -1;
    }

    boolean tryToken(Label<ParseToken> parseTokenLabel) {
      String word = parseTokenLabel.getValue().text();
      Optional<NumberDefinition> optionallyNumberDefinition = numberModel.getNumberDefinition(word);

      switch (state) {
        case NONE:
          if (value != null && "and".equalsIgnoreCase(word)) {
            return false;
          }

          if (basic.tryToken(parseTokenLabel)) {
            state = SequenceState.HAS_BASIC;
            if (value == null) {
              begin = basic.begin;
            }
            end = basic.end;
            valueBuilder = basic.value;
          } else if (value != null) {
            break;
          }
          return false;
        case HAS_BASIC:
          if ("hundred".equalsIgnoreCase(word)) {
            state = SequenceState.RANK_01;
            valueBuilder = valueBuilder * 100;
            end = parseTokenLabel.getEnd();
            basic.reset();
            return false;
          }
        case RANK_01:
          if ("and".equalsIgnoreCase(word)) {
            return false;
          }

          if (basic.state != BasicState.NONE) {
            if (basic.tryToken(parseTokenLabel)) {
              valueBuilder += basic.value;
              end = basic.end;
              state = SequenceState.PAST_FIRST_PART;
            }
          } else {
            basic.tryToken(parseTokenLabel);
            if (basic.state == BasicState.NONE) {
              break;
            }
          }
          return false;
        default:
      }

      // if we fall through to here look for a magnitude

      if (optionallyNumberDefinition.isPresent()) {
        NumberDefinition numberDefinition = optionallyNumberDefinition.get();

        if (NumberType.MAGNITUDE == numberDefinition.numberType) {
          if (value != null) {
            value = value.add(BigInteger.valueOf(1000).pow(numberDefinition.value)
                .multiply(BigInteger.valueOf(valueBuilder)));
          } else {
            value = BigInteger.valueOf(1000).pow(numberDefinition.value)
                .multiply(BigInteger.valueOf(valueBuilder));
          }
          valueBuilder = 0;
          end = parseTokenLabel.getEnd();
          state = SequenceState.NONE;
          return false;
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
      if (value == null) {
        if (state == SequenceState.NONE) {
          return false;
        }
        value = BigInteger.valueOf(valueBuilder);
      } else {
        value = value.add(BigInteger.valueOf(valueBuilder));
      }

      return true;
    }
  }
}
