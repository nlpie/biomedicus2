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

package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.labels.ValueLabeler;
import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;

/**
 * Will tag tokens as acronym/abbreviations or not
 *
 * @author Greg Finley
 * @since 1.5.0
 */
@DocumentScoped
class AcronymDetector implements DocumentProcessor {
    /**
     * class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AcronymVectorModel.class);

    /*
     * All part of speech tags to exclude from consideration as acronyms.
     * Some of the verbs may have to change, but PRP and CC are key (esp. for tokens like "it", "or")
     */
    private static final Set<PartOfSpeech> EXCLUDE_POS = buildExcludedPos();

    private static Set<PartOfSpeech> buildExcludedPos() {
        return EnumSet.of(
                PartOfSpeech.PRP,
                PartOfSpeech.DT,
                PartOfSpeech.CC,
                PartOfSpeech.IN,
                PartOfSpeech.UH,
                PartOfSpeech.TO,
                PartOfSpeech.RP,
                PartOfSpeech.PDT,
                PartOfSpeech.WP,
                PartOfSpeech.WP$,
                PartOfSpeech.WDT,
                PartOfSpeech.POS,
                PartOfSpeech.MD
        );
    }

    /*
     * The model that contains everything known about acronyms
     */
    private final AcronymModel model;

    /*
     * Contains orthographic rules to identify unseen abbreviations
     */
    private final OrthographicAcronymModel orthographicModel;
    private final ValueLabeler acronymLabeler;
    private final Labels<TermToken> termTokens;
    private final Labels<PartOfSpeech> partOfSpeechLabels;

    /**
     * Constructor to initialize the acronym detector
     *
     * @param model             an AcronymModel that contains lists of acronyms and their senses
     * @param orthographicModel optional - an orthographic model for detecting unknown abbreviations
     */
    @Inject
    public AcronymDetector(@Setting("acronym.model") AcronymModel model,
                           OrthographicAcronymModel orthographicModel,
                           Labels<TermToken> termTokens,
                           Labels<PartOfSpeech> partOfSpeechLabels,
                           Labeler<Acronym> acronymLabeler) {
        this.orthographicModel = orthographicModel;
        this.model = model;
        this.termTokens = termTokens;
        this.partOfSpeechLabels = partOfSpeechLabels;
        this.acronymLabeler = acronymLabeler.value(new Acronym());
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Detecting acronyms in a document.");
        for (Label<TermToken> termTokenLabel : termTokens) {
            TermToken termToken = termTokenLabel.value();
            List<Label<PartOfSpeech>> partOfSpeechLabelsForToken = partOfSpeechLabels.insideSpan(termTokenLabel).all();
            boolean excludedPos = partOfSpeechLabelsForToken.stream().map(Label::value).anyMatch(EXCLUDE_POS::contains);
            if (!excludedPos && (model.hasAcronym(termToken) || orthographicSaysAcronym(termToken))) {
                acronymLabeler.label(termTokenLabel);
            }
        }
    }

    private boolean orthographicSaysAcronym(TokenLike tokenLike) {
        return orthographicModel != null && orthographicModel.seemsLikeAbbreviation(tokenLike);
    }
}

