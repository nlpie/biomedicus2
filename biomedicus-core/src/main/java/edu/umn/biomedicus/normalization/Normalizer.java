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
import edu.umn.biomedicus.application.TextView;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.NormForm;
import edu.umn.biomedicus.common.types.text.ParseToken;
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
    private final LabelIndex<ParseToken> parseTokenLabelIndex;
    private final LabelIndex<PartOfSpeech> partOfSpeechLabelIndex;
    private final Labeler<NormForm> normFormLabeler;

    @Inject
    Normalizer(NormalizerModel normalizerModel, TextView document) {
        this.normalizerModel = normalizerModel;
        parseTokenLabelIndex = document.getLabelIndex(ParseToken.class);
        partOfSpeechLabelIndex = document.getLabelIndex(PartOfSpeech.class);
        normFormLabeler = document.getLabeler(NormForm.class);
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Normalizing tokens in a document.");
        for (Label<ParseToken> parseTokenLabel : parseTokenLabelIndex) {
            PartOfSpeech partOfSpeech = partOfSpeechLabelIndex.withTextLocation(parseTokenLabel)
                    .orElseThrow(() -> new BiomedicusException("Part of speech label not found for parse token label"))
                    .value();
            String normalForm = normalizerModel.normalize(parseTokenLabel.value(), partOfSpeech);
            normFormLabeler.value(new NormForm(normalForm)).label(parseTokenLabel);
        }
    }
}
