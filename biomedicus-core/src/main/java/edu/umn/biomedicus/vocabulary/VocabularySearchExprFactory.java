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

import com.google.inject.Inject;
import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary;
import edu.umn.biomedicus.common.dictionary.StringIdentifier;
import edu.umn.biomedicus.framework.SearchExpr;
import edu.umn.biomedicus.framework.SearchExprFactory;
import edu.umn.nlpengine.Span;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs vocabulary substitution for {@link SearchExprFactory}. Looks for strings of the forms:
 * $words"word", $terms"term", and $norms"norm" and replaces them with their respective string
 * indices.
 *
 * @author Ben Knoll
 * @since 1.9.0
 */
public class VocabularySearchExprFactory {

  private static final Pattern WORD_REPLACE = Pattern.compile("\\$words\"([\\p{all}&&[^\"]]*)\"");

  private static final Pattern TERM_REPLACE = Pattern.compile("\\$terms\"([\\p{all}&&[^\"]]*)\"");

  private static final Pattern NORM_REPLACE = Pattern.compile("\\$norms\"([\\p{all}&&[^\"]]*)\"");

  private final SearchExprFactory searchExprFactory;

  private final BidirectionalDictionary wordsIndex;

  private final BidirectionalDictionary termsIndex;

  private final BidirectionalDictionary normsIndex;

  @Inject
  public VocabularySearchExprFactory(Vocabulary vocabulary, SearchExprFactory searchExprFactory) {
    wordsIndex = vocabulary.getWordsIndex();
    termsIndex = vocabulary.getTermsIndex();
    normsIndex = vocabulary.getNormsIndex();
    this.searchExprFactory = searchExprFactory;
  }

  /**
   * Parses the expression, performing substitutions.
   *
   * @param expression the expression with substitution keywords
   * @return search expression with the indices values inserted
   */
  public SearchExpr parseExpression(String expression) {

    NavigableMap<Span, String> replacements = new TreeMap<>();
    findReplacements(replacements, WORD_REPLACE.matcher(expression), wordsIndex);
    findReplacements(replacements, TERM_REPLACE.matcher(expression), termsIndex);
    findReplacements(replacements, NORM_REPLACE.matcher(expression), normsIndex);

    if (replacements.size() > 0) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append(expression);

      for (Entry<Span, String> entry : replacements.descendingMap().entrySet()) {
        stringBuilder.replace(entry.getKey().getStartIndex(), entry.getKey().getEndIndex(), entry.getValue());
      }

      expression = stringBuilder.toString();
    }

    return searchExprFactory.parse(expression);
  }

  private void findReplacements(Map<Span, String> replacements, Matcher matcher,
      BidirectionalDictionary dictionary) {
    while (matcher.find()) {
      String word = matcher.group(1);
      StringIdentifier termIdentifier = dictionary.getTermIdentifier(word);
      if (termIdentifier.isUnknown()) {
        throw new IllegalArgumentException("Unknown string: " + word);
      }
      replacements.put(new Span(matcher.start(), matcher.end()), "" + termIdentifier.value());
    }
  }
}
