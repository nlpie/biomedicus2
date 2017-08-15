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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import edu.umn.biomedicus.common.types.semantics.NumberType;
import edu.umn.biomedicus.common.types.text.ImmutableParseToken;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.measures.NumberRecognizer.BasicNumberAcceptor;
import edu.umn.biomedicus.measures.NumberRecognizer.BasicNumberType;
import edu.umn.biomedicus.measures.NumberRecognizer.FractionAcceptor;
import edu.umn.biomedicus.measures.NumberRecognizer.NumberAcceptor;
import edu.umn.biomedicus.measures.NumberRecognizer.NumberDefinition;
import edu.umn.biomedicus.measures.NumberRecognizer.NumberModel;
import java.math.BigDecimal;
import java.math.BigInteger;
import mockit.Expectations;
import mockit.Injectable;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NumberRecognizerTest {
  static NumberDefinition fiveDef = new NumberDefinition(5, BasicNumberType.UNIT);

  static NumberDefinition fortyDef = new NumberDefinition(40,
      BasicNumberType.DECADE);

  static NumberDefinition fourDef = new NumberDefinition(4,  BasicNumberType.UNIT);

  static NumberDefinition tenDef = new NumberDefinition(10, BasicNumberType.TEEN);

  static NumberDefinition fifteenDef = new NumberDefinition(15,
      BasicNumberType.TEEN);

  static NumberDefinition hundredDef = new NumberDefinition(100, BasicNumberType.MAGNITUDE);

  static NumberDefinition billionDef = new NumberDefinition(3, BasicNumberType.MAGNITUDE);

  static NumberDefinition millionDef = new NumberDefinition(2, BasicNumberType.MAGNITUDE);

  static NumberDefinition sixths = new NumberDefinition(6, BasicNumberType.UNIT);

  @Injectable
  NumberModel numberModel;

  FractionAcceptor fractionAcceptor;

  NumberAcceptor numberAcceptor;

  BasicNumberAcceptor basicNumberAcceptor;

  NumberRecognizer numberRecognizer;

  @BeforeMethod
  public void setUp() {
    basicNumberAcceptor = new BasicNumberAcceptor(numberModel);
    numberAcceptor = new NumberAcceptor(numberModel, basicNumberAcceptor);
    fractionAcceptor = new FractionAcceptor(numberAcceptor);
    numberRecognizer = new NumberRecognizer(fractionAcceptor);
  }

  @Test
  public void testBasicRecognizesUnit() {
    new Expectations() {{
      numberModel.getNumberDefinition("four"); result = fourDef;
    }};

    assertTrue(basicNumberAcceptor.tryToken(Label.create(Span.create(0, 4),
        ImmutableParseToken.builder().text("four").hasSpaceAfter(true).build())));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 4);
    assertEquals(basicNumberAcceptor.value, 4);
  }

  @Test
  public void testBasicRecognizesTeen() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("ten"); result = tenDef;
    }};

    assertTrue(basicNumberAcceptor.tryToken(Label.create(Span.create(0, 3),
        ImmutableParseToken.builder().text("ten").hasSpaceAfter(true).build())));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 3);
    assertEquals(basicNumberAcceptor.value, 10);
  }

  @Test
  public void testBasicRecognizesDecade() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("forty"); result = fortyDef;
      numberModel.getNumberDefinition("people"); result = null;
    }};

    assertFalse(basicNumberAcceptor.tryToken(Label.create(Span.create(0, 6),
        ImmutableParseToken.builder().text("forty").hasSpaceAfter(true).build())));
    assertTrue(basicNumberAcceptor.tryToken(Label.create(Span.create(6, 8), ImmutableParseToken.builder()
        .text("people").hasSpaceAfter(false).build())));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 6);
    assertEquals(basicNumberAcceptor.value, 40);
  }

  @Test
  public void testBasicRecongizesDecadeAnd() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("forty"); result = fortyDef;
      numberModel.getNumberDefinition("five"); result = fiveDef;
    }};

    assertFalse(basicNumberAcceptor.tryToken(Label.create(Span.create(0, 6),
        ImmutableParseToken.builder().text("forty").hasSpaceAfter(true).build())));
    assertTrue(basicNumberAcceptor.tryToken(Label.create(Span.create(7, 12), ImmutableParseToken.builder()
        .text("five").hasSpaceAfter(false).build())));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 12);
    assertEquals(basicNumberAcceptor.value, 45);
  }

  @Test
  public void testBasicRecognizesDecadeHyphen() {
    new Expectations() {{
      numberModel.getNumberDefinition("forty"); result = fortyDef;
      numberModel.getNumberDefinition("-"); result = null;
      numberModel.getNumberDefinition("five"); result = fiveDef;
    }};

    assertFalse(basicNumberAcceptor.tryToken(Label.create(Span.create(0, 6),
        ImmutableParseToken.builder().text("forty").hasSpaceAfter(false).build())));
    assertFalse(basicNumberAcceptor.tryToken(Label.create(Span.create(6, 7),
        ImmutableParseToken.builder().text("-").hasSpaceAfter(false).build())));
    assertTrue(basicNumberAcceptor.tryToken(Label.create(Span.create(7, 12),
        ImmutableParseToken.builder().text("five").hasSpaceAfter(false).build())));

    assertEquals(basicNumberAcceptor.begin, 0);
    assertEquals(basicNumberAcceptor.end, 12);
    assertEquals(basicNumberAcceptor.value, 45);
  }

  @Test
  public void testBasicRandomWord() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("the"); result = null;
      numberModel.getOrdinal("the"); result = null;
    }};

    assertFalse(basicNumberAcceptor.tryToken(Label.create(Span.create(0, 3),
        ImmutableParseToken.builder().text("the").hasSpaceAfter(false).build())));
  }

  @Test
  public void testMagnitude() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("five"); result = fiveDef;
      numberModel.getNumberDefinition("billion"); result = billionDef;
      numberModel.getOrdinal("people"); result = null;
      numberModel.getNumberDefinition("people"); result = null;
    }};

    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(0, 4),
        ImmutableParseToken.builder().hasSpaceAfter(true).text("five").build())));
    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(5, 12),
        ImmutableParseToken.builder().hasSpaceAfter(true).text("billion").build())));
    assertTrue(numberAcceptor.tryToken(Label.create(Span.create(13, 19),
        ImmutableParseToken.builder().hasSpaceAfter(true).text("people").build())));

    assertEquals(numberAcceptor.value, BigInteger.valueOf(5).multiply(BigInteger.valueOf(10).pow(9)));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 12);
  }

  @Test
  public void testBasicOnly() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("four"); result = fourDef;
    }};

    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(0, 4),
        ImmutableParseToken.builder().text("four").hasSpaceAfter(true).build())));

    assertTrue(numberAcceptor.tryToken(Label.create(Span.create(5, 11),
        ImmutableParseToken.builder().text("people").hasSpaceAfter(false).build())));

    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 4);
    assertEquals(numberAcceptor.value, BigInteger.valueOf(4));
  }

  @Test
  public void testHundred() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("fifteen"); result = fifteenDef;
      numberModel.getNumberDefinition("forty"); result = fortyDef;
      numberModel.getNumberDefinition("five"); result = fiveDef;
    }};

    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(0, 7),
        ImmutableParseToken.builder().text("fifteen").hasSpaceAfter(true).build())));
    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(8, 15),
        ImmutableParseToken.builder().text("hundred").hasSpaceAfter(true).build())));
    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(16, 21),
        ImmutableParseToken.builder().text("forty").hasSpaceAfter(true).build())));
    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(22, 26),
        ImmutableParseToken.builder().text("five").hasSpaceAfter(false).build())));
    assertTrue(numberAcceptor.finish());

    assertEquals(numberAcceptor.value, BigInteger.valueOf(1545));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 26);
  }

  @Test
  public void testChained() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("five"); result = fiveDef;
      numberModel.getNumberDefinition("billion"); result = billionDef;
      numberModel.getNumberDefinition("million"); result = millionDef;
    }};

    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(0, 4),
        ImmutableParseToken.builder().text("five").hasSpaceAfter(true).build())));
    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(5, 12),
        ImmutableParseToken.builder().text("billion").hasSpaceAfter(true).build())));
    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(13, 17),
        ImmutableParseToken.builder().text("five").hasSpaceAfter(true).build())));
    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(18, 25),
        ImmutableParseToken.builder().text("million").hasSpaceAfter(false).build())));
    assertTrue(numberAcceptor.finish());

    assertEquals(numberAcceptor.value, BigInteger.valueOf(5).multiply(BigInteger.valueOf(10).pow(9))
        .add(BigInteger.valueOf(5).multiply(BigInteger.valueOf(10).pow(6))));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 25);
  }

  @Test
  public void testEndOfSentenceHundred() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("five"); result = fiveDef;
      numberModel.getNumberDefinition("hundred"); result = hundredDef;
    }};

    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(0, 4),
        ImmutableParseToken.builder().text("five").hasSpaceAfter(true).build())));
    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(5, 12),
        ImmutableParseToken.builder().text("hundred").hasSpaceAfter(true).build())));
    assertTrue(numberAcceptor.finish());

    assertEquals(numberAcceptor.value, BigInteger.valueOf(500));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 12);
  }

  @Test
  public void testEndOfSentenceDecade() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("forty"); result = fortyDef;
    }};

    assertFalse(numberAcceptor.tryToken(Label.create(Span.create(0, 5),
        ImmutableParseToken.builder().text("forty").hasSpaceAfter(true).build())));
    assertTrue(numberAcceptor.finish());

    assertEquals(numberAcceptor.value, BigInteger.valueOf(40));
    assertEquals(numberAcceptor.begin, 0);
    assertEquals(numberAcceptor.end, 5);
  }

  @Test
  public void testFraction() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("five"); result = fiveDef;
      numberModel.getDenominator("forty"); result = null;
      numberModel.getNumberDefinition("forty"); result = fortyDef;
      numberModel.getDenominator("sixths"); result = sixths;
    }};

    assertFalse(fractionAcceptor.tryToken(Label.create(Span.create(0, 4),
        ImmutableParseToken.builder().text("five").hasSpaceAfter(true).build())));

    assertFalse(fractionAcceptor.tryToken(Label.create(Span.create(5, 10),
        ImmutableParseToken.builder().text("forty").hasSpaceAfter(true).build())));

    assertTrue(fractionAcceptor.tryToken(Label.create(Span.create(11, 17),
        ImmutableParseToken.builder().text("sixths").hasSpaceAfter(false).build())));

    assertEquals(fractionAcceptor.numerator, BigInteger.valueOf(5));
    assertEquals(fractionAcceptor.denominator, BigInteger.valueOf(46));
    assertEquals(fractionAcceptor.begin, 0);
    assertEquals(fractionAcceptor.end, 17);
  }

  @Test
  public void testFractionTwoWordFraction() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("forty"); result = fortyDef;
      numberModel.getNumberDefinition("sixths"); result = null;
      numberModel.getDenominator("sixths"); result = sixths;
    }};

    assertFalse(fractionAcceptor.tryToken(Label.create(Span.create(0, 5),
        ImmutableParseToken.builder().text("forty").hasSpaceAfter(true).build())));

    assertTrue(fractionAcceptor.tryToken(Label.create(Span.create(6, 12),
        ImmutableParseToken.builder().text("sixths").hasSpaceAfter(false).build())));

    assertEquals(fractionAcceptor.numerator, BigInteger.valueOf(40));
    assertEquals(fractionAcceptor.denominator, BigInteger.valueOf(6));
    assertEquals(fractionAcceptor.begin, 0);
    assertEquals(fractionAcceptor.end, 12);
  }

  @Test
  public void testAndHalf() throws Exception {
    new Expectations() {{
      numberModel.getNumberDefinition("five"); result = fiveDef;
    }};

    assertFalse(fractionAcceptor.tryToken(Label.create(Span.create(0, 4),
        ImmutableParseToken.builder().text("five").hasSpaceAfter(true).build())));
    assertFalse(fractionAcceptor.tryToken(Label.create(Span.create(5, 8),
        ImmutableParseToken.builder().text("and").hasSpaceAfter(true).build())));
    assertFalse(fractionAcceptor.tryToken(Label.create(Span.create(9, 10),
        ImmutableParseToken.builder().text("a").hasSpaceAfter(true).build())));
    assertTrue(fractionAcceptor.tryToken(Label.create(Span.create(11, 15),
        ImmutableParseToken.builder().text("half").hasSpaceAfter(true).build())));

    assertEquals(fractionAcceptor.numerator, BigInteger.valueOf(11));
    assertEquals(fractionAcceptor.denominator, BigInteger.valueOf(2));
    assertEquals(fractionAcceptor.begin, 0);
    assertEquals(fractionAcceptor.end, 15);
    assertEquals(fractionAcceptor.numberType, NumberType.FRACTION);
  }

  @Test
  public void testParseDecimalComma() throws Exception {
    String s = numberRecognizer.parseDecimal("42,000");
    assertNotNull(s);
    assertEquals(new BigDecimal(s).compareTo(BigDecimal.valueOf(42_000)), 0);
  }

  @Test
  public void testParseDecimalCommaAndDecimal() throws Exception {
    String s = numberRecognizer.parseDecimal("42,000,000.00");
    assertNotNull(s);
    assertEquals(new BigDecimal(s).compareTo(BigDecimal.valueOf(42_000_000.00)), 0);
  }

  @Test
  public void testParseDecimal() throws Exception {
    String s = numberRecognizer.parseDecimal("450.01");
    assertNotNull(s);
    assertEquals(new BigDecimal(s).compareTo(BigDecimal.valueOf(450.01)), 0);
  }

  @Test
  public void testParseDecimalPercentage() throws Exception {
    String s = numberRecognizer.parseDecimal("50.05%");
    assertNotNull(s);
    assertEquals(new BigDecimal(s).compareTo(BigDecimal.valueOf(0.5005)), 0);
  }

  @Test
  public void testParseDecimalNoDecimal() throws Exception {
    String s = numberRecognizer.parseDecimal("test");
    assertNull(s);
  }

  @Test
  public void testParseDecimalHyphen() throws Exception {
    String s = numberRecognizer.parseDecimal("-");
    assertNull(s);
  }
}