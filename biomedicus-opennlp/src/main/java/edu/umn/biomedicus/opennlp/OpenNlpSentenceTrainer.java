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

package edu.umn.biomedicus.opennlp;

import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.sentdetect.*;
import opennlp.tools.util.CollectionObjectStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Trains OpenNLP sentence detector models.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class OpenNlpSentenceTrainer {
    /**
     * File to use for abbreviations.
     */
    private final Path abbrevsFile;

    /**
     * Output file.
     */
    private final Path destination;

    /**
     * Characters to use for EOS events.
     */
    private final char[] eosChars;

    /**
     * List of sentences to write to.
     */
    private final List<String> sentences;

    /**
     * Creates a new OpenNlpSentenceTrainer.
     *
     * @param abbrevsFile file containing abbreviations, one on each line.
     * @param destination
     * @param eosChars
     * @throws IOException
     */
    public OpenNlpSentenceTrainer(Path abbrevsFile, Path destination, char[] eosChars) throws IOException {
        this.abbrevsFile = abbrevsFile;
        this.destination = destination;
        this.eosChars = eosChars;
        sentences = new ArrayList<>();
    }

    /**
     *
     * @param document
     * @throws IOException
     */
    public void addDocument(Document document) throws IOException {
        for (Sentence sentence : document.getSentences()) {
            sentences.add(sentence.getText().toString());
        }
    }

    /**
     *
     * @throws IOException
     */
    public void finish() throws IOException {
        ObjectStream<String> stringsStream = new CollectionObjectStream<>(sentences);
        ObjectStream<SentenceSample> sentenceSampleObjectStream = new SentenceSampleStream(stringsStream);

        Dictionary abbrevs = Dictionary.parseOneEntryPerLine(Files.newBufferedReader(abbrevsFile));
        SentenceDetectorFactory sentenceDetectorFactory = new SentenceDetectorFactory("en", true, abbrevs, eosChars);
        SentenceModel sentenceModel = SentenceDetectorME.train("en", sentenceSampleObjectStream,
                sentenceDetectorFactory, TrainingParameters.defaultParams());

        try (OutputStream outputStream = Files.newOutputStream(destination, StandardOpenOption.CREATE)) {
            sentenceModel.serialize(outputStream);
        }
    }
}
