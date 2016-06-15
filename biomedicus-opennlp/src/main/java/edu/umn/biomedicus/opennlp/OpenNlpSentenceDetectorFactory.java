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

import edu.umn.biomedicus.sentence.BaseSentenceDetectorFactory;
import edu.umn.biomedicus.sentence.SentenceCandidateGenerator;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

/**
 * A factory which creates sentence detectors that use OpenNLP's Maximum entropy model to detect sentences.
 *
 * @author Ben Knoll
 * @since 1.1.0
 */
public class OpenNlpSentenceDetectorFactory extends BaseSentenceDetectorFactory {
    private opennlp.tools.sentdetect.SentenceModel model;

    /**
     * Default constructor. Initializes with the OpenNLP {@link opennlp.tools.sentdetect.SentenceModel} used to create
     * the OpenNLP sentence detector.
     *
     * @param model sentence model.
     */
    public OpenNlpSentenceDetectorFactory(SentenceModel model) {
        this.model = model;
    }

    @Override
    protected SentenceCandidateGenerator getCandidateGenerator() {
        return new OpenNlpCandidateGenerator(new SentenceDetectorME(model));
    }
}
