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


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary;
import edu.umn.biomedicus.common.dictionary.StringIdentifier;
import edu.umn.biomedicus.framework.SearchExprFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VocabularySearchExprFactoryTest {

  VocabularySearchExprFactory factory;

  SearchExprFactory original;

  Vocabulary vocabulary;

  BidirectionalDictionary dictionary;

  @BeforeEach
  void setUp() {
    vocabulary = mock(Vocabulary.class);
    original = mock(SearchExprFactory.class);
    dictionary = mock(BidirectionalDictionary.class);
    when(vocabulary.getWordsIndex()).thenReturn(dictionary);
    when(vocabulary.getNormsIndex()).thenReturn(dictionary);
    when(vocabulary.getTermsIndex()).thenReturn(dictionary);
    factory = new VocabularySearchExprFactory(vocabulary, original);
  }

  @Test
  public void testNoReplace() {
    String expr = "someExpr";
    factory.parseExpression(expr);
    verify(original).parse(expr);
  }

  @Test
  public void testReplace() {
    String expr = "abc$words\"blah\"123$norms\"foo\"xyz$terms\"aTerm\"";

    when(dictionary.getTermIdentifier("blah")).thenReturn(StringIdentifier.withValue(1));
    when(dictionary.getTermIdentifier("foo")).thenReturn(StringIdentifier.withValue(2));
    when(dictionary.getTermIdentifier("aTerm")).thenReturn(StringIdentifier.withValue(3));

    factory.parseExpression(expr);
    verify(original).parse("abc11232xyz3");
  }
}
