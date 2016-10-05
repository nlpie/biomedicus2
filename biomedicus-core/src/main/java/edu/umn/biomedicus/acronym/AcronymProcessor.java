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

import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.types.semantics.Acronym;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.common.types.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Will tag tokens as acronym/abbreviations or not
 *
 * @author Greg Finley
 * @since 1.5.0
 */
class AcronymProcessor implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcronymVectorModel.class);

    /*
     * All part of speech tags to exclude from consideration as acronyms.
     * Some of the verbs may have to change, but PRP and CC are key (esp. for tokens like "it", "or")
     */
    private static final Set<PartOfSpeech> EXCLUDE_POS = buildExcludedPos();

    private final AcronymModel model;
    @Nullable private final OrthographicAcronymModel orthographicModel;
    private final Labels<TermToken> termTokenLabels;
    private final Labels<PartOfSpeech> partOfSpeechLabels;
    private final Labeler<Acronym> acronymExpansionLabeler;

    /**
     * Constructor to initialize the acronym detector
     *
     * @param model             an AcronymModel that contains lists of acronyms and their senses
     * @param orthographicModel optional - an orthographic model for detecting unknown abbreviations
     */
    @Inject
    public AcronymProcessor(@Setting("acronym.model") AcronymModel model,
                            @Nullable OrthographicAcronymModel orthographicModel,
                            Document document) {
        this.orthographicModel = orthographicModel;
        this.model = model;
        this.termTokenLabels = document.labels(TermToken.class);
        this.partOfSpeechLabels = document.labels(PartOfSpeech.class);
        this.acronymExpansionLabeler = document.labeler(Acronym.class);
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Detecting acronyms in a document.");
        List<Token> allTokens = null;
        // Need to populate a list first: vectorizer needs object identity but UimaLabels' stream() creates new objects
        List<Label<TermToken>> termTokenLabelsList = termTokenLabels.stream().collect(Collectors.toList());
        for (Label<TermToken> termTokenLabel : termTokenLabelsList) {
            TermToken termToken = termTokenLabel.value();
            List<Label<PartOfSpeech>> partOfSpeechLabelsForToken = partOfSpeechLabels.insideSpan(termTokenLabel).all();
            boolean excludedPos = partOfSpeechLabelsForToken.stream().map(Label::value).anyMatch(EXCLUDE_POS::contains);
            if (!excludedPos && (model.hasAcronym(termToken) || orthographicSaysAcronym(termToken))) {
                LOGGER.trace("Found potential acronym: {}", termToken);
                if (allTokens == null) {
                    allTokens = termTokenLabelsList.stream().map(Label::value).collect(Collectors.toList());
                }
                String sense = model.findBestSense(allTokens, termToken);
                if (!Acronyms.UNKNOWN.equals(sense) && !sense.equalsIgnoreCase(termToken.text())) {
                    LOGGER.trace("Labeling acronym expansion: {}", sense);
                    acronymExpansionLabeler.value(new Acronym(sense, termToken.hasSpaceAfter()))
                            .label(termTokenLabel);
                }
            }
        }
    }

    private boolean orthographicSaysAcronym(Token token) {
        return orthographicModel != null && orthographicModel.seemsLikeAbbreviation(token);
    }

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
}

