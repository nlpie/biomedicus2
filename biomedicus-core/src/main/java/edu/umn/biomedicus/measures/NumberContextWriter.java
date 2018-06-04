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

package edu.umn.biomedicus.measures;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.DocumentOperation;
import edu.umn.nlpengine.LabelIndex;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
public class NumberContextWriter implements DocumentOperation {
  private final Path outputDirectory;

  private final int contextSize;

  @Inject
  public NumberContextWriter(
      @ProcessorSetting("outputDirectory") Path outputDirectory,
      @ProcessorSetting("contextSize") Integer contextSize
  ) {
    this.outputDirectory = outputDirectory;
    this.contextSize = contextSize;
  }

  @Override
  public void process(@Nonnull Document document) {
    LabelIndex<Number> numbersIndex = document.labelIndex(Number.class);
    LabelIndex<Sentence> sentencesIndex = document.labelIndex(Sentence.class);
    LabelIndex<ParseToken> tokensIndex = document.labelIndex(ParseToken.class);

    try (BufferedWriter bufferedWriter = Files
        .newBufferedWriter(outputDirectory.resolve(document.getArtifactID() + ".txt"),
            StandardOpenOption.CREATE_NEW)) {

      for (Number number : numbersIndex) {
        LabelIndex<Sentence> sentenceContainingIndex = sentencesIndex.containing(number);
        Sentence sentence = sentenceContainingIndex.first();
        if (sentence == null) {
          throw new RuntimeException("No sentence");
        }
        LabelIndex<ParseToken> sentenceTokensIndex = tokensIndex.inside(sentence);

        Iterator<ParseToken> it = sentenceTokensIndex.backwardFrom(number).iterator();
        List<ParseToken> leftTokens = new ArrayList<>();
        for (int i = 0; i < contextSize; i++) {
          if (it.hasNext()) {
            leftTokens.add(it.next());
          }
        }
        int leftSize = leftTokens.size();
        for (int i = 0; i < leftSize; i++) {
          bufferedWriter.write(leftTokens.get(leftSize - 1 - i).getText() + " ");
        }
        bufferedWriter.newLine();

        for (ParseToken numberToken : tokensIndex.inside(number)) {
          bufferedWriter.write(numberToken.getText() + " ");
        }
        bufferedWriter.newLine();

        Iterator<ParseToken> rightIt = sentenceTokensIndex.forwardFrom(number)
            .iterator();

        for (int i = 0; i < contextSize; i++) {
          if (rightIt.hasNext()) {
            bufferedWriter.write(rightIt.next().getText() + " ");
          }
        }
        bufferedWriter.newLine();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
