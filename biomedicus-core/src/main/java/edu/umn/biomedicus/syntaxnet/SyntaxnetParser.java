/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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
import edu.umn.biomedicus.application.TextView;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.types.text.*;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public final class SyntaxnetParser implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SyntaxnetParser.class);
    private final Path installationDir;
    private final String modelDirString;
    private final LabelIndex<Sentence> sentenceLabelIndex;
    private final LabelIndex<ParseToken> tokenLabelIndex;
    private final Labeler<DependencyParse> dependencyParseLabeler;

    @Inject
    SyntaxnetParser(@Setting("syntaxnet.installationDir.path") Path installationDir,
                    @Setting("syntaxnet.modelDir") String modelDirString,
                    TextView document) {
        this.installationDir = installationDir;
        this.modelDirString = modelDirString;
        sentenceLabelIndex = document.getLabelIndex(Sentence.class);
        tokenLabelIndex = document.getLabelIndex(ParseToken.class);
        dependencyParseLabeler = document.getLabeler(DependencyParse.class);
    }

    @Override
    public void process() throws BiomedicusException {
        Path parserEval = installationDir.resolve("bazel-bin/syntaxnet/parser_eval");
        Path modelDir = installationDir.resolve(modelDirString);

        try {
            Process parser = new ProcessBuilder().directory(installationDir.toFile())
                    .command(parserEval.toString(),
                            "--input=stdin-conll",
                            "--output=stdout-conll",
                            "--hidden_layer_sizes=512,512",
                            "--arg_prefix=brain_parser",
                            "--graph_builder=structured",
                            "--task_context=" + modelDir.resolve("context.pbtxt"),
                            "--model_path=" + modelDir.resolve("parser-params"),
                            "--slim_model",
                            "--batch_size=1024")
                    .start();
            Process tagger = new ProcessBuilder().directory(installationDir.toFile())
                    .command(parserEval.toString(),
                            "--input=stdin-conll",
                            "--output=stdout-conll",
                            "--hidden_layer_sizes=64",
                            "--arg_prefix=brain_tagger",
                            "--graph_builder=structured",
                            "--task_context=" + modelDir.resolve("context.pbtxt"),
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


            try (Writer writer = new OutputStreamWriter(tagger.getOutputStream())) {
                for (Label<Sentence> sentenceLabel : sentenceLabelIndex) {
                    List<Label<ParseToken>> sentenceTokenLabels = tokenLabelIndex.insideSpan(sentenceLabel).all();
                    String conllString = new Tokens2Conll(sentenceTokenLabels).conllString();
                    writer.write(conllString);
                    writer.write("\n");
                }
            }

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(parser.getInputStream()))) {
                for (Label<Sentence> sentenceLabel : sentenceLabelIndex) {
                    StringBuilder sentenceParse = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                        sentenceParse.append(line).append("\n");
                    }
                    dependencyParseLabeler.value(new DependencyParse(sentenceParse.toString())).label(sentenceLabel);
                }
            }

            parser.destroy();
            tagger.destroy();
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }

    private static Runnable errorStreamLogger(Process process) {
        return () -> {
            InputStream errorStream = process.getErrorStream();
            InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.startsWith("F") || line.startsWith("E") || line.startsWith("W")) {
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
}
