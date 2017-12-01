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

package edu.umn.biomedicus.tokenization;

import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Span;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Iterator over a collection of merged tokens. Tokens that are connected by
 * - / \ ' or _ without spaces are merged.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
public final class TermTokenMerger implements Iterator<TermToken> {

  private static final Set<Character> MERGE
      = new HashSet<>(Arrays.asList('-', '/', '\\', '\'', '_'));
  private final List<Token> running = new ArrayList<>();
  private final Iterator<ParseToken> iterator;

  @Nullable
  private TermToken next;

  public TermTokenMerger(Iterator<ParseToken> iterator) {
    this.iterator = iterator;
    findNext();
  }

  public TermTokenMerger(LabelIndex<ParseToken> parseTokens) {
    this(parseTokens.iterator());
  }

  private void findNext() {
    next = null;
    while (next == null && iterator.hasNext()) {
      Token token = iterator.next();
      if (running.size() == 0) {
        running.add(token);
        continue;
      }

      Token lastToken = running.get(running.size() - 1);
      String lastTokenText = lastToken.getText();
      char lastTokenLastChar = lastTokenText
          .charAt(lastTokenText.length() - 1);
      char curTokenFirstChar = token.getText().charAt(0);
      if (lastToken.getHasSpaceAfter() ||
          (!MERGE.contains(curTokenFirstChar) && !MERGE.contains(lastTokenLastChar))) {
        makeTermToken();
      }
      running.add(token);
    }

    if (next == null && !running.isEmpty()) {
      makeTermToken();
    }
  }

  private void makeTermToken() {
    if (running.size() == 0) {
      return;
    }
    StringBuilder tokenText = new StringBuilder();
    for (Token token : running) {
      tokenText.append(token.getText());
    }
    Token lastToken = running.get(running.size() - 1);
    boolean hasSpaceAfter = lastToken.getHasSpaceAfter();

    Span span = new Span(running.get(0).getStartIndex(), lastToken.getEndIndex());
    next = new TermToken(span, tokenText.toString(), hasSpaceAfter);
    running.clear();
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public TermToken next() {
    if (next == null) {
      throw new NoSuchElementException();
    }
    TermToken copy = next;
    findNext();
    return copy;
  }
}
