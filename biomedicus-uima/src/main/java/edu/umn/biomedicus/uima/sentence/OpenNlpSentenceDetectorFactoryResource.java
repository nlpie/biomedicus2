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

package edu.umn.biomedicus.uima.sentence;

import edu.umn.biomedicus.opennlp.OpenNlpSentenceDetectorFactory;
import edu.umn.biomedicus.sentence.SentenceDetectorFactory;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of {@link SentenceDetectorFactoryResource} that provides the OpenNLP
 * MaxEnt sentence detector.
 *
 * @author Ben Knoll
 * @since 1.1.0
 * @see opennlp.tools.sentdetect.SentenceModel
 * @see opennlp.tools.sentdetect.SentenceDetectorME
 */
public class OpenNlpSentenceDetectorFactoryResource implements SentenceDetectorFactoryResource, SharedResourceObject {

    private OpenNlpSentenceDetectorFactory openNlpSentenceDetectorFactory;

    @Override
    public SentenceDetectorFactory getSentenceDetectorFactory() {
        return openNlpSentenceDetectorFactory;
    }

    @Override
    public void load(DataResource aData) throws ResourceInitializationException {
        InputStream inputStream;
        try {
            inputStream = aData.getInputStream();
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        SentenceModel sentenceModel;
        try {
            sentenceModel = new SentenceModel(inputStream);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        openNlpSentenceDetectorFactory = new OpenNlpSentenceDetectorFactory(sentenceModel);
    }
}
