/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.*;
import java.nio.file.Path;

final class ParserEval {
    private final Path parserEval;

    private final Path modelDir;

    private Process parser;

    private Process tagger;

    @Inject
    public ParserEval(@Setting("syntaxnet.parserEval.path") Path parserEval,
                      @Setting("syntaxnet.modelDir.path") Path modelDir) {
        this.parserEval = parserEval;
        this.modelDir = modelDir;
    }

    void start() throws BiomedicusException {
        ProcessBuilder parserBuilder = new ProcessBuilder();
        parserBuilder.command(parserEval.toString(),
                "--input=stdin-conll",
                "--output=stdout-conll",
                "--hidden_layer_sizes=512,512",
                "--arg_prefix=brain_parser",
                "--graph_builder=structured",
                "--task_context=" + modelDir.resolve("context.pbtxt"),
                "--model_path=" + modelDir.resolve("parser-params"),
                "--slim_model",
                "--batch_size=1024");

        ProcessBuilder taggerBuilder = new ProcessBuilder();
        taggerBuilder.command(parserEval.toString(),
                "--input=stdin-conll",
                "--output=stdout-conll",
                "--hidden_layer_sizes=64",
                "--arg_prefix=brain_tagger",
                "--graph_builder=structured",
                "--task_context=" + modelDir.resolve("context.pbtxt"),
                "--model_path=" + modelDir.resolve("tagger-params"),
                "--slim_model",
                "--batch_size=1024");
        taggerBuilder.redirectOutput(parserBuilder.redirectInput());

        try {
            parser = parserBuilder.start();
            tagger = taggerBuilder.start();
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }

    String send(String string) throws BiomedicusException {
        try (Writer writer = new OutputStreamWriter(tagger.getOutputStream());
             BufferedReader reader = new BufferedReader(new InputStreamReader(tagger.getInputStream()))) {
            writer.write(string);

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while (!(line = reader.readLine()).isEmpty()) {
                if (line.startsWith("I")) continue;
                stringBuilder.append(line);
                stringBuilder.append('\n');
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }


    void stop() {
        parser.destroy();
        tagger.destroy();
    }
}
