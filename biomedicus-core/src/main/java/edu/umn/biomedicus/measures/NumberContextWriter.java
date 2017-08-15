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
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.SearchExpr;
import edu.umn.biomedicus.framework.SearchExprFactory;
import edu.umn.biomedicus.framework.Searcher;
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

  private final SearchExpr searcher;

  private final Path outputDirectory;

  @Inject
  public NumberContextWriter(SearchExprFactory searcherFactory,
      @ProcessorSetting("outputDirectory") Path outputDirectory) {
    searcher = searcherFactory.parse(
        "[Sentence (?<leftToks> ParseToken{0,6}) number:Number (?<rightToks> ParseToken{0,6})]");
    this.outputDirectory = outputDirectory;
  }

  @Override
  public void process(@Nonnull Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<ParseToken> tokensIndex = systemView.getLabelIndex(ParseToken.class);

    Searcher search = searcher.createSearcher(systemView);
    while (search.search()) {
      try (BufferedWriter bufferedWriter = Files
          .newBufferedWriter(outputDirectory.resolve(document.getDocumentId() + ".txt"),
              StandardOpenOption.CREATE_NEW)) {

        Optional<Span> leftOpt = search.getSpan("leftToks");
        if (leftOpt.isPresent()) {
          Span leftSpan = leftOpt.get();
          LabelIndex<ParseToken> leftTokenIndex = tokensIndex.insideSpan(leftSpan);
          for (Label<ParseToken> leftToken : leftTokenIndex) {
            bufferedWriter.write(leftToken.getValue().text() + " ");
          }
        }
        bufferedWriter.newLine();

        Span numberSpan = search.getLabel("number")
            .orElseThrow(() -> {
              return new BiomedicusException("Should have a number");
            })
            .toSpan();
        for (ParseToken numberToken : tokensIndex.insideSpan(numberSpan).valuesAsList()) {
          bufferedWriter.write(numberToken.text() + " ");
        }
        bufferedWriter.newLine();

        Optional<Span> rightOpt = search.getSpan("rightToks");
        if (rightOpt.isPresent()) {
          Span rightSpan = rightOpt.get();
          for (ParseToken rightToken : tokensIndex.insideSpan(rightSpan).valuesAsList()) {
            bufferedWriter.write(rightToken.text() + " ");
          }
        }
        bufferedWriter.newLine();
      } catch (IOException e) {
        throw new BiomedicusException(e);
      }
    }
  }
}
