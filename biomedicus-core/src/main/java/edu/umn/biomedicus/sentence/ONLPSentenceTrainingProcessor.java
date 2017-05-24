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

import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;

import javax.inject.Inject;

public class ONLPSentenceTrainingProcessor implements DocumentProcessor {
    private final LabelIndex<Sentence> sentenceLabelIndex;
    private final String text;
    private final ONLPSentenceTrainer onlpSentenceTrainer;

    @Inject
    ONLPSentenceTrainingProcessor(TextView textView,
                                  ONLPSentenceTrainer onlpSentenceTrainer) {
        text = textView.getText();
        sentenceLabelIndex = textView.getLabelIndex(Sentence.class);
        this.onlpSentenceTrainer = onlpSentenceTrainer;
    }

    @Override
    public void process() throws BiomedicusException {
        for (Label<Sentence> sentenceLabel : sentenceLabelIndex) {
            String sentence = sentenceLabel.getCovered(text).toString();
            try {
                onlpSentenceTrainer.addSentenceSample(sentence);
            } catch (InterruptedException e) {
                throw new BiomedicusException("Sentence training interrupted.");
            }
        }
    }
}
