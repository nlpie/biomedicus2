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

package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 *
 */
public class TntProcessor implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TntProcessor.class);

    private final Document document;

    private final TntPosTagger tntPosTagger;

    @Inject
    public TntProcessor(Document document, TntModel tntModel, @Setting("tnt.beam.threshold") Double beamThreshold) {
        this.document = document;
        this.tntPosTagger = new TntPosTagger(tntModel, beamThreshold);
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Tagging tokens in document.");
        for (Sentence sentence : document.getSentences()) {
            tntPosTagger.tagSentence(sentence);
        }
    }
}
