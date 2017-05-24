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

package edu.umn.biomedicus.sentence;

import edu.umn.biomedicus.acronym.AcronymExpansionsModel;
import edu.umn.biomedicus.annotations.ProcessorScoped;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.framework.PostProcessor;
import edu.umn.biomedicus.exc.BiomedicusException;
import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.sentdetect.SentenceDetectorFactory;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.StringList;
import opennlp.tools.util.TrainingParameters;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@ProcessorScoped
public class ONLPSentenceTrainer implements PostProcessor {
    private static final char[] EOS_CHARS = ".!?:\n\t".toCharArray();
    private static final String POISON = ">poison<";
    private final BlockingDeque<String> samplesQueue
            = new LinkedBlockingDeque<>();
    private final Dictionary abbrevs;
    private final Path outputPath;
    private final CountDownLatch modelTrained = new CountDownLatch(1);
    @Nullable private SentenceModel sentenceModel = null;
    @Nullable private IOException ioException = null;

    @Inject
    ONLPSentenceTrainer(AcronymExpansionsModel acronymExpansionsModel,
                        @ProcessorSetting("opennlp.sentence.trainerOutput.path")
                                Path outputPath) {
        this.outputPath = outputPath;
        abbrevs = new Dictionary(true);
        for (String s : acronymExpansionsModel.getAcronyms()) {
            abbrevs.put(new StringList(s));
        }

        new Thread(() -> {
            try {
                SentenceSampleStream samples = new SentenceSampleStream(
                        new ObjectStream<String>() {
                            @Override
                            public String read() throws IOException {
                                try {
                                    String sentenceSample = samplesQueue.take();
                                    // comparing reference equality on purpose.
                                    //noinspection StringEquality
                                    if (sentenceSample == POISON) {
                                        return null;
                                    }
                                    return sentenceSample;
                                } catch (InterruptedException e) {
                                    throw new IOException(e);
                                }
                            }

                            @Override
                            public void reset() throws IOException,
                                    UnsupportedOperationException {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public void close() throws IOException {

                            }
                        });
                SentenceDetectorFactory sentenceDetectorFactory
                        = new SentenceDetectorFactory("en",
                        false, abbrevs, EOS_CHARS);
                TrainingParameters params = TrainingParameters.defaultParams();
                sentenceModel = SentenceDetectorME.train("en",
                        samples, sentenceDetectorFactory, params);
                modelTrained.countDown();
            } catch (IOException e) {
                ioException = e;
            }
        });
    }

    void addSentenceSample(String sentenceSample) throws InterruptedException {
        samplesQueue.put(sentenceSample);
    }

    @Override
    public void afterProcessing() throws BiomedicusException {
        samplesQueue.add(POISON);
        try {
            modelTrained.await();
            if (ioException != null) {
                throw new BiomedicusException(ioException);
            }

            if (sentenceModel == null) {
                throw new BiomedicusException("Error training sentence model.");
            }

            OutputStream outputStream = Files.newOutputStream(outputPath,
                    CREATE, TRUNCATE_EXISTING);

            sentenceModel.serialize(outputStream);
        } catch (InterruptedException e) {
            throw new BiomedicusException(
                    "Interrupted before model could be saved.");
        } catch (IOException e) {
            throw new BiomedicusException("Failed to write out model.");
        }
    }
}
