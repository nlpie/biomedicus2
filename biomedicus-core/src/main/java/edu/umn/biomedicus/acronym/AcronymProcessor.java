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
import edu.umn.biomedicus.application.TextView;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.types.semantics.Acronym;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.TermToken;
import edu.umn.biomedicus.common.types.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Will tag tokens as acronym/abbreviations or not
 *
 * @author Greg Finley
 * @since 1.5.0
 */
class AcronymProcessor implements DocumentProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcronymProcessor.class);

    /*
     * All part of speech tags to exclude from consideration as acronyms.
     * Some of the verbs may have to change, but PRP and CC are key (esp. for tokens like "it", "or")
     */
    private static final Set<PartOfSpeech> EXCLUDE_POS = buildExcludedPos();

    private final AcronymModel model;
    @Nullable private final OrthographicAcronymModel orthographicModel;
    private final LabelIndex<TermToken> termTokenLabels;
    private final LabelIndex<ParseToken> parseTokenLabels;
    private final LabelIndex<PartOfSpeech> partOfSpeechLabels;
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
                            TextView document) {
        this.orthographicModel = orthographicModel;
        this.model = model;
        this.termTokenLabels = document.getLabelIndex(TermToken.class);
        this.parseTokenLabels = document.getLabelIndex(ParseToken.class);
        this.partOfSpeechLabels = document.getLabelIndex(PartOfSpeech.class);
        this.acronymExpansionLabeler = document.getLabeler(Acronym.class);
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Detecting acronyms in a document.");
        List<Token> allTokens = null;
        List<Label<TermToken>> termTokenLabelsList = termTokenLabels.stream().collect(Collectors.toList());

        for (int i = 0; i < termTokenLabelsList.size(); i++) {
            Label<TermToken> termTokenLabel = termTokenLabelsList.get(i);
            TermToken termToken = termTokenLabel.value();
            List<Label<PartOfSpeech>> partOfSpeechLabelsForToken = partOfSpeechLabels.insideSpan(termTokenLabel).all();
            List<Label<? extends Token>> suspectedAcronyms = new ArrayList<>();
            boolean excludedPos = partOfSpeechLabelsForToken.stream().map(Label::value).allMatch(EXCLUDE_POS::contains);
            if (!excludedPos) {
                if (model.hasAcronym(termToken)) {
                    suspectedAcronyms.add(termTokenLabel);
                    LOGGER.trace("Found potential acronym: {}", termToken);
                } else {
                    // check the orthographic model AND all spanned ParseTokens
                    if (orthographicSaysAcronym(termToken)) {
                        suspectedAcronyms.add(termTokenLabel);
                        LOGGER.trace("Found potential acronym: {}", termToken);
                    }
                    List<Label<ParseToken>> parseTokensForToken = parseTokenLabels.insideSpan(termTokenLabel).all();
                    for (Label<ParseToken> parseTokenLabel : parseTokensForToken) {
                        ParseToken parseToken = parseTokenLabel.value();
                        Optional<Label<PartOfSpeech>> pos = partOfSpeechLabels.matching(parseTokenLabel);
                        if ((!pos.isPresent() || !EXCLUDE_POS.contains(pos.get().value())) &&
                            (model.hasAcronym(parseToken) || orthographicSaysAcronym(parseToken)) ) {
                                suspectedAcronyms.add(parseTokenLabel);
                                LOGGER.trace("Found potential acronym: {}", parseToken);
                            }
                        }
                    }
            }
            for (Label<? extends Token> acronymLabel : suspectedAcronyms) {
                if (allTokens == null) {
                    allTokens = termTokenLabelsList.stream().map(Label::value).collect(Collectors.toList());
                }
                Token acronymToken = acronymLabel.value();
                // swap out the current TermToken for the ParseToken we just found and find the sense
                Token tempToken = allTokens.set(i, acronymToken);
                String sense = model.findBestSense(allTokens, i);
                if (!Acronyms.UNKNOWN.equals(sense) && !sense.equalsIgnoreCase(acronymToken.text())) {
                    LOGGER.trace("Labeling acronym expansion: {}", sense);
                    acronymExpansionLabeler.value(new Acronym(sense, acronymToken.hasSpaceAfter()))
                            .label(acronymLabel);
                    // If we just successfully set the term token, don't both with ParseTokens
                    if (acronymToken instanceof TermToken) {
                        break;
                    }
                }
                allTokens.set(i, tempToken);
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
