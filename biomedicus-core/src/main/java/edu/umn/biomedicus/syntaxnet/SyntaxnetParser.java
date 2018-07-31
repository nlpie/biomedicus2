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

package edu.umn.biomedicus.syntaxnet;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.parsing.DependencyParse;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.DocumentTask;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a document by calling out to the Google syntaxnet parser using a createProcessBuilder command.
 *
 * @author Ben Knoll
 * @since 1.5.0
 * @deprecated doesn't actually label anything right now
 */
public final class SyntaxnetParser implements DocumentTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyntaxnetParser.class);

  private final Path installationDir;

  private final String modelDirString;

  @Inject
  SyntaxnetParser(
      @Setting("syntaxnet.installationDir.path") Path installationDir,
      @Setting("syntaxnet.modelDir") String modelDirString
  ) {
    this.installationDir = installationDir;
    this.modelDirString = modelDirString;
  }

  private static Runnable errorStreamLogger(Process process) {
    return () -> {
      InputStream errorStream = process.getErrorStream();
      InputStreamReader inputStreamReader = new InputStreamReader(
          errorStream);
      BufferedReader bufferedReader = new BufferedReader(
          inputStreamReader);
      String line;
      try {
        while ((line = bufferedReader.readLine()) != null) {
          if (line.startsWith("F") || line.startsWith("E") || line
              .startsWith("W")) {
            LOGGER.error(line);
          } else {
            LOGGER.trace(line);
          }
        }
      } catch (IOException e) {
        LOGGER.error("Error reading error stream.", e);
      }
    };
  }

  @Override
  public void run(@Nonnull Document document) {
    LabelIndex<Sentence> sentenceLabelIndex = document.labelIndex(Sentence.class);

    LabelIndex<ParseToken> tokenLabelIndex = document.labelIndex(ParseToken.class);

    Labeler<DependencyParse> dependencyParseLabeler = document.labeler(DependencyParse.class);

    Path parserEval = installationDir.resolve("bazel-bin/syntaxnet/parser_eval");
    Path modelDir = installationDir.resolve(modelDirString);

    try {
      Process parser = new ProcessBuilder()
          .directory(installationDir.toFile())
          .command(parserEval.toString(),
              "--input=stdin-conll",
              "--output=stdout-conll",
              "--hidden_layer_sizes=512,512",
              "--arg_prefix=brain_parser",
              "--graph_builder=structured",
              "--task_context=" + modelDir
                  .resolve("context.pbtxt"),
              "--model_path=" + modelDir.resolve("parser-params"),
              "--slim_model",
              "--batch_size=1024")
          .start();
      Process tagger = new ProcessBuilder()
          .directory(installationDir.toFile())
          .command(parserEval.toString(),
              "--input=stdin-conll",
              "--output=stdout-conll",
              "--hidden_layer_sizes=64",
              "--arg_prefix=brain_tagger",
              "--graph_builder=structured",
              "--task_context=" + modelDir
                  .resolve("context.pbtxt"),
              "--model_path=" + modelDir.resolve("tagger-params"),
              "--slim_model",
              "--batch_size=1024")
          .start();

      new Thread(errorStreamLogger(tagger)).start();

      new Thread(errorStreamLogger(parser)).start();

      new Thread(() -> {
        try (InputStream inputStream = tagger.getInputStream();
            OutputStream outputStream = parser.getOutputStream()) {
          int in;
          while ((in = inputStream.read()) != -1) {
            outputStream.write(in);
          }
        } catch (IOException e) {
          LOGGER.error("Error transferring from input to output.");
        }
      }).start();

      try (Writer writer = new OutputStreamWriter(
          tagger.getOutputStream())) {
        for (Sentence sentence : sentenceLabelIndex) {
          Collection<ParseToken> sentenceTokens = tokenLabelIndex
              .inside(sentence);
          String conllString = new Tokens2Conll(sentenceTokens)
              .conllString();
          writer.write(conllString);
          writer.write("\n");
        }
      }

      try (BufferedReader bufferedReader = new BufferedReader(
          new InputStreamReader(parser.getInputStream()))) {
        for (Sentence sentence : sentenceLabelIndex) {
          StringBuilder sentenceParse = new StringBuilder();
          String line;
          while ((line = bufferedReader.readLine()) != null && !line
              .isEmpty()) {
            sentenceParse.append(line).append("\n");
          }
        }
      }

      parser.destroy();
      tagger.destroy();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
