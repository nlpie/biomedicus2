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
import static org.testng.Assert.assertTrue;

import edu.umn.biomedicus.common.types.text.ImmutableParseToken;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.measures.NumberRecognizer.Basic;
import edu.umn.biomedicus.measures.NumberRecognizer.NumberDefinition;
import edu.umn.biomedicus.measures.NumberRecognizer.NumberModel;
import edu.umn.biomedicus.measures.NumberRecognizer.NumberType;
import edu.umn.biomedicus.measures.NumberRecognizer.Sequence;
import java.math.BigInteger;
import java.util.Optional;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class NumberRecognizerTest {
  static NumberDefinition fiveDef = new NumberDefinition(5, NumberType.UNIT);

  static NumberDefinition fortyDef = new NumberDefinition(40,
      NumberType.DECADE);

  static NumberDefinition fourDef = new NumberDefinition(4,  NumberType.UNIT);

  static NumberDefinition tenDef = new NumberDefinition(10, NumberType.TEEN);

  static NumberDefinition fifteenDef = new NumberDefinition(15,
      NumberType.TEEN);

  static NumberDefinition hundredDef = new NumberDefinition(100, NumberType.MAGNITUDE);

  static NumberDefinition billionDef = new NumberDefinition(3, NumberType.MAGNITUDE);

  static NumberDefinition millionDef = new NumberDefinition(2, NumberType.MAGNITUDE);

  public static class BasicTests {

    @Tested
    Basic acceptor;

    @Injectable
    NumberModel numberModel;

    @Test
    public void testBasicRecognizesUnit() {
      new Expectations() {{
        numberModel.getNumberDefinition("four"); result = Optional.of(fourDef);
      }};

      assertTrue(acceptor.tryToken(Label.create(Span.create(0, 4),
          ImmutableParseToken.builder().text("four").hasSpaceAfter(true).build())));

      assertEquals(acceptor.begin, 0);
      assertEquals(acceptor.end, 4);
      assertEquals(acceptor.value, 4);
    }

    @Test
    public void testBasicRecognizesTeen() throws Exception {
      new Expectations() {{
        numberModel.getNumberDefinition("ten"); result = Optional.of(tenDef);
      }};

      assertTrue(acceptor.tryToken(Label.create(Span.create(0, 3),
          ImmutableParseToken.builder().text("ten").hasSpaceAfter(true).build())));

      assertEquals(acceptor.begin, 0);
      assertEquals(acceptor.end, 3);
      assertEquals(acceptor.value, 10);
    }

    @Test
    public void testBasicRecognizesDecade() throws Exception {
      new Expectations() {{
        numberModel.getNumberDefinition("forty"); result = Optional.of(fortyDef);
        numberModel.getNumberDefinition("people"); result = Optional.empty();
      }};

      assertFalse(acceptor.tryToken(Label.create(Span.create(0, 6),
          ImmutableParseToken.builder().text("forty").hasSpaceAfter(true).build())));
      assertTrue(acceptor.tryToken(Label.create(Span.create(6, 8), ImmutableParseToken.builder()
          .text("people").hasSpaceAfter(false).build())));

      assertEquals(acceptor.begin, 0);
      assertEquals(acceptor.end, 6);
      assertEquals(acceptor.value, 40);
    }

    @Test
    public void testBasicRecongizesDecadeAnd() throws Exception {
      new Expectations() {{
        numberModel.getNumberDefinition("forty"); result = Optional.of(fortyDef);
        numberModel.getNumberDefinition("five"); result = Optional.of(fiveDef);
      }};

      assertFalse(acceptor.tryToken(Label.create(Span.create(0, 6),
          ImmutableParseToken.builder().text("forty").hasSpaceAfter(true).build())));
      assertTrue(acceptor.tryToken(Label.create(Span.create(7, 12), ImmutableParseToken.builder()
          .text("five").hasSpaceAfter(false).build())));

      assertEquals(acceptor.begin, 0);
      assertEquals(acceptor.end, 12);
      assertEquals(acceptor.value, 45);
    }

    @Test
    public void testBasicRecognizesDecadeHyphen() {
      new Expectations() {{
        numberModel.getNumberDefinition("forty"); result = Optional.of(fortyDef);
        numberModel.getNumberDefinition("-"); result = Optional.empty();
        numberModel.getNumberDefinition("five"); result = Optional.of(fiveDef);
      }};

      assertFalse(acceptor.tryToken(Label.create(Span.create(0, 6),
          ImmutableParseToken.builder().text("forty").hasSpaceAfter(false).build())));
      assertFalse(acceptor.tryToken(Label.create(Span.create(6, 7),
          ImmutableParseToken.builder().text("-").hasSpaceAfter(false).build())));
      assertTrue(acceptor.tryToken(Label.create(Span.create(7, 12),
          ImmutableParseToken.builder().text("five").hasSpaceAfter(false).build())));

      assertEquals(acceptor.begin, 0);
      assertEquals(acceptor.end, 12);
      assertEquals(acceptor.value, 45);
    }

    @Test
    public void testBasicRandomWord() throws Exception {
      new Expectations() {{
        numberModel.getNumberDefinition("the"); result = Optional.empty();
      }};

      assertFalse(acceptor.tryToken(Label.create(Span.create(0, 3),
          ImmutableParseToken.builder().text("the").hasSpaceAfter(false).build())));
    }
  }

  public static class SequenceTests {
    @Injectable
    NumberModel numberModel;

    Basic basic;

    Sequence seq;



    @Test
    public void testMagnitude() throws Exception {
      new Expectations() {{
        numberModel.getNumberDefinition("five"); result = Optional.of(fiveDef);
        numberModel.getNumberDefinition("billion"); result = Optional.of(billionDef);
      }};

      assertFalse(seq.tryToken(Label.create(Span.create(0, 4),
          ImmutableParseToken.builder().hasSpaceAfter(true).text("five").build())));
      assertFalse(seq.tryToken(Label.create(Span.create(5, 12),
          ImmutableParseToken.builder().hasSpaceAfter(true).text("billion").build())));
      assertTrue(seq.tryToken(Label.create(Span.create(13, 19),
          ImmutableParseToken.builder().hasSpaceAfter(true).text("people").build())));

      assertEquals(seq.value, BigInteger.valueOf(5).multiply(BigInteger.valueOf(10).pow(9)));
      assertEquals(seq.begin, 0);
      assertEquals(seq.end, 12);
    }

    @Test
    public void testBasicOnly() throws Exception {
      new Expectations() {{
        numberModel.getNumberDefinition("four"); result = Optional.of(fourDef);
      }};

      assertFalse(seq.tryToken(Label.create(Span.create(0, 4),
          ImmutableParseToken.builder().text("four").hasSpaceAfter(true).build())));

      assertTrue(seq.tryToken(Label.create(Span.create(5, 11),
          ImmutableParseToken.builder().text("people").hasSpaceAfter(false).build())));

      assertEquals(seq.begin, 0);
      assertEquals(seq.end, 4);
      assertEquals(seq.value, BigInteger.valueOf(4));
    }

    @Test
    public void testHundred() throws Exception {
      new Expectations() {{
        numberModel.getNumberDefinition("fifteen"); result = Optional.of(fifteenDef);
        numberModel.getNumberDefinition("forty"); result = Optional.of(fortyDef);
        numberModel.getNumberDefinition("five"); result = Optional.of(fiveDef);
      }};

      assertFalse(seq.tryToken(Label.create(Span.create(0, 7),
          ImmutableParseToken.builder().text("fifteen").hasSpaceAfter(true).build())));
      assertFalse(seq.tryToken(Label.create(Span.create(8, 15),
          ImmutableParseToken.builder().text("hundred").hasSpaceAfter(true).build())));
      assertFalse(seq.tryToken(Label.create(Span.create(16, 21),
          ImmutableParseToken.builder().text("forty").hasSpaceAfter(true).build())));
      assertFalse(seq.tryToken(Label.create(Span.create(22, 26),
          ImmutableParseToken.builder().text("five").hasSpaceAfter(false).build())));
      assertTrue(seq.finish());

      assertEquals(seq.value, BigInteger.valueOf(1545));
      assertEquals(seq.begin, 0);
      assertEquals(seq.end, 26);
    }

    @Test
    public void testChained() throws Exception {
      new Expectations() {{
        numberModel.getNumberDefinition("five"); result = Optional.of(fiveDef);
        numberModel.getNumberDefinition("billion"); result = Optional.of(billionDef);
        numberModel.getNumberDefinition("million"); result = Optional.of(millionDef);
      }};

      assertFalse(seq.tryToken(Label.create(Span.create(0, 4),
          ImmutableParseToken.builder().text("five").hasSpaceAfter(true).build())));
      assertFalse(seq.tryToken(Label.create(Span.create(5, 12),
          ImmutableParseToken.builder().text("billion").hasSpaceAfter(true).build())));
      assertFalse(seq.tryToken(Label.create(Span.create(13, 17),
          ImmutableParseToken.builder().text("five").hasSpaceAfter(true).build())));
      assertFalse(seq.tryToken(Label.create(Span.create(18, 25),
          ImmutableParseToken.builder().text("million").hasSpaceAfter(false).build())));
      assertTrue(seq.finish());

      assertEquals(seq.value, BigInteger.valueOf(5).multiply(BigInteger.valueOf(10).pow(9))
          .add(BigInteger.valueOf(5).multiply(BigInteger.valueOf(10).pow(6))));
      assertEquals(seq.begin, 0);
      assertEquals(seq.end, 25);
    }

    @Test
    public void testEndOfSentenceHundred() throws Exception {
      new Expectations() {{
        numberModel.getNumberDefinition("five"); result = Optional.of(fiveDef);
        numberModel.getNumberDefinition("hundred"); result = Optional.of(hundredDef);
      }};

      assertFalse(seq.tryToken(Label.create(Span.create(0, 4),
          ImmutableParseToken.builder().text("five").hasSpaceAfter(true).build())));
      assertFalse(seq.tryToken(Label.create(Span.create(5, 12),
          ImmutableParseToken.builder().text("hundred").hasSpaceAfter(true).build())));
      assertTrue(seq.finish());

      assertEquals(seq.value, BigInteger.valueOf(500));
      assertEquals(seq.begin, 0);
      assertEquals(seq.end, 12);
    }

    @Test
    public void testEndOfSentenceDecade() throws Exception {
      new Expectations() {{
        numberModel.getNumberDefinition("forty"); result = Optional.of(fortyDef);
      }};

      assertFalse(seq.tryToken(Label.create(Span.create(0, 5),
          ImmutableParseToken.builder().text("forty").hasSpaceAfter(true).build())));
      assertTrue(seq.finish());

      assertEquals(seq.value, BigInteger.valueOf(40));
      assertEquals(seq.begin, 0);
      assertEquals(seq.end, 5);
    }

    @BeforeMethod
    public void setUp() throws Exception {
      basic = new Basic(numberModel);

      seq = new Sequence(numberModel, basic);
    }
  }
}