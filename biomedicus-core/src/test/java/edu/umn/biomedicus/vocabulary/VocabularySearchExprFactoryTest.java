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

package edu.umn.biomedicus.vocabulary;


import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary;
import edu.umn.biomedicus.framework.SearchExprFactory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.junit.jupiter.api.Test;

class VocabularySearchExprFactoryTest {

  @Tested
  VocabularySearchExprFactory factory;

  @Injectable
  SearchExprFactory orignal;

  @Injectable
  Vocabulary vocabulary;

  @Mocked
  BidirectionalDictionary dictionary;

  @Test
  public void testNoReplace() {
    String expr = "someExpr";

    factory.parseExpression(expr);

    new Verifications() {{
      orignal.parse("someExpr");
    }};
  }

  @Test
  public void testReplace() {
    String expr = "abc$words\"blah\"123$norms\"foo\"xyz$terms\"aTerm\"";

    new Expectations(){{
      dictionary.getTermIdentifier("blah"); result = 1;
      dictionary.getTermIdentifier("foo"); result = 2;
      dictionary.getTermIdentifier("aTerm"); result = 3;
    }};

    factory.parseExpression(expr);

    new Verifications() {{
      orignal.parse("abc11232xyz3");
    }};
  }
}