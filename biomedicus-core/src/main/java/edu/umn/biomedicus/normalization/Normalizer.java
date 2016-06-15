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

package edu.umn.biomedicus.normalization;

import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 *
 */
public class Normalizer implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Normalizer.class);

    private final NormalizerModel normalizerModel;

    private final Document document;

    @Inject
    Normalizer(NormalizerModel normalizerModel, Document document) {
        this.normalizerModel = normalizerModel;
        this.document = document;
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Normalizing tokens in a document.");
        for (Token token : document.getTokens()) {
            normalizerModel.normalize(token);
        }
    }
}
