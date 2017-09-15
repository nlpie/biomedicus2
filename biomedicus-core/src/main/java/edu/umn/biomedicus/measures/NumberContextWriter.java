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

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.SearchExpr;
import edu.umn.biomedicus.framework.SearchExprFactory;
import edu.umn.biomedicus.framework.Searcher;
import edu.umn.biomedicus.framework.store.DefaultLabelIndex;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextView;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Writes numbers and the context of tokens around them to files.
 * <br/>The format is one line of up to 6 tokens in the sentence to the left of the number, one line
 * making up all the tokens in the number, and one line of up to 6 tokens in the sentence to the
 * right of the number.
 *
 * @author Ben Knoll
 * @since 1.8.0
 */
public class NumberContextWriter implements DocumentProcessor {

  private final Path outputDirectory;

  @Inject
  public NumberContextWriter(@ProcessorSetting("outputDirectory") Path outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  @Override
  public void process(@Nonnull Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<Number> numbersIndex = systemView.getLabelIndex(Number.class);
    LabelIndex<Sentence> sentencesIndex = new DefaultLabelIndex<>(
        systemView.getLabelIndex(Sentence.class));
    LabelIndex<ParseToken> tokensIndex = systemView.getLabelIndex(ParseToken.class);

    try (BufferedWriter bufferedWriter = Files
        .newBufferedWriter(outputDirectory.resolve(document.getDocumentId() + ".txt"),
            StandardOpenOption.CREATE_NEW)) {

      for (Label<Number> numberLabel : numbersIndex) {
        LabelIndex<Sentence> sentenceContainingIndex = sentencesIndex.containing(numberLabel);
        Optional<Label<Sentence>> sentenceOption = sentenceContainingIndex.first();
        if (!sentenceOption.isPresent()) {
          throw new BiomedicusException("No sentence");
        }
        Label<Sentence> sentenceLabel = sentenceOption.get();
        LabelIndex<ParseToken> sentenceTokensIndex = tokensIndex.insideSpan(sentenceLabel);

        Iterator<Label<ParseToken>> it = sentenceTokensIndex.leftwardsFrom(numberLabel)
            .iterator();
        List<ParseToken> leftTokens = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
          if (it.hasNext()) {
            leftTokens.add(it.next().getValue());
          }
        }
        int leftSize = leftTokens.size();
        for (int i = 0; i < leftSize; i++) {
          bufferedWriter.write(leftTokens.get(leftSize - 1 - i).text() + " ");
        }
        bufferedWriter.newLine();

        for (ParseToken numberToken : tokensIndex.insideSpan(numberLabel).values()) {
          bufferedWriter.write(numberToken.text() + " ");
        }
        bufferedWriter.newLine();

        Iterator<Label<ParseToken>> rightIt = sentenceTokensIndex.rightwardsFrom(numberLabel)
            .iterator();

        for (int i = 0; i < 6; i++) {
          if (rightIt.hasNext()) {
            bufferedWriter.write(rightIt.next().getValue().text() + " ");
          }
        }
        bufferedWriter.newLine();
      }
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }
  }
}
